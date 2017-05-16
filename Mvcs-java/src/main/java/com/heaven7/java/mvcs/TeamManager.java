package com.heaven7.java.mvcs;

import java.util.List;

import com.heaven7.java.mvcs.IController.StateListener;
import com.heaven7.java.mvcs.util.SparseArray;

public class TeamManager<P> implements StateListener<P> {

	//final List<Member<P>> mTempList = new ArrayList<Member<P>>();
	private final SparseArray<Team<P>> mMap;
	private TeamCallback<P> mCallback;
	
	public static abstract class TeamCallback<P>{
		
		public void onTeamEnter(Team<P> team){
			
		}
		public void onTeamExit(Team<P> team) {
			
		}
		public void onTeamReenter(Team<P> team) {
			
		}
	}

	public TeamManager() {
		mMap = new SparseArray<>();
	}

	public static class Member<P> {
		IController<? extends AbstractState<P>, P> controller;
		int states; // can be multi
		
		IController<? extends AbstractState<P>, P> getController(){
			return controller;
		}
	}

	public static class Team<P> {
		List<Member<P>> parents ;
		List<Member<P>> children ;
		
		private Team(){	}
		boolean matches(IController<?, P> target, int state) {
			for (Member<P> member : parents) {
				if (member.getController() == target) {
					if ((member.states & state) != 0) {
						return true;
					}
					break;
				}
			}
			return false;
		}
		public List<Member<P>> getParentMembers() {
			return parents;
		}
		public List<Member<P>> getChildMembers() {
			return children;
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
		if(matchTeam(stateFlag, state)){
			final TeamCallback<P> mCallback = this.mCallback;
			final int size = mMap.size();
			for (int i = size - 1; i >= 0; i--) {
				mCallback.onTeamEnter(mMap.valueAt(i));
			}
		}
	}

	private boolean matchTeam(int stateFlag, AbstractState<P> state) {
		final IController<? extends AbstractState<P>, P> controller = state.getController();
		final int size = mMap.size();
		for (int i = size - 1; i >= 0; i--) {
			Team<P> team = mMap.valueAt(i);
			if (team.matches(controller, stateFlag)) {
                return true;
			}
		}
		return false;
	}

	@Override
	public void onExitState(int stateFlag, AbstractState<P> state) {
		if(matchTeam(stateFlag, state)){
			final TeamCallback<P> mCallback = this.mCallback;
			final int size = mMap.size();
			for (int i = size - 1; i >= 0; i--) {
				mCallback.onTeamExit(mMap.valueAt(i));
			}
		}
	}

	@Override
	public void onReenterState(int stateFlag, AbstractState<P> state) {
		if(matchTeam(stateFlag, state)){
			final TeamCallback<P> mCallback = this.mCallback;
			final int size = mMap.size();
			for (int i = size - 1; i >= 0; i--) {
				mCallback.onTeamReenter(mMap.valueAt(i));
			}
		}
	}
	
      
}
