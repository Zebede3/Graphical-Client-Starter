package starter.models;

import java.util.Comparator;

public class SemanticVersion implements Comparable<SemanticVersion> {

	private static final Comparator<SemanticVersion> COMPARATOR =
			Comparator.<SemanticVersion>comparingInt(v -> v.major)
			.thenComparingInt(v -> v.minor)
			.thenComparingInt(v -> v.patch);
	
	private final int major, minor, patch;
	
	public SemanticVersion(int major, int minor, int patch) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
	}

	@Override
	public int compareTo(SemanticVersion o) {
		return COMPARATOR.compare(this, o);
	}
	
}
