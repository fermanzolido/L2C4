package org.l2jmobius.gameserver.managers;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.PlayerTemplate;
import org.l2jmobius.gameserver.model.StatSet;

import sun.misc.Unsafe;
import java.lang.reflect.Field;

public class RaidBossPointsManagerTest
{
    public static void main(String[] args)
    {
        try
        {
            System.out.println("Starting RaidBossPointsManagerTest...");

            // Use Unsafe to instantiate RaidBossPointsManager without calling the constructor
            // because the constructor calls init() which tries to connect to the database.
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Unsafe unsafe = (Unsafe) f.get(null);
            RaidBossPointsManager manager = (RaidBossPointsManager) unsafe.allocateInstance(RaidBossPointsManager.class);

            // Initialize the private fields manually using reflection
            setPrivateField(manager, "_list", new HashMap<Integer, Map<Integer, Integer>>());
            setPrivateField(manager, "_totalPoints", new HashMap<Integer, Integer>());

            // 1. Test single addPoints and cache
            System.out.println("Testing single addPoints and cache...");
            Player mockPlayer = createMockPlayer(1001);
            manager.addPoints(mockPlayer, 1, 100);

            int total = manager.getPointsByOwnerId(1001);
            if (total == 100)
            {
                System.out.println("PASS: Initial points correct (100).");
            }
            else
            {
                System.out.println("FAIL: Initial points incorrect: " + total);
            }

            manager.addPoints(mockPlayer, 1, 50);
            total = manager.getPointsByOwnerId(1001);
            if (total == 150)
            {
                System.out.println("PASS: Merged points correct (150).");
            }
            else
            {
                System.out.println("FAIL: Merged points incorrect: " + total);
            }

            // 2. Test batched addPoints
            System.out.println("Testing batched addPoints...");
            Map<Player, Integer> playerPoints = new HashMap<>();
            Player p1 = createMockPlayer(2001);
            Player p2 = createMockPlayer(2002);
            playerPoints.put(p1, 200);
            playerPoints.put(p2, 300);

            manager.addPoints(playerPoints, 2);

            if (manager.getPointsByOwnerId(2001) == 200 && manager.getPointsByOwnerId(2002) == 300)
            {
                System.out.println("PASS: Batched points correct.");
            }
            else
            {
                System.out.println("FAIL: Batched points incorrect. P1: " + manager.getPointsByOwnerId(2001) + ", P2: " + manager.getPointsByOwnerId(2002));
            }

            // 3. Test ranking calculation (using the cache)
            System.out.println("Testing ranking calculation...");
            int rank1 = manager.calculateRanking(2002); // 300 pts -> Rank 1
            int rank2 = manager.calculateRanking(2001); // 200 pts -> Rank 2
            int rank3 = manager.calculateRanking(1001); // 150 pts -> Rank 3

            if (rank1 == 1 && rank2 == 2 && rank3 == 3)
            {
                System.out.println("PASS: Ranking correct.");
            }
            else
            {
                System.out.println("FAIL: Ranking incorrect. Rank1: " + rank1 + ", Rank2: " + rank2 + ", Rank3: " + rank3);
            }

            System.out.println("RaidBossPointsManagerTest completed successfully.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void setPrivateField(Object obj, String fieldName, Object value) throws Exception
    {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    private static Player createMockPlayer(int objectId) throws Exception
    {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        Unsafe unsafe = (Unsafe) f.get(null);
        Player player = (Player) unsafe.allocateInstance(Player.class);

        // Set objectId using reflection on WorldObject (parent of Player)
        // Find WorldObject class
        Class<?> worldObjectClass = Player.class.getSuperclass().getSuperclass().getSuperclass();
        // Creature -> WorldObject? Let's check
        // Player extends Playable extends Creature extends WorldObject

        Field idField = null;
        Class<?> current = Player.class;
        while (current != null)
        {
            try {
                idField = current.getDeclaredField("_objectId");
                break;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }

        if (idField != null)
        {
            idField.setAccessible(true);
            idField.set(player, objectId);
        }
        else
        {
             // Fallback to name-based if _objectId not found (though it should be there)
             System.out.println("Could not find _objectId field");
        }

        return player;
    }
}
