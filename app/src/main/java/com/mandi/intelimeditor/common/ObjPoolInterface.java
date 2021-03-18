package com.mandi.intelimeditor.common;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/05/04
 *      version : 1.0
 * <pre>
 */
public interface ObjPoolInterface<T> {
    void put(T t);
    void get(T t);
    void remove(T t);
}
