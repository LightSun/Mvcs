package com.heaven7.java.mvcs;

/**
 * the abstract state which is controlled by {@link IController}.
 * <ul>
 *     <li>Life Cycle Methods: {@linkplain #onEnter()} is called when enter the state.
 *          {@linkplain #onReenter()} is called when  reenter the state.
 *          {@linkplain #onExit()} ()} is called when exit the state .
 *          after exit {@linkplain #isDetached()}  always return true.
 *     </li>
 *     <li>Update state: see {@linkplain #onUpdate(Object)} called by
 *              {@linkplain IController#notifyStateUpdate(Object)}.
 *     </li>
 *     <li> Release Resource: when destroy state is called by {@linkplain IController#dispose()}.
 *        seee {@linkplain #dispose()}.
 *     </li>
 * </ul>
 * @param <P> the state parameter type.
 */
public abstract class AbstractState<P> implements Disposeable{

	private IController<?, P> mController;
	private P mParam;
	private boolean mDetached;

	/**
	 * called on attach this state. you shouldn't call it.
	 * @param controller the controller.
	 */
	 void onAttach(IController<?, P> controller){
		this.mController = controller;
		this.mDetached = false;
	}
	
	/**
	 * called on detach this state.
	 * you shouldn't call it. 
	 */
	void onDetach(){
		this.mController = null;
		this.mDetached = true;
	}

	/**
	 * Return true if the state has been explicitly detached from the UI.
	 * That is, {@link #onDetach} have been called.
	 */
	public final boolean isDetached(){
		return mDetached;
	}

	/**
	 * get the owner;
	 * @return the owner
	 * @throws IllegalStateException if the state is detached.
     */
	public Object getOwner(){
		if(isDetached()){
			throw new IllegalStateException("state haven't attach or is detached.");
		}
		return mController.getOwner();
	}
	
	/**
	 * get current controller.
	 * @return the current controller.
	 * @see IController
	 * @throws IllegalStateException if the state is detached.
	 */
	@SuppressWarnings("unchecked")
	public IController<?, P> getController() throws IllegalStateException{
		if(isDetached()){
			throw new IllegalStateException("state haven't attach or is detached.");
		}
		return mController;
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
