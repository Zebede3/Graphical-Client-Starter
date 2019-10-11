package starter.util;

import java.lang.reflect.InvocationTargetException;

public class ReflectionUtil {

	public static void setValue(Object obj, String fieldName, Object value) {
		final String methodName = "set" + (fieldName.length() > 1
								? Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1)
								: fieldName.toUpperCase());
		try {
			obj.getClass().getMethod(methodName, value.getClass()).invoke(obj, value);
		} 
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			e.printStackTrace();
		}

	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getValue(Object obj, String fieldName) {
		final String methodName = "get" + (fieldName.length() > 1
								? Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1)
								: fieldName.toUpperCase());
		try {
			return (T) obj.getClass().getMethod(methodName).invoke(obj);
		} 
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			e.printStackTrace();
			return null;
		}

	}
	
}
