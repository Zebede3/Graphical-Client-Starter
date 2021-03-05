package starter.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

import starter.gson.GsonFactory;

public class TribotAccountGrabber {
	
	private static volatile Account[] cached;

	public static Account[] getAccounts() {
		if (cached == null) {
			cached = tryRead();
		}
		return cached;
	}
	
	public static void addAccounts(Account... accounts) {
		final Account[] initial = tryRead();
		final Account[] existing = Arrays.stream(initial) // remove duplicates
				.filter(a -> Arrays.stream(accounts)
						.noneMatch(acc -> Objects.equals(a.getName(), acc.getName())))
				.toArray(Account[]::new);
		final Account[] joined = ArrayUtil.concat(existing, accounts);
		final AccountHolder holder = new AccountHolder();
		holder.accounts = joined;
		final String s = GsonFactory.buildGson().toJson(holder);
		try {
			Files.write(FileUtil.getAccountJsonFile().toPath(), s.getBytes());
			System.out.println("Exported " + accounts.length + " accounts to the tribot account manager");
			System.out.println("Overwrote " + (initial.length - existing.length) + " accounts in tribots account manager while exporting");
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to add accounts to the tribot account manager");
		}
	}
	
	private static Account[] tryRead() {
		final File f = FileUtil.getAccountJsonFile();
		if (!f.exists()) {
			return new Account[0];
		}
		try {
			final byte[] contents = Files.readAllBytes(f.toPath());
			final String s = new String(contents);
			final AccountHolder accs = GsonFactory.buildGson().fromJson(s, AccountHolder.class);
			if (accs.accounts == null) {
				return new Account[0];
			}
			return Arrays.stream(accs.accounts).filter(Objects::nonNull).toArray(Account[]::new);
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
		private String totpSecret;
		
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

		public void setName(String name) {
			this.name = name;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public void setPin(String pin) {
			this.pin = pin;
		}

		public void setWorld(int world) {
			this.world = world;
		}

		public void setSkill(String skill) {
			this.skill = skill;
		}

		public String getTotpSecret() {
			return this.totpSecret;
		}

		public void setTotpSecret(String totpSecret) {
			this.totpSecret = totpSecret;
		}
	}

}
