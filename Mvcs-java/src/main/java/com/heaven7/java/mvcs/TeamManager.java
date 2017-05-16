package com.heaven7.java.mvcs;

import java.util.ArrayList;
import java.util.List;

import com.heaven7.java.mvcs.IController.StateListener;
import com.heaven7.java.mvcs.util.SparseArray;

public class TeamManager<P> implements StateListener<P> {

	private final SparseArray<DoubleGroupMember> mMap;

	public TeamManager() {
		mMap = new SparseArray<>();
	}

	public static class Member<P> {
		IController<? extends AbstractState<P>, P> controller;
		int states; // can be multi
	}

	private class DoubleGroupMember {
		final List<Member<P>> mTempList = new ArrayList<Member<P>>();
		List<Member<P>> parents;
		List<Member<P>> children;

		public boolean hasTeamControllers(IController<?, P> target, int state) {
			mTempList.addAll(parents);
			boolean found = false;
			for (Member<P> member : parents) {
				if (member.controller == target) {
					if ((member.states & state) != 0) {
						//remove same member(controller).
						mTempList.remove(member);
						found = true;
					}
					break;
				}
			}
			return found;
		}

		public List<Member<P>> getTeamControllers() {
			return mTempList;
		}

		public void clearCache() {
			mTempList.clear();
		}
	}

	public void link(List<Member<P>> members) {

	}

	// 主，从. 只有主的才能通知从的。
	public int makeLink(List<Member<P>> main, List<Member<P>> attach) {

		return 0;
	}

	@Override
	public void onEnterState(int stateFlag, AbstractState<P> state) {
		// if need of state?
		IController<? extends AbstractState<P>, P> controller = state.getController();

		final int size = mMap.size();
		for (int i = size - 1; i >= 0; i--) {
			DoubleGroupMember dMember = mMap.valueAt(i);
			if (dMember.hasTeamControllers(controller, stateFlag)) {
				
                break;
			}
		}
	}

	@Override
	public void onExitState(int stateFlag, AbstractState<P> state) {

	}

	@Override
	public void onReenterState(int stateFlag, AbstractState<P> state) {

	}

}
