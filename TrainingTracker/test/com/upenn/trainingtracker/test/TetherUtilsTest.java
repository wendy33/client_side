package com.upenn.trainingtracker.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.robolectric.Robolectric;

import com.upenn.trainingtracker.TetherUtils;
import com.upenn.trainingtracker.UserTether;

public class TetherUtilsTest 
{
    @Test
    public void currentDateTime() throws Exception 
    {
    	System.out.println(TetherUtils.getCurrentDateTimeString());
    }

}
