package org.l2jmobius.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import org.junit.Test;
import static org.junit.Assert.*;
// import org.l2jmobius.gameserver.model.actor.Player; // Implicitly available if in same package or imported

public class TeleporterParsingTest
{
    @Test
    public void testParseNextInt()
    {
        // Case 1: "chat 1"
        String command = "chat 1";
        StringTokenizer st = new StringTokenizer(command, " ");
        st.nextToken(); // consume "chat"
        assertEquals("Should parse simple integer", 1, Teleporter.parseNextInt(st, 0, null, command));

        // Case 2: "chat  1"
        command = "chat  1";
        st = new StringTokenizer(command, " ");
        st.nextToken(); // consume "chat"
        assertEquals("Should handle multiple spaces", 1, Teleporter.parseNextInt(st, 0, null, command));

        // Case 3: "chat abc" (invalid number)
        command = "chat abc";
        st = new StringTokenizer(command, " ");
        st.nextToken(); // consume "chat"
        assertEquals("Should return default on invalid number", 0, Teleporter.parseNextInt(st, 0, null, command)); // logs warning

        // Case 4: "chat" (no value)
        command = "chat";
        st = new StringTokenizer(command, " ");
        st.nextToken(); // consume "chat"
        assertEquals("Should return default on missing argument", 0, Teleporter.parseNextInt(st, 0, null, command)); // no log

        // Case 5: "chat 1 a"
        command = "chat 1 a";
        st = new StringTokenizer(command, " ");
        st.nextToken(); // consume "chat"
        assertEquals("Should parse first valid integer", 1, Teleporter.parseNextInt(st, 0, null, command));

        // Case 6: Integer Overflow "chat 9999999999"
        command = "chat 9999999999";
        st = new StringTokenizer(command, " ");
        st.nextToken(); // consume "chat"
        assertEquals("Should handle integer overflow", 0, Teleporter.parseNextInt(st, 0, null, command)); // logs warning
    }
}
