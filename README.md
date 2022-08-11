详情见文章：https://juejin.cn/post/7130605924720476167/
# 使用效果
![WX20220811-211059@2x.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/572e4361f7bb4548bbc8cb1dc5dbae63~tplv-k3u1fbpfcp-watermark.image?)
# 使用方式

## 通过 Plugin Marketplace

> 该插件已上架 JetBrains Marketplace，仅支持 Android Studio 、Intellij IDEA 开发工具，并仅支持 Java、Kotlin 语言。

1. 可通过 `Preferences` -> `Plugins` -> `Marketplace` 搜索 `CollectUtilTool` 安装。
  
2. 在工具类或方法上面，添加 `@utilDesc` 关键词的注释。例如：
  
  ```kotlin
  // @utilDesc 工具类UtilClass
  object UtilClass {
  
      /**
       * @utilDesc test
       */
      fun test(){
  
      }
  }
  ```
  

## 下载源码

该插件源码地址：https://github.com/StefanShan/CollectUtil

可通过修改 UtilClassFileManager.kt 文件下内容，修改注释扫描标志词和扫描文件范围。

```kotlin
object UtilClassFileManager {

    /**
     * 注释扫描关键词。通过该关键词判断是否为标记的工具类/方法
     *
     * 用法：
     * // @utilDesc 测试工具类
     * class Test{}
     */
    private const val COMMENT_TAG = "@utilDesc"

    /**
     * 文件扫描范围。
     *
     * 用法：目前支持扫描 java、kotlin 文件
     */
    val supportLanguages = arrayListOf("java", "kt")
}
```
