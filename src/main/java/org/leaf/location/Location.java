package org.leaf.location;

/**
 * Location in a 2D-coordinate space
 */
public class Location {

    public static Location NULL = new Location(Double.NaN, Double.NaN);

	private double x;
	private double y;

	public Location(double x, double y) {
		this.x = x;
		this.y = y;
	}

    /**
     * Euclidean distance between two locations
     */
    public double distance(Location location) {
        return Math.sqrt(
            (location.getY() - this.getY()) * (location.getY() - this.getY()) +
                (location.getX() - this.getX()) * (location.getX() - this.getX())
        );
    }

	public boolean equals(Location other) {
		if (other == this) {
            return true;
        }
		return (this.x == other.x && this.y == other.y);
	}

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
