package starter.util;

public class FileFormats {

	public static final FileFormat CSV = new FileFormat() {
		@Override
		public String delimiter() {
			return ",";
		}

		@Override
		public String extension() {
			return "csv";
		};
	};
	
	public static final FileFormat TSV = new FileFormat() {
		@Override
		public String delimiter() {
			return "\t";
		}
		@Override
		public String extension() {
			return "tsv";
		}
	};
	
}
