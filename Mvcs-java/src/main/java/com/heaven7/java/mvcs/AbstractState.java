package com.heaven7.java.mvcs;

/**
 * the abstract state which is controlled by {@link IController}
 * @param <P> the state parameter type.
 */
public abstract class AbstractState<P> implements Disposeable{

	private P mParam;
	private IController<? extends AbstractState<P>, P> mController;

	<S extends AbstractState<P>> void onAttach(IController<S, P> controller){
		this.mController = controller;
	}
	
	void onDetach(){
		this.mController = null;
	}
	
	/**
	 * get current controller.
	 * @param <S> state type
	 * @return the current controller.
	 * @see IController
	 */
	@SuppressWarnings("unchecked")
	public <S extends AbstractState<P>> IController<S, P> getController(){
		return (IController<S, P>) mController;
	}
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
	 * called on update this state. often called by {@linkplain IController#notifyStateUpdate(Object)} .
	 * @param param the extra parameter.
     */
	public void onUpdate(P param) {
	}

	@Override
	public void dispose() {

	}
}
