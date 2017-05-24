package com.heaven7.java.mvcs.test.teamstate;

import com.heaven7.java.mvcs.Message;

public class Team3Consume extends BaseTeamState{

	@Override
	public boolean handleMessage(Message msg) {
		super.handleMessage(msg);
		System.out.println(">>>>> message is consumed. by Team3Consume. " + msg.toString());
		return true;
	}
}
