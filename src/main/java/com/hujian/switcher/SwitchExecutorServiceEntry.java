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

package com.hujian.switcher;

import com.hujian.switcher.core.ExecutorType;

import java.util.concurrent.ExecutorService;

/**
 * Created by hujian06 on 2017/8/18.
 */
public class SwitchExecutorServiceEntry {

    private static final String DEFAULT_EXECUTOR_NAME = "unNamed-executorService";

    private String executorType;
    private String executorName;
    private ExecutorService executorService;

    private volatile boolean isAssignedName = false;

    /**
     * get an empty switch executor Service entry
     * @return
     */
    public static SwitchExecutorServiceEntry emptyEntry() {
        return new SwitchExecutorServiceEntry();
    }

    /**
     * empty (default) constructor
     */
    public SwitchExecutorServiceEntry() {
        this.executorType = ExecutorType.EMPTY_EXECUTOR_SERVICE.getName();
        this.executorName = DEFAULT_EXECUTOR_NAME;
        this.executorService = null;
    }

    public SwitchExecutorServiceEntry(String executorType, ExecutorService executorService) {
        this.executorType = executorType;
        this.executorService = executorService;
        this.executorName = DEFAULT_EXECUTOR_NAME;
    }

    public SwitchExecutorServiceEntry(String executorType, ExecutorService executorService, String executorName) {
        this.executorType = executorType;
        this.executorName = executorName;
        this.executorService = executorService;
        this.isAssignedName = true;
    }

    public String getExecutorType() {
        return executorType;
    }

    public void setExecutorType(String executorType) {
        this.executorType = executorType;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        if (!isAssignedName) {
            this.executorName = executorName;
            this.isAssignedName = true;
        }
    }

    public boolean isAssignedName() {
        return isAssignedName;
    }
}
