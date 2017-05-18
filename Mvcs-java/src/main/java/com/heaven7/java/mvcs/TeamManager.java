package com.heaven7.java.mvcs;

import java.lang.ref.WeakReference;
import java.util.List;

import com.heaven7.java.mvcs.IController.StateListener;
import com.heaven7.java.mvcs.util.SparseArray;

public class TeamManager<P> implements StateListener<P> {

	/***
	 * the cooperate method: just base. (can't listen mutex state, but include
	 * current state)
	 */
	public static final byte COOPERATE_METHOD_BASE = 1;
	/***
	 * the cooperate method: all.
	 */
	public static final byte COOPERATE_METHOD_ALL = 3;

	// final List<Member<P>> mTempList = new ArrayList<Member<P>>();
	private final SparseArray<Team<P>> mMap;
	private int mId;

	public static abstract class TeamCallback<P> {

		public void onTeamEnter(Team<P> team, AbstractState<P> trigger) {

		}

		public void onTeamExit(Team<P> team, AbstractState<P> trigger) {

		}

		public void onTeamReenter(Team<P> team, AbstractState<P> trigger) {

		}
	}

	public TeamManager() {
		mMap = new SparseArray<>();
	}

	/**
	 * one controller one member.
	 * 
	 * @author heaven7
	 *
	 * @param <P>
	 *            the paramter type
	 */
	public static class Member<P> {
		WeakReference<IController<? extends AbstractState<P>, P>> WeakController;
		int states; // can be multi
		/** the cooperate method with other member(or whole team). */
		byte cooperateMethod = COOPERATE_METHOD_BASE;

		Member(IController<? extends AbstractState<P>, P> controller, int states, byte cooperateMethod) {
			super();
			switch (cooperateMethod) {
			case COOPERATE_METHOD_ALL:
			case COOPERATE_METHOD_BASE:
				break;

			default:
				throw new IllegalArgumentException(
						"caused by cooperateMethod is error. cooperateMethod = " + cooperateMethod);
			}
			if (states <= 0) {
				throw new IllegalArgumentException("caused by states is error. states = " + states);
			}
			this.WeakController = new WeakReference<IController<? extends AbstractState<P>, P>>(controller);
			this.states = states;
			this.cooperateMethod = cooperateMethod;
		}

		public IController<? extends AbstractState<P>, P> getController() {
			return WeakController.get();
		}

		public int getStates() {
			return states;
		}

		public byte getCooperateMethod() {
			return cooperateMethod;
		}
	}

	public static class Team<P> {
		List<Member<P>> formal;
		List<Member<P>> outer;
		TeamCallback<P> callback;

		Team() {}

		public List<Member<P>> getFormalMembers() {
			return formal;
		}

		public List<Member<P>> getOuterMembers() {
			return outer;
		}

		void onEnter(int state, AbstractState<P> trigger) {
			if(hasMember(trigger.getController(), state)){
			     callback.onTeamEnter(this, trigger);
			}
		}
		void onExit(int state, AbstractState<P> trigger) {
			if(hasMember(trigger.getController(), state)){
				callback.onTeamExit(this, trigger);
			}
		}
		void onReenter(int state, AbstractState<P> trigger) {
			if(hasMember(trigger.getController(), state)){
				callback.onTeamReenter(this, trigger);
			}
		}
		private boolean hasMember(IController<?, P> target, int state) {
			for (Member<P> member : formal) {
				if (member.getController() == target) {
					if ((member.states & state) != 0) {
						return true;
					}
					break;
				}
			}
			return false;
		}
	}

	public static <P> Member<P> createMember(IController<? extends AbstractState<P>, P> controller, int states,
			byte cooperateMethod) {
		return new Member<P>(controller, states, cooperateMethod);
	}

	public static <P> Member<P> createMember(IController<? extends AbstractState<P>, P> controller, int states) {
		return new Member<P>(controller, states, COOPERATE_METHOD_BASE);
	}

	public void createTeam(List<Member<P>> members) {

	}

	// 主，从. 只有主的才能通知从的。
	public int createTeam(List<Member<P>> formal, List<Member<P>> outer) {
		Team<P> team = new Team<P>();
		team.formal = formal;
		team.outer = outer;
		mMap.put(++mId, team);
		return mId;
	}

	@Override
	public void onEnterState(int stateFlag, AbstractState<P> state) {
		final int size = mMap.size();
		for (int i = size - 1; i >= 0; i--) {
			 mMap.valueAt(i).onEnter(stateFlag, state);
		}
	}

	@Override
	public void onExitState(int stateFlag, AbstractState<P> state) {
		final int size = mMap.size();
		for (int i = size - 1; i >= 0; i--) {
			 mMap.valueAt(i).onExit(stateFlag, state);
		}
	}

	@Override
	public void onReenterState(int stateFlag, AbstractState<P> state) {
		final int size = mMap.size();
		for (int i = size - 1; i >= 0; i--) {
			 mMap.valueAt(i).onReenter(stateFlag, state);
		}
	}

}
