# LiveEventBus
基于LiveData，实现eventBus，事件统一管理，动态APT生成，生命周期管理

关于分支
master
核心代码实现

demo
通过依赖方式引用master生成的库

combine
通过本地方式依赖master代码


--关于打包生成
可以先基于master生成版本{1.0.0}的依赖，项目依赖方式：

implementation 'com.github.codyer.LiveEventBus:lib:1.0.0'

implementation 'com.github.codyer.LiveEventBus:core:1.0.0'

annotationProcessor 'com.github.codyer.LiveEventBus:compiler:1.0.0' //如果使用注解生成事件管理


然后修改combine分支bus/gradle文件中的版本为1.0.0,基于版本1.0.0发布新的版本v1.0.0
这样就可以如下方式依赖

implementation 'com.github.codyer.LiveEventBus:bus:v1.0.0'

annotationProcessor 'com.github.codyer.LiveEventBus:compiler:1.0.0'

