package com.heaven7.java.mvcs.test.teamstate;

import com.heaven7.java.base.util.PropertyBundle;
import com.heaven7.java.mvcs.impl.DefaultState;
import com.heaven7.java.mvcs.impl.DefaultStateFactory;

public class CommonFactory implements DefaultStateFactory {

	private int teamIndex;

	public CommonFactory(int teamIndex) {
		super();
		this.teamIndex = teamIndex;
	}

	@Override
	public DefaultState createState(int stateKey, PropertyBundle p) {
		switch (stateKey) {
		case StateTeamManagerTest.STATE_MOVE: {
			switch (teamIndex) {
			case 1:
				return new Team1Move();

			case 2:
			case 3:
				return null;

			default:
				throw new RuntimeException();
			}
		}

		case StateTeamManagerTest.STATE_EAT: {
			switch (teamIndex) {
			case 1:

				return new Team1Eat();
			case 2:

				return new Team2Eat();
			case 3:

				return new Team3Eat();

			default:
				throw new RuntimeException();
			}
		}

		case StateTeamManagerTest.STATE_SLEEP: {
			switch (teamIndex) {
			case 1:

				return new Team1Sleep();
			case 2:

				return new Team2Sleep();
			case 3:

				return new Team3Sleep();

			default:
				throw new RuntimeException();
			}
		}
		default:
			throw new RuntimeException();
		}
	}

}
