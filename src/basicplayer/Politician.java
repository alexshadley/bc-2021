package basicplayer;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import common.CoordinateSystem;
import common.Directions;
import common.Flags;
import common.Flags.Type;
import common.Pathfinding;

public class Politician {
    private static final int ACTION_R2 = 9;
    private static final int SENSOR_R2 = 25;
    private static final int DETECT_R2 = 25;

    private static enum PoliticanMode {
        ROAMING,
        RUSHING
    }

    private final RobotController rc;
    private final CoordinateSystem coordinateSystem;
    private final RobotInfo parent;
    private PoliticanMode mode;
    // only valid for RUSHING mode, indicates the coords of the base to rush
    private MapLocation rushCoords;

    public Politician(final RobotController rc, final RobotInfo parent) {
        this.rc = rc;
        this.parent = parent;
        this.coordinateSystem = new CoordinateSystem(parent.location);
        this.mode = PoliticanMode.ROAMING;
    }

    /**
     * Main execution loop
     **/
    public void run() throws GameActionException {
        while (true) {
            checkCommunications();
            attackIfPossible();

            if (mode == PoliticanMode.ROAMING) {
                Pathfinding.tryMove(Directions.getRandomDirection(), rc);
            } else {
                Pathfinding.tryMove(Pathfinding.findPath(rushCoords, rc), rc);
            }

            Clock.yield();
        }
    }

    private void checkCommunications() throws GameActionException {
        // don't check while rushing to save bytecode
        if (mode != PoliticanMode.RUSHING) {
            final int parentFlag = rc.getFlag(parent.ID);
            if (Flags.getFlagType(parentFlag) == Type.ATTACK_ENEMY_EC) {
                System.out.println("Recieved attack orders from EC");
                this.mode = PoliticanMode.RUSHING;

                final int[] coords = Flags.getAttackEnemyECInfo(parentFlag);
                this.rushCoords = coordinateSystem.toAbsolute(coords[0], coords[1]);
            }
        }
    }

    private void attackIfPossible() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }
    }
}
