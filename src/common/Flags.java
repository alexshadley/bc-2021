package common;

import java.util.Optional;

public class Flags {
    final static int TYPE_WIDTH = 8;
    final static int TYPE_BITMASK = 0x0000ff;

    public enum Type {
        NONE,
        ENEMY_EC_FOUND;
    }

    private static int encodeFlag(final Type flag, final int data) {
        return flag.ordinal() | (data << TYPE_WIDTH);
    }

    public static Type getFlagType(final int flag) {
        try {
            return Type.values()[flag & TYPE_BITMASK];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    final static int X_OFFSET = 8;
    final static int Y_OFFSET = 16;
    final static int COORD_WIDTH = 8;
    final static int COORD_BITMASK = 0x0000ff;

    public static int[] getEnemyECFoundInfo(final int flag) {
        final int x = COORD_BITMASK & (flag >>> X_OFFSET);
        final int y = COORD_BITMASK & (flag >>> Y_OFFSET);
        return new int[] {x, y};
    }

    public static int encodeEnemyECFoundFlag(final int x, final int y) {
        return encodeFlag(
            Type.ENEMY_EC_FOUND,
            ((y & COORD_BITMASK) << COORD_WIDTH) | (COORD_BITMASK & x)
        );
    }
}
