package com.jax.jtools;

/**
 * Created by userdev1 on 3/24/2017.
 */

public class Calculator {

    public int add(int a, int b) {
        return a + b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }

    public int divide(int a, int b) {
        if (b == 0)
            throw new IllegalArgumentException("除数不能为0");
        return a / b;
    }

    public int minus(int a, int b) {
        return a - b;
    }

    /***
     * 求a的b次方(还没写好)
     * @param a
     * @param b
     * @return
     */
    public int power(int a, int b) {
        return 0;
    }
}
