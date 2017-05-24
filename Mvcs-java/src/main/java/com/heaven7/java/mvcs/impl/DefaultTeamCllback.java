package com.heaven7.java.mvcs.impl;

import static com.heaven7.java.mvcs.StateTeamManager.COOPERATE_METHOD_ALL;
import static com.heaven7.java.mvcs.StateTeamManager.COOPERATE_METHOD_BASE;

import java.util.Iterator;
import java.util.List;

import com.heaven7.java.mvcs.AbstractState;
import com.heaven7.java.mvcs.IController;
import com.heaven7.java.mvcs.StateTeamManager.Member;
import com.heaven7.java.mvcs.StateTeamManager.Team;
import com.heaven7.java.mvcs.StateTeamManager.TeamCallback;
import com.heaven7.java.mvcs.TeamMediator;;

/**
 * a default implement of {@linkplain TeamCallback}.
 * @author heaven7
 *
 * @param <P> the parameter 
 * @since 1.1.8
 */
public class DefaultTeamCllback<P> extends TeamCallback<P> {
	
	@Override
	public void onTeamEnter(Team<P> team, AbstractState<P> trigger) {
		final boolean byMutex = trigger.hasFlags(AbstractState.FLAG_MUTEX);

		enterImpl(byMutex, trigger, team.getFormalMembers());

		List<Member<P>> outers = team.getOuterMembers();
		if (outers != null) {
			enterImpl(byMutex, trigger, outers);
		}
	}

	@Override
	public void onTeamExit(Team<P> team, AbstractState<P> trigger) {
		final boolean byMutex = trigger.hasFlags(AbstractState.FLAG_MUTEX);

		exitImpl(byMutex, trigger, team.getFormalMembers());

		List<Member<P>> outers = team.getOuterMembers();
		if (outers != null) {
			exitImpl(byMutex, trigger, outers);
		}
	}

	@Override
	public void onTeamReenter(Team<P> team, AbstractState<P> trigger) {
		final boolean byMutex = trigger.hasFlags(AbstractState.FLAG_MUTEX);

		reenterImpl(byMutex, trigger, team.getFormalMembers());

		List<Member<P>> outers = team.getOuterMembers();
		if (outers != null) {
			reenterImpl(byMutex, trigger, outers);
		}
	}
	
	private void enterImpl(final boolean byMutex, AbstractState<P> trigger, List<Member<P>> members) {
		
		final IController<? extends AbstractState<P>, P> triCon =trigger.getController();
		final Iterator<Member<P>> it = members.iterator();
		
		TeamMediator<P> mediator;
		IController<? extends AbstractState<P>, P> controller;
		Member<P> member;

		for (; it.hasNext();) {
			member = it.next();
			controller = member.getController();
			if (controller == null) {
				// trim
				it.remove();
				continue;
			} else if (controller == triCon || !controller.isTeamEnabled()) {
				// same controller . ignore
				continue;
			}

			mediator = controller.getTeamMediator();
			// disable dispatch recursion callback
			controller.setTeamEnabled(false);

			switch (member.getCooperateMethod()) {
			case COOPERATE_METHOD_BASE:
				// in base: mutex trigger do nothing.
				if (byMutex) {
					break;
				}
				
			case COOPERATE_METHOD_ALL:
				onNotifyStateEnter(mediator, member.getStates(), trigger.getStateParameter());
				break;

			default:
				System.err.println("unknown cooperate method: " + member.getCooperateMethod());
			}
			controller.setTeamEnabled(true);
		}
	}
	
	private void exitImpl(final boolean byMutex, AbstractState<P> trigger, List<Member<P>> members) {
		
		final IController<? extends AbstractState<P>, P> triCon =trigger.getController();
		final Iterator<Member<P>> it = members.iterator();
		
		TeamMediator<P> mediator;
		IController<? extends AbstractState<P>, P> controller;
		Member<P> member;
		
		for (; it.hasNext();) {
			member = it.next();
			controller = member.getController();
			if (controller == null) {
				// trim
				it.remove();
				continue;
			} else if (controller == triCon || !controller.isTeamEnabled()) {
				// same controller . ignore
				continue;
			}
			mediator = controller.getTeamMediator();
			// disable dispatch recursion callback
			controller.setTeamEnabled(false);
			
			switch (member.getCooperateMethod()) {
			case COOPERATE_METHOD_BASE:
				// in base: mutex trigger do nothing.
				if (byMutex) {
					break;
				}
				
			case COOPERATE_METHOD_ALL:
				onNotifyStateExit(mediator, member.getStates(), trigger.getStateParameter());
				break;
				
			default:
				System.err.println("unknown cooperate method: " + member.getCooperateMethod());
			}
			controller.setTeamEnabled(true);
		}
	}
	private void reenterImpl(final boolean byMutex, AbstractState<P> trigger, List<Member<P>> members) {
		final IController<? extends AbstractState<P>, P> triCon =trigger.getController();
		final Iterator<Member<P>> it = members.iterator();
		
		TeamMediator<P> mediator;
		IController<? extends AbstractState<P>, P> controller;
		Member<P> member;
		
		for (; it.hasNext();) {
			member = it.next();
			controller = member.getController();
			if (controller == null) {
				// trim
				it.remove();
				continue;
			} else if (controller == triCon || !controller.isTeamEnabled()) {
				// same controller . ignore
				continue;
			}
			
			mediator = controller.getTeamMediator();
			// disable dispatch recursion callback
			controller.setTeamEnabled(false);
			
			switch (member.getCooperateMethod()) {
			case COOPERATE_METHOD_BASE:
				// in base: mutex trigger do nothing.
				if (byMutex) {
					break;
				}
				
			case COOPERATE_METHOD_ALL:
				onNotifyStateReenter(mediator, member.getStates(), trigger.getStateParameter());
				break;
				
			default:
				System.err.println("unknown cooperate method: " + member.getCooperateMethod());
			}
			controller.setTeamEnabled(true);
		}
	}
	
	/**
	 * called on notify team state exit.
	 * @param mediator the team mediator.
	 * @param states the states which is already added to the team.
	 * @param param the team parameter.
	 */
	protected void onNotifyStateExit(TeamMediator<P> mediator, int states, P param){
		mediator.notifyStateExit(states, param);
	}
	/**
	 * called on notify team state enter.
	 * @param mediator the team mediator.
	 * @param states the states which is already added to the team.
	 * @param param the team parameter.
	 */
	protected void onNotifyStateEnter(TeamMediator<P> mediator, int states, P param){
		mediator.notifyStateEnter(states, param);
	}
	/**
	 * called on notify team state reenter.
	 * @param mediator the team mediator.
	 * @param states the states which is already added to the team.
	 * @param param the team parameter.
	 */
	protected void onNotifyStateReenter(TeamMediator<P> mediator, int states, P param){
		mediator.notifyStateReenter(states, param);
	}
}