package com.heaven7.java.mvcs.test;

import com.heaven7.java.mvcs.Message;

/**
 * Created by heaven7 on 2017/4/22.
 */
public class EatState extends MovingState {
	
	@Override
	public boolean handleMessage(Message msg) {
		super.handleMessage(msg);
		if(msg.replier != null){
			msg.replier.reply(Message.obtain(1, "EatState"));
		}
		return true; // mock handle message success.
	}
}
