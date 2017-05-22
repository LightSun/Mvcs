package com.heaven7.java.mvcs.impl;

import com.heaven7.java.base.util.PropertyBundle;
import com.heaven7.java.mvcs.StateTeamManager;

/**
 * simple team manager of java platform.
 * @author heaven7
 * @since 1.1.8
 */
public final class DefaultStateTeamManager extends StateTeamManager<PropertyBundle>{
	
	private static class Creator{
		static final DefaultStateTeamManager INSTANCE = new DefaultStateTeamManager();
	}

	/**
	 * get default team manager.
	 * @return a team manager.
	 */
	public static DefaultStateTeamManager getDefault(){
		return Creator.INSTANCE;
	}
}
