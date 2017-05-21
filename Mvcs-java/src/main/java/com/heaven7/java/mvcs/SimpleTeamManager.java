package com.heaven7.java.mvcs;

import com.heaven7.java.base.util.PropertyBundle;
import com.heaven7.java.mvcs.TeamManager;

/**
 * simple team manager
 * @author heaven7
 * @since 1.1.8
 */
public final class SimpleTeamManager extends TeamManager<PropertyBundle>{
	
	private static class Creator{
		static final SimpleTeamManager INSTANCE = new SimpleTeamManager();
	}

	/**
	 * get default team manager.
	 * @return a team manager.
	 */
	public static SimpleTeamManager getDefault(){
		return Creator.INSTANCE;
	}
}
