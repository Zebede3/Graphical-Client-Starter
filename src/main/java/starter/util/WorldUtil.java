package starter.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import starter.models.ProxyDescriptor;
import starter.util.WorldGrabber.World;

public class WorldUtil {

	private static final Pattern NUMBER = Pattern.compile("(\\-?\\d+)");
	
	public static String parseWorld(String world, List<Integer> blacklist, ProxyDescriptor proxy) throws WorldParseException {
		
		final List<Integer> worlds = new ArrayList<>();
		final Matcher matcher = NUMBER.matcher(world);
		while (matcher.find()) {
			worlds.add(Integer.parseInt(matcher.group()));
		}
		
		if (worlds.size() == 0) {
			return null; // let tribot choose
		}
		
		int num = worlds.get(RandomUtil.randomInRange(0, worlds.size() - 1));
		
		switch (num) {
		case 0:
		case -2:
			num = getRandomWorld(true, blacklist);
			break;
		case -1:
			num = getRandomWorld(false, blacklist);
			break;
		case -3:
			num = getRandomWorldByPing(true, blacklist, proxy);
			break;
		case -4:
			num = getRandomWorldByPing(false, blacklist, proxy);
			break;
		}
		
		return Integer.toString(num);
	}
	
	private static int getRandomWorldByPing(boolean members, List<Integer> blacklist, ProxyDescriptor proxy) throws WorldParseException {
		System.out.println("Attempting to find world by ping. This may take some time. (proxy: " + proxy + ")");
		final Map<Integer, Integer> pings = Arrays.stream(WorldGrabber.getWorlds())
				.filter(world -> isWorldValid(world, members, blacklist))
				.map(World::getId)
				.distinct()
				.parallel()
				.collect(Collectors.toMap(i -> i, i -> WorldPingChecker.getPing(proxy, i)));
		pings.entrySet().removeIf(e -> e.getValue() < 0);
		final Statistics stats = new Statistics(pings.values().stream().mapToInt(Integer::intValue).toArray());
		final Statistics cleaned = new Statistics(pings.values().stream().mapToInt(Integer::intValue).filter(i -> !(stats.isOutlier(i) && i > stats.getMean())).toArray());
		final double max = cleaned.getMin() + (cleaned.getSD() / 2);
		final int[] choices = pings.entrySet()
			.stream()
			.filter(e -> e.getValue() <= max)
			.mapToInt(e -> e.getKey())
			.toArray();
		if (choices.length > 0) {
			return choices[RandomUtil.randomInRange(0, choices.length - 1)];
		}
		else {
			System.out.println("Failed to find world by ping. Most likely a connection issue to the game. If you are using a proxy, your proxy may not work.");
			throw new WorldParseException();
		}
	}
	
	private static int getRandomWorld(boolean members, List<Integer> blacklist) throws WorldParseException {
		final World[] worlds = WorldGrabber.getWorlds();
		final World[] valid = Arrays.stream(worlds)
				.filter(w -> isWorldValid(w, members, blacklist))
				.toArray(World[]::new);
		if (valid.length == 0)
			throw new WorldParseException();
		final int index = RandomUtil.randomInRange(0, valid.length - 1);
		return valid[index].getId();
	}
	
	private static boolean isWorldValid(World w, boolean members, List<Integer> blacklist) {
		return w.isMember() == members
				&& !w.isPvp() && !w.isSkillTotal() && w.isNormalGame()
				&& w.getPlayerCount() < 1980
				&& !blacklist.contains(w.getId());
	}
	
}
