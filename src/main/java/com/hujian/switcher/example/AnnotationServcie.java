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

import com.hujian.switcher.annotation.Switcher;
import com.hujian.switcher.annotation.SwitcherAnnotationResolver;
import com.hujian.switcher.core.SwitchRunntimeException;
import com.hujian.switcher.utils.SwitcherFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by hujian06 on 2017/8/21.
 */
public class AnnotationServcie {

    private static final String IO_EXECUTOR_NAME = "io-executorService";
    private static final String MULTI_IO_EXECUTOR_SERVICE = "multi-io-executorService";
    private static final String COMPUTE_EXECUTOR_SERVICE = "compute-executorService";
    private static final String MULTI_COMPUTE_EXECUTOR_SERVICE = "multi-compute-executorService";
    private static final String SINGLE_EXECUTOR_SERVICE = "single-executorService";
    private static final String NEW_EXECUTOR_SERVICE = "new-executorService";

    @Switcher(switchToExecutorServiceType = IO_EXECUTOR_NAME)
    public static StupidRunner stupidRunnerA;

    @Switcher(switchToExecutorServiceName = "stupidExecutorService")
    public static StupidRunner stupidRunnerB;

    @Switcher(CreateType = MULTI_COMPUTE_EXECUTOR_SERVICE)
    public static StupidRunner stupidRunnerC;

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
        SwitcherAnnotationResolver resolver = new SwitcherAnnotationResolver();
        SwitcherFactory
                .createResultfulSwitcher()
                .transToRichnessSwitcher()
                .transToResultfulSwitcher()
                .switchTo("stupidExecutorService",
                        true, true, MULTI_IO_EXECUTOR_SERVICE)
                .apply(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("check,i am in:" + Thread.currentThread().getName());
                    }
                }, false);

        System.out.println("currentExecutorService =>" +
                SwitcherFactory.getCurResultfulSwitcherIfac().getCurrentExecutorService().getExecutorType() +
                "@" + SwitcherFactory.getCurResultfulSwitcherIfac().getCurrentExecutorService().getExecutorName()
        );

        resolver.execute();

        SwitcherFactory.shutdown();

    }

}
