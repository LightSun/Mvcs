package com.heaven7.java.mvcs;

import static com.heaven7.java.mvcs.TeamManager.COOPERATE_METHOD_ALL;
import static com.heaven7.java.mvcs.TeamManager.COOPERATE_METHOD_BASE;

import java.util.Iterator;
import java.util.List;

import com.heaven7.java.mvcs.TeamManager.Member;
import com.heaven7.java.mvcs.TeamManager.Team;
import com.heaven7.java.mvcs.TeamManager.TeamCallback;;

/**
 * the simple impl of {@linkplain TeamCallback}.
 * @author heaven7
 *
 * @param <P> the parameter 
 * @since 1.1.8
 */
public class SimpleTeamCllback<P> extends TeamCallback<P> {

	@Override
	public void onTeamEnter(Team<P> team, AbstractState<P> trigger) {
		final boolean byMutex = trigger.hasFlags(AbstractState.FLAG_MUTEX);
		final IController<?, P> triCon = trigger.getController();

		enterImpl(byMutex, triCon, team.getFormalMembers());

		List<Member<P>> outers = team.getOuterMembers();
		if (outers != null) {
			enterImpl(byMutex, triCon, outers);
		}
	}

	@Override
	public void onTeamExit(Team<P> team, AbstractState<P> trigger) {
		final boolean byMutex = trigger.hasFlags(AbstractState.FLAG_MUTEX);
		final IController<?, P> triCon = trigger.getController();

		exitImpl(byMutex, triCon, team.getFormalMembers());

		List<Member<P>> outers = team.getOuterMembers();
		if (outers != null) {
			exitImpl(byMutex, triCon, outers);
		}
	}

	@Override
	public void onTeamReenter(Team<P> team, AbstractState<P> trigger) {
		final boolean byMutex = trigger.hasFlags(AbstractState.FLAG_MUTEX);
		final IController<?, P> triCon = trigger.getController();

		reenterImpl(byMutex, triCon, team.getFormalMembers());

		List<Member<P>> outers = team.getOuterMembers();
		if (outers != null) {
			reenterImpl(byMutex, triCon, outers);
		}
	}
	
	private void reenterImpl(final boolean byMutex, IController<?, P> triCon, List<Member<P>> members) {

		final Iterator<Member<P>> it = members.iterator();
		TeamDelegate delegate;
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

			delegate = (TeamDelegate) controller;
			// disable dispatch recursion callback
			delegate.setEnableStateCallback(false);

			switch (member.getCooperateMethod()) {
			case COOPERATE_METHOD_BASE:
				// in base: mutex trigger do nothing.
				if (byMutex) {
					break;
				}
				
			case COOPERATE_METHOD_ALL:
				delegate.notifyStateReenter(member.getStates());
				break;

			default:
				System.err.println("unknown cooperate method: " + member.getCooperateMethod());
			}
			delegate.setEnableStateCallback(true);
		}
	}

	private void exitImpl(final boolean byMutex, IController<?, P> triCon, List<Member<P>> members) {

		final Iterator<Member<P>> it = members.iterator();
		TeamDelegate delegate;
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

			delegate = (TeamDelegate) controller;
			// disable dispatch recursion callback
			delegate.setEnableStateCallback(false);

			switch (member.getCooperateMethod()) {
			case COOPERATE_METHOD_BASE:
				// in base: mutex trigger do nothing.
				if (byMutex) {
					break;
				}
				
			case COOPERATE_METHOD_ALL:
				delegate.notifyStateExit(member.getStates());
				break;

			default:
				System.err.println("unknown cooperate method: " + member.getCooperateMethod());
			}
			delegate.setEnableStateCallback(true);
		}
	}

	private void enterImpl(final boolean byMutex, IController<?, P> triCon, List<Member<P>> members) {

		final Iterator<Member<P>> it = members.iterator();
		TeamDelegate delegate;
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

			delegate = (TeamDelegate) controller;
			// disable dispatch recursion callback
			delegate.setEnableStateCallback(false);

			switch (member.getCooperateMethod()) {
			case COOPERATE_METHOD_BASE:
				// in base: mutex trigger do nothing.
				if (byMutex) {
					break;
				}
				
			case COOPERATE_METHOD_ALL:
				delegate.notifyStateEnter(member.getStates());
				break;

			default:
				System.err.println("unknown cooperate method: " + member.getCooperateMethod());
			}
			delegate.setEnableStateCallback(true);
		}
	}
}