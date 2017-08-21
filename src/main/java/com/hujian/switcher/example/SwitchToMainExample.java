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
