package com.hujian;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hujian06 on 2017/8/18.
 */
public class Origin {

    private static final ConcurrentMap<String, ExecutorService> executorServerMap =
            new ConcurrentHashMap<String, ExecutorService>();

    public Origin(int coreSize) {
        for (int i = 0; i < coreSize; i ++) {
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            executorServerMap.put("Executor-" + i , executorService);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, ExecutorService> executorServiceEntry : executorServerMap.entrySet()) {
            sb.append(executorServiceEntry.getKey());
            sb.append(executorServiceEntry.getValue().isShutdown());
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void main(String ... args) {

        Origin origin = new Origin(10);

        System.out.println(origin);

    }

}
