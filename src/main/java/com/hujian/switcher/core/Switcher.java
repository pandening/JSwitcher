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

import com.hujian.switcher.RichnessSwitcher;
import com.hujian.switcher.SwitchExecutorServiceEntry;

import java.util.concurrent.ExecutorService;

/**
 * Created by hujian06 on 2017/8/18.
 */
public interface Switcher {

    /**
     * get current executorService
     * @return
     */
    SwitchExecutorServiceEntry getCurrentExecutorService();

    /**
     * clear the executor queue
     */
    void clear() throws InterruptedException;

    /**
     * get current switcher
     * @return
     */
    Switcher getCurrentSwitcher();

    /**
     * shutdown
     */
    Switcher shutdown();

    /**
     * shutdown now
     * @return
     */
    Switcher shutdownNow();

    /**
     * assign name for current executorSer
     * @param name
     * @return
     */
    Switcher assignExecutorName(String name);

    /**
     * apply the job on the current executorService
     * @param job
     * @param isCreateMode
     */
    Switcher apply(Runnable job, Boolean isCreateMode) throws SwitchRunntimeException, InterruptedException;

    /**
     * you can switch to main thread and run the job on the main thread
     * @return
     */
    Switcher switchToMain();

    /**
     * you want to set your own executorService
     * @param executorService
     * @return
     */
    Switcher switchToExecutor(ExecutorService executorService) throws InterruptedException;

    /**
     * you want to set your own executorService
     * @param executorService
     * @param name
     * @return
     * @throws InterruptedException
     */
    Switcher switchToExecutor(ExecutorService executorService, String name) throws InterruptedException;

    /**
     * you want to switch the thread to a "io" executorService
     * @param isCreateMode
     * @return
     */
    Switcher switchToIoExecutor(Boolean isCreateMode) throws InterruptedException;

    /**
     * switch to a "multi - io " executor
     * @param isCreateMode
     * @return
     */
    Switcher switchToMultiIoExecutor(Boolean isCreateMode) throws InterruptedException;

    /**
     * you want to switch the thread to a "io" executorService
     * @return
     */
    Switcher switchToNewIoExecutor() throws InterruptedException;

    /**
     * switch to a "multi - io " executor
     * @return
     */
    Switcher switchToNewMultiIoExecutor() throws InterruptedException;

    /**
     * you want to switch the thread to a "compute" executorService
     * @param isCreateMode
     * @return
     */
    Switcher switchToComputeExecutor(Boolean isCreateMode) throws InterruptedException;

    /**
     * switch to a "multi - compute" executor
     * @param isCreateMode
     * @return
     */
    Switcher switchToMultiComputeExecutor(Boolean isCreateMode) throws InterruptedException;

    /**
     * you want to switch the thread to a "compute" executorService
     * @return
     */
    Switcher switchToNewComputeExecutor() throws InterruptedException;

    /**
     * switch to a "multi - compute" executor
     * @return
     */
    Switcher switchToNewMultiComputeExecutor() throws InterruptedException;

    /**
     * switch to a new executorService
     * @param isCreateMode
     * @return
     */
    Switcher switchToSingleExecutor(Boolean isCreateMode) throws InterruptedException;

    /**
     * switch to a new executorService
     * @return
     */
    Switcher switchToNewSingleExecutor() throws InterruptedException;

    /**
     * just switch to an new executor
     * @return
     */
    Switcher switchToNewExecutor() throws InterruptedException;

    /**
     * go back to "io" executor
     * @param isCreateMode  
     * @return
     */
    Switcher switchBackToIoExecutor(Boolean isCreateMode) throws InterruptedException;

    /**
     * go back to "multi - io " executor
     * @param isCreateMode
     * @return
     */
    Switcher switchBackToMultiIoExecutor(Boolean isCreateMode) throws InterruptedException;

    /**
     * go back to "compute" executor
     * @param isCreateMode 
     * @return
     */
    Switcher switchBackToComputeExecutor(Boolean isCreateMode) throws InterruptedException;

    /**
     * go back to "multi - compute" executor
     * @param isCreateMode
     * @return
     */
    Switcher switchBackToMultiComputeExecutor(Boolean isCreateMode) throws InterruptedException;


    /**
     * do the job on the current thread,switch to new executor after the job has been done.
     * @param job
     * @param isMultiMode
     * @param isCreateMode
     * @return
     */
    Switcher switchAfterIOWork(Runnable job, Boolean isMultiMode, Boolean isCreateMode) throws SwitchRunntimeException, InterruptedException;

    /**
     * do the job on the current thread,switch to new executor after the job has been done.
     * @param job
     * @param isMultiMode
     * @param isCreateMode
     * @return
     */
    Switcher switchAfterComputeWork(Runnable job, Boolean isMultiMode, Boolean isCreateMode) throws SwitchRunntimeException, InterruptedException;

    /**
     * do the job on the current thread,switch to new executor after the job has been done.
     * @param job
     * @param isMultiMode
     * @param isCreateMode
     * @return
     */
    Switcher switchAfterWork(Runnable job, Boolean isMultiMode, Boolean isCreateMode) throws SwitchRunntimeException, InterruptedException;

    /**
     * switch to new executor,then do the job!
     * @param isMultiMode
     * @param job
     * @param isCreateMode
     * @return
     */
    Switcher switchBeforeIoWork(Runnable job, Boolean isMultiMode, Boolean isCreateMode) throws InterruptedException;

    /**
     * switch to new executor,then do the job!
     * @param isMultiMode
     * @param job
     * @param isCreateMode
     * @return
     */
    Switcher switchBeforeComputeWork(Runnable job, Boolean isMultiMode, Boolean isCreateMode) throws InterruptedException;

    /**
     * switch to new executor,then do the job!
     * @param isMultiMode
     * @param job
     * @param isCreateMode
     * @return
     */
    Switcher switchBeforeWork(Runnable job, Boolean isMultiMode, Boolean isCreateMode) throws InterruptedException;

    /**
     * trans.
     * @return
     */
    RichnessSwitcher transToRichnessSwitcher();

}
