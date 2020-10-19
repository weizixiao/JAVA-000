## JAVA核心技术--工具与GC策略
- JDK内置命令行工具
- JDK内置图形化工具
- GC的背景与一般原理
- 串行GC/并行 GC*（serial GC/Parallel GC）
- CMS GC/G1 GC*
- ZGC/Shenandoah GC

#### JDK内置命令行工具
- java：Java应用启动程序
- javac：JDK内置的编译工具
- javap：反编译class工具
- jar：打包jar工具，可以将文件和目录打包成jarw文件，本质就是zip文件

- jps/jinfo：查看java进程
  - jsp
  ```
     417 
     933 Main
     85212 Jps
     数字是进程id，后续是名字
  ```
    - jps -lmv 详细信息
    - jinfo id   id就是进程id

------

- jstat：查看JVM内部gc相关信息

```
jstat -gc 417 1000 1000(417是进程id，1000 1000是没秒打一次，一共1000次)
```

| S0C     | S1C     | S0U  | S1U    | EC       | EU       | OC       | OU       | MC       | MU       | CCSC    | CCSU    | YGC  | YGCT   | FGC  | FGCT   | GCT                    |
| ------- | ------- | ---- | ------ | -------- | -------- | -------- | -------- | -------- | -------- | ------- | ------- | ---- | ------ | ---- | ------ | ---------------------- |
| 34048.0 | 34048.0 | 0.0  | 2062.0 | 272640.0 | 100813.6 | 707840.0 | 411750.6 | 497908.0 | 478575.7 | 64880.0 | 57589.7 | 4777 | 68.153 | 2    | 28.353 | 96.506上面表格解释数据 |

- S0C：0号存活区的当前容量（capacity），单位kb

- S1C：1号存活区的当前容量，单位KB

- S0U：0号存活区的使用量，单位KB

- S1U：1号存活区的使用量，单位KB

- EC：Eden区，新生代的当前容量，单位KB

- EU：Eden区，新生代当前使用量，单位KB

- OC：Old区，老年代的当前容量，单位KB
- OU：Old区，老年代的当前使用量，单位KB（important）
- MC：元数据库的容量，单位KB
- MU：元数据区的使用量，单位KB
- CCSC：压缩的class空间容量，单位KB
- CCSU：压缩的class空间使用量，单位KB
- YGC：年轻代GC的次数
- YGCT：年轻代GC消耗的总时间（important）
- FGC：Full GC的次数
- FGCT：FullGC消耗的时间（important）
- GCT：垃圾收集消耗的总时间

```
命令:jstat -gcutil 417 1000 1000
```

这条命了查看数据会简化很多，这样输出的都是使用率的数据

------

- jmap：查看heap或类占用空间信息
  - -heap打印堆内存（/内存池）的配置和使用信息：jmap -heap pid
  - -histo看哪些类占用的空间最多，直方图：jmap -histo pid
  - dump：format=b,file=xxxx.hprof Dump堆内存：jmap -dump：format=b,file=3826.hprof3826
- jstack：查看线程信息
  - jstack -l pid
- jcmd：执行JVM相关分析命令（整合命令）
  - jcmd pid help 打印出整合命令
  - 根据上面打印出的信息可以执行其中命令：jcmd pid [打出info]
- jrunscript/jjs：执行js命令
  - jrunscript -e "cat('http://www.baidu.com')"



#### JDK内置图形化工具

- 在命令行中输入jconsole
- 在命令行中输入jvisualvm
- 在IDEA中点Preferences->plugins->搜索GC->安装VisualGC，在IDEA右下角有一个VisualGC，点开面板
- 在命令行中输入jmc

#### GC的背景与一般原理

为什么有gc，本质上是内存资源有限，因此需要大家共享使用，手工申请，手动释放



GC一般原理是用引用计数，改进方案为引用跟踪

###### 标记清除算法

- Marking（标记）：遍历所有的可达对象，并在本地内存中分门别类记下
- Sweeping（清除）：这一步保证了，不可达对手所占用的内存，在之后尽心内存分配时可以重用
- 压缩整理：把不连续的内存整理成连续的空间

**并行GC和CMS的基本原理都是引用跟踪（上面的标记清除）：优势，可以处理循环依赖，只扫描部分对象**

STW，类似于快照中标记，清除，整理，然后在重新进入程序执行状态



GC的分代假设，大部分新生对象很快无用；在新生代中，一般存活15代不被回收的，会进入老年代

![](/Users/weizi/Desktop/javaStudy/JAVA-000/Week_01/images/栈内存划分.png)

内存池划分：不同类型的对象不同区域，不同策略处理

![](/Users/weizi/Desktop/javaStudy/JAVA-000/Week_01/images/GC内存方向.png)
对象分配在新生代的Eden区，标记阶段Eden存活的对象就会复制到存活区；两个存活区from和to，互换角色，对象存活到一定周期会提升到老年代

标记次数15是可以自己控制参数的，-XX: +MaxTenuringThreshold=15

老年代默认都是存活对象，采用移动方式：

- 标记所有通过GC roots可达的对象
- 删除所有不可达对象
- 整理老年代空间中的内容，方法是将所有的存活对象复制，从老年代空间开始的地方依次存放

持久带/元数据区

- 1.8之前 -XX:MaxPermSize=256m
- 1.8之后 -XX:MaxMetaspaceSize=256m

![](/Users/weizi/Desktop/javaStudy/JAVA-000/Week_01/images/GC的STW图.png)

可以作为GC Roots的对象

- 当前正在执行的方法里的局部变量和输入参数
- 活动现场
- 所有类的静态字段
- JNI引用

此阶段暂停的时间，与堆内存大小，对象的总数没有直接关系，而是由存活对象的数量来决定。所有增加堆内存的大小并不会直接影响标记阶段占用的时间

**年轻代用复制，老年代用移动，分别有什么好处？**



#### 串行GC/并行 GC*（serial GC/Parallel GC）

###### 串行GC

- -XX: +UseSerialGC 配置串行GC

串行GC堆年轻代使用mark-copy（标记-复制）算法，对老年代使用mark-sweep-compact（标记-清除-整理）算法。

两者都是单线程的垃圾收集器，不能进行多并行处理，所以都会触发全线暂停（STW），停止所有的应用线程。

因此这种GC算法不能充分利用多核CPU。不管有多少CPU，JVM的垃圾收集器时都只能使用单个核心。

CPU利用率高，暂停时间长。简单粗暴，就像老式的电脑，动不动就卡死。

-XX: +UseParNewGC 改进版的Serial GC，可以配合CMS使用

###### 并行 GC

- -XX: +UseParallelGC
- -XX: +UseParallelOldGC
- -XX: +UseParallelGC -XX: +UseParallelOldGC

年轻代和老年代的垃圾回收都会触发STW事件。

在年轻代使用标记-复制（mark-copy）算法，在老年代使用 标记-清除-整理（mark-sweep-compact）算法。

**-XX: ParallelGCThreads=N 来制定GC线程数，其默认值为CPU核心数**

并行垃圾收集器使用多核服务器，主要目标是增加吞吐量。因为对系统资源的有效使用，能达到更高吞吐量：

- 在GC期间，所有CPU内核都在并行清理垃圾，所有总暂时时间更短
- 在两次GC周期的间隔期，没有GC线程在运行，不会消耗任何系统资源

#### CMS GC/G1 GC*

CMS GC（Mostly Concurrent Mark and Sweep Garbage Collector）

8及8以前的JDK版本都是并行 GC，8以后是CMS GC/G1 GC

-XX: +UseConcMarkSweepGC

其对年轻代采用并行STW方式的mark-copy（标记-复制）算法，对老年代主要使用并发mark-sweep（标记-清除）算法。

CMS GC 的设计目标是避免在老年代垃圾收集时出现长时间的卡顿，主要通过两种手段来达成次目标：

- 不对老年代进行整理，而是使用空闲列表（free-lists）来管理，内存空间的回收
- 在mark-and-sweep（标记-清除）阶段的大部分工作和应用线程一起并发执行。

也就是说，在这些阶段并没有明显的应用线程暂停。但值得注意的是，它仍然和应用线程争抢CPU时间。默认情况下，CMS使用的并发线程数等于CPU核心数的1/4.

如果服务器是多核CPU，并且注意调优目标是降低GC停顿导致的系统延迟，那么使用CMS是个明智的选择。进行老年代的并发回收时，可能会伴随着多次年轻的的minor GC。

**CMS GC—六个阶段（STW）**

- 阶段1：initial Mark（初始标记）
- 阶段2：Concurrent Mark（并发标记）
- 阶段3：Concurrent Preclean（并发预清理）
- 阶段4：Final Remark（最终标记）
- 阶段5：Concurrent Sweep（并发清除）
- 阶段6：Concurrent Reset（并发重置）

**阶段1伴随着STW暂停。出事标记的目标是标记所有的根对象，包括根对象直接引用的对象，以及被年轻代中所有存活对象所引用的对象（老年代单独回收）**

------

**G1 GC**

G1的全称是Garbage-FIrst，意为垃圾优先，哪一块的垃圾最多就优先清理他。

G1 GC最主要的设计目标是：将STW停顿的时间和分布，变为可预期且可配置的。

事实上，G1 GC是一款软实时垃圾收集器，可以为其设置某项特点的性能指标。为了达到可预期停顿的指标，G1 GC有一些独特的实现。

首先，堆不再分成年轻代和老年代，而是划分为多个（通常是2048个）可以存放对象的小块堆区域（smaller heap regions）。每个一块，可能一会被定义成Eden区，一会被指定为SUrvivor区或者Old区。在逻辑上，所有的Eden区和SUrvivor区合起来就是年轻代，所有的Old区拼在一起那就是老年代。

**-XX:+UseG1GC -XX:MaxGCPauseMillis=50 **

###### G1 GC— 配置参数

- -XX: +UseG1GC：启动G1 GC
- -XX: G1NewSizePercent：初始年轻代占整个Java Heap的大小，默认为5%
- -XX: G1MaxNewSizePercent：最大年轻代占整个Java Heap的大小，默认值为60%
- -XX: G1HeapRegionSize：设置每个Region的大小，单位MB，需要为1，2，4，8，16，32中的某个值，默认是堆内存1/2000.如果这个值设置比较大，那么大对象就可以进入Region了
- -XX: ConcGCThreads：与Java应用一起执行的GC线程，默认Java线程的1/4，减少这个参数的数值可能会提升并回收的效率，提高系统内部吞吐量。如果这个数值过低，参与回收垃圾线程不足，也会导致并行回收机制耗时加长
- -XX: +InitiatingHeapOccupancyPercent （简称IHOP）：G1内部并行回收循环启动的阙值，默认为Java Heap的45%。这个可以理解为老年代使用大于等于45%的时候，JVM会启动垃圾回收。这个值非常重要，它决定了在什么时间启动老年代的并行回收
- -XX: G1HeapWastePercent：G1停止回收的最小内存大小，默认是堆大小的5%。GC会收集所有的Region中的对象，但是如果下降到5%，就会停下来不再收集了。就是护送，不必每次回收就把所有的垃圾都处理完，可以遗留少量的下次处理，这样也就降低了单词消耗的时间
- -XX:G1MixedGCConutTarget：设置并行循环之后需要有多少个混合GC启动，默认值是8个。老年代Regions的回收时间通常比年轻代的手机时间要长一些。所有如果混合收集器比较多，可以允许G1延长老年代的收集时间

。。。

#### 各GC对比

| 收集器            | 串行、并行or并发 | 新生代/老年代 | 算法               | 目标         | 适应场景                                |
| ----------------- | ---------------- | ------------- | ------------------ | ------------ | --------------------------------------- |
| Serial            | 串行             | 新生代        | 复制算法           | 响应速度优先 | 单CPU环境下的Client模式                 |
| Serial Old        | 串行             | 老年代        | 标记-整理          | 响应速度优先 | 单CPU环境下的Client模式、CMS的后备预案  |
| ParNew            | 并行             | 新生代        | 法制算法           | 响应速度优先 | 多CPU环境时再Server模式下与CMS配合      |
| Parallel Scavenge | 并行             | 新生代        | 复制算法           | 吞吐量优先   | 在后台运算而不需要太多交互的任务        |
| Parallel Old      | 并行             | 老年代        | 标记-整理          | 吞吐量优先   | 在后台运算而不需要太多交互的任务        |
| CMS               | 并发             | 老年代        | 标记-清除          | 响应速度优先 | 几种在互联网站或B/S系统服务上的Java应用 |
| G1                | 并发             | both          | 标记-整理+复制算法 | 响应速度优先 | 面向服务的应用，将来替换CMS             |

##### 常用的组合

- Serial + Serial Old 实现单线程的低延迟垃圾回收机制
- ParNew+CMS，实现多线程的低延迟垃圾回收机制
- Parallel Scavenge和Parallel Scavenge Old，实现多线程的高吞吐量垃圾回收机制

##### 如何选择GC

选择正确的GC算法，唯一可行的方式就是去尝试，一般性的指导原则：

- 如果系统考虑吞吐优先，CPU资源都用来最大程序处理业务，用Parallel GC；
- 如果系统考虑低延迟有限，每次GC时间尽量短，用CMS GC；
- 如果系统内存堆较大，同时希望整体来看平均GC时间可控，使用G1 GC；

**对于内存大小的考量**

- 一般4G以上，算是比较大，用G1的性价比比较高
- 一般超过8G，比如16G-64G，非常推荐使用G1 GC

#### ZGC/Shenandoah GC

JDK11 出的

-XX:  +UnlockExperimentalVmOptions -XX: +useZGC -Xmx16g

**ZGC最主要的特点包括**

- GC最大停顿时间不超过10ms

- 堆内存支持范围广，小至几百MB的堆空间，大至4TB的超大堆内存（JDK13升至16TB）

- 与G1相比，应用吞吐量下降不超过15%

- 当前只支持Linux/x64位平台，JDK15后支持windows

  

#### Shennandoah GC

JDK12+才有

-XX: +UnlockExperimentalVMOptions -XX:+UseshenandoahGC -Xmx16g

Shennandoah GC立项比ZGC更早，设计为GC线程与应用线程并发执行的方式，通过实现垃圾回收过程的并发处理，改善停顿时间，使得GC执行线程能够在业务处理线程运行过程中进行堆压缩、标记和整理，从而消除了绝大部分的暂停时间。

Shennandoah团队对外宣称Shennandoah GC的暂停时间与堆大小无关，都可以保障具有很低的暂停时间



### summary

- 串行GC（Serial GC）：单线程执行，应用需要暂停
- 并行GC（ParNew、Parallel Scavenge、Parallel Old）：多线程并行执行垃圾回收，关注与高吞吐
- CMS（Concurrent Mark-Sweep）：多线程并发标记和清除，关注与降低延迟
- G1（G First）：通过划分多个内存区域做增量整理和回收，进一步降低延迟
- ZGC（Z Garbage Collector）：通过着色指针和读屏障，实现几乎全部的并发执行，几毫秒级别的额延迟，线程可扩展
- Epsilon：实验性的GC，供性能分析使用
- Shenandoah：G1的改进版本，跟ZGC类似



作业：使用G1 GC启动一个程序，仿照课上案例分析一下







