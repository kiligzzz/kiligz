# Kiligz

## 简介

包含一些自己写的工具类、常用算法、常用设计模式模板、以及一些特殊实现等

## 工具类

> 包含并发管理器、Nacos配置工具类、Class助手类、唯一标识、计时器、git等

### [Concurrents](src/main/java/com/kiligz/concurrent/Concurrents.java)

> 并发管理器

1. 线程池
   - 支持创建、管理多个四种可命名线程池;
   - 添加CountDownLatch任务执行计数，可阻塞等待所有或指定线程池或任务执行、获取结果;
   - 实现Executor，可供CompletableFuture使用;
2. 线程共享对象
   - 支持添加、管理、获取线程共享对象;
3. ThreadLocal
   - 支持创建、管理ThreadLocal及其值;
   - 若需在线程池中使用则依赖TransmittableThreadLocal，同时包装线程池
4. 结束标记
   - 放入、记录、判断结束标记;
5. 线程池创建
   - 线程池使用;
   - CountDownLatch;
   - Support;
6. 当前map信息获取
7. 工具方法
   - 支持获取一个在当前线程执行任务的Executor对象



###  [NacosConfigUtil](src/main/java/com/kiligz/nacos/NacosConfigUtil.java) 

> 用于实时获取nacos上的配置，并转换为相应格式

   1. 懒汉式单例获取配置，第一次调用时初始化、获取、格式化，不会重复
   2. 监听nacos上的配置，发生变化实时更新
   3. 若更新时出错，则放弃本次更新，使用上次正常更新时的配置



###  [ClassHelper](src/main/java/com/kiligz/classHelper/ClassHelper.java) 

> Class助手类，主要是对于Class类型的操作

1. 支持依据不同类加载器创建实例，实例可缓存，默认使用线程上下文类加载器
2. 支持获取类、接口的 整个模块中 或 指定包 中的子类、实现类的Class
3. 支持获取 整个模块中 或 指定包 中的所有类的Class
4. 支持获取 整个模块中 或 指定包 中的所有类的全限定名
5. 支持根据类的全限定名获取类的Class
    (Tips:2、3、4、5都支持获取项目依赖的jar中的内容，并且都支持缓存Class)
6. 支持获取一个类的所有内部类的Class



## 设计模式模板

### [Prototype](src/main/java/com/kiligz/designPattern/Prototype.java)   

> 原型模式接口，提供默认的浅拷贝、深拷贝（序列化方式）方法

### [Memento](src/main/java/com/kiligz/designPattern/Memento.java) 

> 备忘录模式接口，搭配深拷贝（原型模式）

1. 提供保存、恢复备忘录、深拷贝方法
2. 每个对象根据key操作自己的备忘录，可有相同的key

### [Transformer](src/main/java/com/kiligz/designPattern/Transformer.java) 

> 责任链模式，转换器抽象类

1. 支持通过方法构造和拼接链式Transformer



## 算法模板

###  [Dijkstra](src/main/java/com/kiligz/algorithm/Dijkstra.java)

> 求最短路径



## 特殊实现

> 包含 内存安全的blockingQueue、lucene的一些特殊场景、高速缓存ASM的应用、单词查找树应用等

### [分布式异步日志全链路追踪](src/main/java/com/kiligz/trace)

> TransmittableThreadLocal、Trace对象、http拦截器、rpc过滤器、重写MDCAdapter等实现

