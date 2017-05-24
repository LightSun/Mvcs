package com.heaven7.java.mvcs;

import com.heaven7.java.base.anno.CalledInternal;
import com.heaven7.java.base.anno.Hide;
import com.heaven7.java.mvcs.impl.DefaultStateTeamManager;

/**
 * the team mediator. it can communicate with {@linkplain IController} and {@link StateTeamManager}.
 * @author heaven7
 *
 * @param <P> the parameter
 * @since 1.1.8
 * @see IController
 * @see StateTeamManager
 * @see DefaultStateTeamManager
 */
public abstract class TeamMediator<P> {

	private StateTeamManager<P> mStm ;
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
	 * <h1>Note: you must not call this method. this is called by Framework.</h1>
	 * @param stm
	 *            the team manager
	 */
	@Hide
	@CalledInternal
	final void setStateTeamManager(StateTeamManager<P> stm){
		this.mStm = stm;
	}
	
	/**
	 * get the state team manager
	 * @return the team manager.
	 */
	public final StateTeamManager<P> getStateTeamManager(){
		return mStm;
	}

	/**
	 * notify states enter which is from a team and controlled by target controller.
	 * 
	 * @param states
	 *            the states to handle
	 */
	public abstract void notifyStateEnter(int states, P param);

	/**
	 * notify state exit which is from a team.
	 * 
	 * @param states
	 *            the states to handle
	 */
	public abstract void notifyStateExit(int states, P param);

	/**
	 * notify state reenter which is from a team.
	 * 
	 * @param states
	 *            the states to handle
	 */
	public abstract void notifyStateReenter(int states, P param);

}
