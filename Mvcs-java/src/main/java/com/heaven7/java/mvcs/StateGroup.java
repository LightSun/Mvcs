package com.heaven7.java.mvcs;

import static com.heaven7.java.mvcs.util.MathUtil.max2K;

import com.heaven7.java.mvcs.StateController.StateFactory;
import com.heaven7.java.mvcs.util.SparseArray;
/**
 * the state group . manage a group of state.
 * @author Administrator
 *
 * @param <SP> the state parameter type.
 */
/*public*/ final class StateGroup<SP extends StateParameter>{
	
	//TODO log

	private final SparseArray<AbstractState<SP>> mStateMap;
	private final StateFactory<SP> mFactory;
	private int mCurrenStates; 
	private SP mParam;

	public StateGroup(StateFactory<SP> factory) {
		this.mStateMap = new SparseArray<>();
		this.mFactory = factory;
	}
	
	public SP getStateParameter(){
		return mParam;
	}

	public StateFactory<SP> getStateFactory() {
		return mFactory;
	}
	
	public boolean addState(int states, SP extra) {
		if(states <= 0 ) return false;
		//no change.
		int shareFlags = mCurrenStates & states;
		if(shareFlags == states){
			return false;
		}
		//dispatchStateChange(shareFlags, enterFlags, exitFlags);
		return false;
	}
	
	public void setStates(int newStates, SP p){
		if(newStates <= 0 ) return;
		
		final int mCurr = this.mCurrenStates;
		this.mCurrenStates = newStates;
		this.mParam = p;
		dispatchStateChange(mCurr, newStates);
		mParam = null;
	}
	  /**
     * dispatch the state change if need.
     *
     * @param currentState the current state before this state change.
     * @param newState     the target or new state
     */
    private void dispatchStateChange(int currentState, int newState) {
        final int shareFlags = currentState & newState;
        final int enterFlags = newState & ~shareFlags;
        final int exitFlags = currentState & ~shareFlags;
        dispatchStateChange(shareFlags, enterFlags, exitFlags);
    }
    
    protected void dispatchStateChange(int shareFlags, int enterFlags, int exitFlags){
    	// Call the exit method of the existing state
        if (exitFlags != 0) {
            exitState(exitFlags);
        }
        // Call the entry method of the new state
        if (enterFlags != 0) {
        	enterState(enterFlags);
        }
        //call reenter state
        if(shareFlags != 0){
        	reenter(shareFlags);
        }
    }
    
    private void reenter(int sharFlags) {
		int maxKey;
		for( ; sharFlags > 0 ; ){
			maxKey = max2K(sharFlags);
			if(maxKey > 0){
				reenter0(maxKey);
				sharFlags -= maxKey;
			}else{
				//never reach here
			}
		}
	}

	private void exitState(int exitFlags) {
		int maxKey;
		for( ; exitFlags > 0 ; ){
			maxKey = max2K(exitFlags);
			if(maxKey > 0){
				exit0(maxKey);
				exitFlags -= maxKey;
			}else{
				//never reach here
			}
		}
	}
	private void enterState(int enterFlags) {
		final StateFactory<SP> factory = getStateFactory();
		final SP sp = getStateParameter();
		int maxKey;
		for( ; enterFlags > 0 ; ){
			maxKey = max2K(enterFlags);
			if(maxKey > 0){
				enter0(maxKey, factory.createState(maxKey, sp));
				enterFlags -= maxKey;
			}else{
				//never reach here
			}
		}
	}

	public AbstractState<SP> getState(int key) {
		return mStateMap.get(key);
	}
	
	public boolean hasKey(int key){
		return mStateMap.indexOfKey(key) >=0;
	}

	public int getStateCount() {
		return mStateMap.size();
	}
	
	private void reenter0(int singleState){
		AbstractState<SP> state = mStateMap.get(singleState);
		state.onReenter();
	}
	
	private void enter0(int singleState, AbstractState<SP> state){
		mStateMap.put(singleState, state);
		state.onEnter();
	}
	
	private void exit0(int singleState){
		AbstractState<SP> state = mStateMap.get(singleState);
		mStateMap.remove(singleState);
		state.onExit();
	}


}
