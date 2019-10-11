package starter.gui;

import starter.util.EnumUtil;

public enum AccountColumn {

	NAME,
	PASSWORD,
	PIN,
	
	SCRIPT,
	ARGS,
	
	WORLD,
	BREAK_PROFILE,
	HEAP_SIZE,
	USE_PROXY,
	PROXY,
	;

	public boolean isDefaultColumn() {
		switch (this) {
		case PASSWORD:
		case PIN:
			return false;
		default:
			return true;
		}
	}
	
	@Override
	public String toString() {
		return EnumUtil.toString(this);
	}
	
}
