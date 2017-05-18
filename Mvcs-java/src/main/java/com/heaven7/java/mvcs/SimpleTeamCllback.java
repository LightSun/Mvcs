package com.heaven7.java.mvcs;

import java.util.ArrayList;
import java.util.List;

import com.heaven7.java.mvcs.TeamManager.Member;
import com.heaven7.java.mvcs.TeamManager.Team;
import com.heaven7.java.mvcs.TeamManager.TeamCallback;
import static com.heaven7.java.mvcs.TeamManager.COOPERATE_METHOD_ALL;
import static com.heaven7.java.mvcs.TeamManager.COOPERATE_METHOD_BASE;;

public class SimpleTeamCllback<P> extends TeamCallback<P> {

	private final List<Member<P>> mTempMembers = new ArrayList<>();

	public void onTeamEnter(Team<P> team, AbstractState<P> trigger) {
		final boolean byMutex = trigger.hasFlags(AbstractState.FLAG_MUTEX);
		final IController<?, P> triCon = trigger.getController();
		// merge members.
		mTempMembers.addAll(team.getFormalMembers());
		List<Member<P>> outers = team.getOuterMembers();
		if (outers != null) {
			mTempMembers.addAll(outers);
		}

		enterImpl(byMutex, triCon);
		mTempMembers.clear();
	}

	private void enterImpl(final boolean byMutex, IController<?, P> triCon) {
		IController<? extends AbstractState<P>, P> controller;
		for (Member<P> member : mTempMembers) {
			controller = member.getController();

			// same controller. have nothing effect
			if (controller != null && controller != triCon && controller instanceof SimpleController) {
				// disable dispatch recursion callback
				((SimpleController) controller).setEnableStateCallback(false);
				switch (member.getCooperateMethod()) {
				case COOPERATE_METHOD_BASE:
					// in base: mutex trigger do nothing.
					if (byMutex) {
						break;
					}
				case COOPERATE_METHOD_ALL:
					if (!controller.addState(member.getStates())) {
						System.err.println("SimpleTeamCllback>>> called onTeamEnter(): add state(" + member.getStates()
								+ ") failed .");
					}
					break;

				default:
					System.err.println("unknown cooperate method: " + member.getCooperateMethod());
				}
				((SimpleController) controller).setEnableStateCallback(true);
			}
		}
	}

	public void onTeamExit(Team<P> team, AbstractState<P> from) {

	}

	public void onTeamReenter(Team<P> team, AbstractState<P> from) {

	}
}