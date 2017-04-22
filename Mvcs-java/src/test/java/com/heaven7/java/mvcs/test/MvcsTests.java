package com.heaven7.java.mvcs.test;

import com.heaven7.java.mvcs.IController;
import com.heaven7.java.mvcs.SimpleController;
import com.heaven7.java.mvcs.SimpleState;
import junit.framework.TestCase;

import java.util.List;

/**
 * Created by heaven7 on 2017/4/22.
 */
public class MvcsTests extends TestCase {

    static final int STATE_MOVING = 1;
    static final int STATE_EAT    = 2;
    static final int STATE_SLEEP  = 4;
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
                }
                return null;
            }
        });
        mController.setParameterMerger(new ParamepterMergerImpl());
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
        assertFalse(mController.addState(STATE_MOVING | STATE_EAT));

        System.out.println("start remove state -------------");
        mController.removeState(STATE_MOVING, "tag_remove");
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
