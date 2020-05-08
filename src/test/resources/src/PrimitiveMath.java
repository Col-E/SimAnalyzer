public class PrimitiveMath {
	static long math1(int a, long b) {
		return b >> a;
	}

	static long math2(int a, long b) {
		return b >>> a;
	}

	static long math3(int a, long b) {
		return b << a;
	}

	static int convoluted(int a) {
		return ((a << 5) | a) + (((a >> 2 & 0x010) != 0)  ? -a >>> 1 : a << 3);
	}

	static int maskOp(int a, long b) {
		return (int) (b & 0x00001111) << a;
	}
}