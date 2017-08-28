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

package com.hujian.switcher.annotation;

import com.google.common.base.Strings;
import com.hujian.switcher.ResultfulSwitcherIfac;
import com.hujian.switcher.core.SwitchRunntimeException;
import com.hujian.switcher.statistic.SampleSwitcherStatistic;
import com.hujian.switcher.statistic.Statistic;
import com.hujian.switcher.utils.SwitcherFactory;
import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by hujian06 on 2017/8/21.
 */
public class RunThenSwitchResolver extends SwitcherAnnotationResolver{
    private static final Logger LOGGER = Logger.getLogger(RunThenSwitchResolver.class);

    private static final String IO_EXECUTOR_NAME = "io-executorService";
    private static final String MULTI_IO_EXECUTOR_SERVICE = "multi-io-executorService";
    private static final String COMPUTE_EXECUTOR_SERVICE = "compute-executorService";
    private static final String MULTI_COMPUTE_EXECUTOR_SERVICE = "multi-compute-executorService";
    private static final String SINGLE_EXECUTOR_SERVICE = "single-executorService";
    private static final String NEW_EXECUTOR_SERVICE = "new-executorService";

    private String _scanPath = "."; // the default scan package
    private static Boolean isRunThenSwitchMode = true;

    private Statistic statistic = SampleSwitcherStatistic.getInstance();
    private static final String DEFAULT_SESSION_ID = "Annotation-statistic";
    private Map<Runnable, String> tagMap = new ConcurrentHashMap<>();

    private Reflections reflections = null;

    private ConcurrentMap<Runnable, ExecutorService> runnableExecutorServiceConcurrentMap = null;
    private BlockingDeque<MoveStatusEntry> moveStatusEntryBlockingDeque = null;


    public RunThenSwitchResolver() {

    }

    public RunThenSwitchResolver(String scanPath) {
        this._scanPath = scanPath;
    }

    public RunThenSwitchResolver(String scanPath, Reflections reflections) {
        this._scanPath = scanPath;
        this.reflections = reflections;
    }

    /**
     * init the env
     */
    private void initEnv() {
        if (reflections == null) {
            reflections = new Reflections(
                    new ConfigurationBuilder()
                            .setUrls(ClasspathHelper.forPackage(_scanPath))
                            .addScanners(new FieldAnnotationsScanner())
            );
        }
        runnableExecutorServiceConcurrentMap = new ConcurrentHashMap<>();
        moveStatusEntryBlockingDeque = new LinkedBlockingDeque<>();
    }

    /**
     * scan the package.
     */
    @SuppressWarnings(value = "unchecked")
    private void scanPackage() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException, InterruptedException, NoExecutorServiceException {
        if (Strings.isNullOrEmpty(_scanPath) || reflections == null) {
            return;
        }

        //get total annotation fields
        Set<Field> fields = reflections.getFieldsAnnotatedWith(RunThenSwitch.class);
        if (!isRunThenSwitchMode) {
            fields = reflections.getFieldsAnnotatedWith(SwitchThenRun.class);
        }

        if (fields == null || fields.isEmpty()) {
            return;
        }

        for (Field field : fields) {
            Class cls = field.getType();
            Constructor constructor = cls.getConstructor();

            //get the runnable
            Runnable runnable = (Runnable) constructor.newInstance();

            String switchToExecutorServiceType = null;
            String switchToExecutorServiceName = null;
            String createType = null;
            if (isRunThenSwitchMode) {
                RunThenSwitch switcher = field.getAnnotation(RunThenSwitch.class);
                switchToExecutorServiceType = switcher.switchToExecutorServiceType();
                switchToExecutorServiceName = switcher.switchToExecutorServiceName();
                createType = switcher.CreateType();
            } else {
                SwitchThenRun switcher = field.getAnnotation(SwitchThenRun.class);
                switchToExecutorServiceType = switcher.switchToExecutorServiceType();
                switchToExecutorServiceName = switcher.switchToExecutorServiceName();
                createType = switcher.CreateType();
            }

            //n..2,1
            moveStatusEntryBlockingDeque.putFirst(new MoveStatusEntry(switchToExecutorServiceType,
                    switchToExecutorServiceName, createType));

            ExecutorService executorService;

            ResultfulSwitcherIfac resultfulSwitcherIfac =
                    SwitcherFactory.getCurResultfulSwitcherIfac();

            if (resultfulSwitcherIfac == null) {
                resultfulSwitcherIfac = SwitcherFactory.createResultfulSwitcher();
            }

            executorService = resultfulSwitcherIfac.getCurrentExecutorService().getExecutorService();
            if (executorService == null) {
                throw new NoExecutorServiceException("No ExecutorService to run job:" + runnable);
            }

            LOGGER.info("run the job:" + runnable + " at ExecutorService:" + executorService);
            //run the job on the current  executorService
            runnableExecutorServiceConcurrentMap.put(runnable, executorService);
            tagMap.put(runnable, cls.getName());
        }

        LOGGER.info("scan package:" + _scanPath + " done,find total " +
                runnableExecutorServiceConcurrentMap.size() + " 'Switcher' annotation");
    }

    /**
     *
     * @param switcherIfac
     * @param moveStatusEntry
     * @throws InterruptedException
     */
    private void doSwitch(ResultfulSwitcherIfac switcherIfac, MoveStatusEntry moveStatusEntry)
            throws InterruptedException {
        if (moveStatusEntry == null ) {
            LOGGER.warn("moveStatusEntry is empty~");
            return;
        }
        //switch to new thread
        if (!Strings.isNullOrEmpty(moveStatusEntry.getSwitchToExecutorServiceType())) {
            switch (moveStatusEntry.getSwitchToExecutorServiceType()) {
                case IO_EXECUTOR_NAME:
                    switcherIfac.switchToIoExecutor(true);
                    break;
                case MULTI_IO_EXECUTOR_SERVICE:
                    switcherIfac.switchToMultiIoExecutor(true);
                    break;
                case COMPUTE_EXECUTOR_SERVICE:
                    switcherIfac.switchToComputeExecutor(true);
                    break;
                case MULTI_COMPUTE_EXECUTOR_SERVICE:
                    switcherIfac.switchToMultiComputeExecutor(true);
                    break;
                case SINGLE_EXECUTOR_SERVICE:
                    switcherIfac.switchToSingleExecutor(true);
                    break;
                case NEW_EXECUTOR_SERVICE:
                    switcherIfac.switchToNewExecutor();
                    break;
                default:
                    switcherIfac.switchToNewExecutor();
                    break;
            }
        } else if (!Strings.isNullOrEmpty(moveStatusEntry.getSwitchToExecutorServiceName())) {
            switcherIfac.switchTo(moveStatusEntry.getSwitchToExecutorServiceName(),
                    true, true, NEW_EXECUTOR_SERVICE);
        } else if (!Strings.isNullOrEmpty(moveStatusEntry.getCreateType())) {
            switch (moveStatusEntry.getCreateType()) {
                case IO_EXECUTOR_NAME:
                    switcherIfac.switchToNewIoExecutor();
                    break;
                case MULTI_IO_EXECUTOR_SERVICE:
                    switcherIfac.switchToNewMultiIoExecutor();
                    break;
                case COMPUTE_EXECUTOR_SERVICE:
                    switcherIfac.switchToNewComputeExecutor();
                    break;
                case MULTI_COMPUTE_EXECUTOR_SERVICE:
                    switcherIfac.switchToNewMultiComputeExecutor();
                    break;
                case SINGLE_EXECUTOR_SERVICE:
                    switcherIfac.switchToNewSingleExecutor();
                    break;
                case NEW_EXECUTOR_SERVICE:
                    switcherIfac.switchToNewExecutor();
                    break;
                default:
                    switcherIfac.switchToNewExecutor();
                    break;
            }
        } else {
            LOGGER.warn("Annotation error:switchToExecutorServiceType == null " +
                    "&& switchToExecutorServiceName == null " +
                    "&& CreateType == null, stop to switch executorService");
        }
    }

    private void runJob() throws SwitchRunntimeException, InterruptedException {
        if (runnableExecutorServiceConcurrentMap == null || runnableExecutorServiceConcurrentMap.isEmpty()) {
            LOGGER.warn("empty runnableExecutorServiceConcurrentMap");
            return;
        }

        if (moveStatusEntryBlockingDeque == null || moveStatusEntryBlockingDeque.isEmpty()) {
            throw new SwitchRunntimeException("moveStatusEntryBlockingDeque is null or empty");
        }

        //run the job
        for (Map.Entry<Runnable, ExecutorService> executorServiceEntry :
                runnableExecutorServiceConcurrentMap.entrySet()) {
            if (executorServiceEntry == null) {
                LOGGER.error("Empty entry");
                continue;
            }
            Runnable runnable = executorServiceEntry.getKey();
            ExecutorService executorService = executorServiceEntry.getValue();

            MoveStatusEntry moveStatusEntry = moveStatusEntryBlockingDeque.takeLast();

            if (!isRunThenSwitchMode) {
                doSwitch(SwitcherFactory.getCurResultfulSwitcherIfac(), moveStatusEntry);
                executorService = SwitcherFactory.createResultfulSwitcher()
                        .getCurrentExecutorService().getExecutorService();
            }

            if (runnable != null && executorService != null && !executorService.isShutdown()) {
                long startTime = System.currentTimeMillis();
                Exception exception = null;
                try {
                    executorService.submit(runnable);
                } catch (Exception e) {
                    exception = e;
                } finally {
                    long costTime = System.currentTimeMillis() -startTime;
                    String TAG = tagMap.get(runnable);
                    if (exception != null) { // error
                        statistic.incErrCount(DEFAULT_SESSION_ID, TAG, "", exception);
                        statistic.setErrTime(DEFAULT_SESSION_ID, TAG, "", costTime);
                    } else { // success
                        statistic.incSucCount(DEFAULT_SESSION_ID, TAG, "");
                        statistic.setSucTime(DEFAULT_SESSION_ID, TAG,"",costTime);
                    }
                }

                if (isRunThenSwitchMode) {
                    doSwitch(SwitcherFactory.getCurResultfulSwitcherIfac(), moveStatusEntry);
                }
            } else {
                LOGGER.error("!(runnable != null && executorService != null && !executorService.isShutdown())");
            }
        }

    }

    public void execute() throws InvocationTargetException, NoSuchMethodException,
            InstantiationException, InterruptedException, IllegalAccessException, SwitchRunntimeException {
        initEnv();
        scanPackage();
        runJob();
    }

    public Boolean getRunThenSwitchMode() {
        return isRunThenSwitchMode;
    }

    public void setRunThenSwitchMode(Boolean runThenSwitchMode) {
        isRunThenSwitchMode = runThenSwitchMode;
    }

    private static class MoveStatusEntry {
        private String switchToExecutorServiceType = null;
        private String switchToExecutorServiceName = null;
        private String CreateType = null;

        public MoveStatusEntry(String switchToExecutorServiceType, String switchToExecutorServiceName,
                               String createType) {
            this.switchToExecutorServiceName = switchToExecutorServiceName;
            this.switchToExecutorServiceType = switchToExecutorServiceType;
            this.CreateType = createType;
        }

        public String getSwitchToExecutorServiceType() {
            return switchToExecutorServiceType;
        }

        public void setSwitchToExecutorServiceType(String switchToExecutorServiceType) {
            this.switchToExecutorServiceType = switchToExecutorServiceType;
        }

        public String getSwitchToExecutorServiceName() {
            return switchToExecutorServiceName;
        }

        public void setSwitchToExecutorServiceName(String switchToExecutorServiceName) {
            this.switchToExecutorServiceName = switchToExecutorServiceName;
        }

        public String getCreateType() {
            return CreateType;
        }

        public void setCreateType(String createType) {
            CreateType = createType;
        }
    }


}
