package com.heaven7.java.mvcs;

import com.heaven7.java.mvcs.StateController.StateFactory;
import com.heaven7.java.mvcs.util.SparseArray;

import java.util.List;

import static com.heaven7.java.mvcs.util.MathUtil.max2K;

/**
 * the state group . manage a group of state.
 *
 * @param <P> the state parameter type.
 * @author Administrator
 */
/*public*/ final class StateGroup<P extends StateParameter> {

    //TODO log
    private final SparseArray<AbstractState<P>> mStateMap;
    private final StateFactory<P> mFactory;
    private int mCurrentStates;
    private P mParam;

    public StateGroup(StateGroup<P> group) {
        this(group.mFactory);
    }

    public StateGroup(StateFactory<P> factory) {
        this.mStateMap = new SparseArray<AbstractState<P>>();
        this.mFactory = factory;
    }
    public P getStateParameter() {
        return mParam;
    }
    public StateFactory<P> getStateFactory() {
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
        final StateFactory<P> factory = getStateFactory();
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

    public AbstractState<P> getState(int key) {
        return mStateMap.get(key);
    }

    public int getStateCount() {
        return mStateMap.size();
    }

    private void reenter0(int singleState) {
        AbstractState<P> state = mStateMap.get(singleState);
        state.onReenter();
    }

    private void enter0(int singleState, AbstractState<P> state) {
        mStateMap.put(singleState, state);
        state.onEnter();
    }

    private void exit0(int singleState) {
        AbstractState<P> state = mStateMap.get(singleState);
        mStateMap.remove(singleState);
        state.onExit();
    }

    public List<AbstractState<P>> getState() {
        return mStateMap.getValues();
    }
}
