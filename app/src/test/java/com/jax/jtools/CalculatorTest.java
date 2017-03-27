package com.jax.jtools;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by userdev1 on 3/24/2017.
 */
public class CalculatorTest {

    Calculator mCalculator;
    @Mock
    List moked_list;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mCalculator = new Calculator();
    }

    @Test
    public void add() throws Exception {
        assertEquals(6, mCalculator.add(1, 5));
    }

    @Test
    public void multiply() throws Exception {
    }

    @Test
    public void minus() throws Exception {
        assertEquals(0, mCalculator.minus(5, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void divide() {
        mCalculator.divide(5, 0);
    }

    @Test
    @Ignore
    public void power() throws Exception {

    }

    //list ut
    @Test
    public void addList() {
        moked_list.add("one");
        //确定moked对象调用了add("one")这个方法
        verify(moked_list).add("one");

        //替换函数的返回结果
        when(moked_list.get(0)).thenReturn(null);
        if (null == moked_list.get(0))
            System.out.println("list object is null");
        else
            System.out.println(moked_list.get(0));
    }
}