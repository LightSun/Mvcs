package com.heaven7.java.mvcs;

import static com.heaven7.java.mvcs.util.MathUtil.max2K;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.heaven7.java.mvcs.util.SparseArray;

/**
 * a simple implements of {@linkplain IController}
 * @param <S> the state type .
 * @param <P> the state parameter type
 * @see IController
 * @see AbstractState
 * @see ParameterMerger
 */
public class SimpleController<S extends AbstractState<P>, P> extends TeamDelegate<P>
		implements IController<S,P> {

	/** current state group/ */
	private final StateGroup<S,P> mGroup;
	private final StateGroup.Callback<S, P> mCallback;
	private StateGroup<S,P> mGlobalGroup;

	private final SparseArray<S> mStateMap;
	private StateFactory<S,P> mFactory;
	private ParameterMerger<P> mMerger;
	/** true to enable state cache */
	private boolean mEnableStateCache;

	/**
	 * the history state stack.
	 */
	private LinkedList<StateNode> mStateStack;
    /**  indicate the state history is enabled or not.  */
	private boolean mStateHistoryEnabled;

	/** the locked event keys */
	private ArrayList<Integer> mLockEvents;
    /**  the max size of state stack/history */
	private int mMaxStackSize = 10;

	private P mShareParam;
	/** the owner of this controller or states. */
	private Object mOwner;

	/** mutex state group 1*/
	private int[] mMutexStates1;
	/** mutex state group 2*/
	private int[] mMutexStates2;
	/** the mutex groups(key is the sum of states, value is group (indicate any one is mutex with each other).) */
	private SparseArray<int[]> mMutexMap;
	
	/** the transaction */
	private StateTransactionImpl mTransaction;
	/** the delay messages. */
	private List<MessageInfo> mDelayMessages;
	
	/** temp states */
	private List<S> mTempStates;

	private class StateNode{
		int states;
		P param;
		public StateNode(int states, P param) {
			this.states = states;
			this.param = param;
		}
        /** auto wrap share param. */
		public P getParam(){
			return mergeShareParam(param);
		}
	}
	public SimpleController(Object owner){
		this();
		setOwner(owner);
	}
	public SimpleController(){
		this.mStateMap = new SparseArray<S>();
		this.mCallback = new StateGroup.Callback<S, P>() {
			@Override
			public ParameterMerger<P> getMerger() {
				return mMerger;
			}
			@Override
			public StateFactory<S, P> getStateFactory() {
				return mFactory;
			}
			@Override
			public SparseArray<S> getStateMap() {
				return mStateMap;
			}
		};
		this.mGroup = new StateGroup<S,P>(this, mCallback);
	}

	private void addHistory(int states, P extra){
		if(isStateStackEnable() && mStateStack.size() < mMaxStackSize ) {
			mStateStack.offerLast(new StateNode(states, extra));
		}
	}
	private P mergeShareParam(P param){
		if(param != null){
			return mMerger.merge(mShareParam,  param);
		}else{
			return mShareParam;
		}
	}

	@Override
	public final void addMutexState(int[] groupState) {
		if(groupState == null || groupState.length == 0){
			throw new IllegalArgumentException();
		}
		if(mMutexMap == null){
			mMutexMap = new SparseArray<int[]>(4);
		}
		int key = 0;
		for(int s : groupState){
			key |= s;
		}
		final int[] val = mMutexMap.get(key);
		if(val == null){
			mMutexMap.put(key, groupState);
		}
	}

	@Override
	public final void setMutexState(int[] groupState1, int[] groupState2) {
		this.mMutexStates1 = groupState1;
		this.mMutexStates2 = groupState2;
	}
	@Override
	public final int[] getMutexState(int mainState) {
		if(mMutexStates1 != null && mMutexStates1.length > 0) {
			for (int state : mMutexStates1) {
				if (state == mainState) {
					return mMutexStates2;
				}
			}
		}
		if(mMutexStates2 != null && mMutexStates2.length > 0) {
			for (int state : mMutexStates2) {
				if (state == mainState) {
					return mMutexStates1;
				}
			}
		}
		final SparseArray<int[]> mMutexMap = this.mMutexMap;
		if(mMutexMap != null) {
			final int size = mMutexMap.size();
			for (int i = size - 1; i >= 0; i--) {
				if ((mMutexMap.keyAt(i) & mainState) != 0) {
					return mMutexMap.valueAt(i);
				}
			}
		}
		return null;
	}
	
	
	@Override
	public final Object getOwner() {
		return mOwner;
	}
	@Override
	public final void setOwner(Object owner) {
		if(owner == null){
			throw new NullPointerException();
		}
		this.mOwner = owner;
	}

	@Override
	public final void setShareStateParam(P param) {
		this.mShareParam = param;
	}

	@Override
	public final P getShareStateParam() {
		return mShareParam;
	}
	
	@Override
	public final void setStateCacheEnabled(boolean enable) {
		if(mEnableStateCache != enable){
		    mEnableStateCache = enable;
		}
	}

	@Override
	public final boolean isStateCacheEnabled() {
		return mEnableStateCache;
	}

	@Override
	public final void destroyStateCache() {
		if(mGlobalGroup != null){
			mGlobalGroup.destroyStateCache();
		}
		mGroup.destroyStateCache();
	}

	@Override
	public final void clearStateStack() {
		if(mStateStack != null){
			mStateStack.clear();
		}
	}

	@Override
	public final void setMaxStateStackSize(int max) {
		this.mMaxStackSize = max;
	}

	@Override
	public final int getMaxStateStackSize() {
		return mMaxStackSize;
	}

	@Override
	public final boolean isStateStackEnable() {
		return mStateHistoryEnabled;
	}

	@Override
	public final void setStateStackEnable(boolean enable) {
		mStateHistoryEnabled = enable;
		if(enable){
			 if(mStateStack == null) {
				 mStateStack = new LinkedList<StateNode>();
			 }
		 }else{
			 mStateStack.clear();
		 }
	}

	@Override
	public final void notifyStateUpdate(P param) {
		if(mGlobalGroup != null){
			mGlobalGroup.notifyStateUpdate(param);
		}
		mGroup.notifyStateUpdate(param);
	}

	@Override
	public final boolean addState(@StateFlags int states, P extra) {
		checkMemberState();
		extra = mergeShareParam(extra);
		if(mGroup.addState(states, extra)){
			addHistory(mGroup.getStateFlags(), extra);
			return true;
		}
		return false;
	}

	@Override
	public final boolean addState(@StateFlags int states) {
		return addState(states, null);
	}

	@Override
	public final boolean removeState(@StateFlags int states, P param) {
		checkMemberState();
		param = mergeShareParam(param);
		if(mGroup.removeState(states, param)){
			addHistory(mGroup.getStateFlags(), param);
			return true;
		}
		return false;
	}

	@Override
	public final boolean removeState(@StateFlags int states) {
		return removeState(states, null);
	}

	@Override
	public final void clearState() {
        clearState(null);
	}

	@Override
	public final void clearState(P param) {
		checkMemberState();
		param = mergeShareParam(param);
		if(mGroup.clearState(param)){
			addHistory(mGroup.getStateFlags(), param);
		}
	}

	@Override
	public final boolean setState(@StateFlags int newStates) {
	    return setState(newStates, null);
	}

	@Override
	public final boolean setState(@StateFlags int newStates, P extra) {
		checkMemberState();
		extra = mergeShareParam(extra);
		if(mGroup.setStates(newStates, extra)){
			addHistory(mGroup.getStateFlags(), extra);
			return true;
		}
		return false;
	}

	@Override
	public final boolean revertToPreviousState() {
		if(!isStateStackEnable()){
			throw new IllegalStateException("you must enable state stack b" +
					"y calling setStateStackEnable() first.");
		}
		checkMemberState();
		StateNode node = mStateStack.pollLast();
		if(node == null){
			return false;
		}
		final int stateFlags = mGroup.getStateFlags();
		for(;  node.states == stateFlags;){
			node = mStateStack.pollLast();
		}
        return mGroup.setStates(node.states, node.getParam());
	}

	@Override
	public final void setGlobalState(@StateFlags int states) {
        setGlobalState(states, null);
	}

	@Override
	public final void setGlobalState(@StateFlags int states, P extra) {
		if(mGlobalGroup == null) {
			mGlobalGroup = new StateGroup<S,P>(this, mCallback);
		}
		mGlobalGroup.setStates(states, extra);
	}

	@Override
	public final int getGlobalStateFlags() {
		return mGlobalGroup != null ? mGlobalGroup.getStateFlags() : 0;
	}

	@Override
	public final List<S> getGlobalStates(List<S> outStates) {
		return mGlobalGroup !=null ? mGlobalGroup.getForegroundStates(outStates) : null;
	}
	@Override
	public final List<S> getGlobalStates() {
		return mGlobalGroup !=null ? mGlobalGroup.getForegroundStates(null) : null;
	}
	@Override
	public final S getGlobalState() {
		return mGlobalGroup != null ? mGlobalGroup.getMaxState() : null;
	}

	@Override
	public final boolean isInState(@StateFlags int states) {
		checkMemberState();
		return mGroup.getStateFlags() == states;
	}

	@Override
	public final boolean hasState(@StateFlags int state) {
		checkMemberState();
		return mGroup.hasState(state);
	}

	@Override
	public final List<S> getCurrentStates() {
		return getCurrentStates(null);
	}
	
	@Override
	public final List<S> getCurrentStates(List<S> outStates) {
		checkMemberState();
		return  mGroup.getForegroundStates(outStates);
	}

	@Override
	public final S getCurrentState() {
		return mGroup.getMaxState();
	}

	@Override
	public final int getCurrentStateFlags() {
		return mGroup.getStateFlags();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Integer> getLockedEvents() {
		return mLockEvents != null ? (List<Integer>) mLockEvents.clone() : null;
	}

	@Override
	public final boolean lockEvent(int...eventKeys) {
		if(eventKeys == null || eventKeys.length == 0){
			throw new IllegalArgumentException();
		}
		if(mLockEvents == null){
			mLockEvents = new ArrayList<Integer>();
		}
		boolean result = true;
		final ArrayList<Integer> mLockEvents = this.mLockEvents;
		for(int key : eventKeys){
			if(mLockEvents.contains(key)){
				result = false;
			}else {
				mLockEvents.add(key);
			}
		}
		return result;
	}

	@Override
	public final boolean unlockEvent(int... keys) throws IllegalArgumentException {
		if(keys == null || keys.length == 0){
			throw new IllegalArgumentException();
		}
		if(mLockEvents == null){
			return false;
		}
		boolean result = true;
		int index;
		for(int key : keys){
			index = mLockEvents.indexOf(key);
			if(index != -1){
				mLockEvents.remove(index);
			}else{
				result = false;
			}
		}
		return result;
	}

	@Override
	public final boolean unlockAllEvent() {
		if(mLockEvents == null){
			return false;
		}
		mLockEvents.clear();
		return true;
	}

	@Override
	public final boolean isLockedEvent(int eventKey) {
		return mLockEvents != null && mLockEvents.contains(eventKey);
	}

	@Override
	public final void setStateFactory(StateFactory<S,P> factory) {
		if(factory == null){
			throw new NullPointerException();
		}
		this.mFactory = factory;
	}

	@Override
	public final void setParameterMerger(ParameterMerger<P> merger) {
		if(merger == null){
			throw new NullPointerException();
		}
		this.mMerger = merger;
	}
	
	@Override
	public final void dispose() {
		//destroy foreground states.
		if(mGlobalGroup != null){
			mGlobalGroup.dispose();
		}
		mGroup.dispose();

		//destroy back/cache state. and clear
		final SparseArray<S> map = this.mStateMap;
		for(int size = map.size() , i = size - 1 ; i >= 0 ;i --){
			map.valueAt(i).dispose();
		}
		map.clear();
		
		synchronized (this) {
			if(mDelayMessages != null){
				for(MessageInfo info : mDelayMessages){
					info.msg.recycleUnchecked();
				}
                mDelayMessages.clear();
			}
		}
		
		//clean up controller
		this.mOwner = null;
	}
	
	@Override
	public final StateTransaction<P> beginTransaction() {
		if(mTransaction == null){
			mTransaction = new StateTransactionImpl();
		}
		return mTransaction;
	}
	
	@Override
	public final void clearStateParameter() {
		clearStateParameter(true);		
	}
	@Override
	public final void clearStateParameter(boolean includeCachedState) {
		mGroup.clearStateParameter(includeCachedState);
	}
	@Override
	public final S getTargetState(int state) {
		return mStateMap.get(state);
	}
	
	@Override
	public final List<S> getTargetStates(int states, List<S> outStates) {
		if(outStates == null){
			outStates = new ArrayList<S>();
		}
		final SparseArray<S> map = this.mStateMap;
		int maxKey;
		S s;
		for (; states > 0;) {
			maxKey = max2K(states);
			s = map.get(maxKey);
			if(s != null){
				outStates.add(s);
			}
			states -= maxKey;
		}
		return outStates;
	}
	@Override
	public List<S> getTargetStates(int states, int scopeFlags, List<S> outStates) {
		if(outStates == null){
			outStates = new ArrayList<S>();
		}
		
		//TODO should handle double state between mGroup and mGlobalGroup?
		if((scopeFlags & FLAG_SCOPE_CURRENT) == FLAG_SCOPE_CURRENT){
			mGroup.getForegroundStates(states, outStates);
		}
		if((scopeFlags & FLAG_SCOPE_GLOBAL) == FLAG_SCOPE_GLOBAL && mGlobalGroup != null){
			mGlobalGroup.getForegroundStates(states, outStates);
		}
		if((scopeFlags & FLAG_SCOPE_CACHED) == FLAG_SCOPE_CACHED){
			mGroup.getBackgroundStates(states,outStates);
			if(mGlobalGroup != null){
				mGlobalGroup.getBackgroundStates(states, outStates);
			}
		}
		return outStates;
	}
	@Override
	public boolean sendMessage(Message msg,@PolicyType byte policy) {
		return sendMessage(msg, policy, FLAG_SCOPE_CURRENT);
	}
	
	@Override
	public boolean sendMessage(Message msg,@PolicyType byte policy, @ScopeFlags byte scope) {
		//check in use or mark it.
		if (msg.isInUse()) {
			throw new IllegalStateException(msg + " This message is already in use.");
		}
		msg.markInUse();

		//filter delay message.wait it will handle in update method.
		long now = System.currentTimeMillis();
		if(msg.when > now){
			synchronized (this) {
				if(mDelayMessages == null){
					mDelayMessages = new ArrayList<>(8);
				}
				mDelayMessages.add(new MessageInfo(msg, policy, scope));
			}
			return false;
		}
		//dispatch to state
		return dispatchMessage(msg, policy, scope);
	}
	@Override
	public void update(long deltaTime) {
		update(deltaTime, null);
	}
	@Override
	public void update(long deltaTime, P param) {
		
		final long now = System.currentTimeMillis() ;
		MessageInfo info;
		synchronized (this) {
			if(mDelayMessages != null){
				final Iterator<MessageInfo> it = mDelayMessages.iterator();
				for( ; it.hasNext() ; ){
					info = it.next();
					if(info.msg.when <= now){
						dispatchMessage(info.msg, info.policy, info.scope);
						it.remove();
					}
				}
			}
		}
		
		//update active state
		if(mTempStates == null){
			mTempStates = new ArrayList<>();
		}
		final List<S> mTempStates = this.mTempStates;
		
		getGlobalStates(mTempStates);
		getCurrentStates(mTempStates);
		for(S state : mTempStates){
			state.onUpdate(deltaTime, param);
		}
		mTempStates.clear();
	}
	
	@Override
	public void update(int activeStates, long deltaTime, P param) {
		if(mTempStates ==null){
			mTempStates = new ArrayList<>();
		}
		final List<S> mTempStates = this.mTempStates;
		getTargetStates(activeStates, FLAG_SCOPE_CURRENT | FLAG_SCOPE_GLOBAL, mTempStates);
		
		for(S state : mTempStates){
			state.addFlags(AbstractState.FLAG_TEAM);
			state.onUpdate(deltaTime, param);
			state.clearOnceFlags();
		}
		mTempStates.clear();
	}
	
	@Override
	public void clearMessages() {
		synchronized (this) {
			if(mDelayMessages != null){
				mDelayMessages.clear();
			}
		}
	}
	
	@Override
	public boolean hasMessage(Message expect) {
		synchronized (this) {
			if(mDelayMessages != null){
				return mDelayMessages.contains(new MessageInfo(expect));
			}
		}
		return false;
	}
	
	@Override
	public boolean hasMessage(int what) {
		
		MessageInfo info;
		synchronized (this) {
			if(mDelayMessages != null){
				final Iterator<MessageInfo> it = mDelayMessages.iterator();
				for( ; it.hasNext() ; ){
					info = it.next();
					if(info.msg.what == what){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public void removeMessage(int what) {
		MessageInfo info;
		synchronized (this) {
			if(mDelayMessages != null){
				final Iterator<MessageInfo> it = mDelayMessages.iterator();
				for( ; it.hasNext() ; ){
					info = it.next();
					if(info.msg.what == what){
						it.remove();
					}
				}
			}
		}
	}
	@Override
	public void removeMessage(Message expect) {
		Message msg;
		synchronized (this) {
			if(mDelayMessages != null){
				final Iterator<MessageInfo> it = mDelayMessages.iterator();
				for( ; it.hasNext() ; ){
					msg = it.next().msg;
					if(msg.equals(expect)){
						it.remove();
					}
				}
			}
		}
	}
	
	private void checkMemberState() {
		if(mFactory == null){
			throw new IllegalStateException("you must call setStateFactory(). first.");
		}
		if(mMerger == null){
			throw new IllegalStateException("you must call setParameterMerger(). first.");
		}
	}
	private boolean dispatchMessage(Message msg, byte policy, byte scope) {
		boolean handled = false;
		final boolean includeCache = (scope & FLAG_SCOPE_CACHED) != 0;
		if( (scope & FLAG_SCOPE_GLOBAL) != 0  && mGlobalGroup != null){
			handled |= mGlobalGroup.handleMessage(msg, policy, includeCache);
		}
		if(policy == POLICY_CONSUME  && handled){
			msg.recycleUnchecked();
			return true;
		}
		if((scope & FLAG_SCOPE_CURRENT) != 0 ){
			handled |= mGroup.handleMessage(msg, policy, includeCache);
		}
		//recycle
		msg.recycleUnchecked();
		return handled;
	}
	
	//======================== start internal method =============================
	
	@Override
	void setStateCallbackEnabled(boolean enable){
		mGroup.setStateCallbackEnabled(enable);
		if(mGlobalGroup != null){
			mGlobalGroup.setStateCallbackEnabled(enable);
		}
	}
	
	@Override
	void notifyStateEnter(int states, P param) {
		//enter. only online AbstractState can receive team callback. so just reenter.
		notifyStateReenter(states, param);
	}
	@Override
	void notifyStateExit(int states,P param) {
		if(mTempStates == null){
			mTempStates = new ArrayList<>();
		}
		getTargetStates(states, FLAG_SCOPE_CURRENT | FLAG_SCOPE_GLOBAL, mTempStates);
		for(S s : mTempStates){
			s.setTeamParameter(param);
			s.exit(AbstractState.FLAG_TEAM);
			s.clearOnceFlags();
		}
		mTempStates.clear();
	}
	@Override
	void notifyStateReenter(int states, P param) {
		if(mTempStates == null){
			mTempStates = new ArrayList<>();
		}
		getTargetStates(states, FLAG_SCOPE_CURRENT | FLAG_SCOPE_GLOBAL, mTempStates);
		for(S s : mTempStates){
			s.setTeamParameter(param);
			s.reenter(AbstractState.FLAG_TEAM);
			s.clearOnceFlags();
		}
		mTempStates.clear();
	}
	@Override
	void setStateListener(StateListener<P> l) {
		//register to state group(curren and global)
		mGroup.setStateListener(l);
		if(mGlobalGroup != null){
			mGlobalGroup.setStateListener(l);
		}
	}
	//======================== end internal method =============================
	
	private static class MessageInfo{
		Message msg;
		byte policy;
		byte scope;
		public MessageInfo(Message msg){
			this.msg = msg;
		}
		public MessageInfo(Message msg, byte policy, byte scope) {
			super();
			this.msg = msg;
			this.policy = policy;
			this.scope = scope;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MessageInfo other = (MessageInfo) obj;
			if (msg == null) {
				if (other.msg != null)
					return false;
			} else if (!msg.equals(other.msg))
				return false;
			return true;
		}
		
	}
	
	private class StateTransactionImpl extends StateTransaction<P>{
		@Override
		protected boolean performTransaction() {
			
			final int states = mOperateStates;
			final P param = mParam;
			
			boolean result = false;
	        switch (mOp) {
			case StateTransaction.OP_ADD:
				result = addState(states, param);
				break;
				
			case StateTransaction.OP_SET:
				result = setState(states, param);
				break;
				
			case StateTransaction.OP_REMOVE:
				result = removeState(states, param);
				break;

			default:
				System.err.println("execute StateTransaction failed. " + this.toString());
			}		
			return result;
		} 
	}
}
