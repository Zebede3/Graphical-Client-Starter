package starter.util;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import starter.util.WorldGrabber.World;

public class WorldUtil {

	public static String parseWorld(String world, List<Integer> blacklist) throws WorldParseException {
		
		int num;
		try {
			num = Integer.parseInt(world.trim());
		}
		catch (Exception e) {
			return null; // let tribot choose
		}
		
		if (num == 0 || num == -2)
			num = getRandomWorld(true, blacklist);
		
		else if (num == -1)
			num = getRandomWorld(false, blacklist);
		
		return Integer.toString(num);
	}
	
	private static int getRandomWorld(boolean members, List<Integer> blacklist) throws WorldParseException {
		final World[] worlds = WorldGrabber.getWorlds();
		final World[] valid = Arrays.stream(worlds)
				.filter(w -> w.isMember() == members)
				.filter(w -> !w.isPvp() && !w.isSkillTotal() && w.isNormalGame())
				.filter(w -> w.getPlayerCount() < 1980)
				.filter(w -> !blacklist.contains(w.getId()))
				.toArray(World[]::new);
		if (valid.length == 0)
			throw new WorldParseException();
		final Random random = new Random();
		final int index = random.nextInt(valid.length);
		return valid[index].getId();
	}
	
}
