package com.jhindin.midi;

public class PreciseTime  {
	long millis;
	int nanos;

	public PreciseTime(long millis, int nanos) {			
		this.millis = millis + nanos / Main.MILLION;
		this.nanos = nanos % Main.MILLION;
	}
	
	public static void mult(PreciseTime a1, long a2, PreciseTime res) {
		res.millis = a1.millis * a2;
		
		long n = a1.nanos * a2;
		res.millis += n / Main.MILLION;
		res.nanos = (int)(n % Main.MILLION);
	}

	public static void add(PreciseTime a1, PreciseTime a2, PreciseTime res) {
		res.millis = a1.millis + a2.millis;
		
		int n = a1.nanos + a2.nanos; 
		res.millis += n / Main.MILLION;
		res.nanos = (int)(n % Main.MILLION);
	}
	
	public static void div(PreciseTime a1, long a2, PreciseTime res) {
		res.millis = a1.millis / a2;
		
		long n = ((a1.millis % a2) * Main.MILLION + a1.nanos) / a2; 
		res.millis += n / Main.MILLION;
		res.nanos = (int)(n % Main.MILLION);
		
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (obj == this)
			return true;
		
		if (! (obj instanceof PreciseTime))
			return false;
		
		PreciseTime t1 = (PreciseTime) obj;
		return millis ==  t1.millis && nanos == t1.nanos;
	}

	@Override
	public String toString() {
		int shortNanos = nanos;
		
		while (shortNanos > 0 && shortNanos % 10 == 0)
			shortNanos /= 10;
		
		return String.format("%d.%d", millis, shortNanos);
	}
}