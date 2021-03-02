package starter.util;

public enum ImportAction {

	CREATE_NEW("Create new account for each line"),
	MERGE_ROW_INDEX("Merge lines with existing accounts where possible (by row index)"),
	MERGE_LOGIN_NAME("Merge lines with existing accounts where possible (by login name)"),
	ONLY_MERGE_ROW_INDEX("Merge lines with existing accounts and skip non-matching rows (by row index)"),
	ONLY_MERGE_LOGIN_NAME("Merge lines with existing accounts and skip non-matching rows (by login name)");
	
	private final String display;
	
	private ImportAction(String display) {
		this.display = display;
	}
	
	@Override
	public String toString() {
		return this.display;
	}
	
}
