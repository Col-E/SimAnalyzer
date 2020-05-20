import java.util.*;

public class SetItToNull {
	static void foo1() {
		SetItToNull it = null;
		try {
			it = new SetItToNull();
		} catch(Throwable t) {}
		if (it == null)
			return;
		System.out.println(it.toString());
	}
	
	static void foo2(){
		SetItToNull it = new SetItToNull();
		do {
			it.foobar();
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
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (it != null)
					it.foobar();
			}
		}
	}
	
	void foobar() {}
	
	boolean bar() {return true;}
}