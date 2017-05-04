package com.heaven7.java.mvcs;

import com.heaven7.java.mvcs.util.ResultAction;

/**
 * the state transaction
 * 
 * @author heaven7
 * @since 1.1.5
 */
public class StateTransaction<P> {

	/* private */ static final byte OP_ADD = 1;
	static final byte OP_REMOVE = 2;
	static final byte OP_SET = 3;

	private final TransactionExecutor<P> mCallback;

	/** the states to operate */
	/* private */ int mOperateStates = -1;
	/** add, set, or remove. */
	byte mOp;
	boolean mSaveStateParam;
	P mParam;

	private Runnable mStart;
	private ResultAction<Boolean> mEnd;

	StateTransaction(TransactionExecutor<P> callback) {
		this.mCallback = callback;
	}

	public StateTransaction<P> add() {
		this.mOp = OP_ADD;
		return this;
	}

	public StateTransaction<P> set() {
		this.mOp = OP_SET;
		return this;
	}

	public StateTransaction<P> remove() {
		this.mOp = OP_REMOVE;
		return this;
	}

	public StateTransaction<P> saveStateParameter() {
		this.mSaveStateParam = true;
		return this;
	}

	public StateTransaction<P> state(int operateStates) {
		this.mOperateStates = operateStates;
		return this;
	}

	public StateTransaction<P> parameter(P param) {
		this.mParam = param;
		return this;
	}

	public StateTransaction<P> withResultAction(ResultAction<Boolean> endAction) {
		this.mEnd = endAction;
		return this;
	}

	public StateTransaction<P> withStartAction(Runnable startAction) {
		this.mStart = startAction;
		return this;
	}

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
		final boolean result = mCallback.execute(this);
		if(mEnd != null){
			mEnd.onAction(result);
		}
		reset();
	}
	
	private void reset(){
		this.mOp = 0;
		this.mOperateStates = -1;
		this.mSaveStateParam = false;
		this.mParam = null;
		
		this.mStart = null;
		this.mEnd = null;
	}

	@Override
	public String toString() {
		return "StateTransaction [mOperateStates=" + mOperateStates + ", mOp=" + mOp
				+ ", mSaveStateParam=" + mSaveStateParam + ", mParam=" + mParam + ", mStart=" + mStart + ", mEnd="
				+ mEnd + "]";
	}



	/* public */ interface TransactionExecutor<P> {
		boolean execute(StateTransaction<P> transaction);
	}

}
