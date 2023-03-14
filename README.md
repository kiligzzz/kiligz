# Kiligz

## 简介

包含一些自己写的工具类、以及一些特殊实现等

## 工具类

> 包含并发管理器、唯一标识、计时器、git等

### [Concurrent](src/main/java/com/kiligz/concurrent/Concurrent.java)

> 并发管理器

1. 线程池
   - 支持创建、管理多个四种可命名线程池;
   - 添加CountDownLatch任务执行计数，可阻塞等待所有或指定线程池或任务执行、获取结果;
   - 实现Executor，可供CompletableFuture使用;
2. 线程共享对象
      - 支持添加、管理、获取线程共享对象;
3. ThreadLocal
         - 支持创建、管理ThreadLocal及其值;
4. 结束标记
         - 放入、记录、判断结束标记;
5. 线程池创建
         - 线程池使用;
         - CountDownLatch;
         - Support;
6. 当前map信息获取
7. 工具方法
           - 支持获取一个在当前线程执行任务的Executor对象



## 特殊实现

> 包含 内存安全的blockingQueue、一些设计模式（原型、备忘录、责任链）的实现应用、lucene的一些特殊场景、高速缓存ASM的应用、单词查找树应用等

### [分布式异步日志全链路追踪](src/main/java/com/kiligz/trace)

> TransmittableThreadLocal、Trace对象、http拦截器、rpc过滤器、重写MDCAdapter等实现



DAT