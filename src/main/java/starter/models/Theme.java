package starter.models;

import starter.util.EnumUtil;

public enum Theme {

	DEFAULT("/css/default.css"),
	BOOTSTRAP2("/css/bootstrap2.css"),
	DARK("/css/dark-theme.css"),
	FLAT_BEE("/css/flat-bee.css"),
	FLAT_RED("/css/flat-red.css"),
	GREEN("/css/green.css"),
	MIST("/css/mist.css"),
	MODENA_DARK("/css/modena-dark.css"),
	MUSTARD("/css/mustard.css"),
	WIN7_GLASS("/css/win7-glass.css"),
	;
	
	private final String css;
	
	private Theme(String css) {
		this.css = css;
	}
	
	public String getCss() {
		return this.css;
	}
	
	@Override
	public String toString() {
		return EnumUtil.toString(this);
	}
	
}
