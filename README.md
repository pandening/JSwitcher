What is JSwitch?
====================
```
     JSwitcher is a Convenient tool to switch schedule base on RxJava, and Jswitcher also implement a sample 
 Version Observer/Observable, you can learn how RxJava works from the sample codes. it's easy to switch to 
 Another schedule from current schedule. you just need to care about your bussiness, using 'switchTo'
 Operator to switch to the longing schedlue when you want to do the work on the suitable schedule.
 There are some especial schedules for you, like I/O Bound Schedule, Compute Bound Schedule, And Single 
 Schedule, etc, if you want to create a extra schedule by yourself, it's ok for JSwitcher, and it's 
 very easy to do this .And the most important thing is the jswitch support 'chain operator', that means
 you can switch to a schedule, then fit on this schedule some works, then you can do switch operator 
 continue from current position, or you can just fit another work on current schedule, and jswitcher has 
 terminal operator like 'waitAndShutdown', after do the operator, you can not do 'chain operator' anymore. 
 and the jswitcher will wait some time and shutdown all of schedule. 

```

the following legend show the main class of Jswitcher:

![image](https://github.com/pandening/JSwitcher/blob/master/src/main/resources/schedule.20170901.png)
![image](https://github.com/pandening/JSwitcher/blob/master/src/main/resources/switcher.20170901.png)


How Jswitcher works?
=====================

```
  1. base on RxJava
  2. jswitcher always maintain an object refence to the current schedule. if you switch to 
     andother schedule, the object will change the refence to the new schedule immediately.
  3. you should offer an runnable object to fit on the current schedule, the runnable object
     should contain the work you want to do in the target schedule. you can also offer an 
     object of {@link com.hujian.switcher.ScheduleRunner} , there is an abstract class for
     you to extent, you can see it at {@link com.hujian.switcher.AbstractScheduleRunner}. 
     do not forget to set a executor for the object if you want to fix Jswitcher's code.
  4. you can reference to the legend above to understand how jswitcher works.
```

How to program with Jswitcher?
=============================
   the following is the a sample demo to program with Jswitcher, you can find the complete 
demo code at {@link com.hujian.switcher.example.ScheduleDemo.java}

``` 
  
   SwitcherFitter.switcherFitter()
                .switchToIoSchedule() //switch to i/o bound schedule
                .switchToSingleSchedule() //switch to single schedule
                .fit(normalRunner, future1, true) //do the normal runner at current schedule
                .switchToComputeSchedule() // switch to cpu bound schedule
                .fit(normalRunner, future2, true) // do
                .fit(timeoutRunner, future3, true) // do
                .switchToSingleSchedule() //switch
                .switchToSingleSchedule() //switch
                .fit(timeoutRunner, future4, true) //do
                .awaitFuturesCompletedOrTimeout(100,
                        completableFutures, timeoutFutures, 10) //wait for the future
                .switchToComputeSchedule() //switch
                .fit(() -> {
                    System.out.println("i am a tester->" + Thread.currentThread().getName());
                }) // do the stupid work
                .waitAndShutdown(1000); //wait and shutdown !
```

Why choose Jswitcher?
=====================

```
   Convenient, Powerful, Sample, Amaze ~
```

Update Note
====================
```
[1] 2017-08-21
      * set up the project, base on RxJava.
[2] 2017-08-22
      * sample version of 'Switcher' is available. 
[3] 2017-08-23
      * Observer/Observer/Subscribe is available.(Sample Version)
      * fix some bugs
      * adjust file architecture
[4] 2017-08-27
      * add reactive package, copy RxJava's skeleton to Jswitcher
      * fix some bugs
[5] 2017-08-29
      * Observable/Obsrrver/subscribe -> schedule support now
      * fix some bugs
      * more demo code
[6] 2017-09-01
      * adjust file architecture, remove some unuseful codes
      * fix some bugs
      * let release branch as the default branch

```

Developer
===================
HuJian 
E-mail:<1425124481@qq.com>

License
==================

```java
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
