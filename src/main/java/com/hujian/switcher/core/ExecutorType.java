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

/**
 * Created by hujian06 on 2017/8/18.
 */
public enum ExecutorType {
    IO_EXECUTOR_SERVICE(1, "io-executorService"),
    MULTI_IO_EXECUTOR_SERVICE(2, "multi-io-executorService"),
    COMPUTE_EXECUTOR_SERVICE(3, "compute-executorService"),
    MULTI_COMPUTE_EXECUTOR_SERVICE(4, "multi-compute-executorService"),
    SINGLE_EXECUTOR_SERVICE(5, "single-executorService"),
    NEW_EXECUTOR_SERVICE(6, "new-executorService"),
    EMPTY_EXECUTOR_SERVICE(7, "empty-executorService"),
    CUSTOM_EXECUTOR_SERVICE(8, "custom-executorService"),
    DEFAULT_RUN_EXECUTOR_SERVICE(8, "default-executorService");

    private int index;
    private String name;

    ExecutorType(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public static String getName(int index) {
        for (ExecutorType c : ExecutorType.values()) {
            if (c.getIndex() == index) {
                return c.name;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
