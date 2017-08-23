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

import com.hujian.switcher.flowable.SwitcherDisposable;
import com.hujian.switcher.flowable.SwitcherFlowException;
import com.hujian.switcher.flowable.SwitcherObservableOnSubscribe;
import com.hujian.switcher.flowable.SwitcherObserver;
import com.hujian.switcher.utils.SwitcherFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by hujian06 on 2017/8/23.
 */
@SuppressWarnings(value = "unchecked")
public class SampleFlowableDemo {
    public static void main(String ... args) throws InterruptedException {
        try {
            SwitcherFactory
                    .createSwitcherObservable()
                    .switchToComputeExecutor(true)
                    .transToRichnessSwitcher()
                    .transToResultfulSwitcher()
                    .transToSampleSwitcherObservable()
                    .createSwitcherObservable((SwitcherObservableOnSubscribe) observer -> observer.emit("hujian"))
                    .delaySubscribe(new SwitcherObserver() {
                        @Override
                        public void control(SwitcherDisposable disposable) {
                            //disposable.request(10); //default is -1
                        }

                        @Override
                        public void start() {

                        }

                        @Override
                        public void emit(Object data) {
                            System.out.println("recv:" + data);
                        }

                        @Override
                        public void emit(List dataList) {

                        }

                        @Override
                        public void errors(SwitcherFlowException e) {

                        }

                        @Override
                        public void complete() {

                        }
                    }, TimeUnit.SECONDS, 2);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SwitcherFactory.shutdown();
        }
    }

}
