package com.heaven7.java.mvcs;
/**
 * the team mediator.
 * @author heaven7
 *
 * @param <P> the parameter
 * @since 1.1.8
 */
public interface TeamMediator<P> {

	/*
	 * the state listener
	 * 
	 * @author heaven7
	 *
	 * @param <P> the parameter
	 */
	/*
	 * public interface StateListener<P>{
	 * 
	 * void onEnterState(int stateFlag, AbstractState<P> state);
	 * 
	 * void onExitState(int stateFlag, AbstractState<P> state);
	 * 
	 * void onReenterState(int stateFlag, AbstractState<P> state); }
	 */
	/**
	 * set state team manager.
	 * 
	 * @param stm
	 *            the team manager
	 */
	void setStateTeamManager(StateTeamManager<P> stm);
	
	/**
	 * get the state team manager
	 * @return the team manager.
	 */
	StateTeamManager<P> getStateTeamManager();

	/**
	 * notify states enter which is from a team and controlled by target controller.
	 * 
	 * @param states
	 *            the states to handle
	 */
	void notifyStateEnter(int states, P param);

	/**
	 * notify state exit which is from a team.
	 * 
	 * @param states
	 *            the states to handle
	 */
	void notifyStateExit(int states, P param);

	/**
	 * notify state reenter which is from a team.
	 * 
	 * @param states
	 *            the states to handle
	 */
	void notifyStateReenter(int states, P param);

}
