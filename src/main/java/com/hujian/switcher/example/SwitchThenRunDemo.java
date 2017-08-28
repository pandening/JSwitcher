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

import com.hujian.switcher.annotation.SwitchThenRun;
import com.hujian.switcher.annotation.SwitchThenRunResolver;
import com.hujian.switcher.core.SwitchRunntimeException;
import com.hujian.switcher.utils.SwitcherFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by hujian06 on 2017/8/21.
 */
public class SwitchThenRunDemo {
    private static final String IO_EXECUTOR_NAME = "io-executorService";
    private static final String MULTI_IO_EXECUTOR_SERVICE = "multi-io-executorService";
    private static final String COMPUTE_EXECUTOR_SERVICE = "compute-executorService";
    private static final String MULTI_COMPUTE_EXECUTOR_SERVICE = "multi-compute-executorService";
    private static final String SINGLE_EXECUTOR_SERVICE = "single-executorService";
    private static final String NEW_EXECUTOR_SERVICE = "new-executorService";


    @SwitchThenRun(switchToExecutorServiceType = MULTI_COMPUTE_EXECUTOR_SERVICE)
    public static AnnotationServcie.StupidRunner stupidRunnerA;

    @SwitchThenRun(switchToExecutorServiceName = "stupid-executorService")
    public static AnnotationServcie.StupidRunner stupidRunnerB;

    @SwitchThenRun(CreateType = MULTI_IO_EXECUTOR_SERVICE)
    public static AnnotationServcie.StupidRunner stupidRunnerC;

    public static class StupidRunner implements Runnable {

        @Override
        public void run() {
            System.out.println("i am stupid runner at :" + Thread.currentThread().getName());
        }
    }

    /**
     * @param args
     */
    public static void main(String ... args) throws InterruptedException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException, SwitchRunntimeException {
        SwitcherFactory
                .createResultfulSwitcher()
                .transToRichnessSwitcher()
                .transToResultfulSwitcher()
                .switchTo("stupidExecutorService",
                        true, true, MULTI_IO_EXECUTOR_SERVICE);

        System.out.println("currentExecutorService =>" +
                SwitcherFactory.getCurResultfulSwitcherIfac().getCurrentExecutorService().getExecutorType() +
                "@" + SwitcherFactory.getCurResultfulSwitcherIfac().getCurrentExecutorService().getExecutorName()
        );

        SwitchThenRunResolver resolver = new SwitchThenRunResolver();
        resolver.execute();

        SwitcherFactory.shutdown();

    }
}
