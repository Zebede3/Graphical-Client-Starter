package starter.models;

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
	PROXY_IP,
	PROXY_PORT,
	PROXY_USER,
	PROXY_PASS,
	;

	public boolean isDefaultColumn() {
		switch (this) {
		case PASSWORD:
		case PIN:
		case PROXY_IP:
		case PROXY_PORT:
		case PROXY_USER:
		case PROXY_PASS:
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
