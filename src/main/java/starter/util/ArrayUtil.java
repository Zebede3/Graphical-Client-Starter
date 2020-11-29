package starter.util;

import java.lang.reflect.Array;

public class ArrayUtil {

	public static <T> T[] concat(T[] a, T[] b) {
		
	    final int aLen = a.length;
	    final int bLen = b.length;

	    @SuppressWarnings("unchecked")
	    final T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
	    System.arraycopy(a, 0, c, 0, aLen);
	    System.arraycopy(b, 0, c, aLen, bLen);

	    return c;
	}
	
}
