package basicplayer;

import java.util.Comparator;

import battlecode.common.MapLocation;

public class LocationComparator implements Comparator<Location> {
    public int compare( Location l1, Location l2 ) {
        int cost = 0;

        if ( l1 instanceof DestLocation ) {
            cost = compare( (DestLocation) l1, (DestLocation) l2 );
        } else if ( l1 instanceof FleeLocation ) {
            cost = compare( (FleeLocation) l1, (FleeLocation) l2 );
        } else {
            cost = defaultCompare( l1, l2 );
        }
        return ( cost );
    }

    public int compare( DestLocation l1, DestLocation l2 ) {
        double l1Cost = l1.getDistance() + 0.5 / l1.getPassability();
        double l2Cost = l2.getDistance() + 0.5 / l2.getPassability();

        return ( (int) ( l1Cost - l2Cost ) );
    }

    public int compare( FleeLocation l1, FleeLocation l2 ) {
        double l1AvgEnemyDist = 0;
        double l2AvgEnemyDist = 0;

        if ( l1.getEnemies().size() > 0 ) {
            for ( MapLocation location : l1.getEnemies() ) {
                l1AvgEnemyDist += l1.getLocation().distanceSquaredTo( location );
                l2AvgEnemyDist += l2.getLocation().distanceSquaredTo( location );
            }

            l1AvgEnemyDist /= l1.getEnemies().size();
            l2AvgEnemyDist /= l1.getEnemies().size();
        }

        double l1AvgECDist = 0;
        double l2AvgECDist = 0;
        
        if ( l1.getEnlightenmentCenters().size() > 0 ) {
            for ( MapLocation location : l1.getEnlightenmentCenters() ) {
                l1AvgECDist += l1.getLocation().distanceSquaredTo( location );
                l2AvgECDist += l2.getLocation().distanceSquaredTo( location );
            }

            l1AvgECDist /= l1.getEnlightenmentCenters().size();
            l2AvgECDist /= l2.getEnlightenmentCenters().size();
        }

        double l1Cost = l1AvgEnemyDist + l1AvgECDist + 0.5 / l1.getPassability();
        double l2Cost = l2AvgEnemyDist + l2AvgECDist + 0.5 / l2.getPassability();

        return ( (int) ( l2Cost - l1Cost ) );
    }

    private int defaultCompare( Location l1, Location l2 ) {
        double l1Cost = 0.5 / l1.getPassability();
        double l2Cost = 0.5 / l2.getPassability();

        return ( (int) ( l1Cost - l2Cost ) );
    }
}
