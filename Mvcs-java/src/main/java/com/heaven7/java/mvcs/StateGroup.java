package com.heaven7.java.mvcs;

import static com.heaven7.java.mvcs.util.MathUtil.max2K;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import com.heaven7.java.base.anno.IntDef;
import com.heaven7.java.base.util.Disposeable;
import com.heaven7.java.base.util.SparseArray;
import com.heaven7.java.mvcs.IController.StateFactory;
import com.heaven7.java.mvcs.util.MutexStateException;

/**
 * the state group . manage a group of state.
 *
 * @param
 * 			<P>
 *            the state parameter type.
 * @author heaven7
 */
/* public */ final class StateGroup<S extends AbstractState<P>, P> implements Disposeable {
	
	private static final byte ACTION_ENTER           = 1 ;
	private static final byte ACTION_EXIT            = 2 ;
	private static final byte ACTION_REENTER         = 3 ;
	//private static final byte ACTION_HANDLE_MESSAGE  = 4 ; //not use now
	
	@IntDef({
		ACTION_ENTER, 
		ACTION_EXIT,
		ACTION_REENTER,
	})
	@Retention(RetentionPolicy.SOURCE)
	@Target({ElementType.PARAMETER})
	@interface ActionType{
	}

	private int mCurrentStates;
	private P mParam;
	
	private final Callback<S, P> mCallback;
	private final IController<S, P> mController;
	/**
	 * the cached all states without current states. that means background
	 * states.
	 */
	private int mCachedState;
	
	/** the list which is lazy load. */
	final List<Integer> sTempFlags = new ArrayList<Integer>(6);
	
	/** if false. {@linkplain StateListener} will never call back. default is true.*/
	private boolean mTeamEnabled = true;

	public interface Callback<S extends AbstractState<P>, P> {
		
		ParameterMerger<P> getMerger();

		StateFactory<S, P> getStateFactory();

		SparseArray<S> getStateMap();

		List<S> ensureAndGetTempList();
	}

	public StateGroup(IController<S, P> controller, Callback<S, P> callback) {
		this.mController = controller;
		this.mCallback = callback;
	}

	// ========================== easy methods ===========================
	
	private P getStateParameter() {
		return mParam;
	}

	private StateFactory<S, P> getStateFactory() {
		return mCallback.getStateFactory();
	}

	private SparseArray<S> getStateMap() {
		return mCallback.getStateMap();
	}

	private ParameterMerger<P> getMerger() {
		return mCallback.getMerger();
	}

	private IController<S, P> getController() {
		return mController;
	}

	private boolean isStateCacheEnabled() {
		return mController.isStateCacheEnabled();
	}
	// ========================================================================
	
	public void setTeamEnabled(boolean enable) {
		this.mTeamEnabled = enable;
	}
	public boolean isTeamEnabled() {
		return mTeamEnabled;
	}

	public int getCachedStateFlags() {
		return mCachedState;
	}

	/**
	 * get current state flags
	 * @return current state flags. 
	 *  */
	public int getStateFlags() {
		return mCurrentStates;
	}

	public boolean hasState(int state) {
		checkMutexState(state);
		return state > 0 && (getStateFlags() & state) != 0;
	}

	public boolean clearState(P param) {
		final int current = mCurrentStates;
		if (current == 0) {
			return false;
		}
		this.mCurrentStates = 0;
		this.mParam = param;
		dispatchStateChange(current, 0);
		this.mParam = null;
		return true;
	}

	/**
	 * only if all states remove success return true.
	 * 
	 * @param states
	 *            the target states to remove
	 * @param param
	 *            the parameter
	 * @return true if remove all states success.
	 */
	public boolean removeState(int states, P param) {
		if (states <= 0)
			return false;
		checkMutexState(states);

		final int shareFlags = mCurrentStates & states;
		if (shareFlags == 0) {
			return false;
		}
		this.mCurrentStates &= ~states;
		this.mParam = param;
		dispatchStateChange(0, 0, shareFlags);
		this.mParam = null;
		return shareFlags == states;
	}
	
	//TODO should trigger mutex ?
	public void removeForgroundStateFromTeam(int states, P teamP){
		final int share = mCurrentStates & states;
		if(share == 0){
			return ;
		}
		final boolean cacheEnabled = isStateCacheEnabled();
		//handle state exit
		final SparseArray<S> stateMap = getStateMap();
		final List<S> tempList = mCallback.ensureAndGetTempList();
		getForegroundStates(share, tempList);
		for (S s : tempList) {
			s.setTeamParameter(teamP);
			s.exit(AbstractState.FLAG_TEAM);
			s.clearOnceFlags();
			s.onDetach();
			//remove state instance if need
			if(!cacheEnabled){
				stateMap.remove(s.getId());
			}
		}
		tempList.clear();
		//handle current state and cache state.
		mCurrentStates &= ~ share;
		if(cacheEnabled){
			mCachedState |= share;
		}else{
			mCachedState &= ~share;
		}
	}

	public boolean addState(int states, P extra) {
		if (states <= 0)
			return false;
		checkMutexState(states);
		// no change.
		final int shareFlags = mCurrentStates & states;
		if (shareFlags == states) {
			// reenter
		} else {
			this.mCurrentStates |= states;
		}
		this.mParam = extra;
		dispatchStateChange(shareFlags, states & ~shareFlags, 0);
		this.mParam = null;
		return true;
	}

	public boolean setStates(int newStates, P p) {
		if (newStates <= 0)
			return false;
		checkMutexState(newStates);

		final int mCurr = this.mCurrentStates;
		if (mCurr == newStates) {
			// no reenter
			return false;
		}
		this.mCurrentStates = newStates;
		this.mParam = p;
		dispatchStateChange(mCurr, newStates);
		mParam = null;
		return true;
	}

	/**
	 * dispatch the state change if need. can't call this in remove method.
	 *
	 * @param currentState
	 *            the current state before this state change.
	 * @param newState
	 *            the target or new state
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
	 * @param shareFlags
	 *            the share flags to reenter.
	 * @param enterFlags
	 *            the enter flags to enter
	 * @param exitFlags
	 *            the exit flags to exit.
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
		// call reenter state
		if (shareFlags != 0) {
			reenter(shareFlags);
		}
	}

	private void reenter(int sharFlags) {
		int maxKey;
		for (; sharFlags > 0;) {
			maxKey = max2K(sharFlags);
			if (maxKey > 0) {
				reenter0(maxKey);
				sharFlags -= maxKey;
			}
		}
	}

	private void exitState(int exitFlags) {
		int maxKey;
		for (; exitFlags > 0;) {
			maxKey = max2K(exitFlags);
			if (maxKey > 0) {
				exit0(maxKey, false);
				exitFlags -= maxKey;
			}
		}
	}

	private void enterState(int enterFlags) {
		final StateFactory<S, P> factory = getStateFactory();
		final P sp = getStateParameter();
		int maxKey;
		for (; enterFlags > 0;) {
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

	/*public int getStateCount() {
		return getStateMap().size();
	}*/

	private void reenter0(int singleState) {
		S state = getStateMap().get(singleState);
		final P p = getMerger().merge(state.getStateParameter(), getStateParameter());
		state.setStateParameter(p);
		//state.onAttach(getController());
		state.setId(singleState);
		state.reenter(0);
		dispatchStateCallback(ACTION_REENTER, singleState, state, null);
		state.clearOnceFlags();
	}

	private void enter0(int singleState, S state) {
		if (state == null) {
			throw new IllegalStateException("create state failed. Are you forget to create State " + "for state_flag = "
					+ singleState + " by StateFactory? ");
		}
		// cache state
		mCachedState &= ~singleState;
		getStateMap().put(singleState, state);
		final P p = getMerger().merge(state.getStateParameter(), getStateParameter());
		state.setStateParameter(p);
		state.onAttach(getController());
		state.setId(singleState);
		state.enter(0);
		//dispatch callback
		dispatchStateCallback(ACTION_ENTER, singleState, state, null);
		state.clearOnceFlags();

		// handle mutex states
		int[] mutexStates = getController().getMutexState(singleState);
		if (mutexStates != null) {
			final SparseArray<S> stateMap = getStateMap();
			int oppositeState = 0;
			for (int s : mutexStates) {
				// state is not the main state.
				if (s != singleState && stateMap.get(s) != null) {
					oppositeState |= s;
					exit0(s, true);
				}
			}
			this.mCurrentStates &= ~oppositeState;
			// System.out.println("mutex state occurs. Main state : " +
			// singleState + " , Mutex states : "+
			// Arrays.toString(mutexStates));
			// System.out.println("mutex state occurs. after adjust current
			// state : " + mCurrentStates);
		}
	}

	private void exit0(int singleState, boolean byMutex) {
		final SparseArray<S> stateMap = getStateMap();
		S state = stateMap.get(singleState);
		// no cache ? remove from cache
		if (!isStateCacheEnabled()) {
			stateMap.remove(singleState);
			mCachedState &= ~singleState;
		} else {
			mCachedState |= singleState;
		}
		final P p = getMerger().merge(state.getStateParameter(), getStateParameter());
		state.setStateParameter(p);
		state.exit(byMutex ? AbstractState.FLAG_MUTEX : 0);
		dispatchStateCallback(ACTION_EXIT, singleState, state, null);
		state.onDetach();
	}
	
	/**
	 *  get all foreground states instance.
	 * @param outStates can be null
	 * @return the states list
	 */
	public List<S> getForegroundStates(List<S> outStates) {
		return getForegroundStates(mCurrentStates, outStates);
	}
	/**
	 *  get the foreground states instance which is assigned by target states.
	 * @param targetStates the target states .must be overlap with current states.
	 * @param outStates can be null
	 * @return the states list
	 */
	public List<S> getForegroundStates(int targetStates, List<S> outStates) {
		//overlap state
		targetStates = mCurrentStates & targetStates;
		return getTargetStates(targetStates, outStates);
	}
	/**
	 *  get the background states instance which is assigned by target states.
	 * @param targetStates the target states .must be overlap with cached states.
	 * @param outStates can be null
	 * @return the states list
	 */
	public List<S> getBackgroundStates(int targetStates, List<S> outStates) {
		//overlap state
		targetStates = mCachedState & targetStates;
		return getTargetStates(targetStates, outStates);
	}

	/**
	 *  get all background/cache states instance.
	 * @param outStates can be null
	 * @return the states list
	 */
	public List<S> getBackgroundStates(List<S> outStates) {
		return getBackgroundStates(mCachedState, outStates);
	}

	/**
	 * get max state. And the max is indicated the by the flag digital.
	 * 
	 * @return the max state.
	 */
	public S getMaxState() {
		if (mCurrentStates == 0) {
			return null;
		}
		int maxKey = max2K(this.mCurrentStates);
		return getStateByKey(maxKey);
	}

	public void notifyStateUpdate(P param) {
		final List<S> states = getForegroundStates(null);
		if (states != null) {
			for (S s : states) {
				s.onUpdate(-1, param);
			}
		}
	}

	/** destroy state cache without current states. */
	public void destroyStateCache() {
		if (mCachedState > 0) {
			final SparseArray<S> map = getStateMap();
			getFlagsInternal(mCachedState, sTempFlags);
			for(int state : sTempFlags){
				map.remove(state);
				// System.out.println("destroy state = " + state);
			}
			sTempFlags.clear();
			mCachedState = 0;
		} /*
			 * else{ System.out.println("no state cache..."); }
			 */
	}

	public void dispose() {
		final SparseArray<S> map = getStateMap();
		getFlagsInternal(mCurrentStates, sTempFlags);
		for(int state : sTempFlags){
			final S s = map.get(state);
			//TODO should destroy foreground state.?
			s.exit(0);
			s.onDetach();
			s.dispose();
			map.remove(state);
			// System.out.println("dispose : " + s.toString());
		}
		sTempFlags.clear();
		
		this.mCurrentStates = 0;
		this.mCachedState = 0;
		this.mParam = null;
	}

	/**
	 * clear state parameter
	 * @param includeCachedState true to include cached state.
	 * @since 1.1.5
	 */
	public void clearStateParameter(boolean includeCachedState) {
		final SparseArray<S> map = getStateMap();
		getFlagsInternal(includeCachedState ? mCurrentStates | mCachedState : mCurrentStates, sTempFlags);
		for(int state : sTempFlags){
			map.get(state).setStateParameter(null);
		}
		sTempFlags.clear();
	}
	
	/**
	 * handle the target message now.
	 * @param states the target states to handle message. -1 means all.
	 * @param msg the message to handle
	 * @param policy the handle policy
	 * @param includeCache true to include cache
	 * @return true if handled the message.
	 * @since 1.1.6
	 */
	public boolean handleMessage(int states, Message msg, byte policy, boolean includeCache) {
		final SparseArray<S> map = getStateMap();
		final int curState = states == -1 ? mCurrentStates : mCurrentStates & states;
		getFlagsInternal(curState, sTempFlags);
		
		boolean handled = false;
		
		outLoop:
		switch (policy) {
		case IController.POLICY_CONSUME:
			for(int state : sTempFlags){
				if(map.get(state).handleMessage(msg)){
					handled = true;
					break outLoop;
				}
			}
			if(includeCache){
				sTempFlags.clear();
				getFlagsInternal(states == -1 ? mCachedState : mCachedState & states, sTempFlags);
				for(int state : sTempFlags){
					if(map.get(state).handleMessage(msg)){
						handled = true;
						break outLoop;
					}
				}
			}
			break;
			
		case IController.POLICY_BROADCAST:
			for(int state : sTempFlags){
				handled |= map.get(state).handleMessage(msg);
			}
			if(includeCache){
				sTempFlags.clear();
				getFlagsInternal(states == -1 ? mCachedState : mCachedState & states, sTempFlags);
				for(int state : sTempFlags){
					handled |= map.get(state).handleMessage(msg);
				}
			}
			break;

		default:
			throw new IllegalStateException("error policy = " + policy);
		}
		//clear temp
		sTempFlags.clear();
		return handled;
	}
	
	private void dispatchStateCallback(@ActionType byte action , int stateFlag, S state, Object param) {
		final StateTeamManager<P> mTeamM = getController().getTeamMediator().getStateTeamManager();
		if(mTeamEnabled && mTeamM != null){
			//final IController<S, P> controller = getController();
			switch (action) {
			case ACTION_ENTER:
				mTeamM.onEnterState(stateFlag, state);
				break;
				
			case ACTION_EXIT:
				mTeamM.onExitState(stateFlag, state);
				break;
				
			case ACTION_REENTER:
				mTeamM.onReenterState(stateFlag, state);
				break;

			default:
				System.out.println("StateGroup >>> called dispatchStateCallback(): but action can't be resolved.");
				break;
			}
		}
	}

	/**
	 * check mutex state of the target expect states.
	 * 
	 * @param expect
	 *            the expect states
	 * @throws MutexStateException
	 *             if the expect states have multi states and have mutex state.
	 * @since 1.1.2
	 */
	private void checkMutexState(int expect) throws MutexStateException {
		// check only one state.
		if (max2K(expect) == expect) {
			// System.out.println("only one state. state = " + expect);
			return;
		}
		final IController<S, P> contro = this.mController;
		int flags = expect;
		int key;
		for (; flags > 0;) {
			key = max2K(flags);
			int[] mutexStates = contro.getMutexState(key);
			if (mutexStates != null && mutexStates.length > 0) {
				for (int s : mutexStates) {
					if (s != key && (expect & s) != 0) {
						// flags &= ~ s; //cancel state
						throw new MutexStateException("check parameter ,find unexpect mutex states, "
								+ "mutex states = (" + key + ", " + s + ")");
					}
				}
			}
			flags -= key;
		}
	}
	private List<S> getTargetStates(int targetStates, List<S> outStates) {
		if (targetStates == 0) {
			return null;
		}
		if(outStates == null){
			outStates = new ArrayList<S>();
		}
		final SparseArray<S> stateMap = getStateMap();
		getFlagsInternal(targetStates, sTempFlags);
		for(int state : sTempFlags){
			S s = stateMap.get(state);
			if(s != null){
			   s.setId(state);
			   outStates.add(s);
			}else{
				System.err.println("[WARN] StateGroup >>> called [ getTargetStates()] : state not exit. state =" + state);
			}
		}
		sTempFlags.clear();
		return outStates;
	}
	private static List<Integer> getFlagsInternal(int targetFlags, List<Integer> outStates) {
		if(outStates == null){
			outStates = new ArrayList<Integer>();
		}
		//int curFlags = this.mCurrentStates;
		int maxKey;
		for (; targetFlags > 0;) {
			maxKey = max2K(targetFlags);
			if (maxKey > 0) {
				//sort ascending(up)  
				outStates.add(0, maxKey);
				targetFlags -= maxKey;
			}
		}
		return outStates;
	}


}
