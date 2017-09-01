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

package com.hujian.switcher.schedulers.core;

/**
 * Created by hujian06 on 2017/8/29.
 */

import com.hujian.switcher.ScheduleHooks;

/**
 * A Callable to be submitted to an ExecutorService that runs a Runnable
 * action periodically and manages completion/cancellation.
 */
public final class ScheduledDirectPeriodicTask extends AbstractDirectTask implements Runnable {

    public ScheduledDirectPeriodicTask(Runnable runnable) {
        super(runnable);
    }

    @Override
    public void run() {
        runner = Thread.currentThread();
        try {
            try {
                runnable.run();
            } catch (Throwable ex) {
                lazySet(FINISHED);
                ScheduleHooks.onError(ex);
            }
        } finally {
            runner = null;
        }
    }
}
