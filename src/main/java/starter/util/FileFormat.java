package starter.util;

public interface FileFormat {
	
	String delimiter();
	
	String extension();
	
	default String description() {
		return extension().toUpperCase();
	}
	
}