import java.util.*;

public class SetItToNull {
	String s = "oof";
	
	static void foo1() {
		SetItToNull it = null;
		try {
			it = new SetItToNull();
		} catch(Throwable t) {}
		if (it == null)
			return;
		System.out.println(it.toString());
		System.out.println(it.s);
	}
	
	static void foo2(){
		SetItToNull it = new SetItToNull();
		do {
			it.foobar();
			System.out.println(it.s);
			it = null;
		} while(it != null);
	}
	
	static void foo3(){
		SetItToNull it = new SetItToNull();
		try {
			do {
				if (it.bar())
					it = null;
				it = new SetItToNull();
				it.foobar();
			} while(it != null);
		} catch (Exception e) {
			it.foobar();
			System.out.println(it.s);
		}
	}
	
	static void foo4(){
		for (int i = 0; i < 10; i++) {
			SetItToNull it = new SetItToNull();
			try {
				if (it.bar()) {
					it = null;
					continue;
				}
				it = new SetItToNull();
				it.foobar();
				System.out.println(it.s);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (it != null) {
					it.foobar();
					System.out.println(it.s);
				}
			}
		}
	}
	
	static SetItToNull foo5() {
		SetItToNull it = null;
		return it;
	}
	
	static String foo6() {
		SetItToNull it = new SetItToNull();
		String s = it.s;
		if (!s.equals(it)) {
			s = null;
		}
		return s;
	}
	
	void foobar() {}
	
	boolean bar() {return true;}
}