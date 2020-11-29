package starter.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class ReflectionUtil {

	public static void setValue(Object obj, String fieldName, Object value) {
		setValue(obj, fieldName, value, value.getClass());
	}
	
	public static void setValue(Object obj, String fieldName, Object value, Class<?> valueClass) {
		final String methodName = "set" + (fieldName.length() > 1
								? Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1)
								: fieldName.toUpperCase());
		try {
			obj.getClass().getMethod(methodName, valueClass).invoke(obj, value);
		} 
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			e.printStackTrace();
		}
	}
	
	public static void setValueDirectly(Object obj, String fieldName, Object value) {
		try {
			final Field field = obj.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(obj, value);
		} 
		catch (IllegalAccessException | IllegalArgumentException
				| SecurityException | NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getValueDirectly(Object obj, String fieldName) {
		try {
			final Field field = obj.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T) field.get(obj);
		} 
		catch (IllegalAccessException | IllegalArgumentException
				| SecurityException | NoSuchFieldException e) {
			e.printStackTrace();
			return null;
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
