package com.heaven7.java.mvcs;

import com.heaven7.java.base.anno.CalledInternal;
import com.heaven7.java.base.anno.Deprecated;

/**
 * the abstract state which is controlled by {@link IController}.
 * <ul>
 * <li>Life Cycle Methods: {@linkplain #onEnter()} is called when enter the
 * state. {@linkplain #onReenter()} is called when reenter the state.
 * {@linkplain #onExit()} ()} is called when exit the state . after exit
 * {@linkplain #isDetached()} always return true.</li>
 * <li>Update state: see {@linkplain #onUpdate(Object)} called by
 * {@linkplain IController#notifyStateUpdate(Object)}.</li>
 * <li>Release Resource: when destroy state is called by
 * {@linkplain IController#dispose()}. see {@linkplain #dispose()} or
 * {@linkplain #onDispose()}.</li>
 * <li>Handle Message: {@linkplain #handleMessage(Message)} which comes from the
 * call of {@linkplain IController#sendMessage(Message, byte, byte)}.</li>
 * <li>The Detach state: if {@linkplain #isDetached()} return true , that means
 * this state is detached, and also may be in pool. see
 * {@linkplain IController#setStateCacheEnabled(boolean)}</li>
 * </ul>
 * 
 * @param
 * 			<P>
 *            the state parameter type.
 */
public abstract class AbstractState<P> implements Disposeable {

	/** the id of this state. often is the stateFlag of this state. */
	private int mId;
	private IController<?, P> mController;
	private P mParam;
	private boolean mDetached;

	/**
	 * called on attach this state. you shouldn't call it.
	 * 
	 * @param controller
	 *            the controller.
	 */
	void onAttach(IController<?, P> controller) {
		this.mController = controller;
		this.mDetached = false;
	}

	/**
	 * called on detach this state. you shouldn't call it.
	 */
	void onDetach() {
		this.mController = null;
		this.mDetached = true;
	}

	/**
	 * set the id of this state. called on attach.
	 * 
	 * @param id
	 *            the id ,you can consider the id is unique in the controller.
	 * @see IController
	 * @since 1.1.7
	 */
	void setId(int id) {
		this.mId = id;
	}

	/**
	 * Return true if the state has been explicitly detached from the
	 * controller. That is, {@link #onDetach} have been called.
	 */
	public final boolean isDetached() {
		return mDetached;
	}

	/**
	 * get the id of this state. often the id is the flag/key of this state.
	 * @return the id of this state.  if previous haven't attached. return 0.
	 * @since 1.1.7
	 */
	public final int getId() {
		return mId;
	}

	/**
	 * get the owner;
	 * 
	 * @return the owner
	 * @throws IllegalStateException
	 *             if the state is detached.
	 */
	public Object getOwner() {
		if (isDetached()) {
			throw new IllegalStateException("state haven't attach or is detached.");
		}
		return mController.getOwner();
	}

	/**
	 * get current controller.
	 * 
	 * @return the current controller.
	 * @see IController
	 * @throws IllegalStateException
	 *             if the state is detached.
	 */
	public IController<?, P> getController() throws IllegalStateException {
		if (isDetached()) {
			throw new IllegalStateException("state haven't attach or is detached.");
		}
		return mController;
	}

	/**
	 * get the state parameter.
	 * 
	 * @return the state parameter.
	 */
	public P getStateParameter() {
		return mParam;
	}

	/**
	 * set the state parameter
	 * 
	 * @param p
	 *            the parameter.
	 */
	public void setStateParameter(P p) {
		this.mParam = p;
	}

	/**
	 * this is called on enter this state.
	 */
	@CalledInternal
	public abstract void onEnter();

	/**
	 * this is called on reenter this state.
	 */
	@CalledInternal
	public abstract void onReenter();

	/**
	 * this is called on exit this state.
	 */
	@CalledInternal
	public abstract void onExit();

	/**
	 * called on update this state. often called by
	 * {@linkplain IController#notifyStateUpdate(Object)} .
	 * 
	 * @param param
	 *            the extra parameter.
	 */
	@CalledInternal
	public void onUpdate(P param) {
	}

	@CalledInternal
	@Deprecated("use #onDispose() instead, this will be delete in 2.x version.")
	@Override
	public void dispose() {
		onDispose();
	}

	@CalledInternal
	public void onDispose() {

	}

	/**
	 * handle the message which comes from
	 * {@linkplain IController#sendMessage(Message, byte, byte)}.
	 * <h2>Note: if you want to reply, please use {@linkplain Message#replier}
	 * </h2>
	 * 
	 * @param msg
	 *            the target in message
	 * @return true if handled.
	 * @see IController#sendMessage(Message, byte)
	 * @see IController#sendMessage(Message, byte,byte)
	 * @see {@linkplain Message#replier}
	 * @since 1.1.6
	 */
	@CalledInternal
	public boolean handleMessage(Message msg) {

		return false;
	}
}
