package com.heaven7.java.mvcs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import com.heaven7.java.base.anno.CalledInternal;
import com.heaven7.java.base.anno.Hide;
import com.heaven7.java.base.anno.IntDef;
import com.heaven7.java.base.util.Disposeable;
import com.heaven7.java.mvcs.util.MutexStateException;

/**
 * a state controller which support multi states.
 * <p><h2>Note, any single state must be 2^n.  so multi states must be the sum of them.</h2></p>.
 * <ul>
 *     <li>State Factory: use {@linkplain #setStateFactory(StateFactory)} to set.
 *     </li>
 *     <li>State Parameter:  [Merge], use {@linkplain #setParameterMerger(ParameterMerger)} to set
 *          merger for state parameter.<br> 
 *          [Clear]: use {@linkplain IController#clearStateParameter(boolean)}
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
 *          and {@linkplain #getMutexState(int)}. And you should care about {@linkplain MutexStateException}.
 *     </li>
 *     <li> State Transaction: use {@linkplain #beginTransaction()}.
 *     </li>
 *     <li>Message Control: send, handle(with reply), has,  remove.<br>
 *         send: {@linkplain #sendMessage(Message, byte, byte)} and  {@linkplain #sendMessage(Message, byte)} <br>
 *         handle: {@linkplain AbstractState#handleMessage(Message)},suggest {@linkplain Message#replier} called in
 *                  {@linkplain AbstractState#handleMessage(Message)}. <br>
 *         has: {@linkplain #hasMessage(int) and  {@linkplain #hasMessage(Message)}. <br>
 *         remove: {@linkplain #removeMessage(int)  , {@linkplain #removeMessage(Message)} and {@linkplain IController#clearMessages()}. <br>
 *         And want to handle delay messages ? please use {@linkplain #update(long)}.
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
	 * the message send policy: broadcast.
	 */
	byte POLICY_BROADCAST   = 1;
	/**
	 * the message send policy: consume.
	 */
	byte POLICY_CONSUME     = 2;
	
	@IntDef({
		POLICY_BROADCAST,
		POLICY_CONSUME,
	})
	@Target({ElementType.PARAMETER})
	@Retention(RetentionPolicy.SOURCE)
	@interface PolicyType{		}
	
	/**
	 * indicate the scope: current state.
	 */
	byte FLAG_SCOPE_CURRENT       = 1 << 0;
	/**
	 * indicate the scope: cached state.
	 */
	byte FLAG_SCOPE_CACHED        = 1 << 1;
	/**
	 * indicate the scope: global state. the priority is highest.
	 */
	byte FLAG_SCOPE_GLOBAL        = 1 << 2;
	
	/** the flags of all scope, handle priority: global > current > cache(global)  */
	byte FLAG_SCOPE_ALL           = FLAG_SCOPE_CURRENT | FLAG_SCOPE_CACHED | FLAG_SCOPE_GLOBAL;
	
	@IntDef(value = {
		FLAG_SCOPE_CURRENT,
		FLAG_SCOPE_CACHED,
		FLAG_SCOPE_GLOBAL,
	},flag = true)
	@Target({ElementType.PARAMETER})
	@Retention(RetentionPolicy.SOURCE)
	@interface ScopeFlags{		}
	
	/**
	 * begin the state transaction with current states.
	 * @return the state transaction.
	 * @see StateTransaction
	 * @since 1.1.5
	 */
	StateTransaction<P> beginTransaction();
	
	/**
	 * clear the state parameter of current states. this will cause call 
	 * {@linkplain AbstractState#setStateParameter(Object)} to clear state parameter.
	 * default clear all states parameter include cached states.
	 * @see 1.1.5
	 */
	void clearStateParameter();
	
	/**
	 * clear the state parameter of current states. this will cause call 
	 * {@linkplain AbstractState#setStateParameter(Object)} to clear state parameter.
	 * @param includeCachedState true if you want to clear the parameter of cached states.
	 * @see 1.1.5
	 */
	void clearStateParameter(boolean includeCachedState);
	
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
     * @return true if add the target states success.As state can reenter , if states > 0 this always return true.
     */
	boolean addState(@StateFlags int states, P extra);

    /**
     * add states(may be multi) to controller. As state can reenter , if states > 0 this always return true.
     * @param states the new states flags.
     * @return true if add the target states success.
     * @throws MutexStateException If the target state contains a mutex.
     */
	boolean addState(@StateFlags int states);
	
	 /**
     * remove the target state from current state.
     * @param states the target state
     * @return true if remove state success. or else this state is not entered,
     * @see {@link #addState(int)}
     * @throws MutexStateException If the target state contains a mutex.
     */
    boolean removeState(@StateFlags int states);

    /**
     * remove the target state from current state.
     * @param states the target state
     * @param param the extra parameter.
     * @return true if remove state success. or else this state is not entered,
     * @see {@link #addState(int)}
     * @throws MutexStateException If the target state contains a mutex.
     */
    boolean removeState(@StateFlags int states, P param);

    /**
     * clear the all states with target parameter
     * @param  param the parameter which will used by state exit.
     */
    void clearState(P param);
    /**
     * clear the all states
     */
    void clearState();

    /**
     * set the current states of this state machine. if the target new states == current states, 
     * this will have nothing effect. 
     *
     * @param newStates the new state to change to.
     * @return true if set new states success ,false otherwise.
     * @throws MutexStateException If the target state contains a mutex.
     */
    boolean setState(@StateFlags int newStates);
    
    /**
     * set the current states of this state machine. if the target new states == current states, 
     * this will have nothing effect. 
     *
     * @param newStates the new state to change to.
     * @param extra the extra parameter
     * @return true if set new states success ,false otherwise.
     * @throws MutexStateException If the target state contains a mutex.
     */
    boolean setState(@StateFlags int newStates, P extra);
    
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
     * @throws MutexStateException If the target state contains a mutex.
     */
    void setGlobalState(@StateFlags int states);
    /**
     * Sets the global state of this state machine.
     *
     * @param states the global state.
     * @param extra the extra parameter
     * @throws MutexStateException If the target state contains a mutex.
     */
    void setGlobalState(@StateFlags int states, P extra);
    
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
     * @throws MutexStateException If the target state contains a mutex.
     */
    boolean isInState(@StateFlags int states);
    
    /**
     * indicate is the target state is acting or not. this is often used in mix state.
     * @param state the target state to check
     * @return true is has the target state.
     * @throws MutexStateException If the target state contains a mutex.
     */
    boolean hasState(@StateFlags int state);
    
    /**
     * get the target state which is assigned by target int flag. And the state can from 
     * current state or cached state or global state , or null if not found.
     * @param state the target state.
     * @return the target single state.
     * @since 1.1.5
     */
    S getTargetState(int state);
    
    /**
     * get the target state which is assigned by target state flags. null if not found.
     * @param states the target state flags.
     * @param scopeFlags the flags of scope. {@linkplain #FLAG_SCOPE_CACHED}/{@linkplain #FLAG_SCOPE_CURRENT}/{@linkplain #FLAG_SCOPE_GLOBAL}
     * @param outStates the out states. optional, can be null.
     * @return the target states.
     * @since 1.1.8
     */
    List<S> getTargetStates(int states, int scopeFlags, List<S> outStates);
    /**
     * get the target state which is assigned by target state flags. And the state can from 
     * current state or cached state or global state , or null if not found.
     * @param states the target state flags.
     * @param outStates the out states. optional, can be null.
     * @return the target states.
     * @since 1.1.5
     */
    List<S> getTargetStates(int states, List<S> outStates);

    /**
     * get the current states  without global states..
     * @param outStates the out states. optional, can be null.
     * @return the all states if multi. or only contains one.
     * @since 1.1.5
     */
	List<S> getCurrentStates(List<S> outStates);
	
	/**
	 * get the current states  without global states..
	 * @return the all states if multi. or only contains one.
	 * @see #getCurrentStates(List)
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
     * @param outStates the out states. optional, can be null.
     * @return the global states.
     * @see  #setGlobalState(int, Object)
     * @see  #setGlobalState(int)
     * @since 1.1.5
     */
    List<S> getGlobalStates(List<S> outStates);
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
	
	//============================ message =====================================

	/**
	 * remove the delayed message  which is indicated by what.
	 * @param what the what flag .
	 * @since 1.1.6
	 */
	void removeMessage(int what);
	/**
	 * remove the delayed message which is indicated by the target message.
	 * @param expect the expect Message .
	 * @since 1.1.6
	 */
	void removeMessage(Message expect);
	
	/**
	 * whether has the target message or not.
	 * @param what the what indicate the message.
	 * @return true if has target what message.
	 * @since 1.1.6
	 */
	boolean hasMessage(int what);
	/**
	 * whether has the target message or not.
	 * @param expect the target message to judge
	 * @return true if has the target message.
	 * @since 1.1.6
	 */
	boolean hasMessage(Message expect);
	
	/**
	 * clear the all messages which are delayed in pool and have not handled.
	 * @since 1.1.6
	 */
	void clearMessages();
	
	/**
	 * dispatch the target message to the target state by the target policy.
	 * And the default scope is {@linkplain IController#FLAG_SCOPE_CURRENT} | {@linkplain IController#FLAG_SCOPE_GLOBAL}.
	 * @param states the target states to receive message, must be active state.
	 * @param msg the target message 
	 * @param policy the policy of send message 
	 * @return true if this message is handled.
	 * @throws IllegalStateException if message is in use.
	 * @throws NullPointerException if the target message is null.
	 * @since 1.1.8
	 */
    boolean dispatchMessage(int states, Message msg, @PolicyType byte policy);
    
    /**
	 * dispatch the target message to the all state by the target policy.
	 * @param msg the target message 
	 * @param policy the policy of send message 
	 * @param scopeFlags the scope flags of this message apply to.
	 * @return true if this message is handled.
	 * @throws IllegalStateException if message is in use.
	 * @throws NullPointerException if the target message is null.
	 * @since 1.1.8
	 */
    boolean dispatchMessage(Message msg, @PolicyType byte policy,@ScopeFlags byte scopeFlags);
    
	/**
	 * dispatch the target message to the all state by the target policy.
	 * And the default scope is {@linkplain IController#FLAG_SCOPE_CURRENT}.
	 * @param msg the target message 
	 * @param policy the policy of send message 
	 * @return true if this message is handled.
	 * @throws IllegalStateException if message is in use.
	 * @throws NullPointerException if the target message is null.
	 * @since 1.1.8
	 */
    boolean dispatchMessage(Message msg, @PolicyType byte policy);
    /**
     * send the target message to the all state by the target policy.
     * And the default scope is {@linkplain IController#FLAG_SCOPE_CURRENT}.
     * <p>use {@linkplain IController#dispatchMessage(Message, byte)} instead.</p>
     * @param msg the target message 
     * @param policy the policy of send message 
     * @return true if this message is handled.
     * @throws IllegalStateException if message is in use.
     * @throws NullPointerException if the target message is null.
     * @since 1.1.6
     */
    @Deprecated
    boolean sendMessage(Message msg, @PolicyType byte policy);

	/**
	 * send the target message to the all state by the target policy.
	 * <p>use {@linkplain IController#dispatchMessage(Message, byte, byte)} instead.</p>
	 * @param msg the target message 
	 * @param policy the policy of send message 
	 * @param scopeFlags the scope flags of this message apply to.
	 * @return true if this message is handled.
	 * @throws IllegalStateException if message is in use.
	 * @throws NullPointerException if the target message is null.
	 * @since 1.1.6
	 */
    @Deprecated
    boolean sendMessage(Message msg, @PolicyType byte policy,@ScopeFlags byte scopeFlags);
    
    /**
     * update the controller. this is often used by game.
     * @param deltaTime the delta time in mill second.
     * @since 1.1.6
     */
    void update(long deltaTime);
    
    /**
     * update the controller.  this is often used by game.
     * @param deltaTime the delta time in mill second.
     * @param param  the parameter to update.
     * @since 1.1.8
     */
    void update(long deltaTime,  P param);
    
    /**
     * update the controller for target active states.  this is often used by game.
     * 
     * @param activeStates the active states to update
     * @param deltaTime the delta time in mill second.
     * @param param  the parameter to update.
     * @since 1.1.8
     */
    void updateActiveStates(int activeStates, long deltaTime,  P param);
    
    

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
     * set the parameter merger.
     * @param merger the target merger.
     */
    void setParameterMerger(ParameterMerger<P> merger);
    
	/**
	 * set the team enabled or not. default is enabled..
	 * 
	 * @param enable
	 *            true to enable , false to disable
	 * @since 1.1.8
	 */
	void setTeamEnabled(boolean enable);

	/**
	 * indicate team is enabled or not.
	 * 
	 * @return true if is enabled. default is true.
	 * @since 1.1.8
	 */
	boolean isTeamEnabled();
	/**
	 * get the mediator which can communicate with team.
	 * @return the team mediator.
	 * @since 1.1.8
	 */
	@Hide
	@CalledInternal
	TeamMediator<P> getTeamMediator();
    
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
    
    /**
     * the state flags that define the values of multi states. so that the states value must be 
     * any one of them or any sum of them.
     * @author heaven7
     *
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.PARAMETER, ElementType.FIELD})
    @IntDef(flag = true , value = {
    	0x00000001,
    	0x00000002,
    	0x00000004,
    	0x00000008,
    	0x00000010,
    	0x00000020,
    	0x00000040,
    	0x00000080,
    	0x00000100,
    	0x00000200,
    	0x00000400,
    	0x00000800,
    	0x00001000,
    	0x00002000,
    	0x00004000,
    	0x00008000,
    	0x00010000,
    	0x00020000,
    	0x00040000,
    	0x00080000,
    	0x00100000,
    	0x00200000,
    	0x00400000,
    	0x00800000,
    	0x01000000,
    	0x02000000,
    	0x04000000,
    	0x08000000,
    	0x10000000,
    	0x20000000,
    	0x40000000,
    	//0x80000000, //-2147483648
    })
    @interface StateFlags{
    	
    }
    
}
