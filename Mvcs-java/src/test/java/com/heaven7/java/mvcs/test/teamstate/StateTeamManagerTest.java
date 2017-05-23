package com.heaven7.java.mvcs.test.teamstate;

import java.util.ArrayList;
import java.util.List;

import com.heaven7.java.base.util.PropertyBundle;
import com.heaven7.java.mvcs.StateTeamManager.Member;
import com.heaven7.java.mvcs.impl.DefaultController;
import com.heaven7.java.mvcs.impl.DefaultStateTeamManager;

import junit.framework.TestCase;

public class StateTeamManagerTest extends TestCase {

	public static final int STATE_MOVE = 1; //team 2 not have
	public static final int STATE_EAT = 2;
	public static final int STATE_SLEEP = 4;
	
	public static final int STATE_ALL = STATE_MOVE| STATE_EAT | STATE_SLEEP;

	final DefaultStateTeamManager mJsTm = DefaultStateTeamManager.getDefault();
	DefaultController mJC1 ;
	DefaultController mJC2 ;
	DefaultController mJC3 ;
	private int mTeamId;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mJC1 = new DefaultController();
		mJC2 = new DefaultController();
		mJC3 = new DefaultController();
		
		mJC1.setStateFactory(new CommonFactory(1));
		mJC2.setStateFactory(new CommonFactory(2));
		mJC3.setStateFactory(new CommonFactory(3));
		//set default disable team
		setTeamEnabled(false);
		
		mTeamId = mJsTm.registerTeam(createMembers());
	}

	public void testTeamEnter(){
		// default team is disabled.
		mJC1.addState(STATE_MOVE);
		mJC2.addState(STATE_EAT);
		mJC3.addState(STATE_SLEEP|STATE_EAT);
	
		System.out.println("============== start enable team ===========");
		setTeamEnabled(true);
		/**
		 * trigger the team state reenter.
		 */
		mJC1.addState(STATE_EAT);
		
		//trigger the team state exit.
		PropertyBundle bundle = new PropertyBundle();
		bundle.put("text", "trigger the team state exit");
		mJC1.removeState(STATE_EAT, bundle);
		System.out.println("dsfdsfsd");
	}
	
	public void testUnregisterTeam(){
		mJsTm.unregisterTeam(mTeamId);
	}
	
	private List<Member<PropertyBundle>> createMembers(){
		List<Member<PropertyBundle>> list = new ArrayList<>();
		list.add(DefaultStateTeamManager.createMember(mJC1, STATE_ALL));
		list.add(DefaultStateTeamManager.createMember(mJC2, STATE_ALL));
		list.add(DefaultStateTeamManager.createMember(mJC3, STATE_ALL));
		return list;
	}
	private void setTeamEnabled(boolean enabled) {
		mJC1.setTeamEnabled(enabled);
		mJC2.setTeamEnabled(enabled);
		mJC3.setTeamEnabled(enabled);
	}
}
