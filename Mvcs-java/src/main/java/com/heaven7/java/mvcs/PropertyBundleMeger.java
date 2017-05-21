package com.heaven7.java.mvcs;

import com.heaven7.java.base.util.PropertyBundle;
import com.heaven7.java.mvcs.ParameterMerger;

/**
 * a parameter merger of {@linkplain PropertyBundle}
 * @author heaven7
 * @since 1.1.8
 */
public class PropertyBundleMeger implements ParameterMerger<PropertyBundle>{

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
