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
可以基于master生成版本{1.0.0}的依赖，项目依赖方式：

implementation 'com.github.codyer.LiveEventBus:core:1.0.0'

annotationProcessor 'com.github.codyer.LiveEventBus:compiler:1.0.0' //如果使用注解生成事件管理

基本配置
compileSdkVersion 28

defaultConfig {
    minSdkVersion 19
    targetSdkVersion 28
    versionCode 1
    versionName version
}
