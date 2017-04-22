package com.heaven7.java.mvcs;

/**
 * the abstract state which is controlled by {@link IController}
 * @param <P> the state parameter type.
 */
public abstract class AbstractState<P> {

	private P mParam;

	/**
	 * get the state parameter.
	 * @return the state parameter.
     */
	public P getStateParameter() {
		return mParam;
	}

	/**
	 * set the state parameter
	 * @param p the parameter.
     */
	public void setStateParameter(P p) {
		this.mParam = p;
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

	/**
	 * called on update this state. often called by {@linkplain IController#notifyStateUpdate(ParameterMerger)}.
	 * @param param the extra parameter.
     */
	public void onUpdate(P param) {
	}
}
