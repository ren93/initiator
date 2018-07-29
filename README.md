# Initiator


[ ![Download](https://api.bintray.com/packages/renjianan/maven/plugin/images/download.svg) ](https://bintray.com/renjianan/maven/plugin/_latestVersion)

Android应用初始化工具

## 如何使用
在程序的任意位置，只要实现`IAppInit`接口就可以了，无需手动调用，[Initiator](https://github.com/renjianan/initiator)会在编译时自动搜索所有实现了该接口的类，并生成调用`init()`方法的的代码。[Initiator](https://github.com/renjianan/initiator)支持kotlin，支持Application类型和library类型的module。
```java
public class PushInit implements IAppInit {
    @Override
    public void init(Application application) {
        Log.d("init==", "PushInit");
    }
}
```

为了满足更多初始化需求，还可以为每个初始化增加多种配置，只要在这个类上加一个`@AppInit`注解就行了：
```java
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface AppInit {
    boolean background() default false; //在工作线程中初始化，默认false

    boolean inChildProcess() default true;//允许在子进程中初始化，多进程应用Application的onCreate方法会调用多次，默认true

    boolean onlyInDebug() default false;//只在debug中做初始化，默认false

    int priority() default 0;//初始化优先级，数字越大，优先级越高，初始化时间越早

    long delay() default 0L;//初始化执行延时时间，在主线程和工作线程都可以延时
}
```

采用编译期注解，代码在编译时生成，不使用反射，对最终程序运行性能影响很小。最终我们的代码可能如下：
```java
@AppInit(priority = 22, delay = 1740, onlyInDebug = true)
public class PushInit implements IAppInit {

    @Override
    public void init(Application application) {
        Log.d("init==", "PushInit");
    }
}

```

注意，如果你没有做这些特别的配置，不需要加这个注解。另外你可能对`Application`做了多重继承[Initiator](https://github.com/renjianan/initiator)会找到多个Application的子类,请在你需要初始化的入口加上`@InitContext`注解：

```
@InitContext
public class App extends BaseApplication {

}
```
目前暂时只支持`Application`类型，后期考虑增加`Activity`的支持，因为有些初始化可以延后放到启动页或首页来做。目前可以用延时策略替代。
## 引入方式
首先，在项目根目录的 `build.gradle `文件中增加以下内容：
```
    dependencies {
         classpath 'com.renny.initiator:plugin:'${latest_version}"
    }

```
然后，在 `application` 或 `library` 模块的` build.gradle` 文件中应用插件：
```java
apply plugin: 'com.android.application'
// apply plugin: 'com.android.library'
apply plugin: 'initiator'
```
## 混淆
```
-keep class com.renny.mylibrary.*
-keepclassmembers class com.renny.mylibrary.**{
    *;
}

```
## LICENSE

    Copyright (c) 2016-present, SateState Contributors.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
