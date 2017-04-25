package com.heaven7.java.mvcs;

import com.heaven7.java.mvcs.IController.StateFactory;
import com.heaven7.java.mvcs.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

import static com.heaven7.java.mvcs.util.MathUtil.max2K;

/**
 * the state group . manage a group of state.
 *
 * @param <P> the state parameter type.
 * @author Administrator
 */
/*public*/ final class StateGroup<S extends AbstractState<P> ,P> {

    private int mCurrentStates;
    private P mParam;
    private final Callback<S,P> mCallback;
    private final IController<S, P> mController;
    /** the cached all states without current states. that means background states.*/
	private int mCachedState;
    
    public StateGroup(IController<S, P> controller, Callback<S,P> callback) {
    	this.mController = controller;
        this.mCallback = callback;
    }
    
    //========================== easy methods ===========================
    private P getStateParameter() {
        return mParam;
    }
    private StateFactory<S,P> getStateFactory() {
        return mCallback.getStateFactory();
    }
    private SparseArray<S> getStateMap(){
        return mCallback.getStateMap();
    }
    private ParameterMerger<P> getMerger(){
        return mCallback.getMerger();
    }
    private IController<S, P> getController(){
    	return mController;
    }
    private boolean isStateCacheEnabled(){
    	return mCallback.isStateCacheEnabled();
    }
    //========================================================================
    
    public int getCachedStateFlags(){
    	return mCachedState;
    }
    
    public int getStateFlags() {
        return mCurrentStates;
    }
    public boolean hasState(int state) {
        return (getStateFlags() & state) != 0;
    }

    public boolean clearState(P param) {
        final int current = mCurrentStates;
        if(current == 0){
            return false;
        }
        this.mCurrentStates = 0;
        this.mParam = param;
        dispatchStateChange(current, 0);
        this.mParam = null;
        return true;
    }
    public boolean removeState(int states, P param) {
        if (states <= 0) return false;
        this.mCurrentStates &= ~states;
        this.mParam = param;
        dispatchStateChange(0, 0, states);
        this.mParam = null;
        return true;
    }

    public boolean addState(int states, P extra) {
        if (states <= 0) return false;
        //no change.
        final int shareFlags = mCurrentStates & states;
        if (shareFlags == states) {
            return false;
        }
        this.mParam = extra;
        this.mCurrentStates |= states;
        dispatchStateChange(shareFlags, states & ~shareFlags, 0);
        this.mParam = null;
        return true;
    }

    public boolean setStates(int newStates, P p) {
        if (newStates <= 0 ) return false;
        final int mCurr = this.mCurrentStates;
        if (mCurr == newStates) {
            return false;
        }
        this.mCurrentStates = newStates;
        this.mParam = p;
        dispatchStateChange(mCurr, newStates);
        mParam = null;
        return true;
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

    /**
     * dispatch state change.
     *
     * @param shareFlags the share flags to exit().
     * @param enterFlags the enter flags to exit()
     * @param exitFlags  the exit flags to exit().
     */
    protected void dispatchStateChange(int shareFlags, int enterFlags, int exitFlags) {
        // Call the exit method of the existing state
        if (exitFlags != 0) {
            exitState(exitFlags);
        }
        // Call the entry method of the new state
        if (enterFlags != 0) {
            enterState(enterFlags);
        }
        //call reenter state
        if (shareFlags != 0) {
            reenter(shareFlags);
        }
    }

    private void reenter(int sharFlags) {
        int maxKey;
        for (; sharFlags > 0; ) {
            maxKey = max2K(sharFlags);
            if (maxKey > 0) {
                reenter0(maxKey);
                sharFlags -= maxKey;
            }
        }
    }

    private void exitState(int exitFlags) {
        int maxKey;
        for (; exitFlags > 0; ) {
            maxKey = max2K(exitFlags);
            if (maxKey > 0) {
                exit0(maxKey);
                exitFlags -= maxKey;
            }
        }
    }

    private void enterState(int enterFlags) {
        final StateFactory<S,P> factory = getStateFactory();
        final P sp = getStateParameter();
        int maxKey;
        for (; enterFlags > 0; ) {
            maxKey = max2K(enterFlags);
            if (maxKey > 0) {
                enter0(maxKey, factory.createState(maxKey, sp));
                enterFlags -= maxKey;
            }
        }
    }

    public S getStateByKey(int key) {
        return getStateMap().get(key);
    }

    public int getStateCount() {
        return getStateMap().size();
    }

    private void reenter0(int singleState) {
        AbstractState<P> state = getStateMap().get(singleState);
        final P p = getMerger().merge(state.getStateParameter(), getStateParameter());
        state.setStateParameter(p);
        state.onAttach(getController());
        state.onReenter();
    }

    private void enter0(int singleState, S state) {
    	//cache state 
    	mCachedState &= ~singleState;
        getStateMap().put(singleState, state);
        final P p = getMerger().merge(state.getStateParameter(), getStateParameter());
        state.setStateParameter(p);
        state.onAttach(getController());
        state.onEnter();
    }

    private void exit0(int singleState) {
        final SparseArray<S> stateMap = getStateMap();
        AbstractState<P> state = stateMap.get(singleState);
        // no cache ? remove from cache
        if(!isStateCacheEnabled()){
            stateMap.remove(singleState);
            mCachedState &= ~singleState;
        }else{
        	mCachedState |= singleState;
        }
        final P p = getMerger().merge(state.getStateParameter(), getStateParameter());
        state.setStateParameter(p);
        state.onExit();
        state.onDetach();
    }

    public List<S> getStates() {
        if(mCurrentStates == 0){
            return null;
        }
        final List<S> list = new ArrayList<S>();
        int curFlags = this.mCurrentStates;
        int maxKey;
        for (; curFlags > 0; ) {
            maxKey = max2K(curFlags);
            if (maxKey > 0) {
                list.add(getStateByKey(maxKey));
                curFlags -= maxKey;
            }
        }
        return list;
    }
    /** get max state.
     * @return  the max state. */
    public S getMaxState() {
        if(mCurrentStates == 0){
            return null;
        }
        int maxKey = max2K(this.mCurrentStates);
        return getStateByKey(maxKey);
    }

    public void notifyStateUpdate(P param) {
        final List<S> states = getStates();
        if(states != null ) {
            for (S s : states) {
                s.onUpdate(param);
            }
        }
    }
    
    /** destroy state cache without current states. */
    public void destroyStateCache() {
    	if(mCachedState > 0){
	    	 final SparseArray<S> map = getStateMap();
	    	 int curFlags = this.mCachedState;
	         int maxKey;
	         for (; curFlags > 0 ; ) {
	             maxKey = max2K(curFlags);
	             if (maxKey > 0) {
	            	 map.remove(maxKey);
	            	// System.out.println("destroy state = " + maxKey);
	                 curFlags -= maxKey;
	             }
	         }
    	}/*else{
    		System.out.println("no state cache...");
    	}*/
	}

    public interface Callback<S extends AbstractState<P>, P>{
        ParameterMerger<P> getMerger();
        StateFactory<S,P> getStateFactory();
        SparseArray<S> getStateMap();
        boolean isStateCacheEnabled();
    }

}
