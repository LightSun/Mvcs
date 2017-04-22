package com.heaven7.java.mvcs;

import java.util.List;

/**
 * a state controller which support multi states.
 * @author heaven7
 *
 * @param <P> the param type.
 */
public interface StateController<P extends StateParameter> {

    /**
     * set the max state stack size. default max is ten.
     * @param max the max size of state stack.
     */
    void setMaxStateStackSize(int max);

    void setStateStackEnable(boolean enable);

    boolean isStateStackEnable();

    void clearStateStack();
	
	boolean addState(int states, P extra);
	
	boolean addState(int states);
	
	 /**
     * remove the target state from current state.
     * @param states the target state
     * @return true if remove state success. or else this state is not entered,
     * @see {@link #addState(int)}
     */
    boolean removeState(int states);
    
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
    
    
    void setGlobalState(int states);
    /**
     * Sets the global state of this state machine.
     *
     * @param states the global state.
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
    

	List<AbstractState<P>> getState();
	
	  /**
     * lock the event
     * @param eventKey  the event key
     * @return true if lock success. false if is already locked.
     */
    boolean lockEvent(int eventKey);

    /**
     * unlock the event .
     * @param eventKey the event key
     * @return true if unlock the event success. false if is not locked.
     */
    boolean unlockEvent(int eventKey);

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
    void setStateFactory(StateFactory<P> factory);
    
    
    interface StateFactory<P extends StateParameter>{
    	
    	AbstractState<P> createState(int stateKey, P p);
    }
    
}
