public class ExplodeStr {
	public static void foo() {
		String test1 = xor("abcdef", 0);
		String test2 = xor("abcdef", 1);
		String test3 = xor("abcdef", 2);
		System.out.println(test1);
		System.out.println(test2);
		System.out.println(test3);
	}
		
	public static String xor(String input, int key) {
		char[] ca = input.toCharArray();
		for (int i = 0; i < ca.length; i++)
			ca[i] = (char) (ca[i] ^ key);
		return new String(ca);
	}
}