//
//  internals.cpp
//  LiquidCoreiOS
//
//  Created by Eric Lange on 2/18/18.
//  Copyright © 2018 LiquidPlayer. All rights reserved.
//
#include <climits>
#include <csignal>
#include <map>
#include <memory>
#include <string>

#include "test/cctest/test-api.h"

#if V8_OS_POSIX
#include <unistd.h>  // NOLINT
#endif

#include "include/v8-util.h"
#include "src/api.h"
#include "src/arguments.h"
#include "src/base/platform/platform.h"
#include "src/code-stubs.h"
#include "src/compilation-cache.h"
#include "src/debug/debug.h"
#include "src/execution.h"
#include "src/futex-emulation.h"
#include "src/heap/incremental-marking.h"
#include "src/lookup.h"
#include "src/objects-inl.h"
#include "src/parsing/preparse-data.h"
#include "src/profiler/cpu-profiler.h"
#include "src/unicode-inl.h"
#include "src/utils.h"
#include "src/vm-state.h"
#include "test/cctest/heap/heap-tester.h"
#include "test/cctest/heap/heap-utils.h"
#include "test/cctest/profiler-extension.h"
#include "test/cctest/print-extension.h"
#include "test/cctest/trace-extension.h"

using namespace v8::internal;

void* Malloced::New(size_t size)
{
    return malloc(size);
}

void Malloced::Delete(void* p)
{
    free(p);
}

Heap::Heap() : external_string_table_(nullptr)
{
    
}

StackGuard::StackGuard()
{
    
}

Builtins::Builtins()
{
    
}

Builtins::~Builtins()
{
    
}

ThreadLocalTop::ThreadLocalTop()
{
    
}

AccountingAllocator::AccountingAllocator()
{
    
}
AccountingAllocator::~AccountingAllocator()
{
    
}
// Gets an empty segment from the pool or creates a new one.
Segment* AccountingAllocator::GetSegment(size_t bytes)
{
    return nullptr;
}
// Return unneeded segments to either insert them into the pool or release
// them if the pool is already full or memory pressure is high.
void AccountingAllocator::ReturnSegment(Segment* memory)
{
    
}

CancelableTaskManager::CancelableTaskManager()
{
    
}

void StackGuard::ThreadLocal::Clear()
{
    
}

const intptr_t v8::internal::Isolate::per_isolate_assert_data_debug_offset_ = 0;
