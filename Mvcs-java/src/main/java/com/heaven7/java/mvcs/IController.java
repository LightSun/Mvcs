package com.heaven7.java.mvcs;

import java.util.List;

/**
 * a state controller which support multi states.
 * <ul>
 *     <li>State Factory: use {@linkplain #setStateFactory(StateFactory)} to set.
 *     </li>
 *     <li>State Parameter Merger: use {@linkplain #setParameterMerger(ParameterMerger)} to set
 *          merger for state parameter.
 *     </li>
 *     <li>State Cache: use {@linkplain #setStateCacheEnabled(boolean)} to enable or disable state cache.
 *     use {@linkplain #destroyStateCache()} to destroy state cache without current states.
 *     </li>
 *     <li>State Stack: use {@linkplain #setStateStackEnable(boolean)} to enable state stack. so that we can
 *         call {@linkplain #revertToPreviousState()} to previous state. Use {@linkplain #setMaxStateStackSize(int)}
 *         to control the max state stack size. or use {@linkplain #clearStateStack()} to clear state stack.
 *     </li>
 *     <li>Share Parameter: share parameter for multi states. you can use {@linkplain #setShareStateParam(Object)} to
 *      set share parameter. {@linkplain #getShareStateParam()} to get shared parameter.
 *     </li>
 *     <li>State Manager: you can use 'CRUD' methods by calling addXXX() , removeXXX() , clearXXX() ,getXXXState.
 *              hasXXX() and so on.
 *     </li>
 *     <li>Update State and dispose:
 *         use {@linkplain #notifyStateUpdate(Object)} to update states. and  {@linkplain #dispose()}
 *         to do the final action.
 *     </li>
 *     <li> Manage Lock Event: {@linkplain #lockEvent(int...)} , {@linkplain #unlockEvent(int...)} .
 *          {@linkplain #unlockAllEvent()}.
 *     </li>
 *     <li>Controller Owner: {@linkplain #setOwner(Object)} and {@linkplain #getOwner()} .
 *     </li>
 *     <li>Mutex States:  what is this ?  This define the states can split to double groups.
 *          any state of one group is mutex with any state of the other group. That is if double states is mutex.
 *          when either one of them called {@linkplain AbstractState#onEnter()}, the other
 *          state's {@linkplain AbstractState#onExit()} must be called.
 *          see {@linkplain #setMutexState(int[], int[])} , {@linkplain #addMutexState(int[])}
 *          and {@linkplain #getMutexState(int)}.
 *     </li>
 * </ul>
 * <h1>Note: current state and global states shouldn't intersect state.</h1>
 * 
 * @author heaven7
 *
 * @param <P> the parameter type.
 */
public interface IController<S extends AbstractState<P>, P> extends Disposeable{


    /**
     * add a group state to mutex. This means any one state of the groupState is mutex with
     * others of the groupState.
     * @param groupState the target group state. which is mutex with each other.
     */
    void addMutexState(int[] groupState);

	/**
	 * set mutex states between the target groupState1 and the target groupState2.
	 * that means any state of groupState1 is mutex with any state of groupState2.
	 * @param groupState1 the input group state1
	 * @param groupState2 the input group state2.
	 */
	void setMutexState(int[] groupState1, int[] groupState2);
	
	/**
	 * get the mutex states for target state.
	 * @param mainState the single state.
	 * @return the states which is mutex with target mainState. or null if not have the states mutex with it.
	 */
	int[] getMutexState(int mainState);
	
	/**
	 * set the owner of this controller.
	 * @param owner the owner.
	 */
	void setOwner(Object owner);
	
	/**
	 * return the owner of this controller.
	 * @return the owner
	 */
	Object getOwner();
	
	/**
	 * set state cache enabled or not. default is false.
	 * @param enable  true to enable state cache.
	 * @see #destroyStateCache()
	 */
	void setStateCacheEnabled(boolean enable);

    /**
     * indicate is the state cache enabled or not.
     * @return true if enabled.
     */
    boolean isStateCacheEnabled();
	
	/**
	 * destroy the state cache without current running states. 
	 * @see IController#setStateCacheEnabled(boolean)
	 */
	void destroyStateCache();

    /**
     * set the max state stack size, if you enabled state stack by calling {@linkplain #setStateStackEnable(boolean)}.default max is ten.
     * @param max the max size of state stack.
     * @see #setStateStackEnable(boolean)
     * @see #revertToPreviousState()
     */
    void setMaxStateStackSize(int max);

    /**
     * get the max state stack size.
     * @return the max state stack size.
     */
    int getMaxStateStackSize();
    /**
     * set if enable state stack/history. so we can revertTo previous state by calling {@linkplain #revertToPreviousState()}.
     * @param enable true to enable false to disable.
     * @see #setMaxStateStackSize(int)
     * @see #revertToPreviousState()
     */
    void setStateStackEnable(boolean enable);

    /**
     * indicate if the state stack is enabled .
     * @return true if the state stack is enabled.
     */
    boolean isStateStackEnable();

    /**
     * clear state stack.
     */
    void clearStateStack();

    /**
     * notify state update by target parameter.
     * @param param the parameter.
     */
    void notifyStateUpdate(P param);

    /**
     * set share state parameter.
     * @param param the parameter. can be null , null means clear share state parameter.
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
    
    /**
     * get the global state if you use single state. or else return the max state .
     *  which is indicated by flag.
     * @return the global single state.
     */
	S getGlobalState();


    @Override
    void dispose();

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
     * get a copy list of locked events. if not have return null.
     * @return a copy list of locked events.
     */
    List<Integer> getLockedEvents();
    
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
