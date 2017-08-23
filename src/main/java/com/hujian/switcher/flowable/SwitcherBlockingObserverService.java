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

import org.apache.log4j.Logger;

/**
 * Created by hujian06 on 2017/8/23.
 */
@SuppressWarnings(value = "unchecked")
public abstract class SwitcherBlockingObserverService<T> implements SwitcherBlockingObserver<T> {
    private static final Logger LOGGER = Logger.getLogger(SwitcherBlockingObserverService.class);

    private static final Class ClassToken = SwitcherProducerClassToken.class; // the token

    private SampleSwitcherObservable.SwitcherObserverInformation _information = null;

    private SwitcherBuffer<T> _buffer = null;

    @Override
    public void control(SampleSwitcherObservable.SwitcherObserverInformation information) {
        ctrl(information);
        this._information = information;
        this._buffer = information.getBuffer();
    }

    @Override
    public void start() {
        onStart();
    }

    @Override
    public void emit(T data) throws InterruptedException {
    }

    @Override
    public void errors(SwitcherFlowException e) {
        onError(e);
    }

    @Override
    public void complete() {
        onComplete();
    }

    protected abstract void ctrl(SampleSwitcherObservable.SwitcherObserverInformation information);
    protected abstract void onStart();
    protected abstract void onEmit(SwitcherBuffer<T> buffer) throws InterruptedException;
    protected abstract void onError(SwitcherFlowException e);
    protected abstract void onComplete();

    /**
     * the observable write to observer
     * @param data the data
     * @throws InterruptedException e
     * @throws InstantiationException e
     * @throws SwitcherClassTokenErrException e
     * @throws IllegalAccessException e
     */
    public void send(T data) throws InterruptedException, InstantiationException,
            SwitcherClassTokenErrException, IllegalAccessException {
        _buffer.put(data, ClassToken);
        long sentCount = _information.getSendCount().get();
        if (sentCount < _information.getDisposable().req()) {
            _information.getSendCount().incrementAndGet();
            onEmit(_buffer);
        } else {
            onComplete();
        }
    }
}
