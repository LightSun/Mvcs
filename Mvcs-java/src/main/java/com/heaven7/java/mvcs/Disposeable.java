package com.heaven7.java.mvcs;

/**
 * indicate is disposeable.
 * Created by heaven7 on 2017/4/25.
 */
public interface Disposeable {
    /**
     * dispose something. often used to release the resource at last. eg: when destroy.
     */
    void  dispose();
}
