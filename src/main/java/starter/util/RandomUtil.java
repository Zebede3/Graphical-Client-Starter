package starter.util;

import java.util.Random;

public class RandomUtil {
	
	private static final ThreadLocal<Random> random = ThreadLocal.withInitial(() -> new Random());
	
	public static int randomInRange(int min, int max) {
		return min + random.get().nextInt(max - min + 1);
	}

}
