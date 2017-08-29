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

import java.util.concurrent.Callable;

/**
 * A Callable to be submitted to an ExecutorService that runs a Runnable
 * action and manages completion/cancellation.
 */
public final class ScheduledDirectTask extends AbstractDirectTask implements Callable<Void> {

    public ScheduledDirectTask(Runnable runnable) {
        super(runnable);
    }

    @Override
    public Void call() throws Exception {
        runner = Thread.currentThread();
        try {
            runnable.run();
        } finally {
            lazySet(FINISHED);
            runner = null;
        }
        return null;
    }
}
