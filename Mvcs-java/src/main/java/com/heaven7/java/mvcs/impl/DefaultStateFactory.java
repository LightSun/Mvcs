package com.heaven7.java.mvcs.impl;

import com.heaven7.java.base.util.PropertyBundle;
import com.heaven7.java.mvcs.IController;
import com.heaven7.java.mvcs.SimpleState;

/**
 * a state factory of simple java platform.
 * @author heaven7
 * @since 1.1.8
 */
public interface DefaultStateFactory extends IController.StateFactory<SimpleState<PropertyBundle>,PropertyBundle>{

}
