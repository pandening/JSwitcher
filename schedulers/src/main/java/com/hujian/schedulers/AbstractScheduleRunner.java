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

package com.hujian.schedulers;

import com.google.common.base.Preconditions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by hujian06 on 2017/8/29.
 *
 * The Abstract Schedule Runner, you should implement
 * {@link AbstractScheduleRunner#realRun()}
 * and
 * {@link AbstractScheduleRunner#fallback(Exception)}
 *
 */
public abstract class AbstractScheduleRunner<T> implements ScheduleRunner<T>{

    private Executor _executor; // the executor

    @Override
    public CompletableFuture<T> queue() {
        Preconditions.checkArgument(_executor != null,
                "Executor is null");
        try {
            CompletableFuture<T> future = (CompletableFuture.supplyAsync(() -> {
                try {
                    return realRun();
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }, _executor));
            return future;
        } catch (RejectedExecutionException e) {
            try {
                return CompletableFuture.completedFuture(fallback(e));
            } catch (Exception ep) {
                CompletableFuture<T> result = new CompletableFuture<>();
                result.completeExceptionally(ep);
                return result;
            }
        }
    }

    @Override
    public T execute() throws Exception {
        try {
            return queue().get();
        } catch (Exception e) {
            //todo : more elaborate solution with exception 'e'
            return fallback(e);
        }
    }

    //**********************************//
    //       actual work here           //
    //**********************************//

    protected abstract T realRun();
    protected abstract T fallback(Exception e);

    /**
     * the method you can assign the executor.
     * @param _executor the executor.
     */
    public void set_executorService(Executor _executor) {
        this._executor = _executor;
    }

    /**
     * the method you can assign the executor.
     * @param executor
     */
    @Override
    public void setExecutor(Executor executor) {
        this._executor = executor;
    }

}
