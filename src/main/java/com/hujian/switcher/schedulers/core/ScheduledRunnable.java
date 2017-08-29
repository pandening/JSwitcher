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

import com.hujian.switcher.schedulers.ScheduleHooks;
import com.hujian.switcher.schedulers.dispose.DisposableContainer;
import com.hujian.switcher.reactive.Disposable;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Created by hujian06 on 2017/8/29.
 */
public final class ScheduledRunnable extends AtomicReferenceArray<Object>
        implements Runnable, Callable<Object>, Disposable {

    private final Runnable actual;

    private static final Object DISPOSED = new Object();

    private static final Object DONE = new Object();

    private static final int PARENT_INDEX = 0;
    private static final int FUTURE_INDEX = 1;
    private static final int THREAD_INDEX = 2;

    /**
     * Creates a ScheduledRunnable by wrapping the given action and setting
     * up the optional parent.
     * @param actual the runnable to wrap, not-null (not verified)
     * @param parent the parent tracking container or null if none
     */
    public ScheduledRunnable(Runnable actual, DisposableContainer parent) {
        super(3);
        this.actual = actual;
        this.lazySet(0, parent);
    }

    @Override
    public Object call() {
        // Being Callable saves an allocation in ThreadPoolExecutor
        run();
        return null;
    }

    @Override
    public void run() {
        lazySet(THREAD_INDEX, Thread.currentThread());
        try {
            try {
                actual.run();
            } catch (Throwable e) {
                ScheduleHooks.onError(e);
            }
        } finally {
            lazySet(THREAD_INDEX, null);
            Object o = get(PARENT_INDEX);
            if (o != DISPOSED && o != null && compareAndSet(PARENT_INDEX, o, DONE)) {
                ((DisposableContainer)o).delete(this);
            }

            for (;;) {
                o = get(FUTURE_INDEX);
                if (o == DISPOSED || compareAndSet(FUTURE_INDEX, o, DONE)) {
                    break;
                }
            }
        }
    }

    public void setFuture(Future<?> f) {
        for (;;) {
            Object o = get(FUTURE_INDEX);
            if (o == DONE) {
                return;
            }
            if (o == DISPOSED) {
                f.cancel(get(THREAD_INDEX) != Thread.currentThread());
                return;
            }
            if (compareAndSet(FUTURE_INDEX, o, f)) {
                return;
            }
        }
    }

    @Override
    public void dispose() {
        for (;;) {
            Object o = get(FUTURE_INDEX);
            if (o == DONE || o == DISPOSED) {
                break;
            }
            if (compareAndSet(FUTURE_INDEX, o, DISPOSED)) {
                if (o != null) {
                    ((Future<?>)o).cancel(get(THREAD_INDEX) != Thread.currentThread());
                }
                break;
            }
        }

        for (;;) {
            Object o = get(PARENT_INDEX);
            if (o == DONE || o == DISPOSED || o == null) {
                return;
            }
            if (compareAndSet(PARENT_INDEX, o, DISPOSED)) {
                ((DisposableContainer)o).delete(this);
                return;
            }
        }
    }

    @Override
    public boolean isDisposed() {
        Object o = get(FUTURE_INDEX);
        return o == DISPOSED || o == DONE;
    }
}

