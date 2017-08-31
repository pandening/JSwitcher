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

package com.hujian.schedulers.core;

/**
 * Created by hujian06 on 2017/8/29.
 */

import com.hujian.schedulers.ScheduleHooks;
import com.hujian.schedulers.SwitcherResultFuture;
import com.hujian.schedulers.dispose.CompositeDisposable;
import com.hujian.switcher.reactive.Disposable;
import com.hujian.switcher.reactive.aux.EmptyDisposable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A scheduler with a shared, single threaded underlying ScheduledExecutorService.
 * @since 2.0
 */
public final class SingleScheduler extends Scheduler {

    private final ThreadFactory threadFactory;
    private final AtomicReference<ScheduledExecutorService> executor =
            new AtomicReference<ScheduledExecutorService>();

    /** The name of the system property for setting the thread priority for this Scheduler. */
    private static final String KEY_SINGLE_PRIORITY = "switcher.single-priority";

    private static final String THREAD_NAME_PREFIX = "SwitcherSingleScheduler";

    private static final SwitcherThreadFactory SINGLE_THREAD_FACTORY;

    private static final ScheduledExecutorService SHUTDOWN;
    static {
        SHUTDOWN = Executors.newScheduledThreadPool(0);
        SHUTDOWN.shutdown();

        int priority = Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY,
                Integer.getInteger(KEY_SINGLE_PRIORITY, Thread.NORM_PRIORITY)));

        SINGLE_THREAD_FACTORY = new SwitcherThreadFactory(THREAD_NAME_PREFIX, priority, 
                true);
    }

    public SingleScheduler() {
        this(SINGLE_THREAD_FACTORY);
    }

    /**
     * @param threadFactory thread factory to use for creating worker threads. Note that this takes precedence over any
     *                      system properties for configuring new thread creation. Cannot be null.
     */
    public SingleScheduler(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        executor.lazySet(createExecutor(threadFactory));
    }

    static ScheduledExecutorService createExecutor(ThreadFactory threadFactory) {
        return SchedulerPoolFactory.create(threadFactory);
    }

    @Override
    public void start() {
        ScheduledExecutorService next = null;
        for (;;) {
            ScheduledExecutorService current = executor.get();
            if (current != SHUTDOWN) {
                if (next != null) {
                    next.shutdown();
                }
                return;
            }
            if (next == null) {
                next = createExecutor(threadFactory);
            }
            if (executor.compareAndSet(current, next)) {
                return;
            }

        }
    }

    @Override
    public void shutdown() {
        ScheduledExecutorService current = executor.get();
        if (current != SHUTDOWN) {
            current = executor.getAndSet(SHUTDOWN);
            if (current != SHUTDOWN) {
                current.shutdownNow();
            }
        }
    }
    
    @Override
    public Worker createWorker() {
        return new ScheduledWorker(executor.get());
    }
    
    @Override
    public Disposable scheduleDirect(Runnable run, long delay, TimeUnit unit) {
        ScheduledDirectTask task = new ScheduledDirectTask(ScheduleHooks.onSchedule(run));
        try {
            Future<?> f;
            if (delay <= 0L) {
                f = executor.get().submit(task);
            } else {
                f = executor.get().schedule(task, delay, unit);
            }
            task.setFuture(f);
            return task;
        } catch (RejectedExecutionException ex) {
            ScheduleHooks.onError(ex);
            return EmptyDisposable.INSTANCE;
        }
    }

    @Override
    public Disposable schedulePeriodicallyDirect(Runnable run, long initialDelay, long period, TimeUnit unit) {
        final Runnable decoratedRun = ScheduleHooks.onSchedule(run);
        if (period <= 0L) {

            ScheduledExecutorService exec = executor.get();

            InstantPeriodicTask periodicWrapper = new InstantPeriodicTask(decoratedRun, exec);
            Future<?> f;
            try {
                if (initialDelay <= 0L) {
                    f = exec.submit(periodicWrapper);
                } else {
                    f = exec.schedule(periodicWrapper, initialDelay, unit);
                }
                periodicWrapper.setFirst(f);
            } catch (RejectedExecutionException ex) {
                ScheduleHooks.onError(ex);
                return EmptyDisposable.INSTANCE;
            }

            return periodicWrapper;
        }
        ScheduledDirectPeriodicTask task = new ScheduledDirectPeriodicTask(decoratedRun);
        try {
            Future<?> f = executor.get().scheduleAtFixedRate(task, initialDelay, period, unit);
            task.setFuture(f);
            return task;
        } catch (RejectedExecutionException ex) {
            ScheduleHooks.onError(ex);
            return EmptyDisposable.INSTANCE;
        }
    }

    static final class ScheduledWorker extends Scheduler.Worker {

        final ScheduledExecutorService executor;

        final CompositeDisposable tasks;

        volatile boolean disposed;

        ScheduledWorker(ScheduledExecutorService executor) {
            this.executor = executor;
            this.tasks = new CompositeDisposable();
        }

        @Override
        public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
            if (disposed) {
                return EmptyDisposable.INSTANCE;
            }

            Runnable decoratedRun = ScheduleHooks.onSchedule(run);

            ScheduledRunnable sr = new ScheduledRunnable(decoratedRun, tasks);
            tasks.add(sr);

            try {
                Future<?> f;
                if (delay <= 0L) {
                    f = executor.submit((Callable<Object>)sr);
                } else {
                    f = executor.schedule((Callable<Object>)sr, delay, unit);
                }

                ///////////////////////////////////////////////////////
                //   here you should get the future now.             //
                //   the recommend way is give the future to caller  //
                //////////////////////////////////////////////////////
                try {
                    f.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                sr.setFuture(f);
            } catch (RejectedExecutionException ex) {
                dispose();
                ScheduleHooks.onError(ex);
                return EmptyDisposable.INSTANCE;
            }

            return sr;
        }

        @Override
        public Disposable schedule(Runnable run, long delay, TimeUnit unit, SwitcherResultFuture<?> future)
                throws ExecutionException, InterruptedException {
            if (disposed) {
                return EmptyDisposable.INSTANCE;
            }

            Runnable decoratedRun = ScheduleHooks.onSchedule(run);

            ScheduledRunnable sr = new ScheduledRunnable(decoratedRun, tasks);
            tasks.add(sr);
            Future<?> f = null;
            try {
                if (delay <= 0L) {
                    f = executor.submit((Callable<Object>)sr);
                } else {
                    f = executor.schedule((Callable<Object>)sr, delay, unit);
                }

                //set the future here
                future.setFuture(f);

                sr.setFuture(f);
            } catch (RejectedExecutionException ex) {
                dispose();
                ScheduleHooks.onError(ex);
                return EmptyDisposable.INSTANCE;
            }

            return sr;
        }

        @Override
        public void dispose() {
            if (!disposed) {
                disposed = true;
                tasks.dispose();
            }
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}

