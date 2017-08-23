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

import com.google.common.base.Preconditions;
import com.hujian.switcher.ResultfulSwitcher;
import com.hujian.switcher.SwitchExecutorServiceEntry;
import com.hujian.switcher.core.ExecutorType;
import com.hujian.switcher.statistic.SampleSwitcherStatistic;
import com.hujian.switcher.statistic.Statistic;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by hujian06 on 2017/8/22.
 */
@SuppressWarnings(value = "unchecked")
public class SampleSwitcherObservable<T> extends ResultfulSwitcher implements SwitcherObservable<T> {
    private static final Logger LOGGER = Logger.getLogger(SampleSwitcherObservable.class);

    private List<SwitcherObserver<T>> subscribeOnList = null; //multi-subscribe observer
    private Map<SwitcherObserver<T>, SwitcherObserverInformation> switcherObserverInformationMap = null;

    private SwitcherFlowableBuffer<T> switcherFlowableBuffer; // the buffer

    private SwitcherObservableOnSubscribe<T> switcherObservableOnSubscribe = null;

    private Statistic statistic = SampleSwitcherStatistic.getInstance(); // the statistic
    private static final String DEFAULT_SESSION_ID = "flowable-switcher-statistic";
    private final String TAG = this.getClass().getName();

    private final SwitcherLifeCycleRunner switcherLifeCycleRunner = new SwitcherLifeCycleRunner(); // the runner

    public SampleSwitcherObservable() {
        subscribeOnList = new ArrayList<>();
        switcherObserverInformationMap = new ConcurrentHashMap<>();
    }

    /**
     * create the Switcher Observable
     * @param onSubscribe the subscriber
     * @return
     */
    public SampleSwitcherObservable createSwitcherObservable(SwitcherObservableOnSubscribe<T> onSubscribe) {
        this.switcherObservableOnSubscribe = onSubscribe;
        return this;
    }

    private SwitcherDisposable createSwitcherDisposable() {
        return new SwitcherDisposable() {
            private Boolean _isDispose = false;
            private Long _req = -1L;

            @Override
            public void dispose() {
                _isDispose = true;
            }

            @Override
            public Boolean isDispose() {
                return _isDispose;
            }

            @Override
            public void request(long req) {
                this._req  = req;
            }

            @Override
            public long req() {
                return _req;
            }
        };
    }

    @Override
    public synchronized SampleSwitcherObservable subscribe(SwitcherObserver<T> switcherObserver)
            throws InterruptedException {
        this.subscribeOnList.add(switcherObserver);
        SwitcherDisposable _disposable = createSwitcherDisposable();
        this.switcherObserverInformationMap.put(switcherObserver,
                new SwitcherObserverInformation(_disposable));
        switcherObserver.control(_disposable);
        actualSubscribe();

        return this;
    }

    @Override
    public synchronized SampleSwitcherObservable subscribe(List<SwitcherObserver<T>> switcherObserverList)
            throws InterruptedException {
        this.subscribeOnList.addAll(switcherObserverList);
        for (SwitcherObserver<T> observable : switcherObserverList) {
            SwitcherDisposable _disposable = createSwitcherDisposable();
            this.switcherObserverInformationMap.put(observable,
                    new SwitcherObserverInformation(_disposable));

            observable.control(_disposable);
        }

        actualSubscribe();

        return this;
    }

    @Override
    public synchronized SampleSwitcherObservable
    delaySubscribe(SwitcherObserver<T> switcherObserver, TimeUnit timeUnit, long time) throws InterruptedException {
        Preconditions.checkArgument(timeUnit != null, "timeUnit must !null");
        this.subscribeOnList.add(switcherObserver);
        SwitcherDisposable _disposable = createSwitcherDisposable();
        this.switcherObserverInformationMap.put(switcherObserver,
                new SwitcherObserverInformation(_disposable, true, timeUnit, time));
        switcherObserver.control(_disposable);
        actualSubscribe();

        return this;
    }

    private void doLifeCycle() {
        if (this.subscribeOnList != null && !this.subscribeOnList.isEmpty()) {
            for (SwitcherObserver<T> observer : this.subscribeOnList) {
                synchronized (SampleSwitcherObservable.class) {
                    SwitcherObserverInformation information = this.switcherObserverInformationMap.get(observer);
                    if (observer == null) {
                        LOGGER.error(observer + " has no SwitcherObserverInformation. reject to run..");
                        continue;
                    }
                    SwitcherDisposable disposable = information.getDisposable();
                    if (disposable.isDispose()) {
                        observer.complete(); // complete | error
                        LOGGER.info(observer + " had disposed!");
                        continue;
                    }

                    //start
                    observer.start();

                    long req = disposable.req();
                    long send = information.getSendCount().get();

                    while (send < req && !information.getError() && !information.getComplete()) {
                        send = information.getSendCount().incrementAndGet();
                        boolean isDelay = information.getDelay();
                        if (isDelay) {
                            TimeUnit unit = information.getTimeUnit();
                            long time = information.getDelayTime();
                            ExecutorService currentExecutorService
                                    = getCurrentExecutorService().getExecutorService();
                            switch (unit) {
                                case NANOSECONDS:
                                    try {
                                        TimeUnit.NANOSECONDS.sleep(time);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case MICROSECONDS:
                                    try {
                                        TimeUnit.MICROSECONDS.sleep(time);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case MILLISECONDS:
                                    try {
                                        TimeUnit.MILLISECONDS.sleep(time);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case SECONDS:
                                    try {
                                        TimeUnit.SECONDS.sleep(time);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case MINUTES:
                                    try {
                                        TimeUnit.MINUTES.sleep(time);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case HOURS:
                                    try {
                                        TimeUnit.HOURS.sleep(time);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case DAYS:
                                    try {
                                        TimeUnit.DAYS.sleep(time);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                            }
                            Exception exception = null;
                            try {
                                switchToExecutor(currentExecutorService);
                                switcherObservableOnSubscribe.subscribe(observer);
                            } catch (InterruptedException e) {
                                LOGGER.error("oops,switchToExecutor Error");
                                exception = e;
                                e.printStackTrace();
                            } finally {
                                if (exception != null) {
                                    observer.errors(new SwitcherFlowException(exception.getCause()));
                                    information.setError(true);
                                }
                            }
                        } else {
                            this.switcherObservableOnSubscribe.subscribe(observer);
                        }
                    }
                    information.setComplete(true);
                    send = information.getSendCount().get();
                    if (-1 == req) {
                        this.switcherObservableOnSubscribe.subscribe(observer);
                        disposable.dispose();
                    }
                }
            }
        }
    }

    private void actualSubscribe() throws InterruptedException {
        SwitchExecutorServiceEntry executorServiceEntry = getCurrentExecutorService();
        if (executorServiceEntry == null ||
                executorServiceEntry.getExecutorType().equals(ExecutorType.EMPTY_EXECUTOR_SERVICE.getName())) {
            //switch to new executorService
            switchToNewExecutor();
        }

        ExecutorService executorService = getCurrentExecutorService().getExecutorService();

        //is shutdown ?
        if (executorService.isShutdown()) {
            executorService = createExecutorService(ExecutorType.NEW_EXECUTOR_SERVICE.getName());
            switchToExecutor(executorService);
        }

        //run the job on the executorService
        executorService.submit(switcherLifeCycleRunner);
    }

    /**
     * run at the current executorService
     */
    private class SwitcherLifeCycleRunner implements Runnable {
        @Override
        public void run() {
            Exception exception = null;
            long startTime = System.currentTimeMillis();
            try {
                doLifeCycle();
            } catch (Exception e) {
                LOGGER.warn("exception while doLifeCycle:" + e);
                exception = e;
            } finally {
                long costTime = System.currentTimeMillis() - startTime;
                if (null == exception) {
                     //success
                    statistic.incSucCount(DEFAULT_SESSION_ID, TAG, "");
                    statistic.setSucTime(DEFAULT_SESSION_ID, TAG,"",costTime);
                } else {
                    //error
                    statistic.setErrTime(DEFAULT_SESSION_ID, TAG, "", costTime);
                    statistic.incErrCount(DEFAULT_SESSION_ID, TAG, "", exception);
                }
            }
        }
    }

    private class SwitcherObserverInformation {
        private SwitcherDisposable disposable = null;
        private Boolean isDelay = false;
        private TimeUnit timeUnit = null;
        private Long delayTime = null;
        private AtomicLong sendCount = null;
        private Boolean isComplete = false;
        private Boolean isError = false;

        public SwitcherObserverInformation(SwitcherDisposable disposable, boolean _isDelay,
                                           TimeUnit unit, long _delayTime) {
            Preconditions.checkArgument(disposable != null,
                    "disposable is null");
            this.disposable = disposable;

            if (_isDelay) {
                this.isDelay = _isDelay;
                this.timeUnit = unit;
                this.delayTime = _delayTime;
            }

            this.sendCount = new AtomicLong(0);
        }

        public SwitcherObserverInformation(SwitcherDisposable disposable) {
            this(disposable, false, null, 0);
        }

        public SwitcherDisposable getDisposable() {
            return disposable;
        }

        public void setDisposable(SwitcherDisposable disposable) {
            this.disposable = disposable;
        }

        public Boolean getDelay() {
            return isDelay;
        }

        public void setDelay(Boolean delay) {
            isDelay = delay;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        public void setTimeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
        }

        public Long getDelayTime() {
            return delayTime;
        }

        public void setDelayTime(Long delayTime) {
            this.delayTime = delayTime;
        }

        public AtomicLong getSendCount() {
            return sendCount;
        }

        public Boolean getComplete() {
            return isComplete;
        }

        public void setComplete(Boolean complete) {
            isComplete = complete;
        }

        public Boolean getError() {
            return isError;
        }

        public void setError(Boolean error) {
            isError = error;
        }
    }

}
