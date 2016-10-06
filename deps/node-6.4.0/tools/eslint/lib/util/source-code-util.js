/**
 * @fileoverview Tools for obtaining SourceCode objects.
 * @author Ian VanSchooten
 */

"use strict";

//------------------------------------------------------------------------------
// Requirements
//------------------------------------------------------------------------------

let lodash = require("lodash"),
    debug = require("debug"),
    CLIEngine = require("../cli-engine"),
    eslint = require("../eslint"),
    globUtil = require("./glob-util"),
    defaultOptions = require("../../conf/cli-options");

debug = debug("eslint:source-code-util");

//------------------------------------------------------------------------------
// Helpers
//------------------------------------------------------------------------------

/**
 * Get the SourceCode object for a single file
 * @param   {string}     filename The fully resolved filename to get SourceCode from.
 * @param   {Object}     options  A CLIEngine options object.
 * @returns {Array}               Array of the SourceCode object representing the file
 *                                and fatal error message.
 */
function getSourceCodeOfFile(filename, options) {
    debug("getting sourceCode of", filename);
    let opts = lodash.assign({}, options, { rules: {}});
    let cli = new CLIEngine(opts);
    let results = cli.executeOnFiles([filename]);

    if (results && results.results[0] && results.results[0].messages[0] && results.results[0].messages[0].fatal) {
        let msg = results.results[0].messages[0];

        throw new Error("(" + filename + ":" + msg.line + ":" + msg.column + ") " + msg.message);
    }
    let sourceCode = eslint.getSourceCode();

    return sourceCode;
}

//------------------------------------------------------------------------------
// Public Interface
//------------------------------------------------------------------------------


/**
 * This callback is used to measure execution status in a progress bar
 * @callback progressCallback
 * @param {number} The total number of times the callback will be called.
 */

/**
 * Gets the SourceCode of a single file, or set of files.
 * @param   {string[]|string}  patterns   A filename, directory name, or glob,
 *                                        or an array of them
 * @param   {Object}           [options]  A CLIEngine options object. If not provided,
 *                                        the default cli options will be used.
 * @param   {progressCallback} [cb]       Callback for reporting execution status
 * @returns {Object}                      The SourceCode of all processed files.
 */
function getSourceCodeOfFiles(patterns, options, cb) {
    let sourceCodes = {},
        filenames,
        opts;

    if (typeof patterns === "string") {
        patterns = [patterns];
    }

    defaultOptions = lodash.assign({}, defaultOptions, {cwd: process.cwd()});

    if (typeof options === "undefined") {
        opts = defaultOptions;
    } else if (typeof options === "function") {
        cb = options;
        opts = defaultOptions;
    } else if (typeof options === "object") {
        opts = lodash.assign({}, defaultOptions, options);
    }
    debug("constructed options:", opts);
    patterns = globUtil.resolveFileGlobPatterns(patterns, opts);

    filenames = globUtil.listFilesToProcess(patterns, opts).reduce(function(files, fileInfo) {
        return !fileInfo.ignored ? files.concat(fileInfo.filename) : files;
    }, []);
    if (filenames.length === 0) {
        debug("Did not find any files matching pattern(s): " + patterns);
    }
    filenames.forEach(function(filename) {
        let sourceCode = getSourceCodeOfFile(filename, opts);

        if (sourceCode) {
            debug("got sourceCode of", filename);
            sourceCodes[filename] = sourceCode;
        }
        if (cb) {
            cb(filenames.length); // eslint-disable-line callback-return
        }
    });
    return sourceCodes;
}

module.exports = {
    getSourceCodeOfFiles: getSourceCodeOfFiles
};