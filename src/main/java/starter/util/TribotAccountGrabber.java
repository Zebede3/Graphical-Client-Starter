package starter.util;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

import com.google.gson.Gson;

public class TribotAccountGrabber {
	
	private static volatile Account[] cached;

	public static Account[] getAccounts() {
		if (cached != null) {
			return cached;
		}
		final File f = FileUtil.getAccountJsonFile();
		if (!f.exists()) {
			return new Account[0];
		}
		try {
			final byte[] contents = Files.readAllBytes(f.toPath());
			final String s = new String(contents);
			final AccountHolder accs = new Gson().fromJson(s, AccountHolder.class);
			if (accs.accounts == null) {
				return new Account[0];
			}
			cached = Arrays.stream(accs.accounts).filter(Objects::nonNull).toArray(Account[]::new);
			return cached;
		}
		catch (Exception e) {
			e.printStackTrace();
			return new Account[0];
		}
	}
	
	private static class AccountHolder {
		
		private Account[] accounts;
		
	}
	
	public static class Account {
		
		private String name;
		private String password;
		private String pin;
		private int world;
		private String skill;

		public String getName() {
			return this.name;
		}

		public String getPassword() {
			return this.password;
		}

		public String getPin() {
			return this.pin;
		}

		public int getWorld() {
			return this.world;
		}

		public String getSkill() {
			return this.skill;
		}
	}

}
