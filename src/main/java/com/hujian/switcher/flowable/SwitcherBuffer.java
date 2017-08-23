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

package com.hujian.switcher.flowable;

/**
 * Created by hujian06 on 2017/8/23.
 */
public interface SwitcherBuffer<T> {

    /**
     * put a data to the blockQueue, you need put data with the "class Token"
     * @param data the data
     * @param clsToken class token
     * @throws InterruptedException e
     * @throws IllegalAccessException e
     * @throws InstantiationException e
     * @throws SwitcherClassTokenErrException e
     */
    void put(T data, Class clsToken) throws InterruptedException,
            IllegalAccessException, InstantiationException, SwitcherClassTokenErrException;

    /**
     * read a data from blocking queue
     * @return the data or block until get data
     * @throws InterruptedException e
     */
    T get() throws InterruptedException;

}
