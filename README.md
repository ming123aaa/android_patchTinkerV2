###  PatchTinkerV2
对[android_patchTinker](https://github.com/ming123aaa/android_patchTinker)的一个优化版本，减少补丁的大小和补丁加载的时间。
虽然使用代码一致，但是不兼容老版本的补丁包,需要用新的补丁工具打包。

### 补丁包生成
使用阿里的SophixPatchTool打包,需要取消检查初始化选项，需要取消检查初始化选项，需要取消检查初始化选项。
补丁包生成需要使用打补丁工具SophixPatchTool，如还未下载打补丁工具，请前往下载Android打包工具。
打包工具下载地址如下：
[Mac版本打包工具下载](https://ams-hotfix-repo.oss-cn-shanghai.aliyuncs.com/SophixPatchTool_macos.zip?spm=a2c4g.11186623.0.0.58d32cd3lkmCPs&file=SophixPatchTool_macos.zip)
[Windows版本打包工具下载](https://ams-hotfix-repo.oss-cn-shanghai.aliyuncs.com/SophixPatchTool_windows.zip?spm=a2c4g.11186623.0.0.58d32cd3lkmCPs&file=SophixPatchTool_windows.zip)
[Linux版本打包工具地址](https://ams-hotfix-repo.oss-cn-shanghai.aliyuncs.com/SophixPatchTool_linux.zip?spm=a2c4g.11186623.0.0.58d32cd3lkmCPs&file=SophixPatchTool_linux.zip)
如果工具打包很久没有完成的话，可能是apk的问题，可以尝试把apk解压然后再压缩成zip包。


### 注意
暂不支持代码和资源的混淆
1.需要在 gradle.properties添加 android.enableResourceOptimizations=false 避免资源优化导致异常
2.AndroidManifest.xml 无法热更。
3.热更新框架本身无法被热更。
4.热更后需要重启应用才能生效。
5.无法修改已存在的assets的资源(若要修改，只需修改一下文件名)

### 使用

```groovy
allprojects {
    repositories {
        maven { url 'https://www.jitpack.io' }
    }
}
```

```groovy
    dependencies {
    implementation 'com.github.ming123aaa:android_patchTinkerV2:Tag' //请使用最新
}
```

必须设置一个基准包的版本号,由于AndroidManifest.xml不会热更所以可用于检测基准包版本是否发生变化。
```xml
<application>
  <meta-data
            android:name="PatchTinker_Version"
            android:value="1" />
</application>
```

通过以下代码获取当前基准包版本。
```
  PatchTinker.getInstance().getPatchTinkerVersion(this);
```

通过以下代码获取补丁包信息
```
PatchTinker.getInstance().getPatchInfo()
```

### 初始化



提供了3种初始化的方式

方式1:

```xml

<application android:name="com.ohuang.patchtinker.PatchApplication">
    <meta-data android:name="Application_Name" android:value="com.ohuang.hotupdate.TestApp" />
    <meta-data android:name="PatchTinker_Version" android:value="1" />
</application>
```

将application的name设置为com.ohuang.patchtinker.PatchApplication
<meta-data android:name="Application_Name">设置为自己的application
PatchApplication初始化热更后会自动替换成自己application




方式2:
类似于Tinker的接入方式
让原来application的代码继承ApplicationLike,这里实现application

```java
public class AppImpl extends ApplicationLike {
    public App(Application application) {
        super(application);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}

```

实现TinkerApplication的getApplicationLikeClassName方法,返回AppImpl的类名。不要使用AppImpl.class.getName()这种方式获取类名

```java
public class TkApp extends TinkerApplication {
    //记得防止类名被混淆
    @Override
    public String getApplicationLikeClassName() {
        return "com.ohuang.hotupdate.AppImpl";
    }
}

```

将TkApp添加到name,TkApp类就无法通过热更修改
```xml
<application android:name=".TkApp" />
```



方式3:
手动调用补丁初始化方式

```java
public class App extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        PatchUtil.getInstance().init(this);
    }
}
```

调用PatchUtil.getInstance().init(base);方法之前加载的类无法热更新



```java
public class App extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (ProcessCheck.check(base)) {
            PatchUtil.getInstance().init(this);
        }
    }
}
```

### 加载补丁包:(完成后需要重启才能生效)

PatchTinker.getInstance().loadPatchApk(StartActivity.this, patch_path);

### 进程白名单

进程白名单：
<meta-data android:name="PatchTinker_WhiteProcess"/>  进程白名单,白名单的进程不会自动执行热更 (多个进程用","隔开 以":"开头代表子进程 )

```xml

<application >
    <meta-data android:name="PatchTinker_WhiteProcess" android:value=":phoenix" />
</application>
```

### 类白名单
android sdk 24及以上版本支持
可根据startWith、equals来匹配类名,匹配到的类不进行热更。(配置多个类用,隔开 )
记得防止类名被混淆
```xml

<application >
    <meta-data android:name="PatchTinker_WhiteClassStartWith" android:value="com.aaa.bbb,com.tt.aaa" />
    <meta-data android:name="PatchTinker_WhiteClassEquals" android:value="com.aaa.bbb,com.tt.aaa" />
</application>
```

### 保护模式
保护模式不会替换classloader,主要用于加固环境,默认false. (设置后会导致类白名单功能失效)  
主要用于加固环境下
```xml
<application >
<meta-data android:name="PatchTinker_isProtect" android:value="true" />
</application>
```




### 关于混淆

混淆配置
```
#防止被混淆
-keep class com.ohuang.patchtinker.**{*;}
#防止inline
-dontoptimize
```
还有自己的ApplicationLike也别忘了混淆


每次打完包记得保存 mapping.txt 文件用于下次打补丁包配置

配置mapping.txt 仅打补丁包的时候配置
在proguard-rules.pro文件上添加以下配置
```
#改成你的mapping.txt路径
-applymapping "D:\Users\ali213\AndroidStudioProjects\MyApplication2\app\mapping.txt" 
```

使用proguad混淆

如果开启了代码混淆，需要关闭R8，使用proguard进行混淆。不然可能导致生成补丁异常。根据使用的Android Gradle Plugin版本，具体操作如下：

Android Gradle Plugin低于7.0

在项目根目录的gradle.properties中添加如下配置。

```
android.enableR8=false
```
Android Gradle Plugin 7.0以上

在项目根目录的build.gradle中添加如下ProGuard Gradle Plugin配置。

```
buildscript {
repositories {
// For the Android Gradle plugin.
google()
// For the ProGuard Gradle Plugin.
mavenCentral()
}
dependencies {
// The Android Gradle plugin.
classpath("com.android.tools.build:gradle:x.y.z")
// The ProGuard Gradle plugin.
classpath("com.guardsquare:proguard-gradle:7.1.+")
}
}
```
在app目录的build.gradle中应用ProGuard Gradle Plugin。

```
apply plugin: 'com.guardsquare.proguard'
```
然后，关闭R8混淆。

```
android {
buildTypes {
release {
// 关闭 R8.
minifyEnabled false
}
}
}
```
最后，配置ProGuard混淆。

```
android {
...
}

proguard {
configurations {
release {
defaultConfiguration 'proguard-android.txt'
configuration 'proguard-rules.pro'
}
debug {
defaultConfiguration 'proguard-android-debug.txt'
configuration 'proguard-rules.pro'
}
}
}
```
