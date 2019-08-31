package starter.models;

public class AccountColumnData {

	private final String label;
	private final String fieldName;
	
	public AccountColumnData(String label, String fieldName) {
		this.label = label;
		this.fieldName = fieldName;
	}

	public String getLabel() {
		return label;
	}

	public String getFieldName() {
		return fieldName;
	}
	
}
