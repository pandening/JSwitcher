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

package com.hujian.switcher;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.hujian.switcher.core.ExecutorType;
import com.hujian.switcher.core.SwitchExecutorService;
import com.hujian.switcher.core.SwitchRunntimeException;
import com.hujian.switcher.core.Switcher;
import com.hujian.switcher.utils.DebugHelper;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by hujian06 on 2017/8/18.
 */
public class SampleSwitcher implements Switcher {
    private static Logger LOGGER = Logger.getLogger(SampleSwitcher.class);

    private static final String IO_EXECUTOR_NAME = "io-executorService";
    private static final String MULTI_IO_EXECUTOR_SERVICE = "multi-io-executorService";
    private static final String COMPUTE_EXECUTOR_SERVICE = "compute-executorService";
    private static final String MULTI_COMPUTE_EXECUTOR_SERVICE = "multi-compute-executorService";
    private static final String SINGLE_EXECUTOR_SERVICE = "single-executorService";
    private static final String NEW_EXECUTOR_SERVICE = "new-executorService";

    private static final String UPSUPPORTED_OPERATOR_ERROR = "unsupported operator now";

    private static volatile Boolean isMainThread = false; // set the main thread

    protected static volatile BlockingDeque<SwitchExecutorServiceEntry> switchExecutorServicesQueue;

    /**
     * the current executor Service
     */
    private static volatile SwitchExecutorServiceEntry currentExecutorService =
            SwitchExecutorServiceEntry.emptyEntry();

    //do some initialize job here
    static {
        switchExecutorServicesQueue = new LinkedBlockingDeque<>();
        LOGGER.info("switchExecutorServicesQueue has been initialized");
    }

    /**
     * switch.
     * @param activityExecutorService
     * @param activityExecutorType
     */
    protected static synchronized void
        switchExecutorService(String activityExecutorType,
                          ExecutorService activityExecutorService) throws InterruptedException {
        if (activityExecutorType == null) {
            LOGGER.error("null activityExecutorType");
            return;
        }

        if (activityExecutorService == null) {
            LOGGER.error("null activityExecutorService");
            return;
        }
        if (! ExecutorType.EMPTY_EXECUTOR_SERVICE.getName().equals(currentExecutorService.getExecutorType())) {
            //switchExecutorServicesQueue.putFirst(currentExecutorService);
            LOGGER.info("switch executorService from [" + currentExecutorService.getExecutorService() +"] " +
                    "to [" + activityExecutorService + "]");
        } else {
            LOGGER.info("this is the first executorService on the BlockingDeque.[" + activityExecutorService + "]");
        }
        currentExecutorService = new SwitchExecutorServiceEntry(activityExecutorType, activityExecutorService);
        isMainThread = false;
    }

    protected static ExecutorService createExecutorService(String executorType) throws InterruptedException {
        ExecutorService executorService = SwitchExecutorService.createNewExecutorService(executorType);
        if (executorService != null && !switchExecutorServicesQueue.contains(executorService)) {
            switchExecutorServicesQueue.putFirst(new SwitchExecutorServiceEntry(executorType, executorService));
        }
        switchExecutorService(executorType, executorService);
        return executorService;
    }

    private static ExecutorService getOrCreateExecutorService(String executorType, Boolean isCreateMode)
            throws InterruptedException {
        Preconditions.checkArgument(executorType != null && !executorType.isEmpty(),
                "the executor type is null or empty");
        //debug
        //DebugHelper.trackExecutorQueue(executorType, switchExecutorServicesQueue);

        if (currentExecutorService != null && executorType.equals(currentExecutorService.getExecutorType())) {
            return currentExecutorService.getExecutorService();
        }

        ExecutorService executorService = null;
        Iterator iterator = switchExecutorServicesQueue.iterator();
        while (iterator.hasNext()) {
            SwitchExecutorServiceEntry executorServiceEntry = (SwitchExecutorServiceEntry) iterator.next();
            if (executorType.equals(executorServiceEntry.getExecutorType())) {
                executorService = executorServiceEntry.getExecutorService();
                break;
            }
        }
        if (executorService != null) {
            //switchExecutorServicesQueue.remove(executorService);
        } else if (isCreateMode){
            executorService =  SwitchExecutorService.createNewExecutorService(executorType);
            if (executorService != null && !switchExecutorServicesQueue.contains(executorService)) {
                switchExecutorServicesQueue
                        .putFirst(new SwitchExecutorServiceEntry(executorType, executorService));
            }
        }

        //do switch
        switchExecutorService(executorType, executorService);

        //the {@code executorService} may as null,so you should check the return value before using it!
        return executorService;
    }

    /**
     * back to old executor.
     * @param executorType
     * @param isCreateMode
     * @return
     */
    private static ExecutorService getBackExecutorService(String executorType, Boolean isCreateMode)
            throws InterruptedException {
        ExecutorService executorService;
        SwitchExecutorServiceEntry executorServiceEntry  = switchExecutorServicesQueue.takeLast();
        if (executorServiceEntry == null ||
                !executorType.equals(executorServiceEntry.getExecutorType()) && isCreateMode) {
            switch (executorType) {
                case IO_EXECUTOR_NAME:
                    executorService = SwitchExecutorService.ioExecutorService;
                    break;
                case MULTI_IO_EXECUTOR_SERVICE:
                    executorService = SwitchExecutorService.multiIoExecutorService;
                    break;
                case COMPUTE_EXECUTOR_SERVICE:
                    executorService = SwitchExecutorService.computeExecutorService;
                    break;
                case MULTI_COMPUTE_EXECUTOR_SERVICE:
                    executorService = SwitchExecutorService.multiComputeExecutorService;
                    break;
                case SINGLE_EXECUTOR_SERVICE:
                    executorService = SwitchExecutorService.singleExecutorService;
                    break;
                case NEW_EXECUTOR_SERVICE:
                    executorService = SwitchExecutorService.newExecutorService;
                    break;
                default:
                    executorService = SwitchExecutorService.newExecutorService;
                    break;
            }
        } else {
            executorService = executorServiceEntry.getExecutorService();
            switchExecutorServicesQueue.remove(executorService);
        }

        //do switch
        switchExecutorService(executorType, executorService);

        return executorService;
    }

    @Override
    public SwitchExecutorServiceEntry getCurrentExecutorService() {
        return currentExecutorService;
    }

    @Override
    public void clear() throws InterruptedException {
        if (switchExecutorServicesQueue == null || switchExecutorServicesQueue.isEmpty()) {
            return;
        }
        Iterator<SwitchExecutorServiceEntry> iterator = switchExecutorServicesQueue.iterator();

        //sign shutdown flag.
        while (iterator.hasNext()) {
            SwitchExecutorServiceEntry executorServiceEntry = iterator.next();
            if (!executorServiceEntry.getExecutorService().isShutdown()) {
                executorServiceEntry.getExecutorService().shutdownNow();
            }
        }

        //NOTE!!!
        if (!currentExecutorService.getExecutorService().isShutdown()) {
            currentExecutorService.getExecutorService().shutdownNow();
        }
    }

    @Override
    public Switcher getCurrentSwitcher() {
        return this;
    }

    @Override
    public synchronized Switcher shutdown() {
        if (!SwitchExecutorServiceEntry.emptyEntry().getExecutorType()
                .equals(currentExecutorService.getExecutorType())
                && !currentExecutorService.getExecutorService().isShutdown()) {
            currentExecutorService.getExecutorService().shutdown();
        }
        return this;
    }

    @Override
    public synchronized Switcher shutdownNow() {
        if (!SwitchExecutorServiceEntry.emptyEntry().getExecutorType()
                .equals(currentExecutorService.getExecutorType())
                && !currentExecutorService.getExecutorService().isShutdown()) {
            currentExecutorService.getExecutorService().shutdownNow();
        }
        return this;
    }

    @Override
    public Switcher assignExecutorName(String name) {
        Preconditions.checkArgument(name != null && !name.isEmpty(),
                "name must not null and empty");
        Preconditions.checkArgument(currentExecutorService != null,
                "currentExecutorService is null");
        if (Strings.isNullOrEmpty(currentExecutorService.getExecutorName())) {
            currentExecutorService.setExecutorName(name);
        } else {
            LOGGER.warn("currentExecutorService had been assigned an " +
                    "name:" + currentExecutorService.getExecutorName());
        }

        return this;
    }

    @Override
    public Switcher apply(Runnable job, Boolean isCreateMode) throws SwitchRunntimeException, InterruptedException {
        synchronized (SampleSwitcher.class) {
            if (isMainThread) { // run the job on the main thread
                job.run();
                return this;
            }
            if (ExecutorType.EMPTY_EXECUTOR_SERVICE.getName().equals(currentExecutorService.getExecutorType())) {
                if (isCreateMode) {
                    LOGGER.info("No Executor To Run Job: " + job + " , try to get an new Executor..");
                    ExecutorService executorService = SwitchExecutorService.createNewExecutorService(NEW_EXECUTOR_SERVICE);
                    executorService.submit(job);

                    switchExecutorService(NEW_EXECUTOR_SERVICE, executorService);
                } else {
                    LOGGER.error("No Executor To Run Job:" + job);
                    throw  new SwitchRunntimeException("No ExecutorService to Run Job:" + job);
                }
            } else {
                if (!currentExecutorService.getExecutorService().isShutdown()) {
                    //currentExecutorService.getExecutorService().shutdownNow();
                }
                currentExecutorService.getExecutorService().submit(job);
            }
        }
        return this;
    }

    @Override
    public Switcher switchToMain() {
        isMainThread = true;
        return this;
    }

    @Override
    public Switcher switchToExecutor(ExecutorService executorService) throws InterruptedException {
        return switchToExecutor(executorService, "");
    }

    @Override
    public Switcher switchToExecutor(ExecutorService executorService, String name)
            throws InterruptedException {
        Preconditions.checkArgument(executorService != null,
                "executorService is null");
        Preconditions.checkArgument(name != null, "executor name is null");

        String executorType = ExecutorType.CUSTOM_EXECUTOR_SERVICE.getName();

        SwitchExecutorServiceEntry executorServiceEntry =
                new SwitchExecutorServiceEntry(executorType, executorService, name);

        //append to the queue
        if (switchExecutorServicesQueue != null && switchExecutorServicesQueue.contains(executorServiceEntry)) {
            switchExecutorServicesQueue.putFirst(executorServiceEntry);
        }

        //switch executorService
        switchExecutorService(executorType, executorService);

        return this;
    }

    @Override
    public Switcher switchToIoExecutor(Boolean isCreateMode) throws InterruptedException {
        getOrCreateExecutorService(IO_EXECUTOR_NAME, isCreateMode);
        return this;
    }

    @Override
    public Switcher switchToMultiIoExecutor(Boolean isCreateMode) throws InterruptedException {
        getOrCreateExecutorService(MULTI_IO_EXECUTOR_SERVICE, isCreateMode);
        return this;
    }

    @Override
    public Switcher switchToNewIoExecutor() throws InterruptedException {
        createExecutorService(IO_EXECUTOR_NAME);
        return this;
    }

    @Override
    public Switcher switchToNewMultiIoExecutor() throws InterruptedException {
        createExecutorService(MULTI_IO_EXECUTOR_SERVICE);
        return this;
    }

    @Override
    public Switcher switchToComputeExecutor(Boolean isCreateMode) throws InterruptedException {
        getOrCreateExecutorService(COMPUTE_EXECUTOR_SERVICE, isCreateMode);
        return this;
    }

    @Override
    public Switcher switchToMultiComputeExecutor(Boolean isCreateMode) throws InterruptedException {
        getOrCreateExecutorService(MULTI_COMPUTE_EXECUTOR_SERVICE, isCreateMode);
        return this;
    }

    @Override
    public Switcher switchToNewComputeExecutor() throws InterruptedException {
        createExecutorService(COMPUTE_EXECUTOR_SERVICE);
        return this;
    }

    @Override
    public Switcher switchToNewMultiComputeExecutor() throws InterruptedException {
        createExecutorService(MULTI_COMPUTE_EXECUTOR_SERVICE);
        return this;
    }

    @Override
    public Switcher switchToSingleExecutor(Boolean isCreateMode) throws InterruptedException {
        getOrCreateExecutorService(SINGLE_EXECUTOR_SERVICE, isCreateMode);
        return this;
    }

    @Override
    public Switcher switchToNewSingleExecutor() throws InterruptedException {
        createExecutorService(SINGLE_EXECUTOR_SERVICE);
        return this;
    }

    @Override
    public Switcher switchToNewExecutor() throws InterruptedException {
        getOrCreateExecutorService(NEW_EXECUTOR_SERVICE, true);
        return this;
    }

    @Override
    public Switcher switchBackToIoExecutor(Boolean isCreateMode) throws InterruptedException {
        getBackExecutorService(IO_EXECUTOR_NAME, isCreateMode);
        return this;
    }

    @Override
    public Switcher switchBackToMultiIoExecutor(Boolean isCreateMode) throws InterruptedException {
        getBackExecutorService(MULTI_IO_EXECUTOR_SERVICE, isCreateMode);
        return this;
    }

    @Override
    public Switcher switchBackToComputeExecutor(Boolean isCreateMode) throws InterruptedException {
        getBackExecutorService(COMPUTE_EXECUTOR_SERVICE, isCreateMode);
        return this;
    }

    @Override
    public Switcher switchBackToMultiComputeExecutor(Boolean isCreateMode) throws InterruptedException {
        getBackExecutorService(MULTI_COMPUTE_EXECUTOR_SERVICE, isCreateMode);
        return this;
    }

    /**
     * do the job~
     * @param job
     * @param isCreateMode
     */
    private static void doWorkInExecutorService(Runnable job, Boolean isCreateMode, String expectExecutorType)
            throws SwitchRunntimeException, InterruptedException {
        if (isMainThread) {
            job.run();
            return;
        }
        if (ExecutorType.EMPTY_EXECUTOR_SERVICE.getName().equals(currentExecutorService.getExecutorType())) {
            if (isCreateMode) {
                LOGGER.info("No ExecutorService to Run Job:" + job + " try to get an new ExecutorService..");
                synchronized (SampleSwitcher.class) {
                    ExecutorService executorService = createExecutorService(expectExecutorType);
                    executorService.submit(job);
                    switchExecutorService(expectExecutorType, executorService);
                }
            } else {
                LOGGER.error("Fail to Run Job:" + job);
                throw new SwitchRunntimeException("No ExecutorService to Run Job:" + job);
            }
        } else {
            ExecutorService currentExecutor = currentExecutorService.getExecutorService();
            currentExecutor.submit(job);
        }
    }

    @Override
    public Switcher switchAfterIOWork(Runnable job, Boolean isMultiMode, Boolean isCreateMode)
            throws SwitchRunntimeException, InterruptedException {
        doWorkInExecutorService(job, isCreateMode, IO_EXECUTOR_NAME);
        return switchToIoExecutor(isCreateMode);
    }

    @Override
    public Switcher switchAfterComputeWork(Runnable job, Boolean isMultiMode, Boolean isCreateMode)
            throws SwitchRunntimeException, InterruptedException {
        doWorkInExecutorService(job, isCreateMode, COMPUTE_EXECUTOR_SERVICE);
        return switchToComputeExecutor(isCreateMode);
    }

    @Override
    public Switcher switchAfterWork(Runnable job, Boolean isMultiMode, Boolean isCreateMode)
            throws SwitchRunntimeException, InterruptedException {
        doWorkInExecutorService(job, isCreateMode, SINGLE_EXECUTOR_SERVICE);
        return switchToSingleExecutor(isCreateMode);
    }

    @Override
    public Switcher switchBeforeIoWork(Runnable job, Boolean isMultiMode, Boolean isCreateMode)
            throws InterruptedException {
        ExecutorService executorService;
        if (isMultiMode) {
            executorService = getOrCreateExecutorService(MULTI_IO_EXECUTOR_SERVICE, isCreateMode);
            executorService.submit(job);
            switchExecutorService(MULTI_IO_EXECUTOR_SERVICE, executorService);
        } else {
            executorService = getOrCreateExecutorService(IO_EXECUTOR_NAME, isCreateMode);
            executorService.submit(job);
            switchExecutorService(IO_EXECUTOR_NAME, executorService);
        }

        return this;
    }

    @Override
    public Switcher switchBeforeComputeWork(Runnable job, Boolean isMultiMode, Boolean isCreateMode)
            throws InterruptedException {
        ExecutorService executorService;
        if (!isMultiMode) {
            executorService = getOrCreateExecutorService(COMPUTE_EXECUTOR_SERVICE, isCreateMode);
            executorService.submit(job);
            switchExecutorService(COMPUTE_EXECUTOR_SERVICE, executorService);
        } else {
            executorService = getOrCreateExecutorService(MULTI_COMPUTE_EXECUTOR_SERVICE, isCreateMode);
            executorService.submit(job);
            switchExecutorService(MULTI_COMPUTE_EXECUTOR_SERVICE, executorService);
        }

        return this;
    }

    @Override
    public Switcher switchBeforeWork(Runnable job, Boolean isMultiMode, Boolean isCreateMode)
            throws InterruptedException {
        throw new UnsupportedOperationException(UPSUPPORTED_OPERATOR_ERROR);
    }

    @Override
    public RichnessSwitcher transToRichnessSwitcher() {
        return (RichnessSwitcher) this;
    }

}
