package com.heaven7.java.mvcs;

/*public*/  abstract class TeamDelegate<P>{

	/**
	 * set the state callback enabled or not.
	 * @param enable true to enable , false to disable
	 */
	void setStateCallbackEnabled(boolean enable){
		
	}
	/**
	 * notify state enter which is from a team.
	 * @param states the states to handle
	 */
	void notifyStateEnter(int states, P param){
		
	}
	/**
	 * notify state exit which is from a team.
	 * @param states the states to handle
	 */
	void notifyStateExit(int states, P param){
		
	}
	/**
	 * notify state reenter which is from a team.
	 * @param states the states to handle
	 */
	void notifyStateReenter(int states, P param){
		
	}
}
