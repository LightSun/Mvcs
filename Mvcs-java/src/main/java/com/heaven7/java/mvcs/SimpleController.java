package com.heaven7.java.mvcs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * a simple implements of {@linkplain IController}
 * @param <S> the state type .
 * @param <P> the state parameter type
 * @see IController
 * @see AbstractState
 * @see StateParameter
 */
public class SimpleController<S extends AbstractState<P>, P extends StateParameter>
		implements IController<S,P> {

	/**
	 * the history state stack.
	 */
	private LinkedList<StateNode> mStateStack;
	private StateGroup<S,P> mGroup;
	private StateGroup<S,P> mGlobalGroup;
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
	}
	public SimpleController(StateFactory<S,P> factory) {
		setStateFactory(factory);
	}

	private void addHistory(int states, P extra){
		if(isStateStackEnable() && mLockEvents.size() < mMaxStackSize ) {
			mStateStack.offerLast(new StateNode(states, extra));
		}
	}
	private P mergeShareParam(P param){
		if(param != null){
			param.merge(mShareParam);
			return param;
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
		if(mGroup != null){
			mGroup.notifyStateUpdate(param);
		}
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
	public boolean removeState(int states) {
		checkState();
		if(mGroup.removeState(states)){
			addHistory(mGroup.getStateFlags(), null);
			return true;
		}
		return false;
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
		final StateNode node = mStateStack.pollLast();
		if(node != null){
            return mGroup.setStates(node.states, node.getParam());
		}
		return false;
	}

	@Override
	public void setGlobalState(int states) {
        setGlobalState(states, null);
	}

	@Override
	public void setGlobalState(int states, P extra) {
		if(mGlobalGroup == null) {
			mGlobalGroup = new StateGroup<S,P>(mGroup);
		}
		mGlobalGroup.setStates(states, extra);
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
		return mGroup.getCurrentStates();
	}

	@Override
	public S getCurrentState() {
		return mGroup.getCurrentState();
	}

	@Override
	public boolean lockEvent(int eventKey) {
		if(mLockEvents == null){
			mLockEvents = new ArrayList<Integer>();
		}
		if(mLockEvents.contains(eventKey)){
			return false;
		}
		mLockEvents.add(eventKey);
		return true;
	}

	@Override
	public boolean unlockEvent(int eventKey) {
		if(mLockEvents == null){
			return false;
		}
		final int index = mLockEvents.indexOf(eventKey);
		if(index != -1){
			mLockEvents.remove(index);
			return true;
		}
		return false;
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
		this.mGroup = new StateGroup<S,P>(factory);
	}

	private void checkState() {
		if(mGroup == null){
			throw new IllegalStateException("you must call setStateFactory(). first.");
		}
	}
}
