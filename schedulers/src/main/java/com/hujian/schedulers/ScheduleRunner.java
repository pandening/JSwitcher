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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Created by hujian06 on 2017/8/29.
 * The origin runner
 */
public interface ScheduleRunner<T> {

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
     * set the executor to run the job
     * @param executor
     */
    void setExecutor(Executor executor);

}
