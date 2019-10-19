package starter.models;

import java.util.function.Function;

public class ProxyColumnData {

	private final Function<ProxyDescriptor, String> displayFunction;
	
	private final AccountColumn corresponding;
	
	public ProxyColumnData(Function<ProxyDescriptor, String> displayFunction,
			AccountColumn corresponding) {
		this.displayFunction = displayFunction;
		this.corresponding = corresponding;
	}

	public String getLabel() {
		return this.corresponding.getLabel();
	}

	public Function<ProxyDescriptor, String> getDisplayFunction() {
		return this.displayFunction;
	}

	public AccountColumn getCorresponding() {
		return this.corresponding;
	}
	
}
