package com.heaven7.java.mvcs.test.teamstate;

import com.heaven7.java.base.util.PropertyBundle;
import com.heaven7.java.mvcs.Message;
import com.heaven7.java.mvcs.impl.DefaultState;

public abstract class BaseTeamState extends DefaultState {

	@Override
	public void onEnter() {
		super.onEnter();
		System.out.println(getName() + ": onEnter() " + "---> param = " + getStateParameter() 
				+ ", team param = " + getTeamParameter());
		System.err.println("is from team = " + hasFlags(FLAG_TEAM));
	}

	@Override
	public void onExit() {
		super.onExit();
		System.out.println(getName() + ": onExit() " + "---> param = " + getStateParameter()
		    + ", team param = " + getTeamParameter());
		System.err.println("is from team = " + hasFlags(FLAG_TEAM));
	}

	@Override
	public void onUpdate(long deltaTime, PropertyBundle param) {
		super.onUpdate(deltaTime, param);
		System.out.println(getName() + ": onUpdate(). param = " + param 
				+ ", team param = " + getTeamParameter());
		System.err.println("is from team = " + hasFlags(FLAG_TEAM));
	}

	@Override
	public void onReenter() {
		super.onReenter();
		System.out.println(getName() + ": onReenter() " + "---> param = " + getStateParameter()
		       + ", team param = " + getTeamParameter());
		System.err.println("is from team = " + hasFlags(FLAG_TEAM));
	}

	@Override
	public boolean handleMessage(Message msg) {
		System.out.println(getName() + ": handleMessage() ---> " + msg.toString()
		+ ", team param = " + getTeamParameter());
		System.err.println("is from team = " + hasFlags(FLAG_TEAM));
		return false;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	private String getName() {
		return getClass().getSimpleName();
	}
}
