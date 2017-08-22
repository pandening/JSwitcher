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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

/**
 * Created by hujian06 on 2017/8/21.
 */
public class SwitcherAnnotationResolver {
    private static final Logger LOGGER = Logger.getLogger(SwitcherAnnotationResolver.class);

    private static final String IO_EXECUTOR_NAME = "io-executorService";
    private static final String MULTI_IO_EXECUTOR_SERVICE = "multi-io-executorService";
    private static final String COMPUTE_EXECUTOR_SERVICE = "compute-executorService";
    private static final String MULTI_COMPUTE_EXECUTOR_SERVICE = "multi-compute-executorService";
    private static final String SINGLE_EXECUTOR_SERVICE = "single-executorService";
    private static final String NEW_EXECUTOR_SERVICE = "new-executorService";

    private Statistic statistic = SampleSwitcherStatistic.getInstance();
    private static final String DEFAULT_SESSION_ID = "Annotation-statistic";
    private static String TAG = "";
    private Map<Runnable, String> tagMap = new ConcurrentHashMap<>();

    private String _scanPath = "."; // the default scan package

    private Reflections reflections = null;

    private ConcurrentMap<Runnable, ExecutorService> runnableExecutorServiceConcurrentMap = null;

    public SwitcherAnnotationResolver() {

    }

    public SwitcherAnnotationResolver(String scanPath) {
        _scanPath = scanPath;
    }

    public SwitcherAnnotationResolver(String scanPath, Reflections reflections) {
        this._scanPath = scanPath;
        this.reflections = reflections;
    }

    /**
     * init the env
     */
    private void initEnv() {
        reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(_scanPath))
                        .addScanners(new FieldAnnotationsScanner())
        );
        runnableExecutorServiceConcurrentMap = new ConcurrentHashMap<>();
    }

    /**
     * scan the package.
     */
    @SuppressWarnings(value = "unchecked")
    private void scanPackage() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException, InterruptedException {
        if (reflections == null || Strings.isNullOrEmpty(_scanPath)) {
            return;
        }

        Set<Field> fields = reflections.getFieldsAnnotatedWith(Switcher.class);
        if (fields == null || fields.isEmpty()) {
            return;
        }

        for (Field field : fields) {
            Switcher switcher = field.getAnnotation(Switcher.class);
            Class cls = field.getType();
            Constructor constructor = cls.getConstructor();

            //get the runnable
            Runnable runnable = (Runnable) constructor.newInstance();

            String switchToExecutorServiceType = switcher.switchToExecutorServiceType();
            String switchToExecutorServiceName = switcher.switchToExecutorServiceName();
            String createType = switcher.CreateType();

            ExecutorService executorService;

            ResultfulSwitcherIfac resultfulSwitcherIfac =
                    SwitcherFactory.getCurResultfulSwitcherIfac();

            if (resultfulSwitcherIfac == null) {
                resultfulSwitcherIfac = SwitcherFactory.createResultfulSwitcher();
            }

            if (!Strings.isNullOrEmpty(switchToExecutorServiceType)) {
                switch (switchToExecutorServiceType) {
                    case IO_EXECUTOR_NAME:
                        resultfulSwitcherIfac = (ResultfulSwitcherIfac) resultfulSwitcherIfac.switchToIoExecutor(true);
                        break;
                    case MULTI_IO_EXECUTOR_SERVICE:
                        resultfulSwitcherIfac = (ResultfulSwitcherIfac) resultfulSwitcherIfac.switchToMultiIoExecutor(true);
                        break;
                    case COMPUTE_EXECUTOR_SERVICE:
                        resultfulSwitcherIfac = (ResultfulSwitcherIfac) resultfulSwitcherIfac.switchToComputeExecutor(true);
                        break;
                    case MULTI_COMPUTE_EXECUTOR_SERVICE:
                        resultfulSwitcherIfac = (ResultfulSwitcherIfac) resultfulSwitcherIfac.switchToMultiComputeExecutor(true);
                        break;
                    case SINGLE_EXECUTOR_SERVICE:
                        resultfulSwitcherIfac = (ResultfulSwitcherIfac) resultfulSwitcherIfac.switchToSingleExecutor(true);
                        break;
                    case NEW_EXECUTOR_SERVICE:
                        resultfulSwitcherIfac = (ResultfulSwitcherIfac) resultfulSwitcherIfac.switchToNewExecutor();
                        break;
                    default:
                        resultfulSwitcherIfac = (ResultfulSwitcherIfac) resultfulSwitcherIfac.switchToNewExecutor();
                        break;
                }
            } else if (!Strings.isNullOrEmpty(switchToExecutorServiceName)) {
                resultfulSwitcherIfac = (ResultfulSwitcherIfac) resultfulSwitcherIfac
                        .switchTo(switchToExecutorServiceName, true, true, NEW_EXECUTOR_SERVICE);
            } else if (!Strings.isNullOrEmpty(createType)) {
                switch (createType) {
                    case IO_EXECUTOR_NAME:
                        resultfulSwitcherIfac = (ResultfulSwitcherIfac) resultfulSwitcherIfac.switchToNewIoExecutor();
                        break;
                    case MULTI_IO_EXECUTOR_SERVICE:
                        resultfulSwitcherIfac = (ResultfulSwitcherIfac) resultfulSwitcherIfac.switchToNewMultiIoExecutor();
                        break;
                    case COMPUTE_EXECUTOR_SERVICE:
                        resultfulSwitcherIfac = (ResultfulSwitcherIfac) resultfulSwitcherIfac.switchToNewComputeExecutor();
                        break;
                    case MULTI_COMPUTE_EXECUTOR_SERVICE:
                        resultfulSwitcherIfac = (ResultfulSwitcherIfac) resultfulSwitcherIfac.switchToNewMultiComputeExecutor();
                        break;
                    case SINGLE_EXECUTOR_SERVICE:
                        resultfulSwitcherIfac = (ResultfulSwitcherIfac) resultfulSwitcherIfac.switchToNewSingleExecutor();
                        break;
                    case NEW_EXECUTOR_SERVICE:
                        resultfulSwitcherIfac = (ResultfulSwitcherIfac) resultfulSwitcherIfac.switchToNewExecutor();
                        break;
                    default:
                        resultfulSwitcherIfac = (ResultfulSwitcherIfac) resultfulSwitcherIfac.switchToNewExecutor();
                        break;
                }
            } else {
                LOGGER.warn("annotation error:switchToExecutorServiceType == null " +
                        "&& switchToExecutorServiceName == null " +
                        "&& CreateType == null, try to create an new executor to run the job:" + runnable);
                resultfulSwitcherIfac = (ResultfulSwitcherIfac) resultfulSwitcherIfac.switchToNewExecutor();
            }

            executorService = resultfulSwitcherIfac.getCurrentExecutorService().getExecutorService();

            LOGGER.info("run the job:" + runnable + " at ExecutorService:" + executorService);

            runnableExecutorServiceConcurrentMap.put(runnable, executorService);
            tagMap.put(runnable, cls.getName());

        }

        LOGGER.info("scan package:" + _scanPath + " done,find total " +
                runnableExecutorServiceConcurrentMap.size() + " 'Switcher' annotation");
    }

    public void execute() throws InvocationTargetException, NoSuchMethodException,
            InstantiationException, InterruptedException, IllegalAccessException, SwitchRunntimeException {
        initEnv();
        scanPackage();

        //run the job
        if (runnableExecutorServiceConcurrentMap == null || runnableExecutorServiceConcurrentMap.isEmpty()) {
            LOGGER.warn("empty runnableExecutorServiceConcurrentMap");
            return;
        }

        for (Map.Entry<Runnable, ExecutorService> executorServiceEntry
                : runnableExecutorServiceConcurrentMap.entrySet()) {
            if (executorServiceEntry == null) {
                LOGGER.error("Empty entry.");
                continue;
            }
            Runnable runnable = executorServiceEntry.getKey();
            ExecutorService executorService = executorServiceEntry.getValue();

            if (runnable != null && executorService != null && !executorService.isShutdown()) {
                Exception exception = null;
                long startTime = System.currentTimeMillis();
                try {
                    executorService.submit(runnable);
                } catch (Exception e) {
                    exception = e;
                } finally {
                    long costTime = System.currentTimeMillis() -startTime;
                    TAG = tagMap.get(runnable);
                    if (exception != null) {
                        statistic.setErrTime(DEFAULT_SESSION_ID, TAG, "", costTime);
                        statistic.incErrCount(DEFAULT_SESSION_ID, TAG, "", exception);
                    } else {
                        statistic.incSucCount(DEFAULT_SESSION_ID, TAG, "");
                        statistic.setSucTime(DEFAULT_SESSION_ID, TAG,"",costTime);
                    }
                }
            } else {
                LOGGER.error("!(runnable != null && executorService != null && !executorService.isShutdown())");
            }
        }
    }

}
