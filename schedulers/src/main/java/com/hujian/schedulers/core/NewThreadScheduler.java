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

/**
 * Created by hujian06 on 2017/8/29.
 */

import java.util.concurrent.ThreadFactory;

/**
 * Schedules work on a new thread.
 */
public final class NewThreadScheduler extends Scheduler {

    private final ThreadFactory threadFactory;

    private static final String THREAD_NAME_PREFIX = "SwitcherNewThreadScheduler";
    private static final SwitcherThreadFactory THREAD_FACTORY;

    /** The name of the system property for setting the thread priority for this Scheduler. */
    private static final String KEY_NEWTHREAD_PRIORITY = "switcher.newthread-priority";

    static {
        int priority = Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY,
                Integer.getInteger(KEY_NEWTHREAD_PRIORITY, Thread.NORM_PRIORITY)));

        THREAD_FACTORY = new SwitcherThreadFactory(THREAD_NAME_PREFIX, priority);
    }

    public NewThreadScheduler() {
        this(THREAD_FACTORY);
    }

    public NewThreadScheduler(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @Override
    public Worker createWorker() {
        return new NewThreadWorker(threadFactory);
    }
}
