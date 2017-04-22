package com.heaven7.java.mvcs;

import java.util.LinkedList;
import java.util.List;

public class StateControllerImpl<P extends StateParameter> implements StateController<P> {

	/**
	 * the history state stack.
	 */
	private LinkedList<StateNode> mStateStack;
	private StateGroup<P> mGroup;
	private StateGroup<P> mGlobalGroup;

	private class StateNode{
		int states;
		P param;
		public StateNode(int states, P param) {
			this.states = states;
			this.param = param;
		}
	}
	public StateControllerImpl(){

	}
	public StateControllerImpl(StateFactory<P> factory) {
		mGroup = new StateGroup<P>(factory);
	}

	private void addHistory(int states, P extra){
		if(isStateHistoryEnable()) {
			mStateStack.offerLast(new StateNode(states, extra));
		}
	}

	@Override
	public boolean isStateHistoryEnable() {
		return mStateStack != null;
	}

	@Override
	public void setStateHistoryEnable(boolean enable) {
         if(enable){
			 if(mStateStack == null) {
				 mStateStack = new LinkedList<StateNode>();
			 }
		 }else{
			 mStateStack = null;
		 }
	}

	@Override
	public boolean addState(int states, P extra) {
		checkState();
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
		if(mGroup.setStates(newStates, extra)){
			addHistory(mGroup.getStateFlags(), null);
		}
	}

	@Override
	public boolean revertToPreviousState() {
		if(!isStateHistoryEnable()){
			throw new IllegalStateException("you must enable state stack b" +
					"y calling setStateStackEnable() first.");
		}
		final StateNode node = mStateStack.pollLast();
		if(node != null){
            return mGroup.setStates(node.states, node.param);
		}
		return false;
	}

	@Override
	public void setGlobalState(int states) {
        setGlobalState(states, null);
	}

	@Override
	public void setGlobalState(int states, P extra) {
		checkState();
		if(mGlobalGroup == null) {
			mGlobalGroup = new StateGroup<P>(mGroup);
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
	public List<AbstractState<P>> getState() {
		return mGroup.getState();
	}

	@Override
	public boolean lockEvent(int eventKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unlockEvent(int eventKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLockedEvent(int eventKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setStateFactory(StateFactory<P> factory) {
		this.mGroup = new StateGroup<P>(factory);
	}

	private void checkState() {
		if(mGroup == null){
			throw new IllegalStateException("you must call setStateFactory(). first.");
		}
	}
}
