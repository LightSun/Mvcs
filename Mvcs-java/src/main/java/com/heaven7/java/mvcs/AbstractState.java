package com.heaven7.java.mvcs;

public abstract class AbstractState<P extends StateParameter> {

	private P mParam;

	public P getStateParameter() {
		return mParam;
	}

	public void setStateParameter(P p) {
		this.mParam = p;
	}
	
	public void mergeStateParameter(P p){
	    if(mParam == null){
	    	mParam = p;
	    }else{
	    	mParam.merge(p);
	    }
	}
	
	/**
	 * this is called on enter this state.
	 */
	public abstract void onEnter();


	/**
	 * this is called on reenter this state.
	 */
	public abstract void onReenter();

	/**
	 * this is called on exit this state.
	 */
	public abstract void onExit();

	public void onUpdate(P param) {
	}
}
