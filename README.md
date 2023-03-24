# Kiligz

## 简介

包含一些自己写的工具类、以及一些特殊实现等

## 工具类

> 包含并发管理器、唯一标识、计时器、git等

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



### [NacosUtil](src/main/java/com/kiligz/nacos/NacosUtil.java)

> 用于实时获取nacos上的配置，并转换为相应格式

   1. 懒汉式单例获取配置，第一次调用时初始化、获取、格式化，不会重复
   2. 监听nacos上的配置，发生变化实时更新
   3. 若更新时出错，则放弃本次更新，使用上次正常更新时的配置



## 特殊实现

> 包含 内存安全的blockingQueue、一些设计模式（原型、备忘录、责任链）的实现应用、lucene的一些特殊场景、高速缓存ASM的应用、单词查找树应用等

### [分布式异步日志全链路追踪](src/main/java/com/kiligz/trace)

> TransmittableThreadLocal、Trace对象、http拦截器、rpc过滤器、重写MDCAdapter等实现

