package starter.models;

public class GitlabPackage {
	
	private String version;

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return this.version;
	}
	
}
