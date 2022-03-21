## 一个用于 RecyclerView 列表快速实现曝光逻辑的库

一个好的产品离不开数据分析，在手机 APP 中，数据分析极致化需要细致到某个时刻列表曝光的了哪几个 Item。

2022 年了，基本上目前 Android 上可以滑动的复杂列表都是 RecyclerView 或者其扩展，这里分享一个封装的思路。

# 一、基本思路

---

### 什么是列表曝光

---

> 简单的理解就是用户在肉眼可感知范围内真正看到了数据就算曝光，包括数据刷新了


如果非要细化细节：

- 1、列表数据变化时，比如上滑下滑
- 2、页面从隐藏到显示，比如切换页面、前后台切换

### 一些方案的对比

---

各种方案核心都差不多，最关键的就是通过 LayoutManager 获取屏幕内第一个可见和最后一个可见 item position，上报其区间内的 Item。这里简称这个逻辑为**检查上报逻辑**。

但是触发时机有所不同，通常如下方案一和二所述，当然除了方案一和方案二外，还有一些别的方案，比如监听 RecyclerView 的布局树变化触发**检查上报逻辑**等方案。

#### 方案一

- 1、监听列表数据变化，比如 RecyclerView 通过监听 Adapter 的数据变化，数据变化之后触发**检查上报逻辑**
- 2、监听列表滑动，在列表停止滑动时触发**检查上报逻辑**
- 3、页面隐藏到显示的时候触发**检查上报逻辑**

#### 方案二

> 这个是在想降低曝光埋点复杂度时，阅读 RecyclerView 源码，并且经过 Demo 不断测试和调试发现的新路子 😊


- 1、通过注册 `RecycleView`的`OnChildAttachStateChangeListener` 接口来监听子 view attached 和 detached 的情况，这个接口有个特点：子 View 滑动到可以`RecyclerView` 区域内时会触发 `onChildViewAttachedToWindow`，相反移出`RecyclerView` 区域外则触发 `onChildViewDetachedFromWindow`，正所谓天然的触发曝光的接口，我们可以建立收集数据集逻辑， 在 `onChildViewAttachedToWindow` 时加入 item 到集合，`onChildViewDetachedFromWindow` 时从集合移除 item，在人眼可以感知到的时间内比如收集行为结束 500ms 后统一汇总集合中的 item，将 item 一一上报。
- 2、页面隐藏到显示的时候触发**检查上报逻辑**

可以发现方案二相比方案一更有利于减少各种回调的注册和周期的控制，下文会在方案二的基础上，阐述用法和相关实现思路。

![](https://upload-images.jianshu.io/upload_images/3515789-26d12b4bb1d2508a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)




# 二、RecyclerViewExposure 库用法

---

仓库地址：[RecyclerViewExposure](https://github.com/minminaya/RecyclerViewExposure)

---

### 优点：

- 1、抽象相关统计埋点和生命周期管理
- 2、支持 `ConcatAdapter(MergeAdapter)` 

### 缺点：

- 1、未支持 Item 可见程度百分比触发曝光逻辑（由于相对耗费计算性能，在曝光埋点场景暂不允支持，不过留了扩展的方法）
- 2、仅仅支持流式列表和网格列表（网格也是流式列表的一种）（当然可以通过修改核心**检查上报逻辑**达到支持流式和其他列表的目的）

### 业务场景：

- 1、有一个 size 为 n 的列表
- 2、当列表曝光时，在用户可感知范围内上报用户能看到的 item 的信息
	- 可感知：快速滑动时，只有最后停下来看到的 item才算是可感知，慢速移动时，能肉眼看到的 item 都算是可感知

### 配置 Gradle 依赖

- 在 project 级别的 build.gradle 中
	```Kotlin
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
	```Kotlin
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
	

### API 说明

- `IEntityForImpr`：接口，需要上报的列表 Adapter 的数据实体实现该接口，并实现方法 `getIdForImpr()`，目的是为了让 item 保持唯一性
- `AbsListImprEventHelper`：抽象类，实现曝光事件上报的封装类，针对列表数据Adapter 为`RecyclerView.Adapter<K>` 的子类做了封装
	- ①`needPostEvent()`：该方法表示当前的 `item` 是否需要上报统计，返回 `false` 表示不需要上报该`item`
	- ②`getAdapterEntityForPosition()` ：该方法返回 `item` 对应的`entity`
	- ③`onItemExposure()` ：单个 Item 曝光的时候回调，回调数据为 `entity absoluteAdapterPosition bindingAdapterPosition` 
	- ④`onBatchItemExposure()` ：可见项批量曝光回调，一次回调出所有 item 相关的数据类 `Triple`，包含 `entity absoluteAdapterPosition bindingAdapterPosition` 数据
- `AbsListAdapterImprEventHelper`：抽象类，扩展自 `AbsListImprEventHelper` 类，针对列表数据Adapter 为`ListAdapter` 的子类做了封装，定义实现了 `getAdapterEntityForPosition()` 的方法，让RecyclerViewExposure的使用方法更加精简

### 使用方法

#### 首先我们先实现一个列表（部分实现省略）

- 1、创建列表适配器 `ItemRecyclerViewAdapter` 
	```Kotlin
	class ItemRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	
	    val dataList = mutableListOf<PlaceholderContent.PlaceholderItem>()
	
	    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
	
	        return ViewHolder(
	            FragmentItemBinding.inflate(
	                LayoutInflater.from(parent.context),
	                parent,
	                false
	            )
	        )
	    }
	
	    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
	        val idView: TextView = binding.itemNumber
	        val contentView: TextView = binding.content
	
	        override fun toString(): String {
	            return super.toString() + " '" + contentView.text + "'"
	        }
	    }
	
	    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
	        val item = dataList[position]
	        (holder as? ViewHolder)?.apply {
	            idView.text = item.id
	            contentView.text = item.content
	        }
	    }
	
	    override fun getItemCount(): Int {
	        return dataList.size
	    }
	
	}
	```
	
- 2、创建 `ItemRecyclerViewAdapter` 需要用的数据实体类 `PlaceholderContent.PlaceholderItem`
	```Kotlin
	 data class PlaceholderItem(val id: String, val content: String, val details: String)
	```
	
- 3、创建 Activity 容器，绑定 xml 布局
	```Kotlin
	class RecyclerAdapterExampleActivity : AppCompatActivity() {
	    override fun onCreate(savedInstanceState: Bundle?) {
	        super.onCreate(savedInstanceState)
	        setContentView(R.layout.activity_example)
	        val recyclerview = findViewById<RecyclerView>(R.id.list)
	        recyclerview.layoutManager = LinearLayoutManager(this)
	        val adapter = ItemRecyclerViewAdapter()
	        recyclerview.adapter = adapter
	        adapter.dataList.addAll(PlaceholderContent.ITEMS)
	        adapter.notifyDataSetChanged()
	    }
	}
	```
	
	```Kotlin
	<?xml version="1.0" encoding="utf-8"?>
	<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
	    xmlns:app="http://schemas.android.com/apk/res-auto"
	    xmlns:tools="http://schemas.android.com/tools"
	    android:layout_width="match_parent"
	    android:layout_height="match_parent"
	    tools:context="com.minminaya.example.activity.ListAdapterExampleActivity">
	
	    <androidx.recyclerview.widget.RecyclerView
	        android:id="@+id/list"
	        android:name="com.minminaya.example.ItemFragment"
	        android:layout_width="match_parent"
	        android:layout_height="match_parent"
	        android:layout_marginLeft="16dp"
	        android:layout_marginRight="16dp"
	        app:layoutManager="LinearLayoutManager"
	        tools:context="com.minminaya.example.activity.ListAdapterExampleActivity"
	        tools:listitem="@layout/fragment_item" />
	</androidx.constraintlayout.widget.ConstraintLayout>
	```
	
- 4、列表如下
	![](https://upload-images.jianshu.io/upload_images/3515789-8f1f40df175ad525.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


#### 接入 RecyclerViewExposure 库

- 1、让 PlaceholderItem 类实现 `IEntityForImpr` 接口，实现 `getIdForImpr()` 方法，返回 item 的唯一标志
	```Kotlin
	    data class PlaceholderItem(val id: String, val content: String, val details: String) :
	        IEntityForImpr {
	
	        override fun toString(): String = content
	
	        override fun getIdForImpr(): String {
	            return id
	        }
	
	    }
	```
	
- 2、新建埋点帮助类 `RecyclerViewAdapterImprEventHelper` ，让其继承自 `AbsListImprEventHelper` 
	```Kotlin
	class RecyclerViewAdapterImprEventHelper(
	    recyclerView: RecyclerView,
	    componentActivity: ComponentActivity
	) : AbsListImprEventHelper<ItemRecyclerViewAdapter,
	        PlaceholderContent.PlaceholderItem>(
	    recyclerView,
	    componentActivity
	) {
	
	    /**
	     * 是否需要统计曝光事件
	     *
	     * @param entity entity
	     */
	    override fun needPostEvent(entity: PlaceholderContent.PlaceholderItem): Boolean {
	        return true
	    }
	
	    /**
	     * 当bindingAdapterPosition项曝光的时候回调
	     *
	     * @param entity                  entity
	     * @param absoluteAdapterPosition 相对 RecyclerView 的 item position
	     * @param bindingAdapterPosition  相对子 Adapter 级别 item position
	     */
	    override fun onItemExposure(
	        entity: PlaceholderContent.PlaceholderItem,
	        absoluteAdapterPosition: Int,
	        bindingAdapterPosition: Int
	    ) {
	      //上报逻辑，通常是调用某些统计 sdk
	        Log.d(
	            "RecyclerViewAdapterImprEventHelper",
	            "onItemExposure:---- absoluteAdapterPosition:$absoluteAdapterPosition ,$entity"
	        )
	    }
	
	    /**
	     * 抽象提供 Adapter 中数据集合对象
	     *
	     * @param bindingAdapterPosition sub Adapter中的位置
	     * @param viewHolder             viewHolder
	     */
	    override fun getAdapterEntityForPosition(
	        bindingAdapterPosition: Int,
	        viewHolder: RecyclerView.ViewHolder
	    ): PlaceholderContent.PlaceholderItem? {
	        //自定义返回 Adapter 中某个 item 对应的数据
	        return (viewHolder.bindingAdapter as? ItemRecyclerViewAdapter)?.let {
	            if (bindingAdapterPosition in 0 until it.dataList.size) {
	                return@let it.dataList[bindingAdapterPosition]
	            } else null
	        }
	    }
	    
	     /**
	     * 可见项批量曝光回调
	     *
	     * @param tripleList 包含entity absoluteAdapterPosition bindingAdapterPosition的数据类
	     * @apiNote entity                  entity
	     * @apiNote absoluteAdapterPosition 相对 RecyclerView 的 item position
	     * @apiNote bindingAdapterPosition  相对子 Adapter级别 item position
	     */
	    override fun onBatchItemExposure(tripleList: MutableList<Triple<PlaceholderContent.PlaceholderItem, Int, Int>>) {
	        super.onBatchItemExposure(tripleList)
	        Log.d(
	            "Event",
	            "onBatchItemExposure:---- tripleList:$tripleList"
	        )
	    }
	
	}
	```
	
	- 将 Adapter 的类声明和 Adapter Item 的数据类声明作为范型补充到 `RecyclerViewAdapterImprEventHelper` 中
		```Kotlin
		class RecyclerViewAdapterImprEventHelper(
		    recyclerView: RecyclerView,
		    componentActivity: ComponentActivity
		) : AbsListImprEventHelper<ItemRecyclerViewAdapter,
		        PlaceholderContent.PlaceholderItem>(
		    recyclerView,
		    componentActivity
		) {
		...
		```
		
		AbsListImprEventHelper 内部会判断某个曝光的 Item 是否属于指定的 Adapter 从而做前置数据过滤，非指定 Adapter 的数据曝光会被丢弃
	- 实现 `getAdapterEntityForPosition()` 
		```Kotlin
		    override fun getAdapterEntityForPosition(
		        bindingAdapterPosition: Int,
		        viewHolder: RecyclerView.ViewHolder
		    ): PlaceholderContent.PlaceholderItem? {
		        //自定义返回 Adapter 中某个 item 对应的数据
		        return (viewHolder.bindingAdapter as? ItemRecyclerViewAdapter)?.let {
		            if (bindingAdapterPosition in 0 until it.dataList.size) {
		                return@let it.dataList[bindingAdapterPosition]
		            } else null
		        }
		    }
		```
		
		这里主要目的是为了获取当前某个position 对应的 Item 数据，这里我简单通过 bindingAdapterPosition 和 Adapter 中的 ItemList 获取特定的 item 值
- 3、在 Activity 中应用 `RecyclerViewAdapterImprEventHelper` 类
	```Kotlin
	    override fun onCreate(savedInstanceState: Bundle?) {
	        super.onCreate(savedInstanceState)
	        setContentView(R.layout.activity_example)
	        val recyclerview = findViewById<RecyclerView>(R.id.list)
	        recyclerview.layoutManager = LinearLayoutManager(this)
	        val adapter = ItemRecyclerViewAdapter()
	        recyclerview.adapter = adapter
	        adapter.dataList.addAll(PlaceholderContent.ITEMS)
	        adapter.notifyDataSetChanged()
	        RecyclerViewAdapterImprEventHelper(recyclerview, this)
	    }
	```
	
- 只需要对 `RecyclerViewAdapterImprEventHelper` 进行实例化即可，无需手动维护某些组件的生命周期，框架内部自动维护
- 4、这个例子中，当列表曝光时，将输出日志
	```Kotlin
	2022-03-19 22:49:22.699 13872-13910/com.minminaya.example D/Event: onItemExposure:---- absoluteAdapterPosition:0 ,Item 1
	2022-03-19 22:49:22.699 13872-13910/com.minminaya.example D/Event: onItemExposure:---- absoluteAdapterPosition:1 ,Item 2
	2022-03-19 22:49:22.699 13872-13910/com.minminaya.example D/Event: onItemExposure:---- absoluteAdapterPosition:2 ,Item 3
	2022-03-19 22:49:22.699 13872-13910/com.minminaya.example D/Event: onItemExposure:---- absoluteAdapterPosition:3 ,Item 4
	2022-03-19 22:49:22.699 13872-13910/com.minminaya.example D/Event: onItemExposure:---- absoluteAdapterPosition:4 ,Item 5
	2022-03-19 22:49:22.699 13872-13910/com.minminaya.example D/Event: onItemExposure:---- absoluteAdapterPosition:5 ,Item 6
	2022-03-19 22:49:22.699 13872-13910/com.minminaya.example D/Event: onItemExposure:---- absoluteAdapterPosition:6 ,Item 7
	2022-03-19 22:49:22.699 13872-13910/com.minminaya.example D/Event: onItemExposure:---- absoluteAdapterPosition:7 ,Item 8
	2022-03-19 22:49:22.699 13872-13910/com.minminaya.example D/Event: onItemExposure:---- absoluteAdapterPosition:8 ,Item 9
	2022-03-19 22:49:22.699 13872-13910/com.minminaya.example D/Event: onItemExposure:---- absoluteAdapterPosition:9 ,Item 10
	2022-03-19 22:49:22.699 13872-13910/com.minminaya.example D/Event: onItemExposure:---- absoluteAdapterPosition:10 ,Item 11
	2022-03-19 22:49:22.699 13872-13910/com.minminaya.example D/Event: onItemExposure:---- absoluteAdapterPosition:11 ,Item 12
	2022-03-19 22:49:22.699 13872-13910/com.minminaya.example D/Event: onItemExposure:---- absoluteAdapterPosition:12 ,Item 13
	2022-03-19 22:49:22.699 13872-13910/com.minminaya.example D/Event: onItemExposure:---- absoluteAdapterPosition:13 ,Item 14
	2022-03-19 22:49:22.699 13872-13910/com.minminaya.example D/Event: onBatchItemExposure:---- tripleList:[(Item 1, 0, 0), (Item 2, 1, 1), (Item 3, 2, 2), (Item 4, 3, 3), (Item 5, 4, 4), (Item 6, 5, 5), (Item 7, 6, 6), (Item 8, 7, 7), (Item 9, 8, 8), (Item 10, 9, 9), (Item 11, 10, 10), (Item 12, 11, 11), (Item 13, 12, 12), (Item 14, 13, 13)]
	
	```
	
	列表曝光将按照单个用 `onItemExposure` 回调，单个回调结束后将会调用批量曝光方法 `onBatchItemExposure`

#### 其他优化

- 数据列表 Adapter 继承自 ListAdapter：
	RecyclerViewExposure 内部补充了关于 `ListAdapter` 的`getAdapterEntityForPosition()` 的方法实现，对于 `ListAdapter` 的列表曝光，我们可以直接继承 `AbsListAdapterImprEventHelper` ，补充 `needPostEvent()` 和 `onItemExposure` 方法的声明即可。
	```Kotlin
	class ListAdapterImprEventHelper(
	    recyclerView: RecyclerView,
	    fragment: Fragment
	) : AbsListAdapterImprEventHelper<ItemRecyclerViewListAdapter,
	        PlaceholderContent.PlaceholderItem>(
	    recyclerView,
	    fragment
	) {
	    override fun needPostEvent(entity: PlaceholderContent.PlaceholderItem): Boolean {
	        return true
	    }
	
	    override fun onItemExposure(
	        entity: PlaceholderContent.PlaceholderItem,
	        absoluteAdapterPosition: Int,
	        bindingAdapterPosition: Int
	    ) {
	        Log.d(
	            "ListAdapterImprEventHelper",
	            "onItemExposure:---- absoluteAdapterPosition:$absoluteAdapterPosition ,$entity"
	        )
	    }
	}
	```
	

# 三、源码实现

## 源码目录

![](https://upload-images.jianshu.io/upload_images/3515789-b1e0a721737882dc.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


- `/container`：存放了状态分发需要使用的 Activity/Fragment 容器类
- `/pagestate`：存放了状态分发需要用到的相关接口和状态枚举
- `AbsListImprEventHelper`：`RecyclerViewExposure` 库的主要逻辑实现类，承担埋点的收集曝光和曝光分发逻辑
- `AbsListAdapterImprEventHelper`：扩展自 AbsListAdapterImprEventHelper，针对 ListAdapter 类型列表封装的 EventHelper 类
## 源码分析
这里会主要说明一些主要逻辑，需要完整的逻辑可以 fork [仓库](https://github.com/minminaya/RecyclerViewExposure) 查看
### 思路说明
- 1、为 RecyclerViewExposure 库提供页面可见非可见状态监听
	页面通常分为 Activity 和 Fragment
	- Activity：只需要监听 onStart 和 onStop 即可（比如使用 LifeCycle 就可以简单的做到）
	- Fragment：由于 Fragment 有 hide 这种使用方式，Fragment 的声明周期涉及比较复杂，我们通过 `onHiddenChanged` `onResume` `onPause` 一起结合判断当前 Fragment 是否是可见状态
	- 这里会模仿 Lifecycle 的状态分发方式，新建专门用于页面可见性的生命周期 `PageLifeCycleHolder` 类，用来维护 `PageState.VISIBLE` 和 `PageState.INVISIBLE` 状态，分别表示页面当前为可见和非可见状态。
	- 缺点：模仿 `Lifecycle` 的实现，它通过在`ComponentActivity/Fragment `中维护和分发各个生命周期， 来监听`Lifecycle.State` 的变化，并且将数据变化分发给外部注册者，这种方式需要将分发代码耦合在项目基类Activity 和 Fragment 中，不优雅不易转移使用，接入成本稍微高。
	- 那么怎么解决呢？😋
		如果非要使用这种页面状态分发的形式，而且还不能改变项目原 Activity/Fragment基类的继承方式，不能编码级别的改变，那我们可以编译的时候给加上`PageLifeCycleHolder` 的状态分发代码，这种方式比较常见的做法就是在 Gradle 编译流程中，编写 Gradle 插件，自定义 Transform ，Transform 中使用 ASM/Javassist 来修改最终的class来达到类似 AOP 的目的。
	- 这里为了编译性能更好，选择 ASM 来进行代码的修改，当然 Javassist 也可以，甚至因为 Javassist API 抽象程度相当高，导致其编写成本更低。ASM 需要开发者熟悉 Class 文件体系、JVM 指令集，ASM API 的使用。不过为了 RecyclerViewExposure 不对宿主项目编译速度造成较大影响，选择使用编译速度更快的 ASM。
- 2、RecyclerView 的Item 可见项和非可见管理和收集
	- 如何满足收集条件
		- 在 RecyclerView 中，结合 `OnChildAttachStateChangeListener` 接口，这个比较容易做到，当 `OnChildAttachStateChangeListener` 接口回调 `onChildViewAttachedToWindow()` 时，记录 attached 的 item 到全局集合中，当回调 `onChildViewDetachedFromWindow()` 时将item 从全局集合中去掉，等待 attached/dettached 行为结束 600ms 后(可调整，这里视为 item 被看到 600ms 才算是曝光)，对集合中的剩余 item 触发上报曝光的逻辑
	- 可管理收集 Item 条件？（item 可见百分比等）
		- 因为我们收集到全局列表之前，我们可以从 viewholder 中拿到 view，我们可以通过判断列表可见的第一个 item 和列表可见的最后一个 item 的 View坐标范围和 RecyclerView 自身的 View 坐标范围计算，判断第一个和最后一个 item 的可见区间是否满足可见大于某个百分比【这个特性因为比较耗费计算型性能，RecyclerViewExposure 没有支持，思路共参考】
- 3、使用方法优化精简
	- 考虑到应用程要开放些什么信息
	- 通过范型减少类抽象方法

### 源码设计

#### 1、为 RecyclerViewExposure 库提供页面可见非可见状态监听

思路来自于 lifecycle 的设计，这里主要是想让 Activity/Fragment 提供可见和不可见的状态变化给外部订阅

- 可见，不可见状态定义到 PageState 枚举中
	```Kotlin
	enum class PageState(val number: Int) {
	    /**
	     * 可见状态
	     */
	    VISIBLE(2),
	
	    /**
	     * 不可见状态
	     */
	    INVISIBLE(3),
	}
	```
	
- 定义 PageLifeCyclerHolder，它的职责是分发管理 PageState 的状态
	```Kotlin
	class PageLifeCycleHolder(private val lifecycle: Lifecycle) : LifecycleObserver,
	    IPageStateObserver {
	
	    var pageState: PageState = PageState.INVISIBLE
	
	    private val pageLifeCycleObserverList by lazy {
	        return@lazy mutableListOf<IPageLifeCycleObserver>()
	    }
	
	    /**
	     * 内部会自动解绑
	     *
	     * @param observer IPageLifeCycleObserver
	     */
	    @MainThread
	    fun addPageObserver(observer: IPageLifeCycleObserver) {
	        if (pageLifeCycleObserverList.contains(observer)) {
	            return
	        }
	        pageLifeCycleObserverList.add(observer)
	    }
	
	    @MainThread
	    fun removePageObserver(observer: IPageLifeCycleObserver) {
	        pageLifeCycleObserverList.remove(observer)
	    }
	
	    private fun onDestroy() {
	        pageLifeCycleObserverList.forEach {
	            it.onDestroy()
	        }
	        lifecycle.removeObserver(this)
	        pageLifeCycleObserverList.clear()
	    }
	
	    override fun onPageState(pageState: PageState) {
	        if (this.pageState == pageState) {
	            //避免相同状态回调多次
	            return
	        }
	        this.pageState = pageState
	        pageLifeCycleObserverList.forEach {
	            it.onPageState(pageState)
	            when (pageState) {
	                PageState.VISIBLE -> {
	                    it.onPageVisible()
	                }
	                PageState.INVISIBLE -> {
	                    it.onPageInvisible()
	                }
	            }
	        }
	    }
	
	    init {
	        lifecycle.addObserver(object : DefaultLifecycleObserver {
	            override fun onDestroy(owner: LifecycleOwner) {
	                super.onDestroy(owner)
	                onDestroy()
	            }
	        })
	    }
	
	}
	```
	
	主要实现在 `onPageState(pageState: PageState)` ，供外部页面容器 `Activity/Fragment` 控制页面的可见和不可见状态，当状态发生变化，那么将状态分发给订阅了状态变化的各处地方。
- 在项目的 Activity 基类中补充 PageLifeCycleHolder 全局变量和通过 onStart 和 onStop 分发状态
	```Kotlin
	public class BaseActivity extends AppCompatActivity implements IPageStateLifecycleOwner {
	
	    private PageLifeCycleHolder mPageLifeCycleHolder;
	
	    @NonNull
	    @Override
	    public PageLifeCycleHolder getPageStateLifecycle() {
	        if (mPageLifeCycleHolder == null) {
	            initPageLifeCycleHolder();
	        }
	        return mPageLifeCycleHolder;
	    }
	
	    @Override
	    public void initPageLifeCycleHolder() {
	        mPageLifeCycleHolder = new PageLifeCycleHolder(getLifecycle());
	    }
	
	    @CallSuper
	    @Override
	    protected void onCreate(@Nullable Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        initPageLifeCycleHolder();
	    }
	
	    @Override
	    protected void onResume() {
	        super.onResume();
	        getPageStateLifecycle().onPageState(PageState.VISIBLE);
	    }
	
	    @Override
	    protected void onStop() {
	        super.onStop();
	        getPageStateLifecycle().onPageState(PageState.INVISIBLE);
	    }
	}
	```
	
	那有的同学就问了，你这？？？？我用个库还得改 Activity 基类？？？另外上面目录中的 WrapExposureActivity 是干什么用的呢？
	![](https://upload-images.jianshu.io/upload_images/3515789-e24110ecd313df28.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

	确实，用个库还改基类确实挺爽(keng)的，为此我打算使用 ASM 编译插入代码的方式来补充上述基类的代码到 ComponentActivity 中
	插入代码有两种方式：
- 1、通过修改 `ComponentActivity` 的 `onResume` 方法和 `onStop` 方法的实现已经让 `ComponentActivity` 实现 `IPageStateLifecycleOwner` 接口达到目的
- 2、通过新建一个 `WrapExposureActivity` 继承自 `ComponentActivity` 类，将上述基类中的代码补充到此，编译过程将继承了 `ComponentActivity` 的类，全部修改为 `WrapExposureActivity`，相当于强行在继承关系中插了一腿子 😈
- 3、考虑到`Fragment` 也是类似的方案进行基类代码补充，`Fragment` 相关的逻辑还是稍微有点复杂的，全部改为 ASM 的方式实现会比较麻烦，而且为了防止后续 Android 版本的 `Activity/Fragment` API更新导致 ASM 插桩失败，这里选用方案 2，将基类代码写好在一个类中，通过 ASM 修改继承关系最终达到基类拥有 `PageLifeCycleHolder` 的目的。
- 新建 WrapComponentActivity，主要是上述 BaseActivity 的代码，用于后续给Gradle ASM 插桩修改 Activity 的继承关系使用
	```Kotlin
	public class WrapExposureActivity extends ComponentActivity implements IPageStateLifecycleOwner {
	
	    ... 省略相关实现，见上面的 BaseActivity
	
	}
	```
	
- 新建 WrapExposureFragment，其作用类似 WrapComponentActivity，用于后续给Gradle ASM 插桩修改 Fragment的继承关系使用
	```Kotlin
	public class WrapExposureFragment extends Fragment implements IPageStateLifecycleOwner {
	
	    /**
	     * 曾经有显示过界面
	     */
	    protected boolean hasResume = false;
	
	    private PageLifeCycleHolder mPageLifeCycleHolder;
	
	    @Override
	    public void onResume() {
	        super.onResume();
	        if (!isHidden()) {
	            onFragmentVisible(true);
	        }
	        hasResume = true;
	    }
	
	    @Override
	    public void onPause() {
	        super.onPause();
	        if (!isHidden()) {
	            onFragmentVisible(false);
	        }
	        hasResume = false;
	    }
	
	    @Override
	    public void onHiddenChanged(boolean hidden) {
	        super.onHiddenChanged(hidden);
	        if (hasResume) {
	            onFragmentVisible(!hidden);
	        }
	    }
	
	    /**
	     * @param isVisible true 代表显示
	     */
	    @CallSuper
	    protected void onFragmentVisible(boolean isVisible) {
	        if (isVisible) {
	            getPageStateLifecycle().onPageState(PageState.VISIBLE);
	        } else {
	            getPageStateLifecycle().onPageState(PageState.INVISIBLE);
	        }
	    }
	
	    @NonNull
	    @Override
	    public PageLifeCycleHolder getPageStateLifecycle() {
	        if (mPageLifeCycleHolder == null) {
	            initPageLifeCycleHolder();
	        }
	        return mPageLifeCycleHolder;
	    }
	
	    @Override
	    public void initPageLifeCycleHolder() {
	        mPageLifeCycleHolder = new PageLifeCycleHolder(getLifecycle());
	    }
	
	    @Override
	    public void onCreate(@Nullable Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        initPageLifeCycleHolder();
	    }
	
	}
	```
	
	`Fragment` 的可见和不可见状态分发依靠 `onHiddenChanged` 和 `onResume` `onPause` 的结合达到目的，最终通过 `onFragmentVisible` 来对 `PageLifeCycleHolder` 分发状态
- 新建 Gradle 插件，通过ASM 修改继承关系
	```Kotlin
	@AutoService(ClassTransformer::class)
	class PageLifeCycleHolderTransformer : ClassTransformer {
	
	    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
	        //忽略 WrapExposureFragment 和 WrapExposureActivity
	        if (klass.name in IGNORE_CLASS_NAME_LIST) {
	            return klass
	        }
	
	        //将继承自 androidx/activity/ComponentActivity 的类的父类改为 IGNORE_ACTIVITY_NAME
	        if (klass.superName == "androidx/activity/ComponentActivity") {
	            klass.superName = IGNORE_ACTIVITY_NAME
	        }
	
	        //将继承自 androidx/fragment/app/Fragment 的类的父类改为 IGNORE_FRAGMENT_NAME
	        if (klass.superName == "androidx/fragment/app/Fragment") {
	            klass.superName = IGNORE_FRAGMENT_NAME
	        }
	        return klass
	    }
	
	    companion object {
	        private const val IGNORE_FRAGMENT_NAME =
	            "com/minminaya/exposure/container/WrapExposureFragment"
	        private const val IGNORE_ACTIVITY_NAME =
	            "com/minminaya/exposure/container/WrapExposureActivity"
	
	        private val IGNORE_CLASS_NAME_LIST = listOf(
	            IGNORE_ACTIVITY_NAME,
	            IGNORE_FRAGMENT_NAME,
	        )
	    }
	
	}
	```
	
	主要就是将继承自 `ComponentActivity` 和 `Fragment` 的类的继承关系改为 `WrapExposureActivity`/`WrapExposureFragment` ，相比直接对 `ComponentActivity` 和 `Fragment` 直接插入代码简单和稳定。

#### 2、RecyclerView 的Item 可见项和非可见管理和收集

对 List Item 的收集处理是 RecyclerViewExposure 最核心的收集数据逻辑，这里针对在 Activity 的使用作为例子。上文已经讲述如何做一个 `PageLifeCycleHolder` 为其他组件提供页面可见状态，下文将直接使用。

- 1、新建曝光埋点帮助类 AbsListImprEventHelper，传入两个范型，L 代表当前使用的列表的实际 Adapter，T 代表当前列表使用的数据
	```Kotlin
	public abstract class AbsListImprEventHelper<L extends RecyclerView.Adapter<?>, T extends IEntityForImpr>
	        implements
	        IListImpEventHelper,
	        IPageLifeCycleObserver, RecyclerView.OnChildAttachStateChangeListener {
	        
	}
	```
	
	RecyclerViewExposure 在收集数据的过程中会使用范型 L 来过滤RecyclerView 中 L 类型 Adapter的子项数据
	```Kotlin
	    /**
	     * @return 提供待统计的目标Sub Adapter class类型
	     */
	    @SuppressWarnings("unchecked")
	    @NotNull
	    public Class<?> getRecyclerViewSubAdapterClazz() {
	        if (mRecyclerViewAdapterClass != null) {
	            return mRecyclerViewAdapterClass;
	        }
	        Type type = getClass().getGenericSuperclass();
	        try {
	            Type[] parameter = ((ParameterizedType) type).getActualTypeArguments();
	            mRecyclerViewAdapterClass = (Class<L>) parameter[0];
	            return mRecyclerViewAdapterClass;
	        } catch (Exception exception) {
	            exception.printStackTrace();
	            return Object.class;
	        }
	    }
	    
	    private boolean isBindingAdapter(RecyclerView.ViewHolder viewHolder) {
	        if (viewHolder == null) {
	            return false;
	        }
	        return viewHolder.getBindingAdapter() != null
	                && viewHolder.getBindingAdapter().getClass() == getRecyclerViewSubAdapterClazz();
	    }
	```
	
	通过获取 Class 的第一个范型类型拿到 L 对应的 Class 对象，收集数据过程中，通过判断 `isBindingAdapter()` 来过滤出对应 `Adapter` 的数据，这也是 `RecyclerViewExposure` 库兼容 `ConcatAdapter(MergeAdapter)` 的原因
- 3、构造方法初始化相关监听器
	```Kotlin
	    protected AbsListImprEventHelper(@NonNull RecyclerView recyclerView,
	                                     @NonNull ComponentActivity componentActivity) {
	        this.mRecyclerView = recyclerView;
	        PageLifeCycleHolder pageLifeCycleHolder;
	        if (componentActivity instanceof IPageStateLifecycleOwner) {
	            IPageStateLifecycleOwner pageStateLifecycleOwner = (IPageStateLifecycleOwner) componentActivity;
	            pageLifeCycleHolder = pageStateLifecycleOwner.getPageStateLifecycle();
	        } else {
	            throw new RuntimeException(
	                    "please add below classpath to build.gradle at project root.\n" +
	                            "\"com.didiglobal.booster:booster-gradle-plugin:{booster-gradle-plugin-version}\"\n" +
	                            ",\"com.minminaya:exposure-plugin:{exposure-plugin-version}\"");
	        }
	        init(pageLifeCycleHolder);
	    }
	    
	   private void init(@NonNull PageLifeCycleHolder pageLifeCycleHolder) {
	        pageLifeCycleHolder.addPageObserver(this);
	        if (pageLifeCycleHolder.getPageState() == PageState.VISIBLE) {
	            onPageStart();
	            checkAndPostEvent(mRecyclerView);
	        }
	    }
	    
	   private void onPageStart() {
	        if (mRecyclerView != null && !isAddOnChildAttachStateChangeListener) {
	            isAddOnChildAttachStateChangeListener = true;
	            mRecyclerView.addOnChildAttachStateChangeListener(this);
	        }
	    }
	```
	
	构造方法主要选择在列表可见的时候初始化 `OnChildAttachStateChangeListener` 接口和初始化时进行一次**检查上报逻辑**
- 4、检查上报逻辑 `checkAndPostEvent(mRecyclerView)`
	```Kotlin
	    public void checkAndPostEvent(RecyclerView recyclerView) {
	        if (recyclerView == null) {
	            return;
	        }
	        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
	        if (layoutManager == null) {
	            return;
	        }
	        int newFirstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
	        int newLastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
	
	        if (newFirstVisibleItemPosition == -1 || newLastVisibleItemPosition == -1) {
	            return;
	        }
	        //这里可以插入判断第一个 item 和最后一个 item 可见百分比的逻辑
	        for (int i = newFirstVisibleItemPosition; i <= newLastVisibleItemPosition; i++) {
	            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
	            if (viewHolder == null) {
	                continue;
	            }
	            int bindingAdapterPosition = viewHolder.getBindingAdapterPosition() - getHeaderPositionCount();
	            int absoluteAdapterPosition = viewHolder.getAbsoluteAdapterPosition() - getHeaderPositionCount();
	            if (bindingAdapterPosition < 0 || absoluteAdapterPosition < 0 || !isBindingAdapter(viewHolder)) {
	                continue;
	            }
	            T entity = getAdapterEntityForPosition(bindingAdapterPosition, viewHolder);
	            if (entity != null && needPostEvent(entity)) {
	                putEntity(entity, absoluteAdapterPosition, bindingAdapterPosition);
	            }
	        }
	    }
	```
	
	这里主要是判断当前 RecyclerView 中第一个可见项和最后一个可见项的区间，将区间内的 item 通过调用 `putEntity` 收集到待上报集合中。这里可以插入 item 是否满足可见条件的逻辑（`putEntity()`调用之前即可），判断第一个 item 和最后一个 item 可见百分比即可，比如获取 `newFirstVisibleItemPosition` item 后，通过调用`recyclerView.findViewHolderForAdapterPosition(newFirstVisibleItemPosition)` 获取它的 `ViewHolder` 从而获取 View，通过 `view.getGlobalVisibleRect()` 方法获取其所在 `Rect` 位置，通过与 `RecyclerView` 容器的 `Rect` 对比，可知当前状态下，`newFirstVisibleItemPosition` 这个 `item` 可见百分比是多少（当然这是一个比较耗费性能的操作）。RecyclerViewExposure 选择不支持该功能，有需要的同学可以自己扩展实现。
- 5、`putEntity()` ：收集数据方法
	```Kotlin
	
	    /**
	     * 数据定义
	     *
	     * @apiNote Triple 是包含 entity absoluteAdapterPosition bindingAdapterPosition 的数据类
	     * @apiNote entity                  entity，列表 Item 的数据
	     * @apiNote absoluteAdapterPosition 相对 RecyclerView 的 item position
	     * @apiNote bindingAdapterPosition  相对子 Adapter级别 item position
	     */
	    private final Map<String, Triple<T, Integer, Integer>> mPostEventDataHashMap = new LinkedHashMap<>();
	    
	    /**
	     * 发送事件的 Runnable
	     */
	    private final Runnable mPostEventRunnable = this::postEvent;
	    
	    /**
	     * @param entity                  entity
	     * @param absoluteAdapterPosition 相对RecycleView的位置
	     */
	    private void putEntity(@NonNull T entity, int absoluteAdapterPosition, int bindingAdapterPosition) {
	        String id = entity.getIdForImpr();
	//        Log.d(TAG, "putEntity--id:" + id + ", bindingAdapterPosition:" + bindingAdapterPosition);
	        if (TextUtils.isEmpty(id)) {
	            return;
	        }
	        mPostEventDataHashMap.put(id, new Triple<>(entity, absoluteAdapterPosition, bindingAdapterPosition));
	        UIHelper.removeCallback(mPostEventRunnable);
	        UIHelper.runOnUiThreadDelay(mPostEventRunnable, POST_EVENT_DEBOUNCE);
	    }
	```
	
	符合一定条件之后，putEntity 会被调用，将数据塞到 mPostEventDataHashMap 中，同时开启一个定时，时间为 600ms，结束将调用 `mPostEventRunnable` 去执行 `postEvent()` 从而发送事件。
- 6、`postEvent()` ：发送曝光事件
	```Kotlin
	    private void postEvent() {
	        Map<String, Triple<T, Integer, Integer>> backupMap = new LinkedHashMap<>(mPostEventDataHashMap);
	        mPostEventDataHashMap.clear();
	        ThreadHelper.executeExposureSingleTask(() -> {
	            List<Triple<T, Integer, Integer>> tripleList = new ArrayList<>();
	
	            for (Map.Entry<String, Triple<T, Integer, Integer>> stringPairEntry : backupMap.entrySet()) {
	                Triple<T, Integer, Integer> value = stringPairEntry.getValue();
	                T entity = value.getFirst();
	                if (entity == null) {
	                    continue;
	                }
	                //单个曝光
	                onItemExposure(entity, value.getSecond(), value.getThird());
	                tripleList.add(value);
	            }
	            //批量曝光
	            onBatchItemExposure(tripleList);
	        });
	    }
	```
	
	延迟结束时将执行发送数据的逻辑，主要是遍历 `mPostEventDataHashMap` 集合，将数据通过 `onItemExposure()` 进行单个曝光和批量曝光 `onBatchItemExposure()` 
- 7、`onChildViewAttachedToWindow()` ：RecyclerView Item View 首次加载到屏幕触发
	```Kotlin
	    @Override
	    public void onChildViewAttachedToWindow(@NonNull View view) {
	        if (mRecyclerView == null) {
	            return;
	        }
	        RecyclerView.ViewHolder viewHolder = mRecyclerView.findContainingViewHolder(view);
	        if (viewHolder == null) {
	            return;
	        }
	        int bindingAdapterPosition = -1;
	
	        try {
	            bindingAdapterPosition = viewHolder.getBindingAdapterPosition() - getHeaderPositionCount();
	        } catch (Exception exception) {
	            exception.printStackTrace();
	        }
	
	        int absoluteAdapterPosition = viewHolder.getAbsoluteAdapterPosition() - getHeaderPositionCount();
	        if (bindingAdapterPosition < 0 || absoluteAdapterPosition < 0) {
	            return;
	        }
	
	        if (isBindingAdapter(viewHolder)) {
	            T entity = getAdapterEntityForPosition(bindingAdapterPosition, viewHolder);
	            if (entity == null) {
	                return;
	            }
	            if (needPostEvent(entity)) {
	                putEntity(entity, absoluteAdapterPosition, bindingAdapterPosition);
	            }
	        }
	    }
	```
	
	其实简简单单的获取指定 view 数据和添加可见数据到集合，需要注意的是，假设需要要求 view 曝光百分之 xx 才算曝光，那么在 putEntity 之前需要判断当前 item 相对于 RecyclerView 的百分比
- 8、`onChildViewDetachedFromWindow()` 
	```Kotlin
	    @Override
	    public void onChildViewDetachedFromWindow(@NonNull View view) {
	        if (mRecyclerView == null) {
	            return;
	        }
	        RecyclerView.ViewHolder viewHolder = mRecyclerView.findContainingViewHolder(view);
	        if (viewHolder == null) {
	            return;
	        }
	        int bindingAdapterPosition = viewHolder.getBindingAdapterPosition() - getHeaderPositionCount();
	        int absoluteAdapterPosition = viewHolder.getAbsoluteAdapterPosition() - getHeaderPositionCount();
	        if (bindingAdapterPosition < 0 || absoluteAdapterPosition < 0) {
	            return;
	        }
	
	        if (isBindingAdapter(viewHolder)) {
	            T entity = getAdapterEntityForPosition(bindingAdapterPosition, viewHolder);
	            if (entity == null) {
	                return;
	            }
	            removeEntity(entity);
	        }
	    }
	```
	
	这里主要是 `removeEntity()` 逻辑，在 view 移开屏幕的时候触发，并且这里会重置 `postEvent()` 的倒计时
	```Kotlin
	    /**
	     * @param entity entity
	     */
	    private void removeEntity(T entity) {
	        mPostEventDataHashMap.remove(entity.getIdForImpr());
	        UIHelper.removeCallback(mPostEventRunnable);
	        UIHelper.runOnUiThreadDelay(mPostEventRunnable, POST_EVENT_DEBOUNCE);
	    }
	```
	

#### 3、针对 ListAdapter 精简使用方法

- 由于 ListAdapter 的数据源固定为 `getItem()` ，RecyclerViewExposure 扩展了 `AbsListAdapterImprEventHelper` 类，使 ListAdapter 的列表曝光只需要关注 needPostEvent() 和相关曝光方法

```Kotlin
abstract class AbsListAdapterImprEventHelper<L : ListAdapter<T, RecyclerView.ViewHolder>, T : IEntityForImpr> :
    AbsListImprEventHelper<L, T> {

    constructor(
        recyclerView: RecyclerView,
        componentActivity: ComponentActivity
    ) : super(recyclerView, componentActivity)

    constructor(
        recyclerView: RecyclerView,
        fragment: Fragment
    ) : super(recyclerView, fragment)

    @Suppress("UNCHECKED_CAST", "IMPLICIT_NOTHING_TYPE_ARGUMENT_AGAINST_NOT_NOTHING_EXPECTED_TYPE")
    override fun getAdapterEntityForPosition(
        bindingAdapterPosition: Int,
        viewHolder: RecyclerView.ViewHolder
    ): T? {
        return (viewHolder.bindingAdapter as? L)?.let {
            it.currentList.run {
                if (bindingAdapterPosition >= this.size) {
                    return null
                }
                return this[bindingAdapterPosition]
            }
        }
    }

}
```


- `AbsListAdapterImprEventHelper` 的一个使用例子

```Kotlin
class ListAdapterImprEventHelper(
    recyclerView: RecyclerView,
    fragment: Fragment
) : AbsListAdapterImprEventHelper<ItemRecyclerViewListAdapter,
        PlaceholderContent.PlaceholderItem>(
    recyclerView,
    fragment
) {
    override fun needPostEvent(entity: PlaceholderContent.PlaceholderItem): Boolean {
        return true
    }

    override fun onItemExposure(
        entity: PlaceholderContent.PlaceholderItem,
        absoluteAdapterPosition: Int,
        bindingAdapterPosition: Int
    ) {
        Log.d(
            "ListAdapterImprEventHelper",
            "onItemExposure:---- absoluteAdapterPosition:$absoluteAdapterPosition ,$entity"
        )
    }

}
```


# 四、总结

---

-  Gradle 插件编译插桩（ASM/Javassist） 是很强大的工具，除了本篇提到的替换继承类的功能外，只能用为所欲为来形容它们了。比如常见的项目问题线程池问题的治理，测试mock 数据，ARouter 中路由表的生成和初始化，AndResGuard 中对资源路径的缩减，对三方库中混乱调用系统 API 获取敏感信息进行治理等等。ASM 性能优秀但是入手难度大，需要开发者熟悉 Class 文件体系，JVM 指令集，ASM API 的使用（Visitor 模式），这也要求开发者需要有相关的基础知识储备，不然很容易玩不下去。



#### 其他

[博客链接](https://www.jianshu.com/p/0f6ad1ae5e2b)

# 开源协议

无
