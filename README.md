## 一个用于 RecyclerView 列表快速实现曝光逻辑的库

## 依赖接入

- 在 project 级别的 build.gradle 中

```
buildscript {

    repositories {
        ...
        //booster
        maven { url 'https://oss.sonatype.org/content/repositories/public/' }
        //exposure_plugin
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        classpath "com.didiglobal.booster:booster-gradle-plugin:4.5.3"
        //插件
        classpath "com.github.minminaya.RecyclerViewExposure:exposure_plugin:0.0.3"
    }    
}

allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
        ...
    }
}
```

- 在 app 级别的 build.gradle 中

```
plugins {
    ...
    //应用插件，也可以使用 apply plugin: 'com.didiglobal.booster' 的写法
    id 'com.didiglobal.booster'
}

dependencies {

     //依赖
     implementation 'com.github.minminaya.RecyclerViewExposure:exposure:0.0.3'
}

```

## 如何接入和设计思路

[点此跳转博客链接](https://www.jianshu.com/p/0f6ad1ae5e2b)

## 开源协议

无
