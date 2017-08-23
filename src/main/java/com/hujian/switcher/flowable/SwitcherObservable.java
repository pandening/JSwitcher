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

package com.hujian.switcher.flowable;

import com.hujian.switcher.ResultfulSwitcherIfac;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by hujian06 on 2017/8/22.
 */
public interface SwitcherObservable<T> extends ResultfulSwitcherIfac{

    /**
     * @param switcherObserver subscribe on this observable
     */
    SampleSwitcherObservable subscribe(SwitcherObserver<T> switcherObserver) throws InterruptedException;

    /**
     * @param switcherObserverList subscribe on this observable
     */
    SampleSwitcherObservable subscribe(List<SwitcherObserver<T>> switcherObserverList) throws InterruptedException;

    /**
     * @param switcherObserver  subscribe on this observable
     * @param timeUnit  time unit
     * @param time  delay time {unit}
     */
    SampleSwitcherObservable delaySubscribe(SwitcherObserver<T> switcherObserver, TimeUnit timeUnit, long time) throws InterruptedException;

}