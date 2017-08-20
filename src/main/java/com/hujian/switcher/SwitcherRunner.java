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

import com.hujian.switcher.core.SwitchRunntimeException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Created by hujian06 on 2017/8/20.
 */
public interface SwitcherRunner<T> {

    /**
     * async call
     * @return
     */
    CompletableFuture<T> queue();

    /**
     * sync call
     * @return
     */
    T execute() throws Exception;

    /**
     * you can set the executor.or the switcher will use the current switcher to run the job.
     * also,you can not call this function,the switcher will call this function auto-ly.
     * @param executorService
     */
    void setExecutorService(ExecutorService executorService) throws SwitchRunntimeException;

}
