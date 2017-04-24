package com.heaven7.java.mvcs;

import java.util.List;

/**
 * a state controller which support multi states.
 * @author heaven7
 *
 * @param <P> the param type.
 */
public interface IController<S extends AbstractState<P>, P> {

    /**
     * set the max state stack size. default max is ten.
     * @param max the max size of state stack.
     */
    void setMaxStateStackSize(int max);

    /**
     * set if enable state stack/history.
     * @param enable true to enable false to disable.
     */
    void setStateStackEnable(boolean enable);

    /**
     * indicate if the state stack is enabled.
     * @return true if the state stack is enabled.
     */
    boolean isStateStackEnable();

    /**
     * clear state stack.
     */
    void clearStateStack();

    /**
     * notify state update by target param.
     * @param param the parameter.
     */
    void notifyStateUpdate(P param);

    /**
     * set share state parameter.
     * @param param the parameter
     */
    void setShareStateParam(P param);

    /**
     * get the share state parameter
     * @return the share state parameter
     */
    P getShareStateParam();
    //==============================================

    /**
     * add states(may be multi) to controller.
     * @param states the new states flags.
     * @param extra the extra state parameter
     * @return true if add the target states success.
     */
	boolean addState(int states, P extra);

    /**
     * add states(may be multi) to controller.
     * @param states the new states flags.
     * @return true if add the target states success.
     */
	boolean addState(int states);
	
	 /**
     * remove the target state from current state.
     * @param states the target state
     * @return true if remove state success. or else this state is not entered,
     * @see {@link #addState(int)}
     */
    boolean removeState(int states);

    /**
     * remove the target state from current state.
     * @param states the target state
     * @param param the extra parameter.
     * @return true if remove state success. or else this state is not entered,
     * @see {@link #addState(int)}
     */
    boolean removeState(int states, P param);

    /**
     * clear the all states
     * @param  param the param which will used by state exit.
     */
    void clearState(P param);
    /**
     * clear the all states
     */
    void clearState();

    /**
     * change to the state
     *
     * @param newStates the new state to change to.
     */
    void setState(int newStates);
    
    /**
     * change to the state
     *
     * @param newStates the new state to change to.
     */
    void setState(int newStates, P extra);
    
    /**
     * Change state back to the previous state.
     *
     * @return {@code True} in case there was a previous state that we were able to revert to. In case there is no previous state,
     * no state change occurs and {@code false} will be returned.
     */
    boolean revertToPreviousState();


    /**
     * set global states
     * @param states the target global states.
     */
    void setGlobalState(int states);
    /**
     * Sets the global state of this state machine.
     *
     * @param states the global state.
     * @param extra the extra parameter
     */
    void setGlobalState(int states, P extra);
    
    /**
     * Indicates whether the state machine is in the given state.
     * <p/>
     * This implementation assumes states are singletons (typically an enum) so
     * they are compared with the {@code ==} operator instead of the
     * {@code equals} method.
     *
     * @param states the state to be compared with the current state
     * @return true if the current state and the given state are the same
     * object.
     */
    boolean isInState(int states);
    
    /**
     * indicate is the target state is acting or not. this is often used in mix state.
     * @param state the target state to check
     * @return true is has the target state.
     */
    boolean hasState(int state);

    /**
     * get the current states  without global states..
     * @return the all states if multi. or only contains one.
     */
	List<S> getCurrentStates();

    /**
     * get the current state if you use single state without global states.. or else return the max state
     *  which is indicated by flag..
     * @return the current single state.
     */
	S getCurrentState();

    /**
     * get current state as flags
     * @return the flags of current states without global states.
     */
	int getCurrentStateFlags();

    /**
     * get global state flags. if not set (can call {@linkplain #setGlobalState(int, Object)})return zero.
     * @return the global state flags.
     * @see  #setGlobalState(int, Object)
     * @see  #setGlobalState(int)
     */
    int getGlobalStateFlags();


    /**
     * get global states. if not set (can call {@linkplain #setGlobalState(int, Object)}) return null.
     * @return the global states.
     * @see  #setGlobalState(int, Object)
     * @see  #setGlobalState(int)
     */
    List<S> getGlobalStates();

    //============================== lock event ==================================
	  /**
     * lock the target events
     * @param eventKeys  the event keys
     * @return true if lock the all target events success. false if is already locked.
     * @throws IllegalArgumentException if  eventKeys ==null or eventKeys.length ==0.
     */
    boolean lockEvent(int... eventKeys) throws IllegalArgumentException;

    /**
     * unlock the target events .
     * @param keys the event keys
     * @return true if unlock the all events success. false otherwise..
     * @throws IllegalArgumentException if  eventKeys ==null or eventKeys.length ==0.
     */
    boolean unlockEvent(int...  keys) throws IllegalArgumentException;

    /**
     * unlock all events .
     * @return true if unlock the all events success. false otherwise..
     */
    boolean unlockAllEvent();

    /**
     * is the event locked.
     * @param eventKey  the event key
     * @return true if is locked. false otherwise.
     */
    boolean isLockedEvent(int eventKey);
    
    /**
     * set the state factory
     * @param factory the state factory.
     */
    void setStateFactory(StateFactory<S,P> factory);

    /**
     * set the param merger.
     * @param merger the target merger.
     */
    void setParameterMerger(ParameterMerger<P> merger);

    /**
     * state factory help we create state.
     * @param <S> the state type
     * @param <P> the parameter type.
     */
    interface StateFactory<S extends AbstractState<P>, P>{

        /**
         * create state by key and parameter.
         * @param stateKey the state key
         * @param p the parameter
         * @return a new state.
         */
    	S createState(int stateKey, P p);
    }

}
