/**
 * Copyright 2017 hujian
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

package com.hujian.switcher.core;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hujian06 on 2017/8/18.
 */
public class SwitchExecutorService {
    private static Logger LOGGER = Logger.getLogger(SwitchExecutorService.class);

    private static int IO_PRIORITY = 1;
    private static int COMPUTE_PRIORITY = 10;
    private static int CPU_CORE_SIZE = Runtime.getRuntime().availableProcessors();
    private static int MULTI_IO_THREAD_SIZE = (int) (CPU_CORE_SIZE / 0.2);
    private static int MULTI_COMPUTE_THREAD_SIZE = CPU_CORE_SIZE * 2;

    private static final String IO_EXECUTOR_NAME = "io-executorService";
    private static final String MULTI_IO_EXECUTOR_SERVICE = "multi-io-executorService";
    private static final String COMPUTE_EXECUTOR_SERVICE = "compute-executorService";
    private static final String MULTI_COMPUTE_EXECUTOR_SERVICE = "multi-compute-executorService";
    private static final String SINGLE_EXECUTOR_SERVICE = "single-executorService";
    private static final String NEW_EXECUTOR_SERVICE = "new-executorService";

    public static ExecutorService ioExecutorService;
    public static ExecutorService multiIoExecutorService;
    public static ExecutorService computeExecutorService;
    public static ExecutorService multiComputeExecutorService;
    public static ExecutorService newExecutorService;
    public static ExecutorService singleExecutorService;
    public static ExecutorService defaultRunExecutorService;
    public static ExecutorService mainExecutorService; // fake

    /**
     * you want to force to get an new executor.
     * @param executorType the executorService type
     * @return
     */
    public static ExecutorService createNewExecutorService(String executorType) {
        Preconditions.checkArgument(executorType != null && !executorType.isEmpty(),
                "Failed to create new ExecutorService ExecutorService Type must be assigned");
        ExecutorService executorService;
        switch (executorType) {
            case IO_EXECUTOR_NAME:
                executorService = Executors.newSingleThreadExecutor(getIoThreadFactory());
                break;
            case MULTI_IO_EXECUTOR_SERVICE:
                executorService = Executors.newFixedThreadPool(MULTI_IO_THREAD_SIZE, getMultiIoThreadFactory());
                break;
            case COMPUTE_EXECUTOR_SERVICE:
                executorService = Executors.newSingleThreadExecutor(getComputeThreadFactory());
                break;
            case MULTI_COMPUTE_EXECUTOR_SERVICE:
                executorService = Executors.newFixedThreadPool(MULTI_COMPUTE_THREAD_SIZE, getMultiComputeThreadFactory());
                break;
            case SINGLE_EXECUTOR_SERVICE:
                executorService = Executors.newSingleThreadExecutor(getThreadFactory());
                break;
            case NEW_EXECUTOR_SERVICE:
                executorService = Executors.newSingleThreadExecutor(getThreadFactory());
                break;
            default:
                executorService = Executors.newFixedThreadPool(CPU_CORE_SIZE, getThreadFactory());
                break;
        }

        return executorService;
    }

    static {
        initIoExecutorService();
        initComputeExecutorService();
        initNewExecutorService();
        initSingleExecutorService();
        initMultiIoExecutorService();
        initMultiComputeExecutorService();
        initDefaultRunExecutorService();
        initMainExecutorService();

        LOGGER.info("init executorServices done:" + CPU_CORE_SIZE + "/" +
                MULTI_IO_THREAD_SIZE + "/" + MULTI_COMPUTE_THREAD_SIZE);
    }

    /**
     * error handler
     */
    static Thread.UncaughtExceptionHandler exceptionHandler =
            (t, e) -> LOGGER.error("Uncaught Exception. threadName:" +
                    t.getName() + " error:" + e);

    /**
     * return io type thread factory
     * @return
     */
    private static ThreadFactory getIoThreadFactory() {

        ThreadFactory threadFactory =
                r -> {
                    Thread t = new Thread(r, "SwitcherThread-I/O");
                    t.setPriority(IO_PRIORITY);
                    t.setUncaughtExceptionHandler(exceptionHandler);
                    return t;
                };

        return threadFactory;
    }

    /**
     * return io type thread factory
     * @return
     */
    private static ThreadFactory getMultiIoThreadFactory() {

        ThreadFactory threadFactory =
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "SwitcherThread-I/O-"
                                + threadNumber.getAndIncrement());
                        t.setPriority(IO_PRIORITY);
                        t.setUncaughtExceptionHandler(exceptionHandler);
                        return t;
                    }
                };

        return threadFactory;
    }

    /**
     * return compute type thread factory
     * @return
     */
    private static ThreadFactory getComputeThreadFactory() {

        ThreadFactory threadFactory =
                r -> {
                    Thread t = new Thread(r, "SwitcherThread-Compute");
                    t.setPriority(COMPUTE_PRIORITY);
                    t.setUncaughtExceptionHandler(exceptionHandler);
                    return t;
                };

        return threadFactory;
    }

    /**
     * return compute type thread factory
     * @return
     */
    private static ThreadFactory getMultiComputeThreadFactory() {

        ThreadFactory threadFactory =
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "SwitcherThread-Compute-"
                                + threadNumber.getAndIncrement());
                        t.setPriority(COMPUTE_PRIORITY);
                        t.setUncaughtExceptionHandler(exceptionHandler);
                        return t;
                    }
                };

        return threadFactory;
    }

    /**
     * return an normal thread factory
     * @return
     */
    private static ThreadFactory getThreadFactory() {

        ThreadFactory threadFactory =
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "SwitcherThread-"
                                + threadNumber.getAndIncrement());
                        t.setUncaughtExceptionHandler(exceptionHandler);
                        return t;
                    }
                };

        return threadFactory;
    }


    private static void initIoExecutorService() {
        ioExecutorService = Executors.newSingleThreadExecutor(getIoThreadFactory());
    }

    private static void initComputeExecutorService() {
        computeExecutorService = Executors.newSingleThreadExecutor(getComputeThreadFactory());
    }

    private static void initNewExecutorService() {
        //todo: let the executorService "special"
        newExecutorService = Executors.newSingleThreadExecutor(getThreadFactory());
    }

    private static void initSingleExecutorService() {
        singleExecutorService = Executors.newSingleThreadExecutor(getThreadFactory());
    }

    private static void initMultiIoExecutorService() {
        multiIoExecutorService =
                Executors.newFixedThreadPool(MULTI_IO_THREAD_SIZE, getMultiIoThreadFactory());
    }

    private static void initMultiComputeExecutorService() {
        multiComputeExecutorService =
                Executors.newFixedThreadPool(MULTI_COMPUTE_THREAD_SIZE, getMultiComputeThreadFactory());
    }

    private static void initMainExecutorService() {
        mainExecutorService = null;
    }

    private static void initDefaultRunExecutorService() {
        defaultRunExecutorService =
                new ThreadPoolExecutor(
                        50,
                        500,
                        300,
                        TimeUnit.SECONDS,
                        new SynchronousQueue<>()
                );
    }

}
