package com.heaven7.java.mvcs;

import java.util.List;
import static com.heaven7.java.mvcs.util.MathUtil.max2K;

public class StateControllerImpl<P extends StateParameter> implements StateController<P> {
	
	private StateGroup<P> mGroup;

	@Override
	public boolean addState(int states, P extra) {
		checkState();
		return mGroup.addState(states, extra);
	}

	private void checkState() {
		if(mGroup == null){
			throw new IllegalStateException("you must call setStateFactory(). first.");
		}
	}

	@Override
	public boolean addState(int states) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeState(int states) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setState(int newStates) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setState(int newStates, P extra) {
		// TODO Auto-generated method stub
		
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
		this.mGroup = new StateGroup<P>(factory);
	}
	


}
