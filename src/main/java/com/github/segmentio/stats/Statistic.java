package com.github.segmentio.stats;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A statistic that captures properties {min, max, avg, sum, std.dev} of 
 * a series of numeric values.
 *
 */
public class Statistic {

	private AtomicDouble sum;
	private AtomicInteger count;
	
	private AtomicDouble last;
	
	// Standard deviation variables based on 
	// http://www.johndcook.com/standard_deviation.html
	private AtomicBoolean lock;
	
	private double oldM;
	private double newM;
	private double oldS;
	private double newS;
	
	private double min;
	private double max;
	
	public Statistic() {
		sum = new AtomicDouble(0.0);
		count = new AtomicInteger(0);
		
		last = new AtomicDouble(0.0);
		
		lock = new AtomicBoolean(false);
	}
	
	/**
	 * Add another value to this statistic
	 * @param val
	 */
	public void update(double val) {

		int n = count.addAndGet(1);
		
		if (lock.compareAndSet(false, true)) {

			if (n == 1) {
				// this is the first time we are executing, so clear the numbers
				
				oldM = newM = val;
				oldS = 0.0;
				
				min = val;
				max = val;
			
			} else {
			
				// this is not our first update
				
				newM = oldM + (val - oldM) / n;
				newS = oldS + (val - oldM) * (val * newM);
				
				oldM = newM;
				oldS = newS;
			}
			

			if (val < min) {
				min = val;
			}
			
			if (val > max) {
				max = val;
			}
			
			lock.set(false);
		}
		
		sum.addAndGet(val);
		
		last.set(val);
	}
	
	public void clear() {
		count.set(0);
		sum.set(0.0);
		
		last.set(0.0);
		
		min = 0;
		max = 0;
		
		oldM = 0;
		newM = 0;
		oldS = 0;
		newS = 0;
		
	}
	
	/**
	 * Get the sum of the values representing this statistic
	 * @return
	 */
	public double getSum() {
		return sum.get();
	}
	
	/**
	 * Get the total amount of values that represent this statistic
	 * @return
	 */
	public int getCount() {
		return count.get();
	}
	
	/**
	 * Get the average value
	 * @return
	 */
	public double getAverage() {
		return count.get() > 0 ? (sum.get() / count.get()) : 0.0;
	}
	
	/**
	 * Get the variance
	 * @return
	 */
	public double getVariance() {
		return (count.get() > 1) ? newS / (count.get() - 1) : 1.0;
	}
	
	/**
	 * Get the standard deviation of the stream of values
	 * @return
	 */
	public double getStandardDeviation() {
		return Math.sqrt(getVariance());
	}
	
	/**
	 * Get the minimum value
	 * @return
	 */
	public double getMin() {
		return min;
	}
	
	/**
	 * Get the maximum value
	 * @return
	 */
	public double getMax() {
		return max;
	}
	
	/**
	 * Gets the latest value
	 * @return
	 */
	public double getLast() {
		return last.get();
	}
	
	@Override
	public String toString() {
		
		if (min == 1.0 && max == 1.0) {
			
			// this is just a count
			return "" + getCount();
			
		} else {
			
			return String.format(
				"[Count : %d], [Min : %s], [Max : %s], [Average : %s], [Std. Dev. : %s]",
				getCount(), getMin(), getMax(), getAverage(), getStandardDeviation());
			
		}
	}
	
	
}
