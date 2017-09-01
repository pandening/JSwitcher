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
import com.hujian.switcher.reactive.aux.EmptyDisposable;
import com.hujian.switcher.SwitcherResultFuture;
import com.hujian.switcher.schedulers.dispose.CompositeDisposable;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Scheduler that creates and caches a set of thread pools and reuses them if possible.
 */
public final class IoScheduler extends Scheduler {
    private static final String WORKER_THREAD_NAME_PREFIX = "SwitcherCachedThreadScheduler";
    private static final SwitcherThreadFactory WORKER_THREAD_FACTORY;

    private static final String EVICTOR_THREAD_NAME_PREFIX = "SwitcherCachedWorkerPoolEvictor";
    static final SwitcherThreadFactory EVICTOR_THREAD_FACTORY;

    private static final long KEEP_ALIVE_TIME = 60;
    private static final TimeUnit KEEP_ALIVE_UNIT = TimeUnit.SECONDS;

    static final ThreadWorker SHUTDOWN_THREAD_WORKER;
    private final ThreadFactory threadFactory;
    private final AtomicReference<CachedWorkerPool> pool;

    /** The name of the system property for setting the thread priority for this Scheduler. */
    private static final String KEY_IO_PRIORITY = "switcher.io-priority";

    private static final CachedWorkerPool NONE;
    static {
        SHUTDOWN_THREAD_WORKER = new ThreadWorker(new SwitcherThreadFactory("SwitcherCachedThreadSchedulerShutdown"));
        SHUTDOWN_THREAD_WORKER.dispose();

        int priority = Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY,
                Integer.getInteger(KEY_IO_PRIORITY, Thread.NORM_PRIORITY)));

        WORKER_THREAD_FACTORY = new SwitcherThreadFactory(WORKER_THREAD_NAME_PREFIX, priority);

        EVICTOR_THREAD_FACTORY = new SwitcherThreadFactory(EVICTOR_THREAD_NAME_PREFIX, priority);

        NONE = new CachedWorkerPool(0, null, WORKER_THREAD_FACTORY);
        NONE.shutdown();
    }

    static final class CachedWorkerPool implements Runnable {
        private final long keepAliveTime;
        private final ConcurrentLinkedQueue<ThreadWorker> expiringWorkerQueue;
        final CompositeDisposable allWorkers;
        private final ScheduledExecutorService evictorService;
        private final Future<?> evictorTask;
        private final ThreadFactory threadFactory;

        CachedWorkerPool(long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory) {
            this.keepAliveTime = unit != null ? unit.toNanos(keepAliveTime) : 0L;
            this.expiringWorkerQueue = new ConcurrentLinkedQueue<ThreadWorker>();
            this.allWorkers = new CompositeDisposable();
            this.threadFactory = threadFactory;

            ScheduledExecutorService evictor = null;
            Future<?> task = null;
            if (unit != null) {
                evictor = Executors.newScheduledThreadPool(1, EVICTOR_THREAD_FACTORY);
                task = evictor.scheduleWithFixedDelay(this, this.keepAliveTime, this.keepAliveTime, TimeUnit.NANOSECONDS);
            }
            evictorService = evictor;
            evictorTask = task;
        }

        @Override
        public void run() {
            evictExpiredWorkers();
        }

        ThreadWorker get() {
            if (allWorkers.isDisposed()) {
                return SHUTDOWN_THREAD_WORKER;
            }
            while (!expiringWorkerQueue.isEmpty()) {
                ThreadWorker threadWorker = expiringWorkerQueue.poll();
                if (threadWorker != null) {
                    return threadWorker;
                }
            }

            // No cached worker found, so create a new one.
            ThreadWorker w = new ThreadWorker(threadFactory);
            allWorkers.add(w);
            return w;
        }

        void release(ThreadWorker threadWorker) {
            // Refresh expire time before putting worker back in pool
            threadWorker.setExpirationTime(now() + keepAliveTime);

            expiringWorkerQueue.offer(threadWorker);
        }

        void evictExpiredWorkers() {
            if (!expiringWorkerQueue.isEmpty()) {
                long currentTimestamp = now();

                for (ThreadWorker threadWorker : expiringWorkerQueue) {
                    if (threadWorker.getExpirationTime() <= currentTimestamp) {
                        if (expiringWorkerQueue.remove(threadWorker)) {
                            allWorkers.remove(threadWorker);
                        }
                    } else {
                        // Queue is ordered with the worker that will expire first in the beginning, so when we
                        // find a non-expired worker we can stop evicting.
                        break;
                    }
                }
            }
        }

        long now() {
            return System.nanoTime();
        }

        void shutdown() {
            allWorkers.dispose();
            if (evictorTask != null) {
                evictorTask.cancel(true);
            }
            if (evictorService != null) {
                evictorService.shutdownNow();
            }
        }
    }

    public IoScheduler() {
        this(WORKER_THREAD_FACTORY);
    }

    /**
     * @param threadFactory thread factory to use for creating worker threads. Note that this takes precedence over any
     *                      system properties for configuring new thread creation. Cannot be null.
     */
    public IoScheduler(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        this.pool = new AtomicReference<CachedWorkerPool>(NONE);
        start();
    }

    @Override
    public void start() {
        CachedWorkerPool update = new CachedWorkerPool(KEEP_ALIVE_TIME, KEEP_ALIVE_UNIT, threadFactory);
        if (!pool.compareAndSet(NONE, update)) {
            update.shutdown();
        }
    }
    @Override
    public void shutdown() {
        for (;;) {
            CachedWorkerPool curr = pool.get();
            if (curr == NONE) {
                return;
            }
            if (pool.compareAndSet(curr, NONE)) {
                curr.shutdown();
                return;
            }
        }
    }

    
    @Override
    public Worker createWorker() {
        return new EventLoopWorker(pool.get());
    }

    public int size() {
        return pool.get().allWorkers.size();
    }

    static final class EventLoopWorker extends Scheduler.Worker {
        private final CompositeDisposable tasks;
        private final CachedWorkerPool pool;
        private final ThreadWorker threadWorker;

        final AtomicBoolean once = new AtomicBoolean();

        EventLoopWorker(CachedWorkerPool pool) {
            this.pool = pool;
            this.tasks = new CompositeDisposable();
            this.threadWorker = pool.get();
        }

        @Override
        public void dispose() {
            if (once.compareAndSet(false, true)) {
                tasks.dispose();

                // releasing the pool should be the last action
                pool.release(threadWorker);
            }
        }

        @Override
        public boolean isDisposed() {
            return once.get();
        }

        
        @Override
        public Disposable schedule( Runnable action, long delayTime,  TimeUnit unit) {
            if (tasks.isDisposed()) {
                // don't schedule, we are unsubscribed
                return EmptyDisposable.INSTANCE;
            }

            return threadWorker.scheduleActual(action, delayTime, unit, tasks);
        }

        @Override
        public Disposable schedule(Runnable run, long delay, TimeUnit unit, SwitcherResultFuture<?> future) throws ExecutionException, InterruptedException {
            if (tasks.isDisposed()) {
                // don't schedule, we are unsubscribed
                return EmptyDisposable.INSTANCE;
            }

            return threadWorker.scheduleActual(run, delay, unit, tasks, future);
        }
    }

    static final class ThreadWorker extends NewThreadWorker {
        private long expirationTime;

        ThreadWorker(ThreadFactory threadFactory) {
            super(threadFactory);
            this.expirationTime = 0L;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(long expirationTime) {
            this.expirationTime = expirationTime;
        }
    }
}
