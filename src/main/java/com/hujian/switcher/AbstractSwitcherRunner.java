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
import com.hujian.switcher.core.SwitchRunntimeException;
import org.apache.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by hujian06 on 2017/8/20.
 */
public abstract class AbstractSwitcherRunner<T> implements SwitcherRunner<T> {
    private static final Logger LOGGER = Logger.getLogger(AbstractSwitcherRunner.class);

    private ExecutorService _executorService;

    @Override
    public CompletableFuture<T> queue() {
        Preconditions.checkArgument(_executorService != null,
                "ExecutorService is null");

        try {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return doRun();
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }, _executorService);
        } catch (RejectedExecutionException e) {
            LOGGER.warn("Rejected job error:" + e);
            try {
                return CompletableFuture.completedFuture(doFallback(e));
            } catch (Exception error) {
                CompletableFuture<T> result = new CompletableFuture<>();
                result.completeExceptionally(error);
                return result;
            }
        }
    }

    @Override
    public T execute() throws Exception {
        try {
            return queue().get();
        } catch (Exception e) {
            LOGGER.error("execute error:" + e);
            return doFallback(e);
        }
    }

    /**
     * do the actual work
     * @return
     */
    private T doRun() throws Exception {
        try {
            return run();
        } catch (Exception e) {
            return doFallback(e);
        }
    }

    /**
     * do the fall back work
     * @param e
     * @return
     */
    private T doFallback(Exception e) throws Exception {
        if (e instanceof SwitchRunntimeException) {
            return fallback();
        } else {
            LOGGER.error("Fallback error:" + e);
            throw  e;
        }
    }

    protected abstract T run();
    protected abstract T fallback();

    /**
     * you can set the executor.or the switcher will use the current switcher to run the job.
     *
     * @param executorService
     * @return
     */
    @Override
    public void setExecutorService(ExecutorService executorService) throws SwitchRunntimeException {
        this._executorService = executorService;
        if (_executorService == null) { //set the default executorService
            LOGGER.error("null executorService,no ExecutorService to run job.");
            throw new SwitchRunntimeException("Switcher RunningTime Error.");
        }
    }
}
