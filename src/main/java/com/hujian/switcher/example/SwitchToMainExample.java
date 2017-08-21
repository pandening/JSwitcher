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

package com.hujian.switcher.example;

import com.hujian.switcher.core.SwitchRunntimeException;
import com.hujian.switcher.utils.SwitcherFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by hujian06 on 2017/8/21.
 */
public class SwitchToMainExample {

    public static void main(String ... args) throws InterruptedException, SwitchRunntimeException {

        SwitcherFactory.createResultfulSwitcher()
                .switchToMultiComputeExecutor(true)
                .apply(stupidJob, false)
                .switchToMain()
                .apply(stupidJob, false)
                .switchToComputeExecutor(true)
                .apply(stupidJob, false);

        SwitcherFactory.shutdown();

    }


    private static StupidJob stupidJob = new StupidJob();

    private static class StupidJob implements Runnable {

        @Override
        public void run() {
            if (Thread.currentThread().getName().equals("main")) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("i am in:" + Thread.currentThread().getName());
        }
    }

}
