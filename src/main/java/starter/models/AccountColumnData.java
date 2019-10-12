package starter.models;

public class AccountColumnData {

	private final String label;
	private final String fieldName;
	private final AccountColumn corresponding;
	
	public AccountColumnData(String label, String fieldName, AccountColumn col) {
		this.label = label;
		this.fieldName = fieldName;
		this.corresponding = col;
	}

	public String getLabel() {
		return label;
	}

	public String getFieldName() {
		return fieldName;
	}

	public AccountColumn getCorresponding() {
		return this.corresponding;
	}
	
}
