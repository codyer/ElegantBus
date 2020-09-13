# ElegantBus
[![](https://jitpack.io/v/codyer/ElegantBus.svg)](https://jitpack.io/#codyer/ElegantBus)

ElegantBus is a library on Android， based on LivaData，this is an 'elegant' event bus。

### Compared with the common LivaData Bus

BUS | use reflex | Intrusion system package name | Sticky for one process | Sticky for multi process | Sticky for multi APP | Configurable | different Thread | Group for event | safety for multi App |Sticky forever
---|---|---|---|---|---|----|---|---|---|---
LiveEventBus | :white_check_mark: | :white_check_mark: | :white_check_mark: | :x: | :x: | :x: | :x: | :x:  | :x: |:x:
ElegantBus | :x: | :x: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark:  | :white_check_mark: | :white_check_mark:


## How to config
### （一）To get a Git project into your build:

#### Step 1. Add the JitPack repository to your build file
```
allprojects {
    repositories {
    ...
    maven { url 'https://jitpack.io' }
}
}
```

#### Step 2. Add the dependency
[![](https://jitpack.io/v/codyer/ElegantBus.svg)](https://jitpack.io/#codyer/ElegantBus)

```
def version = "2.2.0"
dependencies {
    implementation "com.github.codyer.ElegantBus:core:$version" // when there is only one single process in you app add this line.
//  implementation "com.github.codyer.ElegantBus:ipc-aidl:$version" // multi process in you app add this line（way 1：aidl implement，include core）
//  implementation "com.github.codyer.ElegantBus:ipc-messenger:$version" //  multi process in you app add this line（way 2：messenger implement，include core）
//	annotationProcessor "com.github.codyer.ElegantBus:compiler:$version"// when you need define event with apt add this line.
}
```
##### If you app is multi process follow the left two step, otherwise you can jump to [How to use](#how-to-use).

#### Step 3. in gradle of app module, add manifestPlaceholders for support multi process(App)
```
manifestPlaceholders = [
    BUS_SUPPORT_MULTI_APP  : true,// this for multi App?
    BUS_MAIN_APPLICATION_ID: "com.example.bus" // the applicationId for the host(main) app.
]
```

you should use the same signer for your app, if not, you will get an error.


#### Step 4. init multi process in Application.
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

## How to use
### （二）ElegantBus in simple way
#### 1、 Send event
when you send and observer event in one process you can use follow method:
```
ElegantBus.getDefault("EventA").post(new Object());
ElegantBus.getDefault("EventA").post("eventA");
ElegantBus.getDefault("EventA").post(888888);
```
when you need cross process ,you can use follow method to get you bus:
```
ElegantBus.getDefault(String group, String event, Class<T> type, boolean multiProcess);
```

#### 2、 Receive event
+ Normal event
only receive event when the page is resumed.
```
ElegantBus.getDefault("EventA").observe(this, new ObserverWrapper<Object>() {
            @Override
            public void onChanged(final Object value) {
                ElegantLog.d(value.toString());
            }
        });
```
+ Sticky event
can receive the last event, even the page is not exist when the event happened.
```
ElegantBus.getDefault("EventA").observeSticky(this, new ObserverWrapper<Object>() {
            @Override
            public void onChanged(final Object value) {
                ElegantLog.d(value.toString());
            }
        });
```
+ Forever event
can receive all event no matter the page is resumed(of course, the page is exist).
```
ObserverWrapper<Object> foreverObserverWrapper;
ElegantBus.getDefault("EventA").observeForever(foreverObserverWrapper = new ObserverWrapper<Object>() {
            @Override
            public void onChanged(final Object value) {
                ElegantLog.d(value.toString());
            }
        });
// when you use forever event, you should remove it by yourself for avoid memory leak.
ElegantBus.getDefault("EventA").removeObserver(foreverObserverWrapper);
```
+ Normal event and Forever Event both support sticky event.

 you can set (sticky = true) for ObserverWrapper
ElegantBus has a default implement：
```
new ObserverWrapper<Object>(true) {
// this is a sticky event observer
		@Override
		public void onChanged(Object value) {}
   })
```

### （二）ElegantBus in other way
+ It can be found that in the above way, the received data type is object.
 Therefore, as long as the event with the same name is sent, the observer can receive it no matter what type it is sending.
 In order to manage events in a unified way and prevent the problems caused by spelling errors such as event conflicts and case, it is not recommended to use above way directly.

**recommended way to use**
#### Define event
+ eg:
```
@EventGroup(value = "TestScope", active = true)
public class EventDefine {
    @Event(description = "eventInt test", multiProcess = false, active = true)
    Integer eventInt;

    @Event(description = "eventString test", multiProcess = true, active = true)
    String eventString;

    @Event(description = "eventBean test", multiProcess = true, active = true)
    JavaBean eventBean;
}
```
##### explain
In fact, the event definition only uses two annotations

1）、@EventGroup is used in class，Define the event group name and whether it is activated.

2）、@Event is used in variables to define the specific event description, whether it is activated, and whether it supports multiple processes`

After defining the annotation, ElegantBus will automatically generate the event bus with the group name defined by the event group through the annotation processor imported previously.
For example, the above definition will generate a 'TestScopeBus'.

Then we can directly use this event bus for event management everywhere.

+ Send event
```
TestScopeBus.eventInt().post(888);
TestScopeBus.eventString().post("new string");
TestScopeBus.eventBean().post(new JavaBean());
```

+ Receive event
```
TestScopeBus.eventInt().observe(owner, new ObserverWrapper<Integer>() {
	@Override
	public void onChanged(final Integer value) {
		...
	}
});
```

#### Welcome to star and submit issue

- More details
[如何优雅的使用LiveData实现一套EventBus（事件总线）](https://www.jianshu.com/p/79d909b6f8bd)
