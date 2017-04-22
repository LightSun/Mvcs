package com.heaven7.java.mvcs.util;

/**
 * the math util
 * @author heaven7
 *
 */
public class MathUtil {

	public static int log2n(int n){
		return (int) (Math.log(n) / Math.log(2));
	}
	
	/***
	 * get the max 2^k . that make 2^k <= n
	 * @param n the target
	 * @return the max value(2^k) . which is the max value below n with value <= n.
	 */
	public static int max2K(int n){
		return (int) Math.pow(2, log2n(n));
	}
	
	
	/* public static void main(String[] args) {
	    System.out.println(Math.log(Math.E));//1
		System.out.println(Math.log(5) /Math.log(2)); //2
		System.out.println(max2K(5)); //4
		System.out.println(max2K(1));
	}*/
}
