package kth.csc.inda.pockettheremin.music;

public enum Scale {
	MAJOR, MINOR, CHROMATIC;

	public int[] steps() {
		switch (this) {
		case MAJOR: {
			int[] steps = { 2, 2, 1, 2, 2, 2, 1 };
			return steps;
		}
		case MINOR: {
			int[] steps = { 2, 1, 2, 2, 1, 2, 2 };
			return steps;
		}
		case CHROMATIC: {
			int[] steps = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
			return steps;
		}
		}
		throw new AssertionError(this);

	}
}
