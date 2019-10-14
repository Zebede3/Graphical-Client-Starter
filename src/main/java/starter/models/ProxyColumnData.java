package starter.models;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ProxyColumnData {

	private final String label;

	private final Function<ProxyDescriptor, String> displayFunction;
	private final BiFunction<String, ProxyDescriptor, ProxyDescriptor> editFunction;
	
	private final AccountColumn corresponding;
	
	public ProxyColumnData(String label,
			Function<ProxyDescriptor, String> displayFunction, 
			BiFunction<String, ProxyDescriptor, ProxyDescriptor> editFunction,
			AccountColumn corresponding) {
		this.label = label;
		this.displayFunction = displayFunction;
		this.editFunction = editFunction;
		this.corresponding = corresponding;
	}

	public String getLabel() {
		return this.label;
	}

	public Function<ProxyDescriptor, String> getDisplayFunction() {
		return this.displayFunction;
	}

	public BiFunction<String, ProxyDescriptor, ProxyDescriptor> getEditFunction() {
		return this.editFunction;
	}

	public AccountColumn getCorresponding() {
		return this.corresponding;
	}
	
}
