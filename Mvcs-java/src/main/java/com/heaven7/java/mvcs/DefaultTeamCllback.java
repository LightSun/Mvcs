package com.heaven7.java.mvcs;

import static com.heaven7.java.mvcs.StateTeamManager.COOPERATE_METHOD_ALL;
import static com.heaven7.java.mvcs.StateTeamManager.COOPERATE_METHOD_BASE;

import java.util.Iterator;
import java.util.List;

import com.heaven7.java.mvcs.StateTeamManager.Member;
import com.heaven7.java.mvcs.StateTeamManager.Team;
import com.heaven7.java.mvcs.StateTeamManager.TeamCallback;;

/**
 * a simple implement of {@linkplain TeamCallback}.
 * @author heaven7
 *
 * @param <P> the parameter 
 * @since 1.1.8
 */
/*public*/ class DefaultTeamCllback<P> extends TeamCallback<P> {
	
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
	
	@SuppressWarnings("unchecked")
	private void enterImpl(final boolean byMutex, AbstractState<P> trigger, List<Member<P>> members) {
		
		final IController<? extends AbstractState<P>, P> triCon =trigger.getController();
		final Iterator<Member<P>> it = members.iterator();
		
		TeamDelegate<P> delegate;
		IController<? extends AbstractState<P>, P> controller;
		Member<P> member;

		for (; it.hasNext();) {
			member = it.next();
			controller = member.getController();
			if (controller == null) {
				// trim
				it.remove();
				continue;
			} else if (controller == triCon || !(controller instanceof TeamDelegate)) {
				// same controller . ignore
				continue;
			}

			delegate = (TeamDelegate<P>) controller;
			// disable dispatch recursion callback
			delegate.setStateCallbackEnabled(false);

			switch (member.getCooperateMethod()) {
			case COOPERATE_METHOD_BASE:
				// in base: mutex trigger do nothing.
				if (byMutex) {
					break;
				}
				
			case COOPERATE_METHOD_ALL:
				delegate.notifyStateEnter(member.getStates(), trigger.getStateParameter());
				break;

			default:
				System.err.println("unknown cooperate method: " + member.getCooperateMethod());
			}
			delegate.setStateCallbackEnabled(true);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void exitImpl(final boolean byMutex, AbstractState<P> trigger, List<Member<P>> members) {
		
		final IController<? extends AbstractState<P>, P> triCon =trigger.getController();
		final Iterator<Member<P>> it = members.iterator();
		
		TeamDelegate<P> delegate;
		IController<? extends AbstractState<P>, P> controller;
		Member<P> member;
		
		for (; it.hasNext();) {
			member = it.next();
			controller = member.getController();
			if (controller == null) {
				// trim
				it.remove();
				continue;
			} else if (controller == triCon || !(controller instanceof TeamDelegate)) {
				// same controller . ignore
				continue;
			}
			
			delegate = (TeamDelegate<P>) controller;
			// disable dispatch recursion callback
			delegate.setStateCallbackEnabled(false);
			
			switch (member.getCooperateMethod()) {
			case COOPERATE_METHOD_BASE:
				// in base: mutex trigger do nothing.
				if (byMutex) {
					break;
				}
				
			case COOPERATE_METHOD_ALL:
				delegate.notifyStateExit(member.getStates(), trigger.getStateParameter());
				break;
				
			default:
				System.err.println("unknown cooperate method: " + member.getCooperateMethod());
			}
			delegate.setStateCallbackEnabled(true);
		}
	}
	@SuppressWarnings("unchecked")
	private void reenterImpl(final boolean byMutex, AbstractState<P> trigger, List<Member<P>> members) {
		
		final IController<? extends AbstractState<P>, P> triCon =trigger.getController();
		final Iterator<Member<P>> it = members.iterator();
		
		TeamDelegate<P> delegate;
		IController<? extends AbstractState<P>, P> controller;
		Member<P> member;
		
		for (; it.hasNext();) {
			member = it.next();
			controller = member.getController();
			if (controller == null) {
				// trim
				it.remove();
				continue;
			} else if (controller == triCon || !(controller instanceof TeamDelegate)) {
				// same controller . ignore
				continue;
			}
			
			delegate = (TeamDelegate<P>) controller;
			// disable dispatch recursion callback
			delegate.setStateCallbackEnabled(false);
			
			switch (member.getCooperateMethod()) {
			case COOPERATE_METHOD_BASE:
				// in base: mutex trigger do nothing.
				if (byMutex) {
					break;
				}
				
			case COOPERATE_METHOD_ALL:
				delegate.notifyStateReenter(member.getStates(), trigger.getStateParameter());
				break;
				
			default:
				System.err.println("unknown cooperate method: " + member.getCooperateMethod());
			}
			delegate.setStateCallbackEnabled(true);
		}
	}
}