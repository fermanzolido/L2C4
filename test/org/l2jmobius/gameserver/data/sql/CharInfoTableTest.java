package org.l2jmobius.gameserver.data.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import sun.misc.Unsafe;

public class CharInfoTableTest
{
	private CharInfoTable _table;

	@Before
	public void setUp() throws Exception
	{
		// We use Unsafe to create an instance of CharInfoTable without calling the constructor,
		// because the constructor tries to connect to the database.
		final Field f = Unsafe.class.getDeclaredField("theUnsafe");
		f.setAccessible(true);
		final Unsafe unsafe = (Unsafe) f.get(null);
		_table = (CharInfoTable) unsafe.allocateInstance(CharInfoTable.class);

		// Initialize the maps manually via reflection since we bypassed the constructor.
		initializeMap("_names");
		initializeMap("_namesLower");
		initializeMap("_accessLevels");
	}

	@SuppressWarnings("unchecked")
	private void initializeMap(String fieldName) throws Exception
	{
		final Field field = CharInfoTable.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		final Constructor<?> concurrentHashMapConstructor = Class.forName("java.util.concurrent.ConcurrentHashMap").getConstructor();
		field.set(_table, concurrentHashMapConstructor.newInstance());
	}

	@Test
	public void testGetIdByName() throws Exception
	{
		// Manually populate the maps using reflection to simulate data loaded from DB or added via addName.
		final Field namesField = CharInfoTable.class.getDeclaredField("_names");
		namesField.setAccessible(true);
		final Map<Integer, String> names = (Map<Integer, String>) namesField.get(_table);

		final Field namesLowerField = CharInfoTable.class.getDeclaredField("_namesLower");
		namesLowerField.setAccessible(true);
		final Map<String, Integer> namesLower = (Map<String, Integer>) namesLowerField.get(_table);

		names.put(1, "Bolt");
		namesLower.put("bolt", 1);

		assertEquals(1, _table.getIdByName("Bolt"));
		assertEquals(1, _table.getIdByName("bolt"));
		assertEquals(1, _table.getIdByName("BOLT"));
		// We can't test getIdByNameFromDb because it would try to connect to the DB.
		// But we verified the O(1) path.
	}

	@Test
	public void testDoesCharNameExist() throws Exception
	{
		final Field namesLowerField = CharInfoTable.class.getDeclaredField("_namesLower");
		namesLowerField.setAccessible(true);
		final Map<String, Integer> namesLower = (Map<String, Integer>) namesLowerField.get(_table);

		namesLower.put("bolt", 1);

		assertTrue(_table.doesCharNameExist("Bolt"));
		assertTrue(_table.doesCharNameExist("bolt"));
		assertTrue(_table.doesCharNameExist("BOLT"));
		// We can't test doesCharNameExistInDb because it would try to connect to the DB.
		// But we verified the O(1) path.
	}
}
