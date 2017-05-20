package com.heaven7.java.mvcs;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

import com.heaven7.java.base.anno.CalledInternal;
import com.heaven7.java.base.anno.Nullable;
import com.heaven7.java.base.util.Throwables;
import com.heaven7.java.mvcs.TeamDelegate.StateListener;
import com.heaven7.java.mvcs.util.SparseArray;

/**
 * the team manager. which can communication with multi controller.
 * 
 * @author heaven7
 *
 * @param
 * 			<P>
 *            the parameter type
 * @since 1.1.8
 */
public final class TeamManager<P> implements StateListener<P> {

	/***
	 * the cooperate method: just base. (can't listen mutex state, but include
	 * current state)
	 */
	public static final byte COOPERATE_METHOD_BASE = 1;
	/***
	 * the cooperate method: all.
	 */
	public static final byte COOPERATE_METHOD_ALL = 3;

	@SuppressWarnings("rawtypes")
	private static final SimpleTeamCllback sDEFAULT_CALLBACK = new SimpleTeamCllback();

	/** a map contains multi teams. */
	private final SparseArray<Team<P>> mMap;
	private int mLastTeamId;

	/**
	 * the callback of team
	 * 
	 * @author heaven7
	 *
	 * @param
	 * 			<P>
	 *            the parameter type
	 * @since 1.1.8
	 */
	public static abstract class TeamCallback<P> {

		/**
		 * called on team enter.
		 * 
		 * @param team
		 *            the team
		 * @param trigger
		 *            the trigger state.
		 */
		public void onTeamEnter(Team<P> team, AbstractState<P> trigger) {

		}

		/**
		 * called on team exit.
		 * 
		 * @param team
		 *            the team
		 * @param trigger
		 *            the trigger state.
		 */
		public void onTeamExit(Team<P> team, AbstractState<P> trigger) {

		}

		/**
		 * called on team reenter.
		 * 
		 * @param team
		 *            the team
		 * @param trigger
		 *            the trigger state.
		 */
		public void onTeamReenter(Team<P> team, AbstractState<P> trigger) {

		}
	}

	public TeamManager() {
		mMap = new SparseArray<>();
	}

	public static <P> Member<P> createMember(IController<? extends AbstractState<P>, P> controller, int states,
			byte cooperateMethod) {
		return new Member<P>(controller, states, cooperateMethod);
	}

	public static <P> Member<P> createMember(IController<? extends AbstractState<P>, P> controller, int states) {
		return new Member<P>(controller, states, COOPERATE_METHOD_BASE);
	}

	/**
	 * create team with formal members and outer members. then register it to
	 * team manager. Among them, if state is in outer members, it can be
	 * notifier state. That means only formal member can notify others(other
	 * formal members or outer members).
	 * 
	 * @param formal
	 *            the formal members
	 * @return the id of the team.
	 */
	public int registerTeam(List<Member<P>> formal) {
		return registerTeam(formal, null);
	}

	// 主，从. 只有主的才能通知从的。
	/**
	 * create team with formal members , outer members and default team
	 * callback. then register it to team manager. . Among them, if state is in
	 * outer members, it can be notifier state. That means only formal member
	 * can notify others(other formal members or outer members).
	 * 
	 * @param formal
	 *            the formal members
	 * @param outer
	 *            the outer members. can be null or empty.
	 * @return the id of the team. >0
	 */
	@SuppressWarnings("unchecked")
	public int registerTeam(List<Member<P>> formal, @Nullable List<Member<P>> outer) {
		return registerTeam(formal, outer, sDEFAULT_CALLBACK);
	}

	/**
	 * create team with formal members , outer members and target team callback,
	 * then register it to team manager. Among them, if state is in outer
	 * members, it can be notifier state. That means only formal member can
	 * notify others(other formal members or outer members).
	 * 
	 * @param formal
	 *            the formal members
	 * @param outer
	 *            the outer members. can be null or empty.
	 * @param callback
	 *            the callback of team.
	 * @return the id of the team. >0
	 */
	public int registerTeam(List<Member<P>> formal, @Nullable List<Member<P>> outer, TeamCallback<P> callback) {
		Throwables.checkEmpty(formal);
		Throwables.checkNull(callback);
		Team<P> team = new Team<P>();
		team.formal = formal;
		team.outer = outer;
		team.callback = callback;
		mMap.put(++mLastTeamId, team);
		return mLastTeamId;
	}
	/**
	 * unregister the team which is assigned by target team id.
	 * @param teamId the target team id.
	 */
	public void unregisterTeam(int teamId){
		mMap.remove(teamId);
	}
	
	/**
	 * delete the member which is indicated by target controller.
	 * @param teamId the team id.
	 * @param controller the controller
	 * @return true of delete member success. or false if don't have.
	 */
	public boolean deleteMember(int teamId, IController<? extends AbstractState<P>, P> controller){
		Team<P> team = mMap.get(teamId);
		if(team == null){
			return false;
		}
		return team.deleteMember(controller);
	}
	
	/**
	 * delete the member states which is indicated by target controller and states.
	 * @param teamId the team id.
	 * @param controller the controller
	 * @param targetStates the target states to delete.
	 * @return true of delete member success. or false if don't have.
	 */
	public boolean deleteMembeStatesr(int teamId, IController<? extends AbstractState<P>, P> 
	        controller, int targetStates){
		Team<P> team = mMap.get(teamId);
		if(team == null){
			return false;
		}
		return team.deleteMemberStates(controller);
	}

	/**
	 * update the all teams.
	 * 
	 * @param deltaTime
	 *            the delta time between last update and this.
	 * @param param
	 *            the paramter.
	 */
	public void update(long deltaTime, P param) {
		final int size = mMap.size();
		for (int i = size - 1; i >= 0; i--) {
			mMap.valueAt(i).update(deltaTime, param);
		}
	}

	// =============================================================
	@CalledInternal
	@Override
	public void onEnterState(int stateFlag, AbstractState<P> state) {
		final int size = mMap.size();
		for (int i = size - 1; i >= 0; i--) {
			mMap.valueAt(i).onEnter(stateFlag, state);
		}
	}

	@CalledInternal
	@Override
	public void onExitState(int stateFlag, AbstractState<P> state) {
		final int size = mMap.size();
		for (int i = size - 1; i >= 0; i--) {
			mMap.valueAt(i).onExit(stateFlag, state);
		}
	}

	@CalledInternal
	@Override
	public void onReenterState(int stateFlag, AbstractState<P> state) {
		final int size = mMap.size();
		for (int i = size - 1; i >= 0; i--) {
			mMap.valueAt(i).onReenter(stateFlag, state);
		}
	}

	/**
	 * one controller corresponding one member. But can have multi states.
	 * 
	 * @author heaven7
	 *
	 * @param
	 * 			<P>
	 *            the parameter type
	 * @since 1.1.8
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

		public void update(long deltaTime, P param) {
			IController<? extends AbstractState<P>, P> controller = getController();
			if (controller != null) {
				controller.update(states, deltaTime, param);
			}
		}
	}

	/**
	 * the team of members.
	 * 
	 * @author heaven7
	 *
	 * @param
	 * 			<P>
	 *            the parameter type
	 * @since 1.1.8
	 * @see {@linkplain Member}
	 */
	public static class Team<P> {
		List<Member<P>> formal;
		List<Member<P>> outer;
		TeamCallback<P> callback;

		Team() {
		}

		public boolean deleteMemberStates(IController<? extends AbstractState<P>, P> controller) {
			// TODO Auto-generated method stub
			return false;
		}

		boolean deleteMember(IController<? extends AbstractState<P>, P> controller) {
			boolean success = false;
			IController<? extends AbstractState<P>, P> temp;
			Member<P> member;
			
			Iterator<Member<P>> it = formal.iterator();
			for(; it.hasNext() ;){
				member = it.next();
				temp = member.getController();
				//if controller is empty or controller is the target want to delete.
				if(temp == null ){
					it.remove();
					continue;
				}
				if(temp == controller){
					it.remove();
					success = true;
					break;
				}
			}
			/**
			 * formal and outer member may use same controller.
			 */
			if (outer != null && !outer.isEmpty()) {
				it = outer.iterator();
				for(; it.hasNext() ;){
					member = it.next();
					temp = member.getController();
					//if controller is empty or controller is the target want to delete.
					if(temp == null ){
						it.remove();
						continue;
					}
					if(temp == controller){
						it.remove();
						success = true;
						break;
					}
				}
			}
			return success;
		}

		void update(long deltaTime, P param) {
			for (Member<P> member : formal) {
				member.update(deltaTime, param);
			}
			if (outer != null) {
				for (Member<P> member : outer) {
					member.update(deltaTime, param);
				}
			}
		}

		public List<Member<P>> getFormalMembers() {
			return formal;
		}

		public List<Member<P>> getOuterMembers() {
			return outer;
		}

		void onEnter(int state, AbstractState<P> trigger) {
			if (hasMember(trigger.getController(), state)) {
				callback.onTeamEnter(this, trigger);
			}
		}

		void onExit(int state, AbstractState<P> trigger) {
			if (hasMember(trigger.getController(), state)) {
				callback.onTeamExit(this, trigger);
			}
		}

		void onReenter(int state, AbstractState<P> trigger) {
			if (hasMember(trigger.getController(), state)) {
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

}
