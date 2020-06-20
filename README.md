# ElegantBus
[![](https://jitpack.io/v/codyer/ElegantBus.svg)](https://jitpack.io/#codyer/ElegantBus)

ElegantBus 是一款 Android 平台，基于LivaData的消息总线框架，这是一款非常 **优雅** 的消息总线框架。

如果对 Elegant 的实现过程，以及考虑点感兴趣的可以看看前几节[自吹](#自吹)

如果只是想先使用的，可以跳过，直接到跳到[使用说明](#使用说明)

## 来龙去脉
### 自吹

ElegantBus 支持跨进程，且支持跨应用的多进程，甚至是支持跨进程间的粘性事件，支持事件管理，支持事件分组，支持自定义事件，支持同名事件等。

之所以称之为最优雅的总线，是因为她不仅实现了该有的功能，而且尽量选用最合适，最轻量，最安全的方式去实现所有的细节。
更值得夸赞的是使用方式的优雅！

### 前言
随着LifeCycle的越来越成熟，基于LifeCycle的LiveData也随之兴起，业内基于LiveData实现的EventBus 也如雨后春笋一般拔地而起。

出于对技术的追求，看过了无数大牛们的实现，各位大神们思路也是出奇的神通，最基础的 LiveData 版 EventBus 其实大同小异，一个单例类管理所有的事件LivaData集合。`如果不清楚的可以随便网上找找`

反正基本功能 LivaData 都支持了，实现 EventBus 只需要把所有事件管理起来就完事了。

业内基于LiveData实现的EventBus，其实考虑的无非就是下面提到的五个挑战，有的人考虑的少，有的人考虑的多，于是各种方案都有。

ElegantBus 主要是集合各家之优势，进行全方面的考虑而产生的。

### 五个挑战 之 路途险阻
#### 挑战一 ： 粘性事件
+ 背景
LivaData的设计之初是为了数据的获取，因此无论是观察开始之前产生的数据，还是观察开始之后产生的数据，都是用户需要的数据，只要是有数据，当LifeCycle处于激活状态，数据就会传递给观察者。这个我们称之为 **粘性数据**。
这种设计对于事件来说有时候就不那么友好了，之前的事件用户可能并不关心，只希望收到注册之后发生的事件。

#### 挑战二 ： 多线程发送事件可能丢失
+ 背景
同样是因为使用场景的原因，LivaData设计在跨线程时，使用post提交数据，只会保留最后一次数据提交的值，因为作为数据来说，用户只需要关心现在有的数据是什么。

#### 挑战三 ： 跨进程事件总线
+ 背景
有时候我们应用需要设置多进程，不同模块可能允许在不同进程中，因为单例模式每个进程都有一份实体，所有无法达到跨进程，这时候设计IP方案选择。

+ 说明
这里提一下为什么不选用广播方式，对广播有一定了解的都知道，全局广播会有信息泄露，信息干扰等问题，而且开销也比较大，因此全局广播并不适合这种情况。
也许有人会说可以用本地广播，然而，本地广播目前来说并不是很好的选择。

Google官方也在 LocalBroadcastManager 的说明里面建议使用LiveData替代：
[原文地址](https://developer.android.google.cn/jetpack/androidx/releases/localbroadcastmanager?hl=zh_cn)

##### 原文如下：

> 2018 年 12 月 17 日
>
> 版本 1.1.0-alpha01 中将弃用 androidx.localbroadcastmanager。
>
> 原因
>
> LocalBroadcastManager 是应用级事件总线，在您的应用中使用了层违规行为；任何组件都可以监听来自其他任何组件的事件。
它继承了系统 BroadcastManager 不必要的用例限制；开发者必须使用 Intent，即使对象只存在且始终存在于一个进程中。由于同一原因，它未遵循功能级 BroadcastManager。
这些问题同时出现，会对开发者造成困扰。
>
> 替换
>
> 您可以将 LocalBroadcastManager 替换为可观察模式的其他实现。合适的选项可能是 LiveData 或被动流，具体取决于您的用例。

更明显的原因是，本地广播好像并不支持跨进程~

#### 挑战四 ： 跨应用（权限问题）
+ 背景
跨进程相对来说还比较好实现，但是有的时候用户会有跨应用的需求，其实这个也是IPC范畴，为什么单独提出来呢？
因为跨应用设计信息安全，权限校验问题，开放给其他应用，但是同时又要兼顾不被非法滥用。

#### 挑战五 ： 兼容性，简洁性
+ 背景
一个好的事件总线需要很好的兼容，不同事件应该有个很好的管理，不会造成冲突，事件可以进行多种配置，如某事件是否支持跨进程，是否激活，属于什么分组等等。


### 和常见LivaData实现的EventBus比较

消息总线 | 使用反射 | 入侵系统包名 | 进程内Sticky | 跨进程Sticky | 跨APP Sticky | 事件可配置化 | 线程分发 | 消息分组 | 跨App安全考虑 |常驻事件Sticky
---|---|---|---|---|---|----|---|---|---|---
LiveEventBus | :white_check_mark: | :white_check_mark: | :white_check_mark: | :x: | :x: | :x: | :x: | :x:  | :x: |:x:
ElegantBus | :x: | :x: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark:  | :white_check_mark: | :white_check_mark:



## 使用说明
### （一）ElegantBus 接入配置

#### 1、项目级别gradle添加依赖 目前使用的是 jitPack
```
allprojects {
    repositories {
    ...
    maven { url 'https://jitpack.io' }
}
}
```

#### 2、在应用 gradle 文件中添加 ElegantBus 最新版本依赖
[![](https://jitpack.io/v/codyer/ElegantBus.svg)](https://jitpack.io/#codyer/ElegantBus)

```
def version = "2.0.0"
dependencies {
    implementation "com.github.codyer.ElegantBus:core:$version" // 不需要跨进程时使用
//  implementation "com.github.codyer.ElegantBus:ipc-aidl:$version" // 跨进程时使用（方式1：aidl 实现，已经包含 core）
//  implementation "com.github.codyer.ElegantBus:ipc-messenger:$version" // 跨进程时使用（方式2：messenger 实现，已经包含 core）
//	annotationProcessor "com.github.codyer.ElegantBus:compiler:$version"// 需要事件自动管理时使用
}
```
##### 如果不需要跨进程，以上两步配置就可以了，如果需要跨进程，第二步选择一个跨进程的方式，并添加第三步配置，且设置第四步。

#### 3、在应用 gradle 文件中的 manifestPlaceholders 配置是否支持跨 App，以及主 App 的 applicationId
```
manifestPlaceholders = [
    BUS_SUPPORT_MULTI_APP  : true,// 是否支持跨App
    BUS_MAIN_APPLICATION_ID: "com.example.bus" // 肯定会被安装的主app的applicationId
]
```

为了App安全性，必须使用相同的密钥签名的App才可以设置为一个公用组，否则Debug模式下会抛出异常，Release模式下会输出 error 信息。


#### 4、分别在应用的 Application 的 onCreate 和 onTerminate 方法中添加开始支持多进程和结束多进程
```
public class BusApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ElegantBus.setDebug(true);// 可以打开日志开关
        ElegantBusX.supportMultiProcess(this);
    }

    @Override
    public void onTerminate() {
        ElegantBusX.stopSupportMultiProcess();
        super.onTerminate();
    }
}
```

##### 以上几步就完成了使用 ElegantBus 的全部配置，下面进入使用环节

### （二）ElegantBus 使用说明
#### 1、 发送事件
最简单方式就是直接一句
```
ElegantBus.getDefault("EventA").post(new Object());
ElegantBus.getDefault("EventA").post("eventA");
ElegantBus.getDefault("EventA").post(888888);
```
可以在任何线程发送都是OK的，考虑大部分是没有跨进程需求的，所以这里默认，这种最简单的方式，这个事件 `EventA` 是不支持跨进程的。
如果要进行跨进程可以使用重载函数进行设置，重载函数如下：
```
ElegantBus.getDefault(String group, String event, Class<T> type, boolean multiProcess);
```

#### 2、 接收事件
接收事件也很简单：
+ 常规事件
```
ElegantBus.getDefault("EventA").observe(this, new ObserverWrapper<Object>() {
            @Override
            public void onChanged(final Object value) {
                ElegantLog.d(value.toString());
            }
        });
```
+ 普通事件的粘性事件
```
ElegantBus.getDefault("EventA").observeSticky(this, new ObserverWrapper<Object>() {
            @Override
            public void onChanged(final Object value) {
                ElegantLog.d(value.toString());
            }
        });
```
+ 常驻事件
```
ObserverWrapper<Object> foreverObserverWrapper;
ElegantBus.getDefault("EventA").observeForever(foreverObserverWrapper = new ObserverWrapper<Object>() {
            @Override
            public void onChanged(final Object value) {
                ElegantLog.d(value.toString());
            }
        });
// 常驻事件要自己取消注册，避免内存泄露
ElegantBus.getDefault("EventA").removeObserver(foreverObserverWrapper);
```
+ 其实普通事件和常驻事件都支持粘性事件

只要创建 ObserverWrapper 时设置 sticky = true 就可以；
ElegantBus 提供了默认构造函数如下：参数true 表示粘性事件
```
new ObserverWrapper<Object>(true) {
		@Override
		public void onChanged(Object value) {}
   })
```

##### 以上简单的使用就介绍完毕了
### 高级特性
+ 可以发现，上面的方式，接收的数据类型是 Object 的，因此，只要是同名的事件，无论发送的是什么类型，观察者都可以接收到。
为了对事件进行统一管理，防止事件冲突，事件大小写等拼写错误带来的问题，个人不建议直接使用这种方式

**推荐使用事件定义方式**
#### 事件定义
+ 先上例子
```
@EventGroup(value = "TestScope", active = true)
public class EventDefine {
    @Event(description = "eventInt 事件测试", multiProcess = false, active = true)
    Integer eventInt;

    @Event(description = "eventString 事件测试", multiProcess = true, active = true)
    String eventString;

    @Event(description = "eventBean 事件测试", multiProcess = true, active = true)
    JavaBean eventBean;
}
```
##### 说明
其实事件定义只用到两个注解

1）、@EventGroup 使用在 class 上，定义`事件分组名`，`是否激活`

2）、@Event 使用在变量上，定义具体 `事件描述`，`是否激活`，`是否支持多进程`

定义完注解后，通过前面导入的注解处理器 annotationProcessor ，ElegantBus 会自动生成以 EventGroup 定义的分组名的事件总线
例如上面的定义就会生成一个 `TestScopeBus`

然后我们所有地方就可以直接使用这个事件总线进行事件管理。

+ 发送事件
```
TestScopeBus.eventInt().post(888);
TestScopeBus.eventString().post("新字符串");
TestScopeBus.eventBean().post(new JavaBean());
```

+ 接收事件
```
TestScopeBus.eventInt().observe(owner, new ObserverWrapper<Integer>() {
	@Override
	public void onChanged(final Integer value) {
		...
	}
});
```
#### 事件回调在非UI线程执行
默认事件是在主线程回调的，如果想在非主线程回调，设置 ObserverWrapper.uiTread = false，同时提供默认构造函数设置是否在UI线程回调。

#### 欢迎 Star 和提交 Issue
- 为了 ElegantBus 更好的为大家提供服务，更好的兼容性，我特意做了很多场景的测试，可能会有覆盖不到的，如果遇到问题，欢迎留言评论

- 测试项目地址: [ElegantBus-example](https://github.com/codyer/ElegantBus-example)

- 老版本请查看分支 v1.0.0
[老版本说明](https://github.com/codyer/ElegantBus/blob/master/README_v1.md)
