package com.heaven7.java.mvcs;

import com.heaven7.java.mvcs.util.SparseArray;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * a simple implements of {@linkplain IController}
 * @param <S> the state type .
 * @param <P> the state parameter type
 * @see IController
 * @see AbstractState
 * @see ParameterMerger
 */
public class SimpleController<S extends AbstractState<P>, P>
		implements IController<S,P> {

	private final StateGroup<S,P> mGroup;
	private final StateGroup.Callback<S, P> mCallback;
	private StateGroup<S,P> mGlobalGroup;

	private final SparseArray<S> mStateMap;
	private StateFactory<S,P> mFactory;
	private ParameterMerger<P> mMerger;

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
	public void setShareStateParam(P param) {
		this.mShareParam = param;
	}

	@Override
	public P getShareStateParam() {
		return mShareParam;
	}

	@Override
	public void clearStateStack() {
		if(mStateStack != null){
			mStateStack.clear();
		}
	}

	@Override
	public void setMaxStateStackSize(int max) {
		this.mMaxStackSize = max;
	}

	@Override
	public boolean isStateStackEnable() {
		return mStateHistoryEnabled;
	}

	@Override
	public void setStateStackEnable(boolean enable) {
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
	public void notifyStateUpdate(P param) {
		if(mGlobalGroup != null){
			mGlobalGroup.notifyStateUpdate(param);
		}
		mGroup.notifyStateUpdate(param);
	}

	@Override
	public boolean addState(int states, P extra) {
		checkState();
		extra = mergeShareParam(extra);
		if(mGroup.addState(states, extra)){
			addHistory(mGroup.getStateFlags(), extra);
			return true;
		}
		return false;
	}

	@Override
	public boolean addState(int states) {
		return addState(states, null);
	}

	@Override
	public boolean removeState(int states, P param) {
		checkState();
		param = mergeShareParam(param);
		if(mGroup.removeState(states, param)){
			addHistory(mGroup.getStateFlags(), param);
			return true;
		}
		return false;
	}

	@Override
	public boolean removeState(int states) {
		return removeState(states, null);
	}

	@Override
	public void clearState() {
        clearState(null);
	}

	@Override
	public void clearState(P param) {
		checkState();
		param = mergeShareParam(param);
		if(mGroup.clearState(param)){
			addHistory(mGroup.getStateFlags(), param);
		}
	}

	@Override
	public void setState(int newStates) {
	    setState(newStates, null);
	}

	@Override
	public void setState(int newStates, P extra) {
		checkState();
		extra = mergeShareParam(extra);
		if(mGroup.setStates(newStates, extra)){
			addHistory(mGroup.getStateFlags(), extra);
		}
	}

	@Override
	public boolean revertToPreviousState() {
		if(!isStateStackEnable()){
			throw new IllegalStateException("you must enable state stack b" +
					"y calling setStateStackEnable() first.");
		}
		checkState();
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
	public void setGlobalState(int states) {
        setGlobalState(states, null);
	}

	@Override
	public void setGlobalState(int states, P extra) {
		if(mGlobalGroup == null) {
			mGlobalGroup = new StateGroup<S,P>(this, mCallback);
		}
		mGlobalGroup.setStates(states, extra);
	}

	@Override
	public int getGlobalStateFlags() {
		return mGlobalGroup != null ? mGlobalGroup.getStateFlags() : 0;
	}

	@Override
	public List<S> getGlobalStates() {
		return mGlobalGroup.getStates();
	}
	@Override
	public S getGlobalState() {
		return mGlobalGroup.getMaxState();
	}

	@Override
	public boolean isInState(int states) {
		checkState();
		return mGroup.getStateFlags() == states;
	}

	@Override
	public boolean hasState(int state) {
		checkState();
		return mGroup.hasState(state);
	}

	@Override
	public List<S> getCurrentStates() {
		checkState();
		return mGroup.getStates();
	}

	@Override
	public S getCurrentState() {
		return mGroup.getMaxState();
	}

	@Override
	public int getCurrentStateFlags() {
		return mGroup.getStateFlags();
	}

	@Override
	public boolean lockEvent(int...eventKeys) {
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
	public boolean unlockEvent(int... keys) throws IllegalArgumentException {
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
	public boolean unlockAllEvent() {
		if(mLockEvents == null){
			return false;
		}
		mLockEvents.clear();
		return true;
	}

	@Override
	public boolean isLockedEvent(int eventKey) {
		return mLockEvents != null && mLockEvents.contains(eventKey);
	}

	@Override
	public void setStateFactory(StateFactory<S,P> factory) {
		if(factory == null){
			throw new NullPointerException();
		}
		this.mFactory = factory;
	}

	@Override
	public void setParameterMerger(ParameterMerger<P> merger) {
		if(merger == null){
			throw new NullPointerException();
		}
		this.mMerger = merger;
	}

	private void checkState() {
		if(mFactory == null){
			throw new IllegalStateException("you must call setStateFactory(). first.");
		}
		if(mMerger == null){
			throw new IllegalStateException("you must call setParameterMerger(). first.");
		}
	}
}
