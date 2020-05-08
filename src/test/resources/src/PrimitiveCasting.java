public class PrimitiveCasting {
	static int b2i(boolean b) {
		return b ? 1 : 0;
	}

	static int c2i(char c) {
		return (int) c;
	}

	static long i2l(int i) {
		return (long) i;
	}

	static short l2s(long i) {
		return (short) i;
	}
}