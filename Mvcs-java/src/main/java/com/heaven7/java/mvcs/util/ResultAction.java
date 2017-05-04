package com.heaven7.java.mvcs.util;

/**
 * the result callback.
 * @author heaven7
 *
 * @param <R> the result type
 * @since 1.1.5
 */
public interface ResultAction<R> {


	/**
	 * called on action result.
	 * @param result the result.
	 */
	void onActionResult(R result);
}
