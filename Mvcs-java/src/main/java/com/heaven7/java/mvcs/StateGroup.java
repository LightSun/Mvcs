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
/*public*/ final class StateGroup<S extends AbstractState<P> ,P extends StateParameter> {

    //TODO log
    private final SparseArray<S> mStateMap;
    private final StateFactory<S,P> mFactory;
    private int mCurrentStates;
    private P mParam;

    public StateGroup(StateGroup<S,P> group) {
        this(group.mFactory);
    }

    public StateGroup(StateFactory<S,P> factory) {
        this.mStateMap = new SparseArray<S>();
        this.mFactory = factory;
    }
    public P getStateParameter() {
        return mParam;
    }
    public StateFactory<S,P> getStateFactory() {
        return mFactory;
    }
    public int getStateFlags() {
        return mCurrentStates;
    }
    public boolean hasState(int state) {
        return (getStateFlags() & state) != 0;
    }
    public boolean removeState(int states) {
        if (states <= 0) return false;
        this.mCurrentStates &= ~states;
        dispatchStateChange(0, 0, states);
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
        //0 means clear state.
        if (newStates < 0 ) return false;
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
        return mStateMap.get(key);
    }

    public int getStateCount() {
        return mStateMap.size();
    }

    private void reenter0(int singleState) {
        AbstractState<P> state = mStateMap.get(singleState);
        state.onReenter();
    }

    private void enter0(int singleState, S state) {
        mStateMap.put(singleState, state);
        state.onEnter();
    }

    private void exit0(int singleState) {
        AbstractState<P> state = mStateMap.get(singleState);
        mStateMap.remove(singleState);
        state.onExit();
    }

    public List<S> getCurrentStates() {
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
    public S getCurrentState() {
        if(mCurrentStates == 0){
            return null;
        }
        int maxKey = max2K(this.mCurrentStates);
        return getStateByKey(maxKey);
    }

    public void notifyStateChanged(P param) {
        final List<S> states = getCurrentStates();
        if(states != null ) {
            for (S s : states) {
                s.onUpdate(param);
            }
        }
    }
}
