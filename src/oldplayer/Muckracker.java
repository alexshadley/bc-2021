package oldplayer;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import java.util.ArrayList;

/**
 * Muckrackers are the current class we use for scouting due to their sense of range of 40.
 * Stats:
 * Initial Conviction: Ceil(.7C)
 * Base Action cool-down: 1.5
 * Action r^2: 12
 * Sensor r^2: 30
 * Detect r^2: 40
 */
public class Muckracker implements Robot {
    private final RobotController robotController;

    //Could be enum in the future
    private static enum MuckMode {
        SCOUT,
        CHOKER,
        SEARCH,
        MUCKMAKER
    }
    private MuckMode mode;

    private static final int ACTION_R2 = 12;
    private static final int SENSOR_R2 = 40;
    private static final int DETECT_R2 = 30;

    private RobotInfo parent;
    private MapLocation enemyEC = null;

    private CoordinateSystem coordinateSystem;
    private MapLocation chokeSpot = null;

    Team enemyTeam = null;

    public Muckracker(RobotController robotController, Team enemyTeam, RobotInfo parent) {
        this.robotController = robotController;
        this.enemyTeam = enemyTeam;
        this.parent = parent;
        this.mode = MuckMode.SCOUT;

        if (parent != null)
            this.coordinateSystem = new CoordinateSystem(parent.location);
    }

    // Main execution loop
    public void run() throws GameActionException {
        while (true) {
            try {
                switch (mode) {
                    case SCOUT:
                        if (seesEnemyHQ()) {
                            mode = MuckMode.MUCKMAKER;
                        } else {
                            goScouting();
                        }
                        break;
                    case MUCKMAKER:
                        if (canChoke()){
                            mode = MuckMode.CHOKER;
                        } else {
                            liarLiarYourPantsAreOnFire();
                        }
                        break;
                    case CHOKER:
                        if ( Logging.ENABLED ) {
                            System.out.println("We are attempting to choke");
                        }

                        if (chokeSpot == null) {
                            ArrayList<MapLocation> openSpots = chokeSpots();
                            if (openSpots == null) {
                                mode = MuckMode.MUCKMAKER;
                            } else {
                                chokeSpot = openSpots.get((int) (Math.random() % openSpots.size()));
                            }
                        }
                        blackOutTheSunMyChildren();
                        break;
                    case SEARCH:
                        scan();
                        break;
                    default:
                        //do nothing
                }
            } catch (GameActionException e) {
            }
            Clock.yield();
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
            if (robotController.canMove(Direction.EAST)) {
                robotController.move(Direction.EAST);
            } else if (robotController.canMove(Direction.NORTHEAST)) {
                robotController.move(Direction.NORTHEAST);
            } else if (robotController.canMove(Direction.SOUTHEAST)) {
                robotController.move(Direction.SOUTHEAST);
            }

            if (!robotController.onTheMap(robotController.getLocation().translate(5, 0))) {
                mode = MuckMode.SEARCH;
            }

        } else {
            if (robotController.canMove(Direction.WEST)) {
                robotController.move(Direction.WEST);
            } else if (robotController.canMove(Direction.NORTHWEST)) {
                robotController.move(Direction.NORTHWEST);
            } else if (robotController.canMove(Direction.SOUTHWEST)) {
                robotController.move(Direction.SOUTHWEST);
            }

            if (!robotController.onTheMap(robotController.getLocation().translate(-5, 0))) {
                mode = MuckMode.SEARCH;
            }
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
        RobotInfo[] robots = robotController.senseNearbyRobots(SENSOR_R2-10);
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                if (robot.getTeam() == enemyTeam) {
                    enemyEC = robot.location;
                    setEnemyHQFlag(enemyEC);
                    return true;
                } else {
                    //Neutral EC
                    return false;
                }
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

        if ( Logging.ENABLED ) {
            System.out.println(String.format("Found enemy HQ at relative coords: %s, %s", coords[0], coords[1]));
        }

        robotController.setFlag(Flags.encodeEnemyECFoundFlag(coords[0], coords[1]));
    }

    // TODO Actual flag
    private void setNeutralEC(MapLocation location, int conviction) throws GameActionException {
        final int[] coords = coordinateSystem.toRelative(location);
        robotController.setFlag(2);
    }

    //Magic directions?
    // Go Scan NS to the East and then west
    private void scan() throws GameActionException {
        boolean goingEast = true;
        while (true) {
            while (robotController.onTheMap(robotController.getLocation().translate(0, 3))) {
                Pathfinding.move(Direction.NORTH, robotController);
                if (seesEnemyHQ()) {
                    mode = MuckMode.MUCKMAKER;
                    return;
                }
            }

            //Check if we can go east, latches false
            goingEast = (goingEast && robotController.onTheMap(robotController.getLocation().translate(3, 0)));

            //Should we unroll?
            if (goingEast) {
                for (int i = 0; i < 12; i++) {
                    Pathfinding.move(Direction.EAST, robotController);
                    if (seesEnemyHQ()) {
                        mode = MuckMode.MUCKMAKER;
                        return;
                    }
                }
            } else {
                for (int i = 0; i < 12; i++) {
                    Pathfinding.move(Direction.WEST, robotController);
                    if (seesEnemyHQ()) {
                        mode = MuckMode.MUCKMAKER;
                        return;
                    }
                }
            }

            while (robotController.onTheMap(robotController.getLocation().translate(0, -3))) {
                Pathfinding.move(Direction.SOUTH, robotController);
                if (seesEnemyHQ()) {
                    mode = MuckMode.MUCKMAKER;
                    return;
                }
            }

            //Check if we can go east still
            goingEast = (goingEast && robotController.onTheMap(robotController.getLocation().translate(3, 0)));

            if (goingEast) {
                for (int i = 0; i < 12; i++) {
                    Pathfinding.move(Direction.EAST, robotController);
                    if (seesEnemyHQ()) {
                        mode = MuckMode.MUCKMAKER;
                        return;
                    }
                }
            } else {
                for (int i = 0; i < 12; i++) {
                    Pathfinding.move(Direction.WEST, robotController);
                    if (seesEnemyHQ()) {
                        mode = MuckMode.MUCKMAKER;
                        return;
                    }
                }
            }
        }
    }

    /**
     * Finds the closest slanderer and moves closer to it, or kills it
     */
    private void liarLiarYourPantsAreOnFire() throws GameActionException {
        RobotInfo closestSlanderer = getClosestSlanderer();

        if (closestSlanderer != null) {
            //if we are in range to kill, kill
            if (Directions.distanceTo(closestSlanderer.getLocation(), robotController.getLocation()) <= ACTION_R2) {
                robotController.expose(closestSlanderer.getLocation());
            } else {
                //move closer to it
                Pathfinding.move(closestSlanderer.getLocation(), robotController);
            }
        } else {
            Pathfinding.tryMove(Directions.getRandomDirection(), robotController);
        }
    }

    /**
     * Returns the robot info of the closest slanderer.
     * If none are found returns null
     * @return A robot's info
     */
    private RobotInfo getClosestSlanderer() {
        RobotInfo[] infos = robotController.senseNearbyRobots(SENSOR_R2, enemyTeam);
        RobotInfo closest = null;

        for (RobotInfo info : infos) {
            if (info.type == RobotType.SLANDERER) {
                if (closest == null) {
                    closest = info;
                } else if (Directions.distanceTo(info.getLocation(), robotController.getLocation()) < Directions.distanceTo(closest.getLocation(), robotController.getLocation())) {
                    closest = info;
                }
            }
        }
        return closest;
    }

    private boolean canChoke() throws GameActionException {
        return seesEnemyHQ() && (chokeSpots() != null);
    }

    /**
     * Harnesses the pure strength of the Muck man by having it just sit next to the enemy EC,
     * preventing it from building any units while also killing any slanderers that come into range
     */
    private void blackOutTheSunMyChildren() throws GameActionException {
        //We have made it to our destination, or it is currently occupied so lets try to kill something
        if (!Pathfinding.tryMove(Pathfinding.findPath(chokeSpot, robotController), robotController)) {
            RobotInfo[] infos = robotController.senseNearbyRobots(ACTION_R2, enemyTeam);
            for (RobotInfo info : infos) {
                if (info.getType() == RobotType.SLANDERER) {
                    robotController.expose(info.getLocation());
                    return;
                }
            }
        }
    }

    /**
     * Finds if any spots are open around the enemy EC. Attempts to choke it out
     * @return
     */
    private ArrayList<MapLocation> chokeSpots() {
        ArrayList<MapLocation> openSpots = new ArrayList<>();
        RobotInfo[] info;

        //Check north
        if (robotController.canSenseLocation(enemyEC.translate(0,1))) {
            info = robotController.senseNearbyRobots(enemyEC.translate(0, 1), 0, enemyTeam);
            if (info.length != 0 && info[0].getTeam() == enemyTeam) {
                openSpots.add(info[0].getLocation());
            }
        }
        //Check east
        if(robotController.canSenseLocation(enemyEC.translate(1,0))) {
            info = robotController.senseNearbyRobots(enemyEC.translate(1, 0), 0, enemyTeam);
            if (info.length != 0 && info[0].getTeam() == enemyTeam) {
                openSpots.add(info[0].getLocation());
            }
        }
        //Check south
        if(robotController.canSenseLocation(enemyEC.translate(0,-1))) {
            info = robotController.senseNearbyRobots(enemyEC.translate(0, -1), 0, enemyTeam);
            if (info.length != 0 && info[0].getTeam() == enemyTeam) {
                openSpots.add(info[0].getLocation());
            }
        }
        //Check west
        if(robotController.canSenseLocation(enemyEC.translate(-1,0))) {
            info = robotController.senseNearbyRobots(enemyEC.translate(-1, 0), 0, enemyTeam);
            if (info.length != 0 && info[0].getTeam() == enemyTeam) {
                openSpots.add(info[0].getLocation());
            }
        }

        if (openSpots.size() == 0) {
            return null;
        } else {
            return openSpots;
        }
    }
}
