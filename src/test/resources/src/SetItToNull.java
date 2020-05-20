public class SetItToNull {
	static void foo() {
		SetItToNull it = null;
		try {
			it = new SetItToNull();
		} catch(Throwable t) {}
		if (it == null)
			return;
		System.out.println(it.toString());
	}
}