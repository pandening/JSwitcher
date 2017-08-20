package com.hujian.switcher.example;

import com.hujian.switcher.AbstractSwitcherRunner;
import com.hujian.switcher.SwitcherResultfulEntry;
import com.hujian.switcher.core.SwitchRunntimeException;
import com.hujian.switcher.utils.SwitcherFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hujian06 on 2017/8/20.
 */
public class RichnessExample {


    public static void main(String ... args)
            throws InterruptedException, SwitchRunntimeException, ExecutionException {

        try {
            SwitcherFactory.createResultfulSwitcher()
                    .switchToExecutor(executorService, "Funy-Executor")
                    .apply(stupidWorker, false)
                    .switchToComputeExecutor(true)
                    .apply(stupidWorker, false)
                    .transToRichnessSwitcher()
                    .switchTo("Funy-Executor", false, false, null)
                    .apply(stupidWorker, false)
                    .switchToComputeExecutor(true)
                    .switchToMultiIoExecutor(true)
                    .transToRichnessSwitcher()
                    .transToResultfulSwitcher()
                    .syncApply(stupidRunner, syncResultfulEntry)
                    .switchToMultiComputeExecutor(true)
                    .transToRichnessSwitcher()
                    .transToResultfulSwitcher()
                    .asyncApply(stupidRunner, asyncResultfulEntry);

            SwitcherFactory.shutdown();

            String syncData = syncResultfulEntry.getResultfulData();
            String asyncData = asyncResultfulEntry.getResultfulData();

            System.out.println("sync Result:" + syncData + "\nasync Result:" + asyncData);
        } catch (Exception e) {
            e.printStackTrace();
            SwitcherFactory.shutdown();
        }

    }

    private static ExecutorService executorService = Executors.newFixedThreadPool(1);
    private static StupidRunner stupidRunner = new StupidRunner();
    private static StupidWorker stupidWorker = new StupidWorker();
    private static SwitcherResultfulEntry<String> asyncResultfulEntry = SwitcherResultfulEntry.emptyEntry();
    private static SwitcherResultfulEntry<String> syncResultfulEntry = SwitcherResultfulEntry.emptyEntry();

    private static class StupidWorker implements Runnable {
        @Override
        public void run() {
            System.out.println("i am in:" + Thread.currentThread().getName());
        }
    }

    private static class StupidRunner extends AbstractSwitcherRunner<String> {

        @Override
        protected String run() {
            return "funny + [" + Thread.currentThread().getName() + "]";
        }

        @Override
        protected String fallback() {
            return "fallback + [" + Thread.currentThread().getName() + "]";
        }
    }

}
