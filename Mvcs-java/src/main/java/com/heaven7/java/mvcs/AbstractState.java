package com.heaven7.java.mvcs;

/**
 * the abstract state which is controlled by {@link IController}
 * @param <P> the state parameter type.
 */
public abstract class AbstractState<P> implements Disposeable{

	private IController<? extends AbstractState<P>, P> mController;
	private P mParam;

	/**
	 * called on attach this state. you shouldn't call it.
	 * @param controller the controller.
	 */
	 <S extends AbstractState<P>> void onAttach(IController<S, P> controller){
		this.mController = controller;
	}
	
	/**
	 * called on detach this state.
	 * you shouldn't call it. 
	 */
	void onDetach(){
		this.mController = null;
	}
	
	public Object getOwner(){
		if(mController == null){
			throw new IllegalStateException("state haven't attach or is detached.");
		}
		return mController.getOwner();
	}
	
	/**
	 * get current controller.
	 * @param <S> state type
	 * @return the current controller.
	 * @see IController
	 */
	@SuppressWarnings("unchecked")
	public <S extends AbstractState<P>> IController<S, P> getController(){
		if(mController == null){
			throw new IllegalStateException("state haven't attach or is detached.");
		}
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
