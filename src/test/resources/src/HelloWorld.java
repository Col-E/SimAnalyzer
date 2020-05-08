public class HelloWorld {
	private String helloField = "Hello World";

	static void hello() {
		System.out.println("Hello World");
	}

	static void helloVariables() {
		String hello = "Hello";
		String world = "World";
		System.out.println(hello + " " + world);
	}

	static void newHello() {
		System.out.println(new HelloWorld().helloField);
	}

	static void helloSplit() {
		sayTwoWords("Hello", "World");
	}

	static void helloFromGet() {
		System.out.println(getHello());
	}

	static void tryCatchFinallyHello() {
		try {
			System.out.println("Hello");
		} catch(Exception ex) {
			System.out.println("(oof)");
		} finally {
			System.out.println("World");
		}
	}

	static CharSequence getHello() {
		return "Hello World";
	}

	static void sayTwoWords(String one, String two) {
		System.out.println(one + " " + two);
	}
}