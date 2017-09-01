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

    private List<SwitcherObservableService<T>> subscribeOnList = null; //multi-subscribe observer
    private Map<SwitcherObservableService<T>, SwitcherObserverInformation> switcherObserverInformationMap = null;

    private List<SwitcherConsumer<T>> switcherConsumerList = null;

    private List<SwitcherBlockingObserverService<T>> switcherBlockingObserverList = null;
    private Map<SwitcherBlockingObserverService<T>, SwitcherObserverInformation> blockSwitcherObserverInformationMap = null;

    private SwitcherObservableOnSubscribe<T> switcherObservableOnSubscribe = null;
    private SwitcherSampleObservableOnSubscribe<T> switcherConsumerOnSubscribe = null;
    private SwitcherBlockingObservableOnSubscribe<T> switcherBlockingObservableOnSubscribe = null;

    private Statistic statistic = SampleSwitcherStatistic.getInstance(); // the statistic
    private static final String DEFAULT_SESSION_ID = "flowable-switcher-statistic";
    private final String TAG = this.getClass().getName();

    private final SwitcherLifeCycleRunner switcherLifeCycleRunner = new SwitcherLifeCycleRunner(); // the runner
    private final SwitcherSampleLifeCycleRunner sampleLifeCycleRunner = new SwitcherSampleLifeCycleRunner(); // the sample runner
    private final SwitcherBlockingLifeCycleRunner blockingLifeCycleRunner = new SwitcherBlockingLifeCycleRunner();// the blocking runner

    public SampleSwitcherObservable() {
        subscribeOnList = new ArrayList<>();
        switcherObserverInformationMap = new ConcurrentHashMap<>();
        switcherConsumerList = new ArrayList<>();
        switcherBlockingObserverList = new ArrayList<>();
        blockSwitcherObserverInformationMap = new ConcurrentHashMap<>();
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

    /**
     * create a sample switcher Observable
     * @param onSubscribe the subscriber
     * @return
     */
    public SampleSwitcherObservable createSwitcherObservable(SwitcherSampleObservableOnSubscribe<T> onSubscribe) {
        this.switcherConsumerOnSubscribe = onSubscribe;
        return this;
    }

    /**
     * create a blocking queue observable
     * @param observableOnSubscribe subscribe
     * @return  e
     */
    public SampleSwitcherObservable createSwitcherBlockingObservable(SwitcherBlockingObservableOnSubscribe<T> observableOnSubscribe) {
        this.switcherBlockingObservableOnSubscribe = observableOnSubscribe;
        return this;
    }

    @Override
    public SampleSwitcherObservable subscribe(SwitcherConsumer<T> consumer) throws InterruptedException {
        this.switcherBlockingObservableOnSubscribe = null;
        this.switcherObservableOnSubscribe = null;

        Preconditions.checkArgument(this.switcherConsumerOnSubscribe != null
                && this.switcherObservableOnSubscribe == null
                && this.switcherBlockingObservableOnSubscribe == null, "check please!");
        this.switcherConsumerList.add(consumer);

        actualSubscribe();
        return this;
    }

    @Override
    public SampleSwitcherObservable subscribe(SwitcherBlockingObserverService<T> blockingObserver)
            throws InterruptedException {
        this.switcherConsumerOnSubscribe = null;
        this.switcherObservableOnSubscribe = null;

        Preconditions.checkArgument(this.switcherConsumerOnSubscribe == null
                && this.switcherObservableOnSubscribe == null
                && this.switcherBlockingObservableOnSubscribe != null, "check please!");
        this.switcherBlockingObserverList.add(blockingObserver);
        SwitcherDisposable _disposable = createSwitcherDisposable();
        SwitcherBuffer<T> _buffer = createBlockingBuffer();

        SwitcherObserverInformation information = new SwitcherObserverInformation(_disposable, _buffer);

        this.blockSwitcherObserverInformationMap.put(blockingObserver, information);

        blockingObserver.control(information);

        actualSubscribe();

        return this;
    }

    @Override
    public synchronized SampleSwitcherObservable subscribe(SwitcherObservableService<T> switcherObserver)
            throws InterruptedException {
        this.switcherConsumerOnSubscribe = null;
        this.switcherBlockingObservableOnSubscribe = null;

        Preconditions.checkArgument(this.switcherConsumerOnSubscribe == null
                && this.switcherObservableOnSubscribe != null
                && this.switcherBlockingObservableOnSubscribe == null, "check please!");

        this.subscribeOnList.add(switcherObserver);
        SwitcherDisposable _disposable = createSwitcherDisposable();
        SwitcherObserverInformation information = new SwitcherObserverInformation(_disposable);
        this.switcherObserverInformationMap.put(switcherObserver, information);
        switcherObserver.control(information);

        actualSubscribe();

        return this;
    }

    @Override
    public synchronized SampleSwitcherObservable subscribe(List<SwitcherObservableService<T>> switcherObserverList)
            throws InterruptedException {
        this.switcherConsumerOnSubscribe = null;
        this.switcherBlockingObservableOnSubscribe = null;

        Preconditions.checkArgument(this.switcherConsumerOnSubscribe == null
                && this.switcherObservableOnSubscribe != null
                && this.switcherBlockingObservableOnSubscribe == null, "check please!");
        this.subscribeOnList.addAll(switcherObserverList);
        for (SwitcherObservableService<T> observable : switcherObserverList) {
            SwitcherDisposable _disposable = createSwitcherDisposable();
            SwitcherObserverInformation information = new SwitcherObserverInformation(_disposable);
            this.switcherObserverInformationMap.put(observable, information);

            observable.control(information);
        }

        actualSubscribe();

        return this;
    }

    @Override
    public synchronized SampleSwitcherObservable
    delaySubscribe(SwitcherObservableService<T> switcherObserver, TimeUnit timeUnit, long time) throws InterruptedException {
        Preconditions.checkArgument(timeUnit != null, "timeUnit must !null");
        this.subscribeOnList.add(switcherObserver);
        SwitcherDisposable _disposable = createSwitcherDisposable();
        SwitcherObserverInformation information =
                new SwitcherObserverInformation(_disposable, true, timeUnit, time, null);
        this.switcherObserverInformationMap.put(switcherObserver, information);
        switcherObserver.control(information);
        actualSubscribe();

        return this;
    }

    private void doBlockingLifeCycle() throws InterruptedException, InstantiationException, SwitcherClassTokenErrException, IllegalAccessException {
        if (this.switcherBlockingObserverList != null && !this.switcherBlockingObserverList.isEmpty()) {
            for (SwitcherBlockingObserverService<T> observer : this.switcherBlockingObserverList) {
                synchronized (SampleSwitcherObservable.class) {
                    if (observer != null) {
                        SwitcherObserverInformation information
                                = this.blockSwitcherObserverInformationMap.get(observer);
                        SwitcherDisposable disposable = information.getDisposable();
                        if (!disposable.isDispose()) {
                            observer.start();
                            long req = disposable.req();
                            long send = information.getSendCount().get();
                            if (send < req && !information.getError() && !information.getComplete()) {
                                this.switcherBlockingObservableOnSubscribe.subscribe(observer);
                            }
                            //complete
                            disposable.dispose();
                            observer.complete();
                        }
                    }
                }
            }
        }
    }

    /**
     * run the sample life cycle
     */
    private void doSampleLifeCycle() {
        if (this.switcherConsumerList != null && !this.switcherConsumerList.isEmpty()) {
            for (SwitcherConsumer consumer : this.switcherConsumerList) {
                synchronized (SampleSwitcherObservable.class) {
                    if (consumer != null) {
                        this.switcherConsumerOnSubscribe.subscribe(consumer);
                    }
                }
            }
        }
    }

    private void doLifeCycle() throws InterruptedException {
        if (this.subscribeOnList != null && !this.subscribeOnList.isEmpty()) {
            for (SwitcherObservableService<T> observer : this.subscribeOnList) {
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

                    if (send < req && !information.getError() && !information.getComplete()) {
                        boolean isDelay = information.getDelay();
                        if (isDelay) {
                            TimeUnit unit = information.getTimeUnit();
                            long time = information.getDelayTime();
                            sleepHelper(unit, time); // delay
                            Exception exception = null;
                            try {
                                switcherObservableOnSubscribe.subscribe(observer);
                            } catch (Exception e) {
                                LOGGER.error("oops,switchToExecutor Error");
                                exception = e;
                                ScheduleHooks.onError(e);
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
                    if (-1 == req) {
                        this.switcherObservableOnSubscribe.subscribe(observer);
                        disposable.dispose();
                    }
                }
            }
        }
    }

    /**
     * according to the subscribe producer,do the right thing .
     * @throws InterruptedException e
     */
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

        //actual run the job here.
        if (switcherObservableOnSubscribe != null) {
            //run the job on the executorService
            executorService.submit(switcherLifeCycleRunner);
        } else if (switcherConsumerOnSubscribe != null) {
            executorService.submit(sampleLifeCycleRunner);
        } else if (switcherBlockingObservableOnSubscribe != null){
            executorService.submit(blockingLifeCycleRunner);
        }

    }

    /**
     * run at the current executorService
     */
    private class SwitcherBlockingLifeCycleRunner implements Runnable {
        @Override
        public void run() {
            Exception exception = null;
            long startTime = System.currentTimeMillis();
            try {
                doBlockingLifeCycle();
            } catch (Exception e) {
                LOGGER.warn("exception while doLifeCycle:" + e);
                exception = e;
            } finally {
                long costTime = System.currentTimeMillis() - startTime;
                statisticHelper(exception,DEFAULT_SESSION_ID, TAG, "", costTime);
            }
        }
    }

    /**
     * run at the current executorService
     */
    private class SwitcherSampleLifeCycleRunner implements Runnable {
        @Override
        public void run() {
            Exception exception = null;
            long startTime = System.currentTimeMillis();
            try {
                doSampleLifeCycle();
            } catch (Exception e) {
                LOGGER.warn("exception while doLifeCycle:" + e);
                exception = e;
            } finally {
                long costTime = System.currentTimeMillis() - startTime;
                statisticHelper(exception,DEFAULT_SESSION_ID, TAG, "", costTime);
            }
        }
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
                LOGGER.warn("get exception while doLifeCycle:" + e);
                exception = e;
            } finally {
                long costTime = System.currentTimeMillis() - startTime;
                statisticHelper(exception,DEFAULT_SESSION_ID, TAG, "", costTime);
            }
        }
    }

    public static class SwitcherObserverInformation<T> {
        private SwitcherDisposable disposable = null;
        private Boolean isDelay = false;
        private TimeUnit timeUnit = null;
        private Long delayTime = null;
        private AtomicLong sendCount = null;
        private Boolean isComplete = false;
        private Boolean isError = false;
        private SwitcherBuffer<T> buffer = null;

        public SwitcherObserverInformation(SwitcherDisposable disposable, boolean _isDelay,
                                           TimeUnit unit, long _delayTime, SwitcherBuffer<T> buffer) {
            Preconditions.checkArgument(disposable != null,
                    "disposable is null");
            this.disposable = disposable;

            if (_isDelay) {
                this.isDelay = _isDelay;
                this.timeUnit = unit;
                this.delayTime = _delayTime;
            }

            this.sendCount = new AtomicLong(0);
            this.buffer = buffer;
        }

        public SwitcherObserverInformation(SwitcherDisposable disposable) {
            this(disposable, false, null, 0, null);
        }

        public SwitcherObserverInformation(SwitcherDisposable disposable, SwitcherBuffer<T> buffer) {
            this(disposable, false, null, 0, buffer);
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

        public SwitcherBuffer<T> getBuffer() {
            return buffer;
        }

        public void setBuffer(SwitcherBuffer<T> buffer) {
            this.buffer = buffer;
        }
    }

    private SwitcherBuffer<T> createBlockingBuffer(int blockingQueueSize) {
        return new BlockingSwitcherBuffer<>(blockingQueueSize);
    }

    private SwitcherBuffer<T> createBlockingBuffer() {
        return new BlockingSwitcherBuffer<>();
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

    private void sleepHelper(TimeUnit unit, long time) {
        switch (unit) {
            case NANOSECONDS:
                try {
                    TimeUnit.NANOSECONDS.sleep(time);
                } catch (InterruptedException e) {
                    ScheduleHooks.onError(e);
                }
                break;
            case MICROSECONDS:
                try {
                    TimeUnit.MICROSECONDS.sleep(time);
                } catch (InterruptedException e) {
                    ScheduleHooks.onError(e);
                }
                break;
            case MILLISECONDS:
                try {
                    TimeUnit.MILLISECONDS.sleep(time);
                } catch (InterruptedException e) {
                    ScheduleHooks.onError(e);
                }
                break;
            case SECONDS:
                try {
                    TimeUnit.SECONDS.sleep(time);
                } catch (InterruptedException e) {
                    ScheduleHooks.onError(e);
                }
                break;
            case MINUTES:
                try {
                    TimeUnit.MINUTES.sleep(time);
                } catch (InterruptedException e) {
                    ScheduleHooks.onError(e);
                }
                break;
            case HOURS:
                try {
                    TimeUnit.HOURS.sleep(time);
                } catch (InterruptedException e) {
                    ScheduleHooks.onError(e);
                }
                break;
            case DAYS:
                try {
                    TimeUnit.DAYS.sleep(time);
                } catch (InterruptedException e) {
                    ScheduleHooks.onError(e);
                }
                break;
        }
    }

    private void statisticHelper(Exception e, String session, String tag, String params, long costTime) {
        if (null == e) {
            //success
            statistic.setSucTime(DEFAULT_SESSION_ID, TAG,"",costTime);
            statistic.incSucCount(DEFAULT_SESSION_ID, TAG, "");
        } else {
            //error
            statistic.setErrTime(DEFAULT_SESSION_ID, TAG, "", costTime);
            statistic.incErrCount(DEFAULT_SESSION_ID, TAG, "", e);
        }
    }

}
