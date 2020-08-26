package model;


public class Direction {

private double _radians;
	
	public enum Reflection {TOP, BOTTOM, RIGHT, LEFT};
	
	public Direction(double radians) {
		_radians = normalize(radians);
	}
	
	public double getRadians() {
		return _radians;
	}
	
	public void rotate(double theta) {
		_radians = normalize(_radians+theta);
	}
	
	public void reflect(Direction.Reflection side) {
		
		switch (side) {
		case BOTTOM: rotate(Math.PI); break;
		case RIGHT: rotate(Math.PI/2.0); break;
		case LEFT: rotate(-1.0*Math.PI/2.0); break;
		}

		if (_radians > Math.PI*2) {
			_radians = 2.0 * Math.PI - _radians;
		} else {
			_radians *= -1.0;
		}

		switch (side) {
		case BOTTOM: rotate(-1.0*Math.PI); break;
		case RIGHT: rotate(-1.0*Math.PI/2.0); break;
		case LEFT: rotate(Math.PI/2.0); break;
		}		
	}
	
	private static double normalize(double angle) {
		if (angle < 0.0 || angle >= (2.0*Math.PI)) { 
			return ((angle % (2.0 * Math.PI)) + (2.0 * Math.PI)) % (2.0 * Math.PI);
		} else {
			return angle;
		}
	}

}
