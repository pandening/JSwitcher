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

import com.hujian.switcher.schedulers.ScheduleHooks;
import com.hujian.switcher.schedulers.dispose.DisposableContainer;
import com.hujian.switcher.reactive.Disposable;
import com.hujian.switcher.reactive.aux.EmptyDisposable;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Base class that manages a single-threaded ScheduledExecutorService as a
 * worker but doesn't perform task-tracking operations.
 *
 */
public class NewThreadWorker extends Scheduler.Worker implements Disposable {
    private final ScheduledExecutorService executor;

    private volatile boolean disposed;

    public NewThreadWorker(ThreadFactory threadFactory) {
        executor = SchedulerPoolFactory.create(threadFactory);
    }

    @Override
    public Disposable schedule(final Runnable run) {
        return schedule(run, 0, null);
    }

    @Override
    public Disposable schedule(final Runnable action, long delayTime, TimeUnit unit) {
        if (disposed) {
            return EmptyDisposable.INSTANCE;
        }
        return scheduleActual(action, delayTime, unit, null);
    }

    /**
     * Schedules the given runnable on the underlying executor directly and
     * returns its future wrapped into a Disposable.
     * @param run the Runnable to execute in a delayed fashion
     * @param delayTime the delay amount
     * @param unit the delay time unit
     * @return the ScheduledRunnable instance
     */
    public Disposable scheduleDirect(final Runnable run, long delayTime, TimeUnit unit) {
        ScheduledDirectTask task = new ScheduledDirectTask(ScheduleHooks.onSchedule(run));
        try {
            Future<?> f;
            if (delayTime <= 0L) {
                f = executor.submit(task);
            } else {
                f = executor.schedule(task, delayTime, unit);
            }
            task.setFuture(f);
            return task;
        } catch (RejectedExecutionException ex) {
            ScheduleHooks.onError(ex);
            return EmptyDisposable.INSTANCE;
        }
    }

    /**
     * Schedules the given runnable periodically on the underlying executor directly
     * and returns its future wrapped into a Disposable.
     * @param run the Runnable to execute in a periodic fashion
     * @param initialDelay the initial delay amount
     * @param period the repeat period amount
     * @param unit the time unit for both the initialDelay and period
     * @return the ScheduledRunnable instance
     */
    public Disposable schedulePeriodicallyDirect(Runnable run, long initialDelay, long period, TimeUnit unit) {
        final Runnable decoratedRun = ScheduleHooks.onSchedule(run);
        if (period <= 0L) {

            InstantPeriodicTask periodicWrapper = new InstantPeriodicTask(decoratedRun, executor);
            try {
                Future<?> f;
                if (initialDelay <= 0L) {
                    f = executor.submit(periodicWrapper);
                } else {
                    f = executor.schedule(periodicWrapper, initialDelay, unit);
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
            Future<?> f = executor.scheduleAtFixedRate(task, initialDelay, period, unit);
            task.setFuture(f);
            return task;
        } catch (RejectedExecutionException ex) {
            ScheduleHooks.onError(ex);
            return EmptyDisposable.INSTANCE;
        }
    }


    /**
     * Wraps the given runnable into a ScheduledRunnable and schedules it
     * on the underlying ScheduledExecutorService.
     * <p>If the schedule has been rejected, the ScheduledRunnable.wasScheduled will return
     * false.
     * @param run the runnable instance
     * @param delayTime the time to delay the execution
     * @param unit the time unit
     * @param parent the optional tracker parent to add the created ScheduledRunnable instance to before it gets scheduled
     * @return the ScheduledRunnable instance
     */
    public ScheduledRunnable scheduleActual(final Runnable run, long delayTime, TimeUnit unit, DisposableContainer parent) {
        Runnable decoratedRun = ScheduleHooks.onSchedule(run);

        ScheduledRunnable sr = new ScheduledRunnable(decoratedRun, parent);

        if (parent != null) {
            if (!parent.add(sr)) {
                return sr;
            }
        }

        Future<?> f;
        try {
            if (delayTime <= 0) {
                f = executor.submit((Callable<Object>)sr);
            } else {
                f = executor.schedule((Callable<Object>)sr, delayTime, unit);
            }
            sr.setFuture(f);
        } catch (RejectedExecutionException ex) {
            if (parent != null) {
                parent.remove(sr);
            }
            ScheduleHooks.onError(ex);
        }

        return sr;
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            executor.shutdownNow();
        }
    }

    /**
     * Shuts down the underlying executor in a non-interrupting fashion.
     */
    public void shutdown() {
        if (!disposed) {
            disposed = true;
            executor.shutdown();
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}

