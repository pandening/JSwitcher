package com.hujian;

import java.util.concurrent.TimeUnit;

/**
 * Created by hujian06 on 2017/8/18.
 */
public class SwitchDemo {

    private static SampleSwitcher sampleSwitcher = new SampleSwitcher();
    private static StupidWorker stupidWorker = new StupidWorker();

    public static void main(String ... args) throws InterruptedException, SwitchRunntimeException {

        sampleSwitcher
                .switchToMultiIoExecutor(true) //switch to a multi-io-executor[first executorService]
                .apply(stupidWorker, true) //do the stupidWorker on the multi-io-executor executorService
                .switchToMultiComputeExecutor(true) //switch to a multi-compute-executor
                .apply(stupidWorker, false) //do the stupidWorker on the multi-compute-executor
                // first of all switch to an compute executor,then do the stupidWorker on the new compute executorService
                .switchBeforeIoWork(stupidWorker, true, false)
                .switchToNewSingleExecutor() // switch to an new single executor
                .apply(stupidWorker, false) // do the stupidWorker on the single executorService
                .clear();

    }

    /**
     * test job.
     */
    private static class StupidWorker implements Runnable {
        @Override
        public void run() {
            System.out.println("i am in:" + Thread.currentThread().getName());
        }
    }

}
