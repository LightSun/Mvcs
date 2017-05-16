package com.heaven7.java.mvcs;

import com.heaven7.java.base.util.Objects;

/**
 * the message communicate between out side to {@linkplain AbstractState}, by calling {@linkplain IController#sendMessage(Message, byte, byte)}.
 * @author heaven7
 * @since 1.1.6
 */
public final class Message {

	  /**
     * User-defined message code so that the recipient can identify 
     * what this message is about. 
     */
    public int what;

    /**
     * arg1 and arg2 are lower-cost alternatives to using
     * {@link #setData(Object)} if you only need to store a
     * few integer values.
     */
    public int arg1; 

    /**
     * arg1 and arg2 are lower-cost alternatives to using
     * {@link #setData(Bundle) setData()} if you only need to store a
     * few integer values.
     */
    public int arg2;

    /**
     * An arbitrary object to send to the recipient. For other data transfer use
     * {@link #setData}.
     */
    public Object obj;
    
    /**
     * extra data.
     */
    public Object data;
    
    /**
     * indicate the message will be handled in future or right now. 
     */
    public long when;
    
    /**
     * indicate the message handler should reply this message.
     */
    public MessageReplier replier;
    
    /** If set message is in use.
     * This flag is set when the message is enqueued and remains set while it
     * is delivered and afterwards when it is recycled.  The flag is only cleared
     * when a new message is created or obtained since that is the only time that
     * applications are allowed to modify the contents of the message.
     *
     * It is an error to attempt to enqueue or recycle a message that is already in use.
     */
    private static final int FLAG_IN_USE = 1 << 0;
    
    private int flags;
    
    private Message next;
    private static final Object sPoolSync = new Object();
    private static Message sPool;
    private static int sPoolSize = 0;

    private static final int MAX_POOL_SIZE = 20;
    
    private Message (){}
    
    /**
     * Return a new Message instance from the global pool. Allows us to
     * avoid allocating new objects in many cases.
     * @param what User-defined message code so that the recipient can identify what this message is about.
     * @param obj  the entity of the message to carry.
     */
    public static Message obtain(int what, Object obj){
    	return obtain(what, 0 , obj, null);
    }
    
    /**
     * Return a new Message instance from the global pool. Allows us to
     * avoid allocating new objects in many cases.
     * @param what User-defined message code so that the recipient can identify what this message is about.
     * @param obj  the entity of the message to carry.
     * @param replier  the message replier.
     */
    public static Message obtain(int what, Object obj, MessageReplier replier){
    	return obtain(what, 0, obj, replier);
    }
    
    /**
     * Return a new Message instance from the global pool. Allows us to
     * avoid allocating new objects in many cases.
     * @param what User-defined message code so that the recipient can identify what this message is about.
     * @param arg1 the extra User-defined code.
     * @param obj  the entity of the message to carry.
     * @param replier  the message replier.
     */
    public static Message obtain(int what, int arg1, Object obj, MessageReplier replier){
    	Message msg = obtain();
    	msg.what = what;
    	msg.arg1 = arg1;
    	msg.obj = obj;
    	msg.replier = replier;
    	return msg;
    }
    
    /**
     * Return a new Message instance from the global pool. Allows us to
     * avoid allocating new objects in many cases.
     */
    public static Message obtain() {
        synchronized (sPoolSync) {
            if (sPool != null) {
                Message m = sPool;
                sPool = m.next;
                m.next = null;
                m.flags = 0; // clear in-use flag
                sPoolSize--;
                return m;
            }
        }
        return new Message();
    }
    
    /**
     * obtain a message from pool and copy the target data to it.
     * @param other the other message.
     * @return the message.
     */
    public static Message obtain(Message other){
    	Message msg = obtain();

    	msg.what = other.what;
    	msg.when = other.when;
    	msg.arg1 = other.arg1;
    	msg.arg2 = other.arg2;
    	
    	msg.data = other.data;
    	msg.obj = other.obj;
    	msg.replier = other.replier;
    	return msg;
    }
    
    /**
     * set the delay of this message to be handled.
     * @param delayMillseconds the delay in millseconds.
     */
    public void setDelay(long delayMillseconds){
    	this.when = System.currentTimeMillis() + delayMillseconds;
    }
    /**
     * Return a Message instance to the global pool.
     * <p>
     * You MUST NOT touch the Message after calling this function because it has
     * effectively been freed.  It is an error to recycle a message that is currently
     * enqueued or that is in the process of being delivered to a Handler.
     * </p>
     */
    public void recycle() {
        if (isInUse()) {
            throw new IllegalStateException("This message cannot be recycled because it "
                        + "is still in use.");
        }
        recycleUnchecked();
    }

    /**
     * Recycles a Message that may be in-use.
     * Used internally by the MessageQueue and Looper when disposing of queued Messages.
     */
    /*public*/ void recycleUnchecked() {
        // Mark the message as in use while it remains in the recycled object pool.
        // Clear out all other details.
        flags = FLAG_IN_USE;
        what = 0;
        arg1 = 0;
        arg2 = 0;
        when = 0;
        
        obj = null;
        data = null;
        replier = null;

        synchronized (sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                next = sPool;
                sPool = this;
                sPoolSize++;
            }
        }
    }
    
	public boolean isInUse(){
    	return (flags & FLAG_IN_USE) != 0;
    }
    /*package*/ void markInUse() {
        flags |= FLAG_IN_USE;
    }
    
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		//when, replier
		Message other = (Message) obj;
		
		if (what != other.what)
			return false;
		if (arg1 != other.arg1)
			return false;
		if (arg2 != other.arg2)
			return false;
		
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		
		if (this.obj == null) {
			if (other.obj != null)
				return false;
		} else if (!this.obj.equals(other.obj))
			return false;
		
		return true;
	}

	@Override
    public String toString() {
    	return Objects.toStringHelper(this)
    	 .add("what", what)
    	 .add("arg1", arg1)
    	 .add("arg2", arg2)
    	 .add("when", when)
    	 
    	 .add("obj", obj)
    	 .add("data", data)
    	 .add("replier", replier)
    	 .add("in-use", isInUse())
    	 .toString();
    }
    
    /**
     * the message replier
     * @since 1.1.6
     */
    public interface MessageReplier{
    	/**
    	 * called when handle message success and want to reply it.
    	 * @param msg the reply message
    	 */
    	void reply(Message msg);
    }
}
