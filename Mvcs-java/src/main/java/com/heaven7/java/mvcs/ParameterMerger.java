package com.heaven7.java.mvcs;

/**
 *  the parameter merger.
 * @author heaven7
 * @see AbstractState
 * @see IController
 */
public interface ParameterMerger<T> {

	T merge(T t1, T t2);
}
