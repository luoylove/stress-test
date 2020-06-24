package core;

/**
 * 压测业务代码需要实现类,压测实际上是压测该类中的task方法
 * @Author: luoy
 * @Date: 2020/6/9 13:39.
 */
public interface StressTask<T> {
    T task() throws Throwable;
}
