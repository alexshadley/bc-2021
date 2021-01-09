package basicplayer;

import battlecode.common.*;
import common.CoordinateSystem;
import common.Flags;
import common.Pathfinding;

/**
 * Muckrackers are the current class we use for scouting due to their sense of range of 40.
 * Stats:
 * Initial Conviction: Ceil(.7C)
 * Base Action cool-down: 1.5
 * Action r^2: 12
 * Sensor r^2: 30
 * Detect r^2: 40
 */
public class Muckracker  {
    private final RobotController robotController;

    //Could be enum in the future
    private boolean isScout = true;

    private static final int ACTION_R2 = 12;
    private static final int SENSOR_R2 = 40;
    private static final int DETECT_R2 = 30;

    private RobotInfo parent;
    private MapLocation enemyEC = null;

    private CoordinateSystem coordinateSystem;

    Team enemyTeam = null;

    public Muckracker(RobotController robotController, Boolean isScout, Team enemyTeam, RobotInfo parent) {
        this.robotController = robotController;
        this.isScout = isScout;
        this.enemyTeam = enemyTeam;
        this.parent = parent;

        if (parent != null)
            this.coordinateSystem = new CoordinateSystem(parent.location);
    }

    // Main execution loop
    public void run() {
        while (true) {
            if (isScout) {
                try {
                    goScouting();
                    isScout = false;
                } catch (GameActionException e) {
                    System.out.println(e.getMessage());
                }
            } else {
                liarLiarYourPantsAreOnFire();
            }
        }
    }

    /**
     * From what I've seen map creation doesn't get too crazy so as a start:
     *  if we are red go east
     *  if we are blue go west
     *  If we hit the edge of the map then we just scan
     */
    private void goScouting() throws GameActionException {
        // if we are red go east
        if (enemyTeam.equals(Team.B)) {
            while (!seesEnemyHQ()) {
                if (robotController.canMove(Direction.EAST)) {
                    robotController.move(Direction.EAST);
                    Clock.yield();
                } else if (robotController.canMove(Direction.NORTHEAST)) {
                    robotController.move(Direction.NORTHEAST);
                    Clock.yield();
                } else if (robotController.canMove(Direction.SOUTHEAST)) {
                    robotController.move(Direction.SOUTHEAST);
                    Clock.yield();
                } else {
                }

                if (!robotController.onTheMap(new MapLocation(robotController.getLocation().x+5, robotController.getLocation().y))) {
                    scan();
                }
            }
            //goHome();
            return;

        } else {
            // if we are blue go west
            while (!seesEnemyHQ()) {
                if (robotController.canMove(Direction.WEST)) {
                    robotController.move(Direction.WEST);
                    Clock.yield();
                } else if (robotController.canMove(Direction.NORTHWEST)) {
                    robotController.move(Direction.NORTHWEST);
                    Clock.yield();
                } else if (robotController.canMove(Direction.SOUTHWEST)) {
                    robotController.move(Direction.SOUTHWEST);
                    Clock.yield();
                } else {
                    //liarLiarYourPantsAreOnFire();
                }

                if (!robotController.onTheMap(new MapLocation(robotController.getLocation().x-5, robotController.getLocation().y))) {
                    scan();
                }
            }
            //goHome();
            return;
        }
    }

    private void goHome() throws GameActionException {
        while (!seesHome()) {
            Direction direction = Pathfinding.findPath(parent.location, robotController);
            robotController.move(direction);
            Clock.yield();
        }
    }

    /**
     * If we can see the enemy HQ from our max sense radius;
     * We then set our flag to let other bots know we found it.
     * @return true if we can see an enemyHQ, false if not.
     * @throws GameActionException Uh oh, we (I) broke something
     */
    private boolean seesEnemyHQ() throws GameActionException {
        RobotInfo[] enemyRobots = robotController.senseNearbyRobots(SENSOR_R2, enemyTeam);
        for (RobotInfo robot : enemyRobots) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                enemyEC = robot.location;
                setEnemyHQFlag(enemyEC);
                return true;
            }
        }
        return false;
    }

    /**
     * Check if we can see home, and therefore home can read our flags
     * @return true if we can see a friendly EC
     */
    private boolean seesHome() {
        RobotInfo[] enemyRobots = robotController.senseNearbyRobots(SENSOR_R2, robotController.getTeam());
        for (RobotInfo robot : enemyRobots) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                return true;
            }
        }
        return false;
    }

    private void setEnemyHQFlag(MapLocation location) throws GameActionException {
        final int[] coords = coordinateSystem.toRelative(location);
        System.out.println(String.format("Found enemy HQ at relative coords: %s, %s", coords[0], coords[1]));
        robotController.setFlag(Flags.encodeEnemyECFoundFlag(coords[0], coords[1]));
    }

    //Magic directions?
    // Go Scan NS to the East and then west
    private void scan() throws GameActionException {
        boolean goingEast = true;
        while (true) {
            while (!robotController.onTheMap(robotController.getLocation().translate(0, 5))) {
                System.out.println("Scanning NORTH");
                Pathfinding.move(Direction.NORTH, robotController);
                if (seesEnemyHQ()) {
                    return;
                }
            }

            //Check if we can go east still
            goingEast = (goingEast && robotController.onTheMap(robotController.getLocation().translate(5, 0)));

            //Should we unroll?
            if (goingEast) {
                for (int i = 0; i < 12; i++) {
                    Pathfinding.move(Direction.EAST, robotController);
                    System.out.println("Scanning EAST");
                    if (seesEnemyHQ()) {
                        return;
                    }
                }
            } else {
                for (int i = 0; i < 12; i++) {
                    Pathfinding.move(Direction.WEST, robotController);
                    System.out.println("Scanning WEST");

                    if (seesEnemyHQ()) {
                        return;
                    }
                }
            }

            while (!robotController.onTheMap(robotController.getLocation().translate(0, -5))) {
                Pathfinding.move(Direction.SOUTH, robotController);
                System.out.println("Scanning SOUTH");
                if (seesEnemyHQ()) {
                    return;
                }
            }

            //Check if we can go east still
            goingEast = (goingEast && robotController.onTheMap(robotController.getLocation().translate(5, 0)));

            if (goingEast) {
                for (int i = 0; i < 12; i++) {
                    Pathfinding.move(Direction.EAST, robotController);
                    System.out.println("Scanning EAST");
                    if (seesEnemyHQ()) {
                        return;
                    }
                }
            } else {
                for (int i = 0; i < 12; i++) {
                    Pathfinding.move(Direction.WEST, robotController);
                    System.out.println("Scanning WEST");
                    if (seesEnemyHQ()) {
                        return;
                    }
                }
            }
        }
    }

    /**
     * Attack mode.
     * This method will have the muckracker search for a slanderer attempt to slay it
     */
    private void liarLiarYourPantsAreOnFire() {
        while (true) {
            RobotInfo[] infos = robotController.senseNearbyRobots(SENSOR_R2, enemyTeam);
            for (RobotInfo info : infos) {
                if (info.type == RobotType.SLANDERER) {
                    try {
                        robotController.expose(info.location);
                    } catch (GameActionException e) {

                    }
                }
            }
        }
    }
}
