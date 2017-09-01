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

import com.hujian.switcher.reactive.Disposable;
import com.hujian.switcher.reactive.aux.DisposableHelper;
import com.hujian.switcher.reactive.aux.EmptyDisposable;
import com.hujian.switcher.reactive.flowable.SequentialDisposable;
import com.hujian.switcher.ScheduleHooks;
import com.hujian.switcher.SwitcherResultFuture;
import com.hujian.switcher.schedulers.dispose.CompositeDisposable;
import com.hujian.switcher.schedulers.ds.MpscLinkedQueue;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by hujian06 on 2017/8/29.
 */

/**
 * Wraps an Executor and provides the Scheduler API over it.
 */
public final class ExecutorScheduler extends Scheduler {
    
    private final Executor executor;

    static final Scheduler HELPER = Schedulers.single();

    public ExecutorScheduler(Executor executor) {
        this.executor = executor;
    }
    
    @Override
    public Worker createWorker() {
        return new ExecutorWorker(executor);
    }
    
    @Override
    public Disposable scheduleDirect(Runnable run) {
        Runnable decoratedRun = ScheduleHooks.onSchedule(run);
        try {
            if (executor instanceof ExecutorService) {
                ScheduledDirectTask task = new ScheduledDirectTask(decoratedRun);
                Future<?> f = ((ExecutorService)executor).submit(task);
                task.setFuture(f);
                //i don't know where to shutdown the executor
                //i think this place is a right place to do shutdown work for the executor
                ((ExecutorService) executor).shutdown();

                return task;
            }

            ExecutorWorker.BooleanRunnable br = new ExecutorWorker.BooleanRunnable(decoratedRun);
            executor.execute(br);

            return br;
        } catch (RejectedExecutionException ex) {
            ScheduleHooks.onError(ex);
            return EmptyDisposable.INSTANCE;
        }
    }

    @Override
    public Disposable scheduleDirect(Runnable run, SwitcherResultFuture<?> future) {
        Runnable decoratedRun = ScheduleHooks.onSchedule(run);
        try {
            if (executor instanceof ExecutorService) {
                ScheduledDirectTask task = new ScheduledDirectTask(decoratedRun);
                Future<?> f = ((ExecutorService)executor).submit(task);
                task.setFuture(f);

                //set the future here
                future.setFuture(f);

                //i don't know where to shutdown the executor
                //i think this place is a right place to do shutdown work for the executor
                ((ExecutorService) executor).shutdown();

                return task;
            }

            ExecutorWorker.BooleanRunnable br = new ExecutorWorker.BooleanRunnable(decoratedRun);
            executor.execute(br);

            return br;
        } catch (RejectedExecutionException ex) {
            ScheduleHooks.onError(ex);
            return EmptyDisposable.INSTANCE;
        }
    }
    
    @Override
    public Disposable scheduleDirect(Runnable run, final long delay, final TimeUnit unit)
            throws ExecutionException, InterruptedException {
        final Runnable decoratedRun = ScheduleHooks.onSchedule(run);
        if (executor instanceof ScheduledExecutorService) {
            try {
                ScheduledDirectTask task = new ScheduledDirectTask(decoratedRun);
                Future<?> f = ((ScheduledExecutorService)executor).schedule(task, delay, unit);
                task.setFuture(f);
                return task;
            } catch (RejectedExecutionException ex) {
                ScheduleHooks.onError(ex);
                return EmptyDisposable.INSTANCE;
            }
        }

        final DelayedRunnable dr = new DelayedRunnable(decoratedRun);

        Disposable delayed = HELPER.scheduleDirect(new DelayedDispose(dr), delay, unit);

        dr.timed.replace(delayed);

        return dr;
    }

    @Override
    public Disposable scheduleDirect(Runnable run, final long delay, final TimeUnit unit, SwitcherResultFuture<?> future)
            throws ExecutionException, InterruptedException {
        final Runnable decoratedRun = ScheduleHooks.onSchedule(run);
        if (executor instanceof ScheduledExecutorService) {
            try {
                ScheduledDirectTask task = new ScheduledDirectTask(decoratedRun);
                Future<?> f = ((ScheduledExecutorService)executor).schedule(task, delay, unit);
                task.setFuture(f);

                //set future here
                future.setFuture(f);

                return task;
            } catch (RejectedExecutionException ex) {
                ScheduleHooks.onError(ex);
                return EmptyDisposable.INSTANCE;
            }
        }

        if (executor instanceof ExecutorService) {
            try {
                ScheduledDirectTask task = new ScheduledDirectTask(decoratedRun);
                Future<?> f = ((ExecutorService)executor).submit(task);
                task.setFuture(f);

                //set future here
                future.setFuture(f);

                //it's time to shutdown ?
                ((ExecutorService) executor).shutdown();

                return task;
            } catch (RejectedExecutionException ex) {
                ScheduleHooks.onError(ex);
                return EmptyDisposable.INSTANCE;
            }
        }

        final DelayedRunnable dr = new DelayedRunnable(decoratedRun);

        Disposable delayed = HELPER.scheduleDirect(new DelayedDispose(dr), delay, unit);

        dr.timed.replace(delayed);

        return dr;
    }

    @Override
    public Disposable schedulePeriodicallyDirect(Runnable run, long initialDelay, long period, TimeUnit unit) throws ExecutionException, InterruptedException {
        if (executor instanceof ScheduledExecutorService) {
            Runnable decoratedRun = ScheduleHooks.onSchedule(run);
            try {
                ScheduledDirectPeriodicTask task = new ScheduledDirectPeriodicTask(decoratedRun);
                Future<?> f = ((ScheduledExecutorService)executor).scheduleAtFixedRate(task, initialDelay, period, unit);
                task.setFuture(f);
                return task;
            } catch (RejectedExecutionException ex) {
                ScheduleHooks.onError(ex);
                return EmptyDisposable.INSTANCE;
            }
        }
        return super.schedulePeriodicallyDirect(run, initialDelay, period, unit);
    }

    /* public: test support. */
    public static final class ExecutorWorker extends Scheduler.Worker implements Runnable {
        final Executor executor;

        final MpscLinkedQueue<Runnable> queue;

        volatile boolean disposed;

        final AtomicInteger wip = new AtomicInteger();

        final CompositeDisposable tasks = new CompositeDisposable();

        public ExecutorWorker(Executor executor) {
            this.executor = executor;
            this.queue = new MpscLinkedQueue<Runnable>();
        }

        @Override
        public Disposable schedule(Runnable run) {
            if (disposed) {
                return EmptyDisposable.INSTANCE;
            }

            Runnable decoratedRun = ScheduleHooks.onSchedule(run);
            BooleanRunnable br = new BooleanRunnable(decoratedRun);

            //set the executor here
            if (run instanceof Scheduler.ResultDisposeTask) {
                ((Scheduler.ResultDisposeTask) run).scheduleRunner
                        .setExecutor(executor);

                //run the runner at the schedule.
                run.run();

                //it's time to shutdown the executor
                if (executor instanceof ExecutorService) {
                    ((ExecutorService) executor).shutdown();
                }

                return br;
            }

            queue.offer(br);

            if (wip.getAndIncrement() == 0) {
                try {
                    executor.execute(this);
                } catch (RejectedExecutionException ex) {
                    disposed = true;
                    queue.clear();
                    ScheduleHooks.onError(ex);
                    return EmptyDisposable.INSTANCE;
                } finally {
                    //it's time to shutdown the executor
                    if (executor instanceof ExecutorService) {
                        ((ExecutorService) executor).shutdown();
                    }
                }
            }

            return br;
        }

        @Override
        public Disposable schedule(Runnable run, long delay, TimeUnit unit) throws ExecutionException, InterruptedException {
            if (delay <= 0) {
                return schedule(run);
            }
            if (disposed) {
                return EmptyDisposable.INSTANCE;
            }

            SequentialDisposable first = new SequentialDisposable();

            final SequentialDisposable mar = new SequentialDisposable(first);

            final Runnable decoratedRun = ScheduleHooks.onSchedule(run);

            ScheduledRunnable sr = new ScheduledRunnable(new SequentialDispose(mar, decoratedRun), tasks);
            tasks.add(sr);

            if (executor instanceof ScheduledExecutorService) {
                try {
                    Future<?> f = ((ScheduledExecutorService)executor).schedule((Callable<Object>)sr, delay, unit);

                    ///////////////////////////////////////////////////////
                    //   here you should get the future now.             //
                    //   the recommend way is give the future to caller  //
                    //////////////////////////////////////////////////////
                    try {
                        f.get();
                    } catch (InterruptedException | ExecutionException e) {
                        ScheduleHooks.onError(e);
                    }

                    sr.setFuture(f);
                } catch (RejectedExecutionException ex) {
                    disposed = true;
                    ScheduleHooks.onError(ex);
                    return EmptyDisposable.INSTANCE;
                }
            } else {
                final Disposable d = HELPER.scheduleDirect(sr, delay, unit);
                sr.setFuture(new DisposeOnCancel(d));
            }

            first.replace(sr);

            return mar;
        }

        @Override
        public Disposable schedule(Runnable run, long delay, TimeUnit unit, SwitcherResultFuture<?> future) throws ExecutionException, InterruptedException {
            if (delay < 0) { // oh, fuck !!!
                return schedule(run);
            }
            if (disposed) {
                return EmptyDisposable.INSTANCE;
            }

            SequentialDisposable first = new SequentialDisposable();

            final SequentialDisposable mar = new SequentialDisposable(first);

            final Runnable decoratedRun = ScheduleHooks.onSchedule(run);

            ScheduledRunnable sr = new ScheduledRunnable(new SequentialDispose(mar, decoratedRun), tasks);
            tasks.add(sr);

            if (executor instanceof ScheduledExecutorService) {
                Future<?> f = null;
                try {
                    f = ((ScheduledExecutorService)executor).schedule((Callable<Object>)sr, delay, unit);

                    //set the future final.
                    future.setFuture(f);

                    sr.setFuture(f);
                } catch (RejectedExecutionException ex) {
                    disposed = true;
                    ScheduleHooks.onError(ex);
                    return EmptyDisposable.INSTANCE;
                }
            } else {
                final Disposable d = HELPER.scheduleDirect(sr, delay, unit);

                Future srF = new DisposeOnCancel(d);
                sr.setFuture(srF);

                //set the future for caller
                future.setFuture(srF);
            }

            first.replace(sr);

            return mar;
        }

        @Override
        public void dispose() {
            if (!disposed) {
                disposed = true;
                tasks.dispose();
                if (wip.getAndIncrement() == 0) {
                    queue.clear();
                }
            }
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        @Override
        public void run() {
            int missed = 1;
            final MpscLinkedQueue<Runnable> q = queue;
            for (;;) {

                if (disposed) {
                    q.clear();
                    return;
                }

                for (;;) {
                    Runnable run = q.poll();
                    if (run == null) {
                        break;
                    }
                    run.run();

                    if (disposed) {
                        q.clear();
                        return;
                    }
                }

                if (disposed) {
                    q.clear();
                    return;
                }

                missed = wip.addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        static final class BooleanRunnable extends AtomicBoolean implements Runnable, Disposable {

            final Runnable actual;
            BooleanRunnable(Runnable actual) {
                this.actual = actual;
            }

            @Override
            public void run() {
                if (get()) {
                    return;
                }
                try {
                    actual.run();
                } finally {
                    lazySet(true);
                }
            }

            @Override
            public void dispose() {
                lazySet(true);
            }

            @Override
            public boolean isDisposed() {
                return get();
            }
        }

        final class SequentialDispose implements Runnable {
            private final SequentialDisposable mar;
            private final Runnable decoratedRun;

            SequentialDispose(SequentialDisposable mar, Runnable decoratedRun) {
                this.mar = mar;
                this.decoratedRun = decoratedRun;
            }

            @Override
            public void run() {
                mar.replace(schedule(decoratedRun));
            }
        }
    }

    static final class DelayedRunnable extends AtomicReference<Runnable> implements Runnable, Disposable {

        final SequentialDisposable timed;

        final SequentialDisposable direct;

        DelayedRunnable(Runnable run) {
            super(run);
            this.timed = new SequentialDisposable();
            this.direct = new SequentialDisposable();
        }

        @Override
        public void run() {
            Runnable r = get();
            if (r != null) {
                try {
                    r.run();
                } finally {
                    lazySet(null);
                    timed.lazySet(DisposableHelper.DISPOSED);
                    direct.lazySet(DisposableHelper.DISPOSED);
                }
            }
        }

        @Override
        public boolean isDisposed() {
            return get() == null;
        }

        @Override
        public void dispose() {
            if (getAndSet(null) != null) {
                timed.dispose();
                direct.dispose();
            }
        }
    }

    final class DelayedDispose implements Runnable {
        private final DelayedRunnable dr;

        DelayedDispose(DelayedRunnable dr) {
            this.dr = dr;
        }

        @Override
        public void run() {
            dr.direct.replace(scheduleDirect(dr));
        }
    }
}
