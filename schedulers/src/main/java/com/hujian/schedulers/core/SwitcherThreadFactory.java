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

package com.hujian.schedulers.core;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by hujian06 on 2017/8/29.
 */
/**
 * A ThreadFactory that counts how many threads have been created and given a prefix,
 * sets the created Thread's name to {@code prefix-count}.
 */
public final class SwitcherThreadFactory extends AtomicLong implements ThreadFactory {

    private final String prefix;

    private final int priority;

    private final boolean nonBlocking;

    public SwitcherThreadFactory(String prefix) {
        this(prefix, Thread.NORM_PRIORITY, false);
    }

    public SwitcherThreadFactory(String prefix, int priority) {
        this(prefix, priority, false);
    }

    public SwitcherThreadFactory(String prefix, int priority, boolean nonBlocking) {
        this.prefix = prefix;
        this.priority = priority;
        this.nonBlocking = nonBlocking;
    }

    @Override
    public Thread newThread(Runnable r) {
        StringBuilder nameBuilder =
                new StringBuilder(prefix).append('-').append(incrementAndGet());

        String name = nameBuilder.toString();
        Thread t = nonBlocking ? new RxCustomThread(r, name) : new Thread(r, name);
        t.setPriority(priority);
        t.setDaemon(true);
        return t;
    }

    @Override
    public String toString() {
        return "SwitcherThreadFactory[" + prefix + "]";
    }

    static final class RxCustomThread extends Thread implements NonBlockingThread {
        RxCustomThread(Runnable run, String name) {
            super(run, name);
        }
    }
}