package com.heaven7.java.mvcs;

import com.heaven7.java.mvcs.util.ResultAction;

/**
 * the state transaction. with support. add,set,remove method for {@linkplain IController}.
 * 
 * @author heaven7
 * @since 1.1.5
 * @see IController#addState(int, Object)
 * @see IController#setState(int, Object)
 * @see IController#removeState(int, Object)
 */
public abstract class StateTransaction<P> {
	
	/* * the flag of save state parameter */
	//public static final int FLAG_SAVE_STATE_PARAM = 1;
	
	/* private */ static final byte OP_ADD = 1;
	static final byte OP_REMOVE = 2;
	static final byte OP_SET = 3;
	
	/** the states to operate */
	/* private */ int mOperateStates = -1;
	/** add, set, or remove. */
	byte mOp;
	P mParam;
	
	/* * the extra flags */
	//int mFlags;

	private Runnable mStart;
	private ResultAction<Boolean> mEnd;

	StateTransaction() {}

	/**
	 * set the operate to 'add'.
	 * @param states the states to add.
	 * @param param the parameter to carry when add.
	 * @return this.
	 * @see IController#addState(int, Object)
	 */
	public StateTransaction<P> operateAdd(int states) {
		this.mOp = OP_ADD;
		this.mOperateStates = states;
		return this;
	}

	/**
	 * set the operate to 'set'.
	 * @param states the states to set.
	 * @return this.
	 * @see IController#setState(int, Object)
	 */
	public StateTransaction<P> operateSet(int states) {
		this.mOp = OP_SET;
		this.mOperateStates = states;
		return this;
	}

	/**
	 * set the operate to 'remove'.
	 * @param states the states to remove.
	 * @return this.
	 * @see IController#removeState(int, Object)
	 */
	public StateTransaction<P> operateRemove(int states) {
		this.mOp = OP_REMOVE;
		this.mOperateStates = states;
		return this;
	}

	/**
	 * add extra flags ,current have nothing effect.
	 * @param flags the flags to add
	 * @return this.
	 */
	public StateTransaction<P> addFlags(int flags) {
		//this.mFlags |= flags;
		return this;
	}

	/**
	 * set the extra parameter to operate which will called by {@linkplain IController}.
	 * @param states2Operate
	 * @return this
	 * @see IController#addState(int, Object)
	 * @see IController#setState(int, Object)
	 * @see IController#removeState(int, Object)
	 */
	public StateTransaction<P> operateParameter(P param) {
		this.mParam = param;
		return this;
	}

	/**
	 * set the result action.
	 * @param action the result action to perform
	 * @return this.
	 */
	public StateTransaction<P> withResultAction(ResultAction<Boolean> action) {
		this.mEnd = action;
		return this;
	}

	/**
	 * set the start action.
	 * @param action the start action to perform
	 * @return this.
	 */
	public StateTransaction<P> withStartAction(Runnable startAction) {
		this.mStart = startAction;
		return this;
	}

	/**
	 * commit the transaction and perform the all operations.
	 */
	public void commit() {
		if( mOp == 0 ){
			throw new IllegalStateException("you must assign the operate of IController.");
		}
		if( mOperateStates < 0){
			throw new IllegalStateException("you must assign the states to operate.");
		}
		if(mStart != null){
			mStart.run();
		}
		final boolean result = performTransaction();
		if(mEnd != null){
			mEnd.onActionResult(result);
		}
		reset();
	}
	
	/** reset transaction */
	private void reset(){
		this.mOp = 0;
		this.mOperateStates = -1;
		//this.mFlags = 0;
		this.mParam = null;
		
		this.mStart = null;
		this.mEnd = null;
	}
	
	@Override
	public String toString() {
		return "StateTransaction [mOperateStates=" + mOperateStates + ", mOp=" + mOp
				 + ", mParam=" + mParam + ", mStart=" + mStart + ", mEnd="
				+ mEnd + "]";
	}
	
	/**
	 * do perform this transaction 
	 * @return true if perform success. false otherwise.
	 */
	protected abstract boolean performTransaction();


}
