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

package com.hujian.switcher.utils;

import com.hujian.switcher.SwitchExecutorServiceEntry;

import java.util.Iterator;
import java.util.concurrent.BlockingDeque;

/**
 * Created by hujian06 on 2017/8/19.
 */
public final class DebugHelper {

    /**
     * check before search an executor
     * @param expectExecutorType
     * @param deque
     */
    public static synchronized void trackExecutorQueue(String expectExecutorType, BlockingDeque<SwitchExecutorServiceEntry> deque) {
        if (deque == null || deque.isEmpty()) {
            System.out.println("trackExecutorQueue:null or empty deque");
        } else {
            System.out.println("expectExecutorType:" + expectExecutorType + " Current ExecutorService " +
                    "size:" + deque.size());
            Iterator iterator = deque.iterator();
            while (iterator.hasNext()) {
                SwitchExecutorServiceEntry switchExecutorServiceEntry = (SwitchExecutorServiceEntry) iterator.next();
                System.out.println("deque element=>" + switchExecutorServiceEntry.getExecutorType());
            }
        }
    }

}
