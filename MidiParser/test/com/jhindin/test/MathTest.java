package com.jhindin.test;

import static org.junit.Assert.*;

import com.jhindin.midi.PreciseTime;

import org.junit.Test;

public class MathTest {

	@Test
	public void testMult1() {
		PreciseTime t1 = new PreciseTime(1, 500000);
		PreciseTime res = new PreciseTime(0, 0);
		
		PreciseTime.mult(t1, 2, res);
		
		assertEquals(res, new PreciseTime(3,  0));
	}

	@Test
	public void testMult2() {
		PreciseTime t1 = new PreciseTime(2, 700000);
		PreciseTime res = new PreciseTime(0, 0);
		
		PreciseTime.mult(t1, 2, res);
		
		assertEquals(res, new PreciseTime(5, 400000));
	}

	@Test
	public void testString1() {
		PreciseTime t1 = new PreciseTime(2, 700000);
		String s = t1.toString();
		
		
		assertEquals(s, "2.7");
	}

	@Test
	public void testString2() {
		PreciseTime t1 = new PreciseTime(14, 753000);
		String s = t1.toString();
		
		
		assertEquals(s, "14.753");
	}

	@Test
	public void testDiv1() {
		PreciseTime t1 = new PreciseTime(2, 700000);
		PreciseTime res = new PreciseTime(0, 0);
		
		PreciseTime.div(t1, 2, res);
		
		
		assertEquals("1.35", res.toString());
	}

	@Test
	public void testDiv2() {
		PreciseTime t1 = new PreciseTime(3, 600000);
		PreciseTime res = new PreciseTime(0, 0);
		
		PreciseTime.div(t1, 3, res);
		
		
		assertEquals("1.2", res.toString());
	}

	@Test
	public void testDiv3() {
		PreciseTime t1 = new PreciseTime(3, 0);
		PreciseTime res = new PreciseTime(0, 0);
		
		PreciseTime.div(t1, 3, res);
		
		
		assertEquals("1.0", res.toString());
	}

	@Test
	public void testDiv4() {
		PreciseTime t1 = new PreciseTime(2, 400000);
		PreciseTime res = new PreciseTime(0, 0);
		
		PreciseTime.div(t1, 6, res);
		
		
		assertEquals("0.4", res.toString());
	}

	@Test
	public void testDiv5() {
		PreciseTime t1 = new PreciseTime(8, 100000);
		PreciseTime res = new PreciseTime(0, 0);
		
		PreciseTime.div(t1, 9, res);
		
		
		assertEquals("0.9", res.toString());
	}

	@Test
	public void testDiv6() {
		PreciseTime t1 = new PreciseTime(8, 100000);
		PreciseTime res = new PreciseTime(0, 0);
		
		PreciseTime.div(t1, 6, res);
		
		
		assertEquals("1.35", res.toString());
	}

	@Test
	public void testDiv7() {
		PreciseTime t1 = new PreciseTime(8, 500000);
		PreciseTime res = new PreciseTime(0, 0);
		
		PreciseTime.div(t1, 6, res);
		
		
		assertEquals("1.416666", res.toString());
	}

	@Test
	public void testDiv8() {
		PreciseTime t1 = new PreciseTime(8, 500000);
		PreciseTime res = new PreciseTime(0, 0);
		
		PreciseTime.div(t1, 3, res);
		
		
		assertEquals("2.833333", res.toString());
	}

}
