package starter.util;

public class EnumUtil {

	public static <T extends Enum<?>> String toString(T e) {
		final String name = e.name();
		if (name.length() == 1)
			return name;
		return name.charAt(0) + name.substring(1).replace("_", " ").toLowerCase();
	}
	
}
