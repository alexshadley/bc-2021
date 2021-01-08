package robots;

import battlecode.common.*;

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

    MapLocation homeBase = null;
    MapLocation enemyEC = null;

    Team enemyTeam = null;

    Muckracker(RobotController robotController, Boolean isScout, Team enemyTeam) {
        this.robotController = robotController;
        this.isScout = isScout;
        this.enemyTeam = enemyTeam;

        run();
    }

    // Main execution loop
    private void run() {
        while (true) {
            if (isScout) {
                try {
                    goScouting();
                    isScout = false;
                } catch (GameActionException e) {
                    System.out.println(e.getMessage());
                }
            } else {
                //idk something
            }
        }
    }

    /**
     * TODO this is probably going to change. Since the validation logic is currently broken in the client
     * From what I've seen map creation doesn't get too crazy so as a start:
     *  if we are red go east
     *  if we are blue go west
     *  bfs if we hit the other side of the map
     *
     * I also think we need to return home, as the EC's can only see flags 40r^2 away.
     * TODO replace the static move east/west with PathFinder.findpath()
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
                    //Do nothing
                }

                if (robotController.onTheMap(new MapLocation(robotController.getLocation().x+6, robotController.getLocation().y))) {
                    bfs();
                }
            }
            goHome(homeBase);

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
                    //Do nothing
                }

                if (robotController.onTheMap(new MapLocation(robotController.getLocation().x-6, robotController.getLocation().y))) {
                    bfs();
                }
            }
            goHome(homeBase);
        }
    }

    private void goHome(MapLocation homeBase) throws GameActionException {
        Direction[] path = PathFinder.findPath(homeBase, robotController);
        while (!seesHome()) {
            for (Direction direction : path ) {
                if (robotController.canMove(direction)) {
                    robotController.move(direction);
                    Clock.yield();
                } else {
                    path = PathFinder.findPath(homeBase, robotController);
                }
            }
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
        //TODO Update with actual flag
        robotController.setFlag(location.x + location.y);
    }

    //Magic directions?
    private void bfs() throws GameActionException {
        while (!seesEnemyHQ()) {

        }
    }
}
