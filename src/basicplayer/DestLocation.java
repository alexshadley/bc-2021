package basicplayer;

import battlecode.common.MapLocation;

/**
 * Locations for moving towards a destination
 */
public class DestLocation extends Location {
    // Distance weight
    private double distance;

    /**
     * Constructor to initialize a location
     */
    public DestLocation( MapLocation location, double passability, double distance ) {
        super( location, passability );
        this.distance = distance;
    }

    /**
     * Get distance to destination from this location
     *
     * @return distance to destination from location
     */
    public double getDistance() {
        return ( distance );
    }

    /**
     * Compare to destination locations for reaching a destination
     */
    @Override
    public int compareTo( Location loc2 ) {
        DestLocation l2 = (DestLocation) loc2;

        double l1Cost = distance + 5.0 / passability;
        double l2Cost = l2.getDistance() + 5.0 / l2.getPassability();

        return ( (int) ( l1Cost - l2Cost ) );
    }
}