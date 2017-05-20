package com.heaven7.java.mvcs;

import com.heaven7.java.base.anno.CalledInternal;
import com.heaven7.java.base.anno.Deprecated;
import com.heaven7.java.base.util.Disposeable;

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
	
	/** flag of detached state. long time flag */
	public static final int FLAG_ATTACH = 0x0001;
	/** flag of triggered by mutex. once flag. */
	public static final int FLAG_MUTEX  = 0x0002;
	/** flag of this state is notify(enter/exit/reenter) from a team, once flag.*/
	public static final int FLAG_TEAM   = 0x0004;

	/** the id of this state. often is the stateFlag of this state. */
	private int mId;
	private IController<?, P> mController;
	private P mParam;
	/** indicate the param is from team callbak. */
	private P mTeamParam;
	
	/**
	 * @since 1.1.8
	 */
	private int mFlags;
	/** the whole enter count. if state was exited the count will be zero.
	 * @see AbstractState#onEnter()
	 * @see AbstractState#onReenter()
	 **/
	private int mRealEnterCount;

	/**
	 * called on attach this state. you shouldn't call it.
	 * 
	 * @param controller
	 *            the controller.
	 */
	void onAttach(IController<?, P> controller) {
		this.mController = controller;
		this.mFlags |= FLAG_ATTACH;
	}

	/**
	 * called on detach this state. you shouldn't call it.
	 */
	void onDetach() {
		this.mController = null;
		//clear attach flag and mutex flag.
		this.mFlags = 0; 
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
	 * enter this state with target flags.
	 * @param flags the target flags
	 * @since 1.1.8
	 */
	void enter(int flags){
		mRealEnterCount ++;
		if(flags > 0){
		    addFlags(flags);
		}
		onEnter();
	}
	/**
	 * reenter this state with target flags.
	 * @param flags the target flags
	 * @since 1.1.8
	 */
	void reenter(int flags){
		mRealEnterCount ++;
		if(flags > 0){
		    addFlags(flags);
		}
		onReenter();
	}
	/**
	 * exit this state with target flags.
	 * @param flags the target flags
	 * @since 1.1.8
	 */
	void exit(int flags){
		mRealEnterCount = 0;
		if(flags > 0){
		    addFlags(flags);
		}
		onExit();
	}
	/**
	 * clear the temp/once flags.
	 * @since 1.1.8
	 */
	void clearOnceFlags(){
		mFlags &= ~(FLAG_MUTEX | FLAG_TEAM);
	}
	
	/**
	 * add some flags for this state. this is often called internal.
	 * @param flags the flags to add.
	 * @since 1.1.8
	 */
	/*public*/ final void addFlags(int flags){
		this.mFlags |= flags;
	}
	
	/**
	 * indicate  this state has some flags or not
	 * @param flags the target flags
	 * @return true if has the flags
	 */
	public final boolean hasFlags(int flags){
		return (mFlags & flags ) != 0;
	}
	
	/**
	 * get the whole enter count, include enter and reenter. if is exited it will be zero.
	 * @return the whole enter count.
	 * @since 1.1.8
	 */
	public final int getEnterCount(){
		return mRealEnterCount;
	}
	
	/**
	 * Return true if the state has been explicitly detached from the
	 * controller. That is, {@link #onDetach} have been called.
	 */
	public final boolean isDetached() {
		return (mFlags & FLAG_ATTACH) != FLAG_ATTACH;
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
	 * get the team parameter.
	 * 
	 * @return the state parameter.
	 */
	public P getTeamParameter() {
		return mTeamParam;
	}

	/**
	 * set the team parameter
	 * 
	 * @param p
	 *            the parameter.
	 */
	public void setTeamParameter(P p) {
		this.mTeamParam = p;
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
	protected abstract void onEnter();

	/**
	 * this is called on reenter this state.
	 */
	protected abstract void onReenter();

	/**
	 * this is called on exit this state.
	 */
	protected abstract void onExit();

	
	/**
	 * called on update this state. often called by
	 * {@linkplain IController#notifyStateUpdate(Object)} .
	 * 
	 * @param deltaTime the delta time between last update and now.
	 * @param param
	 *            the extra parameter.
	 * @since 1.1.8           
	 */
	protected void onUpdate(long deltaTime, P param) {
		onUpdate(param);
	}
	/**
	 * called on update this state. often called by
	 * {@linkplain IController#notifyStateUpdate(Object)} .
	 * 
	 * @param param
	 *            the extra parameter.
	 */
	@Deprecated("please use #onUpdate(long deltaTime, P param) instead.")
	protected void onUpdate(P param) {
	}

	@CalledInternal
	@Deprecated("use #onDispose() instead, this will be delete in 2.x version.")
	@Override
	public final void dispose() {
		onDispose();
	}

	/**
	 * called on dispose this state.
	 */
	@CalledInternal
	protected void onDispose() {

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
