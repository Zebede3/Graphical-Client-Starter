package starter.util;

import java.io.DataInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

class WorldGrabber {

	private static final Map<Integer, World> worlds = new ConcurrentHashMap<>();
	private static final AtomicLong cacheTime = new AtomicLong(0);
	
	private static final long CACHE_THRESHOLD = 30 * 60 * 1000;
	
	public static World getWorld(int world) {
		world %= 300;
		checkCache();
		synchronized (worlds) {
			return worlds.get(world);
		}
	}
	
	public static World[] getWorlds() { 
		checkCache();
		synchronized (worlds) {
			return worlds.values().toArray(new World[0]);
		}
	}
	
	private static void forceRecache() {
		synchronized (worlds) {
			worlds.clear();
			final Map<Integer, World> worldList = loadWorldList();
			if (worldList.size() > 0) {
				worlds.putAll(worldList);
				cacheTime.set(System.currentTimeMillis());
			}
		}
	}
	
	private static void checkCache() {
		synchronized (worlds) {
			if (System.currentTimeMillis() - cacheTime.get() >= CACHE_THRESHOLD)
				forceRecache();
		}
	}
	
	private static Map<Integer, World> loadWorldList() {
		
		try {
		
	        Map<Integer, World> worldList = new HashMap<>();
	        URLConnection urlConnection = new URL("http://oldschool.runescape.com/slr").openConnection();
	        urlConnection.setConnectTimeout(15000);
	        urlConnection.setReadTimeout(15000);
	        try (DataInputStream dataInputStream = new DataInputStream(urlConnection.getInputStream())) {
	            @SuppressWarnings("unused")
				int size = dataInputStream.readInt() & 0xFF;
	            int worldCount = dataInputStream.readShort();
	            for (int i = 0; i < worldCount; i++) {
	                int world = (dataInputStream.readShort() & 0xFFFF) % 300, flag = dataInputStream.readInt();
	                boolean member = (flag & 0x1) != 0, pvp = (flag & 0x4) != 0, highRisk = (flag & 0x400) != 0;
	                StringBuilder sb = new StringBuilder();
	                String host = null, activity;
	                byte b;
	                while (true) {
	                    if ((b = dataInputStream.readByte()) == 0) {
	                        if (host == null) {
	                            host = sb.toString();
	                            sb = new StringBuilder();
	                        } else {
	                            activity = sb.toString();
	                            break;
	                        }
	                    } else {
	                        sb.append((char) b);
	                    }
	                }
	                worldList.put(world, new World(world, flag, member, pvp, highRisk, host, activity, dataInputStream.readByte() & 0xFF, dataInputStream.readShort()));
	            }
	        }
	
	        return worldList;
        
		}
		catch (Exception e) {
			return new HashMap<>();
		}
    }

    public static class World {

        private final int id;
        private final boolean member;
        private final boolean pvp;
        private final boolean highRisk;
        private final boolean deadman, skillTotal;
        private final String host;
        private final String activity;
        private final int serverLoc;
        private final int playerCount;
        private final int flag;

        public World(int id, int flag, boolean member, boolean pvp, boolean highRisk, String host, String activity, int serverLoc, int playerCount) {
            this.id = id;
            this.flag = flag;
            this.member = member;
            this.pvp = pvp;
            this.highRisk = highRisk;
            this.host = host;
            this.activity = activity;
            this.serverLoc = serverLoc;
            this.playerCount = playerCount;
            deadman = activity.toLowerCase().contains("deadman");
            skillTotal = activity.toLowerCase().contains("skill");
        }

        public int getId() {
            return id;
        }

        public int getFlag() {
            return flag;
        }

        public boolean isMember() {
            return member;
        }

        public boolean isPvp() {
            return pvp;
        }

        public boolean isHighRisk() {
            return highRisk;
        }

        public boolean isDeadman() {
            return deadman;
        }

        public String getHost() {
            return host;
        }

        public String getActivity() {
            return activity;
        }

        public int getServerLoc() {
            return serverLoc;
        }

        public int getPlayerCount() {
            return playerCount;
        }

        public boolean isSkillTotal() {
            return skillTotal;
        }

        public boolean isLeague() {
        	return this.activity.toLowerCase().contains("league");
        }
        
		public boolean isBetaWorld() {
			return this.activity.toLowerCase().contains("beta");
		}
        
        @Override
        public String toString() {
            return "[World " + id + " | " + playerCount + " players | " + (member ? "Members" : "F2P") + " | " + activity + "]";
        }

    }
	
}
