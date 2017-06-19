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
	
	/**
	 * @since 1.2.1
	 */
	public static final byte COMPARE_TYPE_HAS    = 1;
	/**
	 * @since 1.2.1
	 */
	public static final byte COMPARE_TYPE_EQUALS = 2;
	
	/**
	 * @since 1.2.1
	 */
	public static final byte APPLY_TYPE_ADD     = 3;
	/**
	 * @since 1.2.1
	 */
	public static final byte APPLY_TYPE_REMOVE  = 4;
	/**
	 * @since 1.2.1
	 */
	public static final byte APPLY_TYPE_SET     = 5;
	
	/** the states to operate */
	/* private */ int mOperateStates = -1;
	/** add, set, or remove. */
	byte mOp;
	P mParam;
	
	/** the compare type 
	 * @since 1.2.1 
	 */
	byte mCompareType;
	/** the compare states 
	 * @since 1.2.1 
	 */
	int mCompareState;
	
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
		this.mOp = APPLY_TYPE_ADD;
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
		this.mOp = APPLY_TYPE_SET;
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
		this.mOp = APPLY_TYPE_REMOVE;
		this.mOperateStates = states;
		return this;
	}
	
	/**
	 * set the compare type
	 * @param cmpType the compare type
	 * @return this
	 * @since 1.2.1 
	 */
	public StateTransaction<P> compareType(byte cmpType){
		this.mCompareType = cmpType;
		return this;
	}
	/**
	 * set the compare states
	 * @param cmpStates the compare states
	 * @return this
	 * @since 1.2.1 
	 */
	public StateTransaction<P> compareStates(int cmpStates){
		this.mCompareState = cmpStates;
		return this;
	}
	/**
	 * set the apply type
	 * @param applyType the apply type
	 * @return this
	 * @since 1.2.1 
	 */
	public StateTransaction<P> applyType(byte applyType){
		this.mOp = applyType;
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
	 * @param param the parameter
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
	 * @param startAction the start action to perform
	 * @return this.
	 */
	public StateTransaction<P> withStartAction(Runnable startAction) {
		this.mStart = startAction;
		return this;
	}

	/**
	 * commit the transaction and perform the all operations.
	 */
	public boolean commit() {
		if( mOp == 0 ){
			throw new IllegalStateException("you must assign the operate of IController.");
		}
		if( mOperateStates < 0){
			throw new IllegalStateException("you must assign the states to operate.");
		}
		final ResultAction<Boolean> mEnd = this.mEnd;
		if(!verifyCompareType(mCompareType)){
			reset();
			if(mEnd != null){
				mEnd.onActionResult(Boolean.FALSE);
			}
			return false;
		}
		
		if(mStart != null){
			mStart.run();
		}
		final boolean result = performTransaction();
		reset();
		if(mEnd != null){
			mEnd.onActionResult(result);
		}
		return result;
	}
	
	/**
	 * verify the compare type
	 * @param type the compare type
	 * @return true if verify success. default is true.
	 * @since 1.2.1
	 */
	protected boolean verifyCompareType(byte type){
		return true;
	}
	
	/** reset transaction */
	private void reset(){
		this.mOp = 0;
		this.mOperateStates = -1;
		//this.mFlags = 0;
		this.mParam = null;
		
		this.mCompareState = 0;
		this.mCompareType = 0;
		
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
