package com.heaven7.java.mvcs;


public class Message {

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
    
    public Object data;
    
    public long when;
    
    /**
     * indicate the message handler should reply this message.
     */
    public MessageReplier reply;
    
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
        obj = null;
        data = null;
        when = 0;

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
    
    public interface MessageReplier{
    	void reply(Message msg);
    }
}
