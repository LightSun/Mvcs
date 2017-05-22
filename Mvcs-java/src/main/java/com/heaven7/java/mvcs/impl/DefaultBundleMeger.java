package com.heaven7.java.mvcs.impl;

import com.heaven7.java.base.util.PropertyBundle;
import com.heaven7.java.mvcs.ParameterMerger;

/**
 * a parameter merger of {@linkplain PropertyBundle} of java platform.
 * @author heaven7
 * @since 1.1.8
 */
public class DefaultBundleMeger implements ParameterMerger<PropertyBundle>{
	
	private static DefaultBundleMeger sInstance;
	
	private DefaultBundleMeger(){}
	
	/**
	 * get the default instance of PropertyBundleMeger
	 * @return an instance of PropertyBundleMeger.
	 */
	public static DefaultBundleMeger getInstance(){
		return sInstance !=null ? sInstance : (sInstance = new DefaultBundleMeger());
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
