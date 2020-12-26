package starter.util;

import java.util.Arrays;

public class Statistics {

	private final double[] values;
	
	// note that this maps every int to a double
	public Statistics(int... values) {
		this(Arrays.stream(values).mapToDouble(Double::valueOf).toArray());
	}
	
	// note that this maps every long to a double
	public Statistics(long... values) {
		this(Arrays.stream(values).mapToDouble(Double::valueOf).toArray());
	}
	
	public Statistics(double... values) {
		this.values = Arrays.copyOf(values, values.length);
		Arrays.sort(this.values);
	}
	
	public double getPercentile(int percentile) {
		if (percentile < 0 || percentile > 100)
			throw new IllegalArgumentException();
		if (this.values.length == 0)
			return -1;
		return this.values[percentile * (this.values.length - 1) / 100];
	}
	
	public double firstQuartile() {
		return getPercentile(25);
	}
	
	public double secondQuartile() {
		return getPercentile(50);
	}
	
	public double median() {
		final int n = this.values.length;
		if (n == 0)
			return -1;
		return n % 2 == 0
				? (this.values[(n / 2) - 1] + this.values[n / 2]) / 2
				: this.values[n / 2];
	}

	public double thirdQuartile() {
		return getPercentile(75);
	}
	
	public double interQuartileRange() {
		return thirdQuartile() - firstQuartile();
	}
	
	public int findPercentile(double value) {
		for (int i = 0; i < this.values.length; i++)
			if (this.values[i] == value)
				return (int) (i / (double) this.values.length * 100);
		return -1;
	}
	
	public boolean isOutlier(double value) {
		return isOutlier(value, 1.5D);
	}
	
	public boolean isOutlier(double value, double iqrScale) {
		
		final double first = firstQuartile();
		final double third = thirdQuartile();
		final double range = third - first;
		
		final double scaledRange = range * iqrScale;
		
		return first - scaledRange > value
				|| third + scaledRange < value;
	}
	
	public double getMax() {
		return this.values.length > 0
				? this.values[this.values.length - 1]
				: -1;
	}
	
	public double getMin() {
		return this.values.length > 0
				? this.values[0]
				: -1;
	}
	
	public int numberOfValues() {
		return this.values.length;
	}
	
	public double[] getValueCopy() {
		return Arrays.copyOf(this.values, this.values.length);
	}
	
	public double getMean() {
		final int n = numberOfValues();
		return n > 0
				? Arrays.stream(this.values).sum() / (double)n
				: 0D;
	}
	
	public double getSD() {
		return getSD(Frame.POPULATION);
	}
	
	public double getSDSample() {
		return getSD(Frame.SAMPLE);
	}
	
	public double getSD(Frame frame) {
		return frame.standardDeviation(getMean(), this.values);
	}
	
	public double getRange() {
		if (this.values.length == 0)
			return -1;
		return this.values[this.values.length - 1] - this.values[0];
	}
	
	public enum Frame {
		
		POPULATION,
		SAMPLE;
		
		public double standardDeviation(double mean, double... values) {
			
			int divisor = 0;
			
			switch (this) {
			case POPULATION:
				divisor = values.length;
				break;
			case SAMPLE:
				divisor = values.length - 1;
				break;
			}
			
			if (divisor <= 0)
				return 0D;
			
			return Math.sqrt(Arrays.stream(values).map(val -> Math.pow(val - mean, 2)).sum() / divisor);
		}
	}

	@Override
	public String toString() {
		return "Statistics [values=" + Arrays.toString(values) + "]";
	}
	
}
