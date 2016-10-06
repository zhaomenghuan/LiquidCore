package org.liquidplayer.node;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Test;
import org.liquidplayer.v8.JSContext;
import org.liquidplayer.v8.JSObject;
import org.liquidplayer.v8.JSObject.jsexport;

import java.io.File;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.*;

public class FSTest {

    private interface OnDone {
        void onDone(JSContext ctx);
    }
    private class Script implements Process.EventListener {

        final private String script;
        final Semaphore processCompleted = new Semaphore(0);
        final private OnDone onDone;
        private JSContext context;

        Script(String script, OnDone onDone) {
            this.script = script;
            this.onDone = onDone;
            new Process(InstrumentationRegistry.getContext(),"_",this);
        }

        @Override
        public void onProcessStart(final Process proc, final JSContext ctx) {
            context = ctx;
            context.evaluateScript(script);
        }

        @Override
        public void onProcessAboutToExit(Process process, int exitCode) {
            onDone.onDone(context);
        }

        @Override
        public void onProcessExit(Process process, int exitCode) {
            processCompleted.release();
        }

        @Override
        public void onProcessFailed(Process process, Exception error) {

        }

    }

    private class Foo extends JSObject {
        Foo(JSContext ctx) { super(ctx); }

        @jsexport(type = Integer.class)
        Property<Integer> x;

        @jsexport(type = String.class)
        Property<String>  y;

        @jsexport(attributes = JSPropertyAttributeReadOnly)
        Property<String> read_only;

        @jsexport(attributes = JSPropertyAttributeReadOnly | JSPropertyAttributeDontDelete)
        int incr(int x) {
            return x+1;
        }
    }

    @Test
    public void testFileSystem1() throws Exception {
        Context context = InstrumentationRegistry.getContext();
        String dirx = context.getFilesDir() + "/__org.liquidplayer.node__/__";
        new File(context.getFilesDir() + "/__org.liquidplayer.node__/test.txt").delete();

        final String script = "" +
                "var fs = require('fs');" +
                "fs.writeFile('test.txt', 'Hello, World!', function(err) {" +
                "   if(err) {" +
                "       return console.log(err);" +
                "   }" +
                "   console.log('The file was saved!');" +
                "   fs.readdir('.', function(err,files) {" +
                "       global.files = files;" +
                "   });" +
                "});" +
                "";
        new Script(script, new OnDone() {
            @Override
            public void onDone(JSContext ctx) {
                assertEquals("test.txt",ctx.property("files").toString());

                Foo foo = new Foo(ctx);
                ctx.property("foo", foo);
                ctx.evaluateScript("foo.x = 5; foo.y = 'test';");
                assertEquals((Integer)5, foo.x.get());
                assertEquals("test", foo.y.get());
                foo.x.set(6);
                foo.y.set("test2");
                assertEquals(6, foo.property("x").toNumber().intValue());
                assertEquals("test2", foo.property("y").toString());
                assertEquals(6, ctx.evaluateScript("foo.x").toNumber().intValue());
                assertEquals("test2", ctx.evaluateScript("foo.y").toString());
                ctx.evaluateScript("foo.x = 11");
                assertEquals((Integer)11, foo.x.get());
                assertEquals(21, ctx.evaluateScript("foo.incr(20)").toNumber().intValue());

                foo.read_only.set("Ok!");
                assertEquals("Ok!", foo.read_only.get());
                foo.read_only.set("Not Ok!");
                assertEquals("Ok!", foo.read_only.get());
                ctx.evaluateScript("foo.read_only = 'boo';");
                assertEquals("Ok!", foo.read_only.get());

            }
        }).processCompleted.acquire();

        String content = new Scanner(new File(dirx + "/test.txt")).useDelimiter("\\Z").next();
        assertEquals("Hello, World!", content);
        assertTrue(new File(dirx + "/test.txt").delete());
    }

    @Test
    public void testLetsBeNaughty() throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream("fsTest.js");
        Scanner s = new Scanner(in).useDelimiter("\\A");
        String script = s.hasNext() ? s.next() : "";

        new Script(script, new OnDone() {
            @Override
            public void onDone(JSContext ctx) {
                android.util.Log.d("testLetsBeNaughty", ctx.property("a").toString());
                assertTrue(ctx.property("a").toString().contains("EACCES"));
                android.util.Log.d("testLetsBeNaughty", ctx.property("b").toString());
                assertTrue(ctx.property("b").toString().contains("EACCES") ||
                        ctx.property("b").toString().contains("ENOENT"));
                android.util.Log.d("testLetsBeNaughty", ctx.property("c").toString());
                assertTrue(ctx.property("c").toString().contains("EACCES"));
                android.util.Log.d("testLetsBeNaughty", ctx.property("d").toString());
                assertTrue(ctx.property("d").toString().contains("EACCES"));
                android.util.Log.d("testLetsBeNaughty", ctx.property("e").toString());
                assertTrue(ctx.property("e").toString().contains("EACCES"));
            }
        }).processCompleted.acquire();

    }

}