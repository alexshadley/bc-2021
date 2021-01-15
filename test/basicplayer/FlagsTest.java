package basicplayer;

import org.junit.Test;
import static org.junit.Assert.*;

public class FlagsTest {
    @Test
    public void testEnemyECFound() {
        final int flag = Flags.encodeEnemyECFoundFlag(1, 2);
        assertArrayEquals(
            new int[] {1, 2},
            Flags.getEnemyECFoundInfo(flag)
        );
    }
}
