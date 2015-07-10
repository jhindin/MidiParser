package com.jhindin.midi.time;

import java.util.Locale;

public class PreciseTime  {
	public long millis;
	public int nanos;

	static final int MILLION = 1000000;
	
	public PreciseTime() {
		millis = 0;
		nanos = 0;
	}

	public PreciseTime(long millis, long nanos) {
		set(this, millis, nanos);
	}
	
	public static final void set(PreciseTime a, long millis, long nanos) {
		a.millis = millis + nanos / MILLION;
		a.nanos = (int)(nanos % MILLION);
	
	}

	public static final void mult(PreciseTime a1, long a2, PreciseTime res) {
		res.millis = a1.millis * a2;
		
		long n = a1.nanos * a2;
		res.millis += n / MILLION;
		res.nanos = (int)(n % MILLION);
	}

	public static final void add(PreciseTime a1, PreciseTime a2, PreciseTime res) {
		res.millis = a1.millis + a2.millis;
		
		int n = a1.nanos + a2.nanos; 
		res.millis += n / MILLION;
		res.nanos = (int)(n % MILLION);
	}

	public static final void substract(PreciseTime a1, PreciseTime a2, PreciseTime res) {
		res.millis = a1.millis - a2.millis;
		
		int n = a1.nanos - a2.nanos;
		if (n < 0) {
			n = MILLION + n;
			res.millis -= 1;
		}
		res.nanos = n;
	}

	public static final boolean greater(PreciseTime a1, PreciseTime a2) {
		if (a1.millis < a2.millis)
			return false;
		else if (a1.millis > a2.millis)
			return true;
		else
			return a1.nanos > a2.nanos;
	}

	public static final void div(PreciseTime a1, long a2, PreciseTime res) {
		res.millis = a1.millis / a2;
		
		long n = ((a1.millis % a2) * MILLION + a1.nanos) / a2; 
		res.millis += n / MILLION;
		res.nanos = (int)(n % MILLION);
		
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
		
		return String.format(Locale.getDefault(), "%d.%d",
			millis, shortNanos);
	}
}
