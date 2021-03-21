package starter.util;

public enum ImportStrategy {

	CREATE_NEW("Create new account for each line"),
	MERGE_ROW_INDEX("Merge lines with existing accounts where possible (by row index)"),
	MERGE_LOGIN_NAME("Merge lines with existing accounts where possible (by login name)"),
	MERGE_ROW_INDEX_SELECTED("Merge lines with existing selected accounts where possible (by row index)"),
	ONLY_MERGE_ROW_INDEX("Merge lines with existing accounts and skip non-matching rows (by row index)"),
	ONLY_MERGE_LOGIN_NAME("Merge lines with existing accounts and skip non-matching rows (by login name)"),
	ONLY_MERGE_ROW_INDEX_SELECTED("Merge lines with existing selected accounts and skip non-matching rows (by row index)"),
	;
	
	private final String display;
	
	private ImportStrategy(String display) {
		this.display = display;
	}
	
	@Override
	public String toString() {
		return this.display;
	}
	
}
