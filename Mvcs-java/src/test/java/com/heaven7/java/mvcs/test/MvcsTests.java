package com.heaven7.java.mvcs.test;

import java.util.List;

import com.heaven7.java.mvcs.IController;
import com.heaven7.java.mvcs.Message;
import com.heaven7.java.mvcs.SimpleController;
import com.heaven7.java.mvcs.SimpleState;
import com.heaven7.java.mvcs.util.ResultAction;

import junit.framework.TestCase;

/**
 * Created by heaven7 on 2017/4/22.
 */
public class MvcsTests extends TestCase {

    static final int STATE_MOVING = 1;
    static final int STATE_EAT    = 2;
    static final int STATE_SLEEP  = 4;
    static final int STATE_EAT_MUTEX  = 8;
    
    static final int STATE_UNKNOWN  = 32;
    private SimpleController<SimpleState<String>,String> mController;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mController = new SimpleController<SimpleState<String>, String>();
        mController.setStateFactory(new IController.StateFactory<SimpleState<String>, String>() {
            @Override
            public SimpleState<String> createState(int stateKey, String s) {
                switch (stateKey){
                    case STATE_EAT:
                        return  new EatState();

                    case STATE_MOVING:
                        return new MovingState();

                    case STATE_SLEEP:
                        return new SleepState();
                        
                    case STATE_EAT_MUTEX:
                    	return new MutexEatState();
                }
                return null;
            }
        });
        mController.setParameterMerger(new ParamepterMergerImpl());
    }
    
    public void testDelayMessage(){
    	final int what = 99;
    	mController.setStateCacheEnabled(true);
    	mController.addState(STATE_EAT | STATE_MOVING);
    	Message msg = Message.obtain(what, "testDelayMessage");
    	msg.setDelay(2000);
    	assertFalse(mController.sendMessage(msg, IController.POLICY_BROADCAST));

        assertTrue(mController.hasMessage(what)); 
        assertTrue(mController.hasMessage(Message.obtain(what, "testDelayMessage"))); 
    	try {
			Thread.sleep(2000);
			mController.update(0);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    public void testMessage1(){
    	mController.setStateCacheEnabled(true);
    	mController.addState(STATE_EAT);
    	mController.addState(STATE_MOVING);
    	mController.setState(STATE_MOVING | STATE_SLEEP);
    	
    	//test cache
    	Message msg = Message.obtain(99, "testMessage1");
    	assertTrue(mController.sendMessage(msg, IController.POLICY_CONSUME, 
    			IController.FLAG_SCOPE_ALL));
    	mController.addState(STATE_EAT);
    	
    	//test consume
    	msg = Message.obtain(99, "testMessage1__1");
    	assertTrue(mController.sendMessage(msg, IController.POLICY_CONSUME, 
    			IController.FLAG_SCOPE_ALL));
    	
    	//test broadcast
    	msg = Message.obtain(99, "testMessage1__1");
    	assertTrue(mController.sendMessage(msg, IController.POLICY_BROADCAST, 
    			IController.FLAG_SCOPE_ALL));
    }
    
    public void testTransaction(){
    	mController.addMutexState(new int[]{ STATE_EAT, STATE_EAT_MUTEX });
    	transactionAdd(STATE_EAT);
    	transactionAdd(STATE_EAT_MUTEX);
    }

	private void transactionAdd(int state) {
		mController.beginTransaction()
	    	.operateAdd(state)
	    	.withStartAction(new Runnable() {
				@Override
				public void run() {
					System.out.println("start action...run()");
				}
			})
	    	.withResultAction(new ResultAction<Boolean>() {
				@Override
				public void onActionResult(Boolean result) {
					System.out.println("add state " + (result ? "success" : "failed") 
							+": state = " + STATE_EAT);
				}
			})
	    	.commit();
	}
    
    public void testMutex3(){
    	mController.addState(STATE_EAT);
        mController.addState(STATE_EAT | STATE_EAT_MUTEX);
    	
        mController.addMutexState(new int[]{ STATE_EAT, STATE_EAT_MUTEX });
        mController.addState(STATE_EAT);
        
        // below all must be exception
        // mController.addState(STATE_EAT | STATE_EAT_MUTEX); 
        // mController.setGlobalState(STATE_EAT | STATE_EAT_MUTEX); 
        // mController.setState(STATE_EAT | STATE_EAT_MUTEX); 
        // mController.removeState(STATE_EAT | STATE_EAT_MUTEX); 
        // mController.hasState(STATE_EAT | STATE_SLEEP | STATE_EAT_MUTEX ); 
    }
    public void testMutex2(){
    	mController.addMutexState(new int[]{ STATE_EAT, STATE_EAT_MUTEX });
    	mController.addState(STATE_EAT);
    	mController.addState(STATE_EAT_MUTEX);
    	mController.addState(STATE_EAT | STATE_SLEEP);
    }
    
    public void testMutex(){
    	mController.setMutexState(new int[]{ STATE_EAT }, new int[]{ STATE_EAT_MUTEX});
    	mController.addState(STATE_EAT);
    	mController.addState(STATE_EAT_MUTEX);
    	mController.addState(STATE_EAT | STATE_SLEEP);
    }

    public void testDispose(){
        mController.setStateCacheEnabled(true);
        testState();
        System.out.println(mController.getCurrentStates());
        mController.dispose();
    }
    
    public void testStateCache(){
    	mController.setStateCacheEnabled(true);
    	testState();
    	System.out.println(mController.getCurrentStates());
    	mController.destroyStateCache();
    }

    public void testLockEvent(){
        assertTrue(mController.lockEvent(1));
        assertTrue(mController.unlockEvent(1));
        mController.lockEvent(1);
        assertFalse(mController.lockEvent(1));
        mController.lockEvent(2);
        mController.lockEvent(4);
        assertFalse(mController.unlockEvent(1,3));
       // assertTrue(mController.unlockEvent(1,2,4));
    }

    public void testGlobalState(){
        mController.setGlobalState(STATE_SLEEP);
        mController.setState(STATE_MOVING, "moving");
        mController.notifyStateUpdate("testGlobalState");
    }

    public void testState(){
        mController.setStateStackEnable(true);
        mController.setShareStateParam("__share__");
        mController.addState(STATE_EAT, "dfdff");
        mController.addState(STATE_MOVING |STATE_SLEEP);
        assertTrue(mController.addState(STATE_MOVING | STATE_EAT));

        System.out.println("start remove state -------------");
        assertFalse(mController.removeState(STATE_UNKNOWN));
        //mController.removeState(STATE_MOVING, "tag_remove");
        assertFalse(mController.removeState(STATE_MOVING | STATE_UNKNOWN));
        assertTrue(mController.addState(STATE_MOVING | STATE_EAT, "tag_reenter"));

        mController.clearState("tag_clear");

        System.out.println("----------- start set state -------------");
        mController.setState(STATE_EAT, "setState_eat");
        mController.setState(STATE_MOVING |STATE_SLEEP);

        System.out.println("----------- start get state -------------");
        final List<SimpleState<String>> states = mController.getCurrentStates();
        System.out.println(states);

        System.out.println(mController.getCurrentState());

        assertTrue(mController.revertToPreviousState());

        System.out.println("----------- start notify update -------------");
        mController.notifyStateUpdate("tag_update");
    }
}
