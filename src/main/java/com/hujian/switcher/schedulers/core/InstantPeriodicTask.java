/**
 * Copyright (c) 2017 hujian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hujian.switcher.schedulers.core;

/**
 * Created by hujian06 on 2017/8/29.
 */

import com.hujian.switcher.reactive.Disposable;
import com.hujian.switcher.reactive.aux.Functions;
import com.hujian.switcher.ScheduleHooks;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Wrapper for a regular task that gets immediately rescheduled when the task completed.
 */
final class InstantPeriodicTask implements Callable<Void>, Disposable {

    private final Runnable task;

    private final AtomicReference<Future<?>> rest;

    private final AtomicReference<Future<?>> first;

    private final ExecutorService executor;

    private Thread runner;

    private static final FutureTask<Void> CANCELLED = new FutureTask<Void>(Functions.EMPTY_RUNNABLE, null);

    InstantPeriodicTask(Runnable task, ExecutorService executor) {
        super();
        this.task = task;
        this.first = new AtomicReference<Future<?>>();
        this.rest = new AtomicReference<Future<?>>();
        this.executor = executor;
    }

    @Override
    public Void call() throws Exception {
        try {
            runner = Thread.currentThread();
            try {
                task.run();
                setRest(executor.submit(this));
            } catch (Throwable ex) {
                ScheduleHooks.onError(ex);
            }
        } finally {
            runner = null;
        }
        return null;
    }

    @Override
    public void dispose() {
        Future<?> current = first.getAndSet(CANCELLED);
        if (current != null && current != CANCELLED) {
            current.cancel(runner != Thread.currentThread());
        }
        current = rest.getAndSet(CANCELLED);
        if (current != null && current != CANCELLED) {
            current.cancel(runner != Thread.currentThread());
        }
    }

    @Override
    public boolean isDisposed() {
        return first.get() == CANCELLED;
    }

    void setFirst(Future<?> f) {
        for (;;) {
            Future<?> current = first.get();
            if (current == CANCELLED) {
                f.cancel(runner != Thread.currentThread());
            }
            if (first.compareAndSet(current, f)) {
                return;
            }
        }
    }

    void setRest(Future<?> f) {
        for (;;) {
            Future<?> current = rest.get();
            if (current == CANCELLED) {
                f.cancel(runner != Thread.currentThread());
            }
            if (rest.compareAndSet(current, f)) {
                return;
            }
        }
    }
}