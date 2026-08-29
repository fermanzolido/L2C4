package org.l2jmobius.gameserver.model.script;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * {@link State#getStateId} switches over a String that {@code Quest.playerEnter} reads
 * straight out of {@code character_quests.value}, a column the schema declares nullable. A
 * switch over a null String throws, and that throw is caught far above by the quest loader's
 * catch(Exception) -- so one bad row would silently cost the character every quest the loader
 * had not read yet.
 * @author Claude
 */
public class StateTest
{
	@Test
	public void knownNamesMapToTheirStates()
	{
		assertEquals("Started", State.STARTED, State.getStateId("Started"));
		assertEquals("Completed", State.COMPLETED, State.getStateId("Completed"));
		assertEquals("Start", State.CREATED, State.getStateId("Start"));
	}

	@Test
	public void anUnknownNameIsTheDefaultState()
	{
		assertEquals(State.CREATED, State.getStateId("nonsense"));
		assertEquals(State.CREATED, State.getStateId(""));
	}

	@Test
	public void aMissingNameIsTheDefaultStateAndNotAThrow()
	{
		assertEquals("A null value must answer like any other unknown name", State.CREATED, State.getStateId(null));
	}

	@Test
	public void everyStateRoundTripsThroughItsName()
	{
		for (byte state : new byte[]
		{
			State.CREATED,
			State.STARTED,
			State.COMPLETED
		})
		{
			assertEquals("State " + state + " must survive being written and read back", state, State.getStateId(State.getStateName(state)));
		}
	}
}
