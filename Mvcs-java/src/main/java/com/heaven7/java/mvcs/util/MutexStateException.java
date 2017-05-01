package com.heaven7.java.mvcs.util;

import com.heaven7.java.mvcs.IController;

/**
 *  a exception class that occurs when mutex state is set or add at same time. that means mutex exists 
 *  in the sample state parameter.  often used by
 *  {@linkplain IController#addState(int,Object)} / {@linkplain IController#addState(int)}
 *  {@linkplain IController#setState(int,Object)} / {@linkplain IController#setState(int)}
 * @author heaven7
 * @since 1.1.2
 * @see IController#addState(int, Object)
 * @see IController#addState(int)
 * @see IController#setState(int)
 * @see IController#setState(int, Object)
 */
public class MutexStateException extends RuntimeException{

	private static final long serialVersionUID = 3131615968585009508L;

	public MutexStateException() {
		super();
	}

	public MutexStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public MutexStateException(String message) {
		super(message);
	}

	public MutexStateException(Throwable cause) {
		super(cause);
	}
	

}
