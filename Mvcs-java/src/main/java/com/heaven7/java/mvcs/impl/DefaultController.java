package com.heaven7.java.mvcs.impl;

import com.heaven7.java.base.util.PropertyBundle;
import com.heaven7.java.mvcs.ParameterMerger;
import com.heaven7.java.mvcs.SimpleController;
import com.heaven7.java.mvcs.SimpleState;

/**
 * a controller of java platform. that default set parameter merger({@linkplain DefaultBundleMeger})
 * which use {@linkplain PropertyBundle} and {@linkplain #setParameterMerger(ParameterMerger)}. 
 * so often you just need to set factory({@linkplain #setStateFactory(com.heaven7.java.mvcs.IController.StateFactory)}). then can use controller. 
 * Created by heaven7 on 2017/4/24.
 * @see #setParameterMerger(ParameterMerger)
 * @see #setStateFactory(com.heaven7.java.mvcs.IController.StateFactory)
 * @author heaven7
 */
public class DefaultController extends SimpleController<SimpleState<PropertyBundle>, PropertyBundle> {

	public DefaultController() {
		super();
		setParameterMerger(DefaultBundleMeger.getInstance());
	}
	public DefaultController(Object owner) {
		super(owner);
		setParameterMerger(DefaultBundleMeger.getInstance());
	}
	
	
}
