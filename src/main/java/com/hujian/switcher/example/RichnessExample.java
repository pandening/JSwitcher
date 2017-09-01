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

package com.hujian.switcher.example;

import com.hujian.switcher.AbstractSwitcherRunner;
import com.hujian.switcher.SwitcherResultfulEntry;
import com.hujian.switcher.core.SwitchRunntimeException;
import com.hujian.switcher.utils.SwitcherFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hujian06 on 2017/8/20.
 */
public class RichnessExample {


    public static void main(String ... args)
            throws InterruptedException, SwitchRunntimeException, ExecutionException {

        try {
            SwitcherFactory.createResultfulSwitcher()
                    .switchToExecutor(executorService, "Funy-Executor")
                    .apply(stupidWorker, false)
                    .switchToComputeExecutor(true)
                    .apply(stupidWorker, false)
                    .transToRichnessSwitcher()
                    .switchTo("Funy-Executor", false, false, null)
                    .apply(stupidWorker, false)
                    .switchToComputeExecutor(true)
                    .switchToMultiIoExecutor(true)
                    .transToRichnessSwitcher()
                    .transToResultfulSwitcher()
                    .syncApply(stupidRunner, syncResultfulEntry)
                    .switchToMultiComputeExecutor(true)
                    .transToRichnessSwitcher()
                    .transToResultfulSwitcher()
                    .asyncApply(stupidRunner, asyncResultfulEntry);

            SwitcherFactory.shutdown();

            String syncData = syncResultfulEntry.getResultfulData();
            String asyncData = asyncResultfulEntry.getResultfulData();

            System.out.println("sync Result:" + syncData + "\nasync Result:" + asyncData);
        } catch (Exception e) {
            ScheduleHooks.onError(e);
            SwitcherFactory.shutdown();
        }

    }

    private static ExecutorService executorService = Executors.newFixedThreadPool(1);
    private static StupidRunner stupidRunner = new StupidRunner();
    private static StupidWorker stupidWorker = new StupidWorker();
    private static SwitcherResultfulEntry<String> asyncResultfulEntry = SwitcherResultfulEntry.emptyEntry();
    private static SwitcherResultfulEntry<String> syncResultfulEntry = SwitcherResultfulEntry.emptyEntry();

    private static class StupidWorker implements Runnable {
        @Override
        public void run() {
            System.out.println("i am in:" + Thread.currentThread().getName());
        }
    }

    private static class StupidRunner extends AbstractSwitcherRunner<String> {

        @Override
        protected String run() {
            return "funny + [" + Thread.currentThread().getName() + "]";
        }

        @Override
        protected String fallback() {
            return "fallback + [" + Thread.currentThread().getName() + "]";
        }
    }

}
