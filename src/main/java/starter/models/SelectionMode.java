package starter.models;

import starter.util.EnumUtil;

public enum SelectionMode {

	ROW,
	CELL,
	;
	
	@Override
	public String toString() {
		return EnumUtil.toString(this);
	}
	
}
