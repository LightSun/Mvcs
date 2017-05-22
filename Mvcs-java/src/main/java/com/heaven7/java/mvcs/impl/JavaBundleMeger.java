package com.heaven7.java.mvcs.impl;

import com.heaven7.java.base.util.PropertyBundle;
import com.heaven7.java.mvcs.ParameterMerger;

/**
 * a parameter merger of {@linkplain PropertyBundle} of java platform.
 * @author heaven7
 * @since 1.1.8
 */
public class JavaBundleMeger implements ParameterMerger<PropertyBundle>{
	
	private static JavaBundleMeger sInstance;
	
	private JavaBundleMeger(){}
	
	/**
	 * get the default instance of PropertyBundleMeger
	 * @return an instance of PropertyBundleMeger.
	 */
	public static JavaBundleMeger getInstance(){
		return sInstance !=null ? sInstance : (sInstance = new JavaBundleMeger());
	}

	@Override
	public PropertyBundle merge(PropertyBundle t1, PropertyBundle t2) {
		if(t1 != null){
			if(t2 != null){
				t1.putAll(t2);
			}
			return t1;
		}
		return t2;
	}
	

}
