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
import com.hujian.switcher.core.ExecutorType;
import com.hujian.switcher.core.SwitchExecutorService;
import org.apache.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by hujian06 on 2017/8/20.
 */
public class ResultfulSwitcher<T> extends RichnessSwitcher implements ResultfulSwitcherIfac {
    private static final Logger LOGGER = Logger.getLogger(ResultfulSwitcher.class);

    private void setExecutorService(SwitcherRunner runner) throws InterruptedException {
        //set the executorService
        ExecutorService curExecutorService = getCurrentExecutorService().getExecutorService();

        try {
            curExecutorService.submit(() -> {
                //TODO just test if this executor is at "Rejected" status (had been shutdown)
            });
            runner.setExecutorService(curExecutorService);
        } catch (RejectedExecutionException e) {
            LOGGER.warn("oops,the current executorService error:" + e);
            runner.setExecutorService(null);
            curExecutorService = SwitchExecutorService.defaultRunExecutorService;
            //switch the executor
            switchExecutorService(ExecutorType.DEFAULT_RUN_EXECUTOR_SERVICE.getName(), curExecutorService);
        }
    }

    @SuppressWarnings(value = "unchecked")
    @Override
    public ResultfulSwitcherIfac syncApply(SwitcherRunner runner, SwitcherResultfulEntry resultfulEntry) throws InterruptedException {
        Preconditions.checkArgument(runner != null, "runner is null");

        //runner.setExecutorService(getCurrentExecutorService().getExecutorService());
        setExecutorService(runner);

        T result = null;
        try {
            result = (T) runner.execute();
        } catch (Exception e) {
            LOGGER.error("can not execute the job,runner is:" + runner);
            e.printStackTrace();
        }

        if (null == resultfulEntry) {
            resultfulEntry = new SwitcherResultfulEntry(result);
        } else {
            resultfulEntry.setRawData(result);
        }

        //double check
        resultfulEntry.setRawData(result);

        return this;
    }

    @SuppressWarnings(value = "unchecked")
    @Override
    public ResultfulSwitcherIfac asyncApply(SwitcherRunner runner, SwitcherResultfulEntry resultfulEntry) throws InterruptedException {
        Preconditions.checkArgument(runner != null, "runner is null");
        //set the executorService
        //runner.setExecutorService(getCurrentExecutorService().getExecutorService());

        setExecutorService(runner);

        CompletableFuture<T> result = null;
        try {
            result = runner.queue();
        } catch (Exception e) {
            LOGGER.error("can not execute the job,runner is:" + runner);
            e.printStackTrace();
        }

        if (null == resultfulEntry) {
            resultfulEntry = new SwitcherResultfulEntry(result);
        } else {
            resultfulEntry.setCompletableFutureData(result);
        }

        //double-check
        resultfulEntry.setCompletableFutureData(result);

        return this;
    }

}