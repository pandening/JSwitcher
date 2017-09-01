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

import com.hujian.switcher.ScheduleHooks;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

/**
 * Static factory methods for returning standard Scheduler instances.
 *
 * The initial and runtime values of the various scheduler types can be overridden via the
 * {@code ScheduleHooks.setInit(scheduler name)SchedulerHandler()} and
 * {@code ScheduleHooks.set(scheduler name)SchedulerHandler()} respectively.
 *
 * <li>{@code switcher.io-priority} (int): sets the thread priority of the {@link #io()} Scheduler, default is {@link Thread#NORM_PRIORITY}</li>
 * <li>{@code switcher.computation-threads} (int): sets the number of threads in the {@link #computation()} Scheduler, default is the number of available CPUs</li>
 * <li>{@code switcher.computation-priority} (int): sets the thread priority of the {@link #computation()} Scheduler, default is {@link Thread#NORM_PRIORITY}</li>
 * <li>{@code switcher.newthread-priority} (int): sets the thread priority of the {@link #newThread()} Scheduler, default is {@link Thread#NORM_PRIORITY}</li>
 * <li>{@code switcher.single-priority} (int): sets the thread priority of the {@link #single()} Scheduler, default is {@link Thread#NORM_PRIORITY}</li>
 * <li>{@code switcher.purge-enabled} (boolean): enables periodic purging of all Scheduler's backing thread pools, default is false</li>
 * <li>{@code switcher.purge-period-seconds} (int): specifies the periodic purge interval of all Scheduler's backing thread pools, default is 1 second</li>
 * </ul>
 */
public final class Schedulers {
    
    private static final Scheduler SINGLE;
    
    private static final Scheduler COMPUTATION;
    
    private static final Scheduler IO;
    
    private static final Scheduler TRAMPOLINE;
    
    private static final Scheduler NEW_THREAD;

    static final class SingleHolder {
        static final Scheduler DEFAULT = new SingleScheduler();
    }

    static final class ComputationHolder {
        static final Scheduler DEFAULT = new ComputationScheduler();
    }

    static final class IoHolder {
        static final Scheduler DEFAULT = new IoScheduler();
    }

    static final class NewThreadHolder {
        static final Scheduler DEFAULT = new NewThreadScheduler();
    }

    static {
        SINGLE = ScheduleHooks.initSingleScheduler(new SingleTask());

        COMPUTATION = ScheduleHooks.initComputationScheduler(new ComputationTask());

        IO = ScheduleHooks.initIoScheduler(new IOTask());

        TRAMPOLINE = TrampolineScheduler.instance();

        NEW_THREAD = ScheduleHooks.initNewThreadScheduler(new NewThreadTask());
    }

    /** Utility class. */
    private Schedulers() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * Returns a default, shared {@link Scheduler} instance intended for computational work.
     * @return a {@link Scheduler} meant for computation-bound work
     */
    public static Scheduler computation() {
        return ScheduleHooks.onComputationScheduler(COMPUTATION);
    }

    /**
     * Returns a default, shared {@link Scheduler} instance intended for IO-bound work.
     * @return a {@link Scheduler} meant for IO-bound work
     */
    public static Scheduler io() {
        return ScheduleHooks.onIoScheduler(IO);
    }

    /**
     * Returns a default, shared {@link Scheduler}
     * This scheduler can't be overridden via an {@link ScheduleHooks} method.
     * @return a {@link Scheduler} that queues work on the current thread
     */
    public static Scheduler trampoline() {
        return TRAMPOLINE;
    }

    /**
     * Returns a default, shared {@link Scheduler} instance that creates a new {@link Thread} for each unit of work.
     * @return a {@link Scheduler} that creates new threads
     */
    public static Scheduler newThread() {
        return ScheduleHooks.onNewThreadScheduler(NEW_THREAD);
    }

    /**
     * Returns a default, shared, single-thread-backed {@link Scheduler} instance for work
     * requiring strongly-sequential execution on the same background thread.
     * @return a {@link Scheduler} that shares a single backing thread.
     */
    public static Scheduler single() {
        return ScheduleHooks.onSingleScheduler(SINGLE);
    }

    /**
     * Wraps an {@link Executor} into a new Scheduler instance and delegates {@code schedule()}
     * calls to it.
     * Note that this method returns a new {@link Scheduler} instance, even for the same {@link Executor} instance.
     * @param executor
     *          the executor to wrap
     * @return the new Scheduler wrapping the Executor
     */
    public static Scheduler from(Executor executor) {
        return new ExecutorScheduler(executor);
    }

    /**
     * Shuts down the standard Schedulers.
     * <p>The operation is idempotent and thread-safe.
     */
    public static void shutdown() {
        computation().shutdown();
        io().shutdown();
        newThread().shutdown();
        single().shutdown();
        trampoline().shutdown();
        SchedulerPoolFactory.shutdown();
    }

    /**
     * Starts the standard Schedulers.
     * <p>The operation is idempotent and thread-safe.
     */
    public static void start() {
        computation().start();
        io().start();
        newThread().start();
        single().start();
        trampoline().start();
        SchedulerPoolFactory.start();
    }

    static final class IOTask implements Callable<Scheduler> {
        @Override
        public Scheduler call() throws Exception {
            return IoHolder.DEFAULT;
        }
    }

    static final class NewThreadTask implements Callable<Scheduler> {
        @Override
        public Scheduler call() throws Exception {
            return NewThreadHolder.DEFAULT;
        }
    }

    static final class SingleTask implements Callable<Scheduler> {
        @Override
        public Scheduler call() throws Exception {
            return SingleHolder.DEFAULT;
        }
    }

    static final class ComputationTask implements Callable<Scheduler> {
        @Override
        public Scheduler call() throws Exception {
            return ComputationHolder.DEFAULT;
        }
    }
}
