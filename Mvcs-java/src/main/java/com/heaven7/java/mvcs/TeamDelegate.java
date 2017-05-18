package com.heaven7.java.mvcs;

public interface TeamDelegate {

	void setEnableStateCallback(boolean enable);
	/**
	 * notify state enter from a team.
	 * @param states
	 */
	void notifyStateEnter(int states);
	void notifyStateExit(int states);
	void notifyStateReenter(int states);
}
