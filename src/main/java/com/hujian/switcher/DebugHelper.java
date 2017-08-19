package com.hujian.switcher;

import java.util.Iterator;
import java.util.concurrent.BlockingDeque;

/**
 * Created by hujian06 on 2017/8/19.
 */
public final class DebugHelper {

    /**
     * check before search an executor
     * @param expectExecutorType
     * @param deque
     */
    public static synchronized void trackExecutorQueue(String expectExecutorType, BlockingDeque<SwitchExecutorServiceEntry> deque) {
        if (deque == null || deque.isEmpty()) {
            System.out.println("trackExecutorQueue:null or empty deque");
        } else {
            System.out.println("expectExecutorType:" + expectExecutorType);
            Iterator iterator = deque.iterator();
            while (iterator.hasNext()) {
                SwitchExecutorServiceEntry switchExecutorServiceEntry = (SwitchExecutorServiceEntry) iterator.next();
                System.out.println("deque element=>" + switchExecutorServiceEntry.getExecutorType());
            }
        }
    }

}
