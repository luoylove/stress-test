## 一个java压测引擎
## 实现方式: 
    1. 线程池进行线程初始化
    2. CyclicBarrier 设置线程集合点
    3. CountDownLatch 监控线程是否执行完成
## 功能: 
    1. 压测模型 
        时长
        次数
        forever
    2. format
        并发数
        总执行次数
        总耗时
        TPS
        最小耗时
        最大耗时
        50%的耗时
        60%的耗时
        70%的耗时
        80%的耗时
        90%的耗时
        95%的耗时
        99%的耗时
    3. 支持压测协议
        http
        其他协议可实现StressTask自动实现
    4. 远程集群压测
        StressServerStart 启动server端
        StressRemoteTester 设置需要压测的机器ip:port 
