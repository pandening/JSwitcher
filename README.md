# JSwitcher

## 什么是JSwitcher？

       这个项目开始于我对线程切换的迷惑不解，对于RxJava的线程随意切换非常羡慕，后来尝试自己搜索关于“java 线程切换”和“RxJava 线程切换”的内容，
    但是都没有找到我想要的答案。我所想要的“线程切换”是非常表面的效果，就是我想让我的代码运行在这个线程，然后切换到另外一个线程做一些事情，我可以
    自由随意的切换线程来让我的工作在不同的线程里面进行。
    
       随后我想到了ExecutorService这个东西，这个东西可以做submit，还可以shutdown，而且做完工作之后不会消失，而是随时待命，我希望的也就是
    这样的效果，那如果一个ExecutorService只有一个线程的话，那样是不是可以很容易做到“线程”切换呢？也就是用线程池的切换来达到线程切换的效果。恕我
    直言，我是真的不想用Thread来做线程切换的，所以后来我就用了ExecutorService这个东西，而且后来的测试都是成功的，可以达到随意切换的效果，但是
    是否可以在生产环境下使用还是未知的，因为是在多线程环境下，很多问题需要重新考虑，但是起码可以做到线程切换了啊，严格来说是线程池切换，以达到线程
    切换的效果。所以一句话，Jswitcher就是一个线程池切换工具。
       在设计上，JSwitcher希望能不停的扩展，吸收一些优秀的技术以及思想，使得JSwitcher的能力更加完善。所以JSwitcher设计了核心“core”，这些内容不
    太可能随着时间而改变，除了“core”的内容都可以是“插件式”的内容，可插拔，但是是有顺序的，现存的一些内容之间是互相继承的关系（或者未来会变得没有关   系？），总之未来对JSwitcher的扩展是不会停歇的。
    
 ![image](https://github.com/pandening/JSwitcher/blob/master/src/main/resources/class-structure.png)   
    
     上面展示的就是JSwitcher中核心类图，ResufulSwitcher目前来说是集大成者，所以你可以使用它来完成你需要做的事情。
     
     JSwitcher 还支持线程类别适配，你可以为你的工作切换到合适类别的线程，比如I/O密集型、计算密集型，下面展示了你可以切换到的线程类别：
     
```java

    IO_EXECUTOR_SERVICE(1, "io-executorService"),
    MULTI_IO_EXECUTOR_SERVICE(2, "multi-io-executorService"),
    COMPUTE_EXECUTOR_SERVICE(3, "compute-executorService"),
    MULTI_COMPUTE_EXECUTOR_SERVICE(4, "multi-compute-executorService"),
    SINGLE_EXECUTOR_SERVICE(5, "single-executorService"),
    NEW_EXECUTOR_SERVICE(6, "new-executorService"),
    EMPTY_EXECUTOR_SERVICE(7, "empty-executorService"),
    CUSTOM_EXECUTOR_SERVICE(8, "custom-executorService"),
    DEFAULT_RUN_EXECUTOR_SERVICE(8, "default-executorService");

```



## JSwitcher可以用来做什么事情？

        我非常喜欢漂亮的代码，看着就舒服，对于我来说，漂亮的代码就是风格统一，逻辑清晰。说白了就是看了就知道在做什么事情。毕竟很多时候我们写的代码是为
     业务服务的，而不是各种难以理解的算法代码。再进一步，什么样的代码看起来就明白是做什么事情呢？对我来说就是“链式代码”，一环扣一环，从头读到尾就可以看
     明白是什么功能的代码，这样维护起来也方便。而JSwitcher不仅仅可以随意切换线程（线程池？），而且还加入了可提交任务的功能，你可以继承下面的这个类来      实现你想要完成的业务代码：AbstractSwitcherRunner，在你完成“逻辑链条”代码的部署之后，你可以取回业务的返回值，你可以选择以同步或者异步的方式来
     来完成你想要做的任务，JSwitcher有时候是“智能”的，比如在你让他以同步的方式来做一件事情的时候，它会先去使用当前的线程池来初始化，因为有可能当前的      线程池在别的地方被shutdown了，所以可能出现你提交的任务呗线程池拒绝的情况，JSwitcher会首先使用一个“空”的任务去尝试往当前线程池提交任务，如果发      现拒绝任务的情况，JSwitcher会使用一个默认的线程池来为你完成任务，如果这个默认的线程已经被你shutdown了，那JSwitcher会选择创建一个新的线程池来      运行你的任务，总之，JSwitcher会想尽办法为你运行任务，当然，这样JSwitcher的维护成本就会变大，后期会逐渐优化。
        所以如果你讨厌自己的代码中出现过长的“链条代码”的话，你可以止步于此了！！但是如果你还是希望看看JSwitcher是如何实现的话，可以进一步观察！！！
     如果你希望你的代码可以从头至尾串起来，看起来更炫酷一些，你可以点进去看代码，并且给出你的建议。JSwitcher的起源是线程切换，所以更适合在需要多线程
     参与的场景下，而且不是那么“核心”的功能模块可以“试探性”的进行部署。
        总之，你要是希望减少线程切换的琐事，而且希望写出来的代码和自己的思维一样可以串起来，那么JSwitcher就是适合你的！！



## 怎么在项目中使用JSwitcher？

    使用ResultfulSwitcher可以满足你大部分的场景，使用JSwitcher，你想怎么做，可以最快速的实现在代码上，比如下面这个需求：
    
       我希望在一个新的IO线程里面读取一堆文件，然后将结果送到一个计算密集的线程来做一些处理，然后我希望在前面的那个IO线程中把结果写回磁盘，然后我
       闲的蛋疼希望切换一次线程，到一个计算密集型线程，并且我希望给这个线程去一个响亮的名字，以便我后面可以很快速的找到它.......
       
       好吧，我不知道怎么编下去了，但是我可以肯定，再怎么“无理取闹”的要求，JSwitcher都可以轻松帮你搞定，你可以通过下面的代码来体验一下JSwitcher
       带来的便利性：
       
       
```java

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

```
   哈哈哈，就问你怕不怕？！！！
   

和我联系？
---------------------------------
E-mail:<1425124481@qq.com>


License
---------------------------------
```
Copyright 2017 HuJian

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

```



