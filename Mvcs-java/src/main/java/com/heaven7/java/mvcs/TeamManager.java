package com.heaven7.java.mvcs;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.heaven7.java.base.anno.CalledInternal;
import com.heaven7.java.base.anno.Nullable;
import com.heaven7.java.base.util.Throwables;
import com.heaven7.java.mvcs.IController.PolicyType;
import com.heaven7.java.mvcs.TeamDelegate.StateListener;
import com.heaven7.java.mvcs.util.SparseArray;

/**
 * the team manager of state, across multi {@linkplain IController}. which can communication with multi controller.
 * 
 * @author heaven7
 *
 * @param
 * 			<P>
 *            the parameter type
 * @since 1.1.8
 */
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

	/** the member scope of formal member */
	public static final byte FLAG_MEMBER_FORMAL = 0x0001;
	/** the member scope of outer member */
	public static final byte FLAG_MEMBER_OUTER = 0x0002;

	private static final SimpleTeamCllback<Object> sDEFAULT_CALLBACK = new SimpleTeamCllback<Object>();

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
	
	/**
	 * create a team with default callback.
	 * @param formal the formal members
	 * @param outer the outer members, can be null
	 * @param callback the team callback.
	 * @return a team.
	 * @see SimpleTeamCllback
	 */
	@SuppressWarnings("unchecked")
	public static <P> Team<P> createTeam(List<Member<P>> formal, @Nullable List<Member<P>> outer) {
		return createTeam(formal, outer, (TeamCallback<P>)sDEFAULT_CALLBACK);
	}
	
	/**
	 * create a team.
	 * @param formal the formal members
	 * @param outer the outer members, can be null
	 * @param callback the team callback.
	 * @return a team.
	 */
	public static <P> Team<P> createTeam(List<Member<P>> formal, @Nullable List<Member<P>> outer, 
			TeamCallback<P> callback) {
		Throwables.checkEmpty(formal);
		Throwables.checkNull(callback);
		Team<P> team = new Team<P>();
		team.formal = formal;
		team.outer = outer;
		team.callback = callback;
		return team;
	}

	/**
	 * create a member .
	 * 
	 * @param
	 * 			<P>
	 *            the parameter type.
	 * @param controller
	 *            the target controller
	 * @param states
	 *            the target states.
	 * @param cooperateMethod
	 *            the cooperate method between member and team.
	 * @return the member.
	 */
	public static <P> Member<P> createMember(IController<? extends AbstractState<P>, P> controller, int states,
			byte cooperateMethod) {
		return new Member<P>(controller, states, cooperateMethod);
	}

	/**
	 * create a member .
	 * 
	 * @param
	 * 			<P>
	 *            the parameter type.
	 * @param controller
	 *            the target controller
	 * @param states
	 *            the target states.
	 * @return the member.
	 */
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
		return registerTeam(formal, outer, (TeamCallback<P>) sDEFAULT_CALLBACK);
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
		return registerTeam(createTeam(formal, outer, callback));
	}
	/**
	 * register a team to manager.
	 * @param team the target team
	 * @return the id of target team
	 */
	public int registerTeam(Team<P> team){
		mMap.put( ++mLastTeamId, team );
		return mLastTeamId;
	}

	/**
	 * unregister the team which is assigned by target team id.
	 * 
	 * @param teamId
	 *            the target team id.
	 */
	public void unregisterTeam(int teamId) {
		mMap.remove(teamId);
	}
	
	/**
	 * unregister the team.
	 * 
	 * @param teamId
	 *            the target team id.
	 */
	public void unregisterTeam(Team<P> team) {
		final int size = mMap.size();
		for (int i = size - 1; i >= 0; i--) {
			Team<P> t = mMap.valueAt(i);
			if(t == team){
				mMap.remove(mMap.keyAt(i));
				break;
			}
		}
	}

	/**
	 * delete formal member which is indicated by target controller. And default
	 * member flags is
	 * "{@linkplain FLAG_MEMBER_FORMAL} | {@linkplain FLAG_MEMBER_OUTER}"
	 * 
	 * @param teamId
	 *            the team id.
	 * @param controller
	 *            the controller
	 * @return true of delete member success. or false if don't have.
	 */
	public boolean deleteOuterMember(int teamId, IController<? extends AbstractState<P>, P> controller) {
		return deleteMember(teamId, controller, FLAG_MEMBER_OUTER);
	}

	/**
	 * delete formal member which is indicated by target controller. And default
	 * member flags is
	 * "{@linkplain FLAG_MEMBER_FORMAL} | {@linkplain FLAG_MEMBER_OUTER}"
	 * 
	 * @param teamId
	 *            the team id.
	 * @param controller
	 *            the controller
	 * @return true of delete member success. or false if don't have.
	 */
	public boolean deleteFormalMember(int teamId, IController<? extends AbstractState<P>, P> controller) {
		return deleteMember(teamId, controller, FLAG_MEMBER_FORMAL);
	}

	/**
	 * delete the member which is indicated by target controller. And default
	 * member flags is
	 * "{@linkplain FLAG_MEMBER_FORMAL} | {@linkplain FLAG_MEMBER_OUTER}"
	 * 
	 * @param teamId
	 *            the team id.
	 * @param controller
	 *            the controller
	 * @return true of delete member success. or false if don't have.
	 */
	public boolean deleteMember(int teamId, IController<? extends AbstractState<P>, P> controller) {
		return deleteMember(teamId, controller, (byte) (FLAG_MEMBER_FORMAL | FLAG_MEMBER_OUTER));
	}

	

	/**
	 * delete the outer member states which is indicated by target controller
	 * and states. And default member flags is
	 * "{@linkplain FLAG_MEMBER_FORMAL} | {@linkplain FLAG_MEMBER_OUTER}"
	 * 
	 * @param teamId
	 *            the team id.
	 * @param controller
	 *            the controller
	 * @param targetStates
	 *            the target states to delete. must >0
	 * @return true of delete member state success. or false if don't have.
	 */
	public boolean deleteOuterMembeStates(int teamId, IController<? extends AbstractState<P>, P> controller,
			int targetStates) {
		return deleteMembeStates(teamId, controller, targetStates, FLAG_MEMBER_OUTER);
	}

	/**
	 * delete the formal member states which is indicated by target controller
	 * and states. And default member flags is
	 * "{@linkplain FLAG_MEMBER_FORMAL} | {@linkplain FLAG_MEMBER_OUTER}"
	 * 
	 * @param teamId
	 *            the team id.
	 * @param controller
	 *            the controller
	 * @param targetStates
	 *            the target states to delete. must >0
	 * @return true of delete member state success. or false if don't have.
	 */
	public boolean deleteFormalMembeStates(int teamId, IController<? extends AbstractState<P>, P> controller,
			int targetStates) {
		return deleteMembeStates(teamId, controller, targetStates, FLAG_MEMBER_FORMAL);
	}

	/**
	 * delete the member states which is indicated by target controller and
	 * states. And default member flags is
	 * "{@linkplain FLAG_MEMBER_FORMAL} | {@linkplain FLAG_MEMBER_OUTER}"
	 * 
	 * @param teamId
	 *            the team id.
	 * @param controller
	 *            the controller
	 * @param targetStates
	 *            the target states to delete. must >0
	 * @return true of delete member state success. or false if don't have.
	 */
	public boolean deleteMembeStates(int teamId, IController<? extends AbstractState<P>, P> controller,
			int targetStates) {
		return deleteMembeStates(teamId, controller, targetStates, (byte) (FLAG_MEMBER_FORMAL | FLAG_MEMBER_OUTER));
	}

	/**
	 * add a formal member for team which is assigned by target teamId.
	 * 
	 * @param teamId
	 *            the team id
	 * @param member
	 *            the formal member
	 * @return true if add success.
	 */
	public boolean addFormalMember(int teamId, Member<P> member) {
		Throwables.checkNull(member);
		Team<P> team = mMap.get(teamId);
		if (team == null) {
			return false;
		}
		return team.formal.add(member);
	}

	/**
	 * add a outer member for team which is assigned by target teamId.
	 * 
	 * @param teamId
	 *            the team id
	 * @param member
	 *            the outer member
	 * @return true if add success.
	 */
	public boolean addOuterMember(int teamId, Member<P> member) {
		Throwables.checkNull(member);
		Team<P> team = mMap.get(teamId);
		if (team == null) {
			return false;
		}
		if (team.outer == null) {
			team.outer = new ArrayList<>(4);
		}
		return team.outer.add(member);
	}

	/**
	 * add a formal member states for team which is assigned by target teamId
	 * and controller.
	 * 
	 * @param teamId
	 *            the team id
	 * @param controller
	 *            the controller
	 * @param states
	 *            the states to add.
	 * @return true if add success.
	 */
	public boolean addFormalMemberStates(int teamId, IController<? extends AbstractState<P>, P> controller,
			int states) {
		if (states <= 0) {
			throw new IllegalArgumentException("targetStates must be positive.");
		}
		Team<P> team = mMap.get(teamId);
		if (team == null) {
			return false;
		}
		return team.addMemberStates(controller, states, FLAG_MEMBER_FORMAL);
	}

	/**
	 * add a outer member states for team which is assigned by target teamId and
	 * controller.
	 * 
	 * @param teamId
	 *            the team id
	 * @param controller
	 *            the controller
	 * @param states
	 *            the states to add.
	 * @return true if add success.
	 */
	public boolean addOuterMemberStates(int teamId, IController<? extends AbstractState<P>, P> controller, int states) {
		if (states <= 0) {
			throw new IllegalArgumentException("targetStates must be positive.");
		}
		Team<P> team = mMap.get(teamId);
		if (team == null) {
			return false;
		}
		return team.addMemberStates(controller, states, FLAG_MEMBER_OUTER);
	}

	/**
	 * get the team for target team id.
	 * 
	 * @param teamId
	 *            the team id.
	 * @return the team.
	 */
	public Team<P> getTeam(int teamId) {
		return mMap.get(teamId);
	}

	/**
	 * indicate the target member is a formal member or not.
	 * 
	 * @param member
	 *            the member
	 * @return true if is in a team and is formal member.
	 */
	public boolean isFormalMember(Member<P> member) {
		Throwables.checkNull(member);
		final int size = mMap.size();
		for (int i = size - 1; i >= 0; i--) {
			if (mMap.get(i).isFormalMember(member)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * indicate the target member is a outer member or not.
	 * 
	 * @param member
	 *            the member
	 * @return true if is in a team and is outer member.
	 */
	public boolean isOuterMember(Member<P> member) {
		Throwables.checkNull(member);
		final int size = mMap.size();
		for (int i = size - 1; i >= 0; i--) {
			if (mMap.get(i).isOuterMember(member)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * get teams for target formal member.
	 * 
	 * @param member
	 *            the member
	 * @param outList
	 *            the out list, can be null
	 * @return the teams
	 */
	public List<Team<P>> getTeamsAsFormal(Member<P> member, @Nullable List<Team<P>> outList) {
		return getTeams(member, FLAG_MEMBER_FORMAL, outList);
	}

	/**
	 * get teams for target outer member.
	 * 
	 * @param member
	 *            the member
	 * @param outList
	 *            the out list, can be null
	 * @return the teams
	 */
	public List<Team<P>> getTeamsAsOuter(Member<P> member, @Nullable List<Team<P>> outList) {
		return getTeams(member, FLAG_MEMBER_OUTER, outList);
	}

	/**
	 * get teams for target member(may be formal or outer member).
	 * 
	 * @param member
	 *            the member
	 * @param outList
	 *            the out list, can be null
	 * @return the teams
	 */
	public List<Team<P>> getTeams(Member<P> member, @Nullable List<Team<P>> outList) {
		return getTeams(member, FLAG_MEMBER_FORMAL | FLAG_MEMBER_OUTER, outList);
	}

	/**
	 * update the all teams.
	 * 
	 * @param deltaTime
	 *            the delta time between last update and this.
	 * @param param
	 *            the parameter.
	 */
	public void update(long deltaTime, P param) {
		final int size = mMap.size();
		for (int i = size - 1; i >= 0; i--) {
			mMap.valueAt(i).update(deltaTime, param);
		}
	}
	
	public boolean sendMessage(int teamId, Message msg, @PolicyType byte policy,
			int memberFlags){
		Team<P> team = getTeam(teamId);
		if(team == null){
			return false;
		}
		return team.dispatchMessage(msg, policy, memberFlags);
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

	// =============================================================================

	// ================ start private method ===============
	
	/**
	 * delete the member states which is indicated by target controller and
	 * states.
	 * 
	 * @param teamId
	 *            the team id.
	 * @param controller
	 *            the controller
	 * @param targetStates
	 *            the target states to delete. must >0
	 * @param memberFlags
	 *            the member flags .see
	 *            {@linkplain TeamManager#FLAG_MEMBER_FORMAL} and
	 *            {@linkplain TeamManager#FLAG_MEMBER_OUTER}.
	 * @return true of delete member state success. or false if don't have.
	 */
	private boolean deleteMembeStates(int teamId, IController<? extends AbstractState<P>, P> controller,
			int targetStates, byte memberFlags) {
		Throwables.checkNull(controller);
		if (targetStates <= 0) {
			throw new IllegalArgumentException("targetStates must be positive.");
		}
		Team<P> team = mMap.get(teamId);
		if (team == null) {
			return false;
		}
		return team.deleteMember(controller, targetStates, memberFlags);
	}
	
	/**
	 * delete the member which is indicated by target controller.
	 * 
	 * @param teamId
	 *            the team id.
	 * @param controller
	 *            the controller
	 * @param memberFlags
	 *            the member flags .see
	 *            {@linkplain TeamManager#FLAG_MEMBER_FORMAL} and
	 *            {@linkplain TeamManager#FLAG_MEMBER_OUTER}.
	 * @return true of delete member success. or false if don't have.
	 */
	private boolean deleteMember(int teamId, IController<? extends AbstractState<P>, P> controller, byte memberFlags) {
		Throwables.checkNull(controller);
		Team<P> team = mMap.get(teamId);
		if (team == null) {
			return false;
		}
		return team.deleteMember(controller, -1, memberFlags);
	}

	private List<Team<P>> getTeams(Member<P> member, int memberFlags, @Nullable List<Team<P>> outList) {
		Throwables.checkNull(member);

		final boolean hasFormal = (memberFlags & FLAG_MEMBER_FORMAL) == FLAG_MEMBER_FORMAL;
		final boolean hasOuter = (memberFlags & FLAG_MEMBER_OUTER) == FLAG_MEMBER_OUTER;
		// no formal and no outer.
		if (!hasFormal && !hasOuter) {
			return null;
		}
		if (outList == null) {
			outList = new ArrayList<>(5);
		}
		final int size = mMap.size();
		Team<P> team;
		for (int i = size - 1; i >= 0; i--) {
			team = mMap.get(i);
			if (hasFormal && team.isFormalMember(member)) {
				outList.add(team);
			} else {
				if (hasOuter && team.isOuterMember(member)) {
					outList.add(team);
				}
			}
		}
		return outList;
	}

	// ============ end private method =====================

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
		WeakReference<IController<? extends AbstractState<P>, P>> weakController;
		/** the multi states*/
		int states; 
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
			this.weakController = new WeakReference<IController<? extends AbstractState<P>, P>>(controller);
			this.states = states;
			this.cooperateMethod = cooperateMethod;
		}

		/**
		 * get the controller
		 * 
		 * @return the controller
		 */
		public IController<? extends AbstractState<P>, P> getController() {
			return weakController.get();
		}

		/**
		 * get the states
		 * 
		 * @return the states
		 */
		public int getStates() {
			return states;
		}

		/**
		 * get the cooperate method.
		 * 
		 * @return the cooperate method.
		 * @see TeamManager#COOPERATE_METHOD_ALL
		 * @see TeamManager#COOPERATE_METHOD_BASE
		 */
		public byte getCooperateMethod() {
			return cooperateMethod;
		}

		void update(long deltaTime, P param) {
			IController<? extends AbstractState<P>, P> controller = getController();
			if (controller != null) {
				controller.updateActiveStates(states, deltaTime, param);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;

			Member<P> other = (Member<P>) obj;
			if (getController() == null) {
				return false;
			}
			if (other.getController() == null) {
				return false;
			}
			if (getController() != other.getController()) {
				return false;
			}
			if (cooperateMethod != other.cooperateMethod)
				return false;
			if (states != other.states)
				return false;
			return true;
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

		/**
		 * indicate the target member is a formal member or not.
		 * 
		 * @param member
		 *            the member.
		 * @return true if is formal member.
		 */
		public boolean isFormalMember(Member<P> member) {
			return formal.contains(member);
		}

		/**
		 * indicate the target member is a outer member or not.
		 * 
		 * @param member
		 *            the member.
		 * @return true if is outer member.
		 */
		public boolean isOuterMember(Member<P> member) {
			return outer != null && outer.contains(member);
		}

		/**
		 * get the formal members
		 * 
		 * @return the formal members
		 */
		public List<Member<P>> getFormalMembers() {
			return formal;
		}

		/**
		 * get the outer members
		 * 
		 * @return the outer members
		 */
		public List<Member<P>> getOuterMembers() {
			return outer;
		}

		public boolean dispatchMessage(Message msg,@PolicyType byte policy, int memberFlags) {
			// TODO Auto-generated method stub
			
			return false;
		}
		private  boolean handleMessage(Message msg, @PolicyType byte policy, 
				List<Member<P>> members){
			
			switch (policy) {
			case IController.POLICY_BROADCAST:
				for(Member<P> m : members){
					//TODO
				}
				break;
				
			case IController.POLICY_CONSUME:
				
				break;

			default:
				break;
			}
			
			return false;
		}

		/**
		 * add member states.
		 * 
		 * @param controller
		 *            the controller.
		 * @param states
		 *            the states
		 * @param memberFlags
		 *            the member flags
		 * @return true if add success. false otherwise.
		 */
		public boolean addMemberStates(IController<? extends AbstractState<P>, P> controller, int states,
				byte memberFlags) {
			boolean success = false;
			if ((memberFlags & FLAG_MEMBER_FORMAL) == FLAG_MEMBER_FORMAL) {
				success |= addMemberStates0(controller, states, formal);
			}
			/**
			 * formal and outer member may use same controller.
			 */
			if ((memberFlags & FLAG_MEMBER_OUTER) == FLAG_MEMBER_OUTER) {
				if (outer != null && !outer.isEmpty()) {
					success |= addMemberStates0(controller, states, outer);
				}
			}
			return success;
		}

		private boolean addMemberStates0(IController<? extends AbstractState<P>, P> controller, int states,
				List<Member<P>> members) {
			IController<? extends AbstractState<P>, P> temp;
			Member<P> member;

			Iterator<Member<P>> it = members.iterator();
			for (; it.hasNext();) {
				member = it.next();
				temp = member.getController();
				// if controller is empty or controller is the target want to
				// delete.
				if (temp == null) {
					it.remove();
					continue;
				}
				if (temp == controller) {
					member.states |= states;
					return true;
				}
			}
			return false;
		}

		/**
		 * delete member by target controller and states. May just delete
		 * states.
		 * 
		 * @param controller
		 *            the controller
		 * @param states
		 *            the states to delete, if == -1. means remove whole member
		 *            of controller
		 * @param memberFlags
		 *            the member flags
		 * @return true if delete success.
		 * @see TeamManager#FLAG_MEMBER_FORMAL
		 * @see TeamManager#FLAG_MEMBER_OUTER
		 */
		boolean deleteMember(IController<? extends AbstractState<P>, P> controller, int states, byte memberFlags) {

			boolean success = false;
			if ((memberFlags & FLAG_MEMBER_FORMAL) == FLAG_MEMBER_FORMAL) {
				success |= deleteMember0(controller, states, formal);
			}

			/**
			 * formal and outer member may use same controller.
			 */
			if ((memberFlags & FLAG_MEMBER_OUTER) == FLAG_MEMBER_OUTER) {
				if (outer != null && !outer.isEmpty()) {
					success |= deleteMember0(controller, states, outer);
				}
			}
			return success;
		}

		private static <P> boolean deleteMember0(IController<? extends AbstractState<P>, P> controller, int states,
				List<Member<P>> members) {
			IController<? extends AbstractState<P>, P> temp;
			Member<P> member;

			Iterator<Member<P>> it = members.iterator();
			for (; it.hasNext();) {
				member = it.next();
				temp = member.getController();
				// if controller is empty or controller is the target want to
				// delete.
				if (temp == null) {
					it.remove();
					continue;
				}
				if (temp == controller) {
					if (states == -1) {
						it.remove();
					} else {
						member.states &= ~states;
						if (member.states <= 0) {
							it.remove();
						}
					}
					return true;
				}
			}
			return false;
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
