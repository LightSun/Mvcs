package com.heaven7.java.mvcs;

import com.heaven7.java.mvcs.util.SparseArray;

import java.util.List;

public class StateControllerImpl<P extends StateParameter> implements StateController<P> {

	private final SparseArray<AbstractState<P>> mStateMap;
	private StateGroup<P> mGroup;

	public StateControllerImpl() {
		mStateMap = new SparseArray<AbstractState<P>>();
	}

	@Override
	public boolean addState(int states, P extra) {
		checkState();
		return mGroup.addState(states, extra);
	}

	@Override
	public boolean addState(int states) {
		return addState(states, null);
	}

	@Override
	public boolean removeState(int states) {
		checkState();
		return mGroup.removeState(states);
	}

	@Override
	public void setState(int newStates) {
	    setState(newStates, null);
	}

	@Override
	public void setState(int newStates, P extra) {
		checkState();
		mGroup.setStates(newStates, extra);
	}

	@Override
	public boolean revertToPreviousState() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setGlobalState(int states) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGlobalState(int states, P extra) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isInState(int states) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasState(int state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<AbstractState<P>> getState() {
		// TODO Auto-generated method stub
		return null;
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
		this.mGroup = new StateGroup<P>(factory , mStateMap);
	}

	private void checkState() {
		if(mGroup == null){
			throw new IllegalStateException("you must call setStateFactory(). first.");
		}
	}
}
