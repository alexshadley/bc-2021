package basicplayer;

public class Flags {
    final static int TYPE_WIDTH = 8;
    final static int TYPE_BITMASK = 0x0000ff;

    public enum Type {
        NONE, // default flag
        ENEMY_EC_FOUND, // used by scouts to indicate enemy ec found
        ATTACK_ENEMY_EC, // used by ecs to initiate a rush
        NEUTRAL_EC // used by mucks
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

    /**
     * Encodes two coordinates as an ENEMY_EC_FOUND Flag
     *
     * @param x enemy x coordinate, relative (must fit in 8 bits, not be negative)
     * @param y enemy y coordinate, relative (must fit in 8 bits, not be negative)
     * @return flag
     */
    public static int encodeEnemyECFoundFlag(final int x, final int y) {
        return encodeFlag(
            Type.ENEMY_EC_FOUND,
            ((y & COORD_BITMASK) << COORD_WIDTH) | (COORD_BITMASK & x)
        );
    }

    public static int encodeNeturalECFoundFlag(final int x, final int y) {
        return encodeFlag(
                Type.NEUTRAL_EC,
                ((y & COORD_BITMASK) << COORD_WIDTH) | (COORD_BITMASK & x)
        );
    }

    public static int[] getAttackEnemyECInfo(final int flag) {
        final int x = COORD_BITMASK & (flag >>> X_OFFSET);
        final int y = COORD_BITMASK & (flag >>> Y_OFFSET);
        return new int[] {x, y};
    }

    /**
     * Encodes two coordinates as an ATTACK_ENEMY_EC Flag
     *
     * @param x enemy x coordinate, relative (must fit in 8 bits, not be negative)
     * @param y enemy y coordinate, relative (must fit in 8 bits, not be negative)
     * @return flag
     */
    public static int encodeAttackEnemyECFlag(final int x, final int y) {
        return encodeFlag(
            Type.ATTACK_ENEMY_EC,
            ((y & COORD_BITMASK) << COORD_WIDTH) | (COORD_BITMASK & x)
        );
    }
}
