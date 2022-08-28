class LongMath {
	static void printNum(long a, long b) {
		long c = ((a * 2L) + (b / 2L)) + (a & b);
		System.out.println("value = " + c);
	}
}