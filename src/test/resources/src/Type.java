import java.util.*;

public class Type {
	static Type foo1(List<Type> types) {
		for (Type type : types)
			bar(type);
		Type ret = null;
		for (Type type : types)
			bar(type);
		return ret;
	}
	
	static Type foo2(List<Type> list) {
		Iterator<Type> iterator = list.iterator();
		while (iterator.hasNext())
			bar(iterator.next());
		Type ret = null;
		Iterator<Type> iterator2 = list.iterator();
		while (iterator2.hasNext())
			bar(iterator2.next());
		return ret;
	}
	
	static void bar(Type t){}
}