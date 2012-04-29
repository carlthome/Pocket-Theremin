package kth.csc.inda.pockettheremin.utils;

public class RangedDouble {
	final public double maximum, minimum, range;
	private double current;

	public RangedDouble(double maximum, double minimum) {
		this.maximum = maximum;
		this.minimum = minimum;
		this.range = maximum - minimum;
		this.current = minimum;
	}

	public boolean set(double value) {
		if (value > maximum)
			return false;

		if (value < minimum)
			return false;

		this.current = value;
		return true;
	}

	public double get() {
		return current;
	}
}