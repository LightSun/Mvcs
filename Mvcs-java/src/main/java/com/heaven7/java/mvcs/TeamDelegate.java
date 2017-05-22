package com.heaven7.java.mvcs;

import com.heaven7.java.base.anno.Hide;

/**
 * the team delegate
 * @author heaven7
 *
 * @param <P> the parameter type.
 * @since 1.1.8
 */
@Hide
/*public*/  abstract class TeamDelegate<P>{
	
	 /*
     * the state listener 
     * @author heaven7
     *
     * @param <P> the parameter
     */
	/*public interface StateListener<P>{
	
		void onEnterState(int stateFlag, AbstractState<P> state);
	
		void onExitState(int stateFlag, AbstractState<P> state);
		
		void onReenterState(int stateFlag, AbstractState<P> state);
	}*/

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
