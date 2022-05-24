package com.example.unsilencer;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Tests {
    @Test
    public void testGregorianCalendar() {
        GregorianCalendar time1 = new GregorianCalendar(2002, 10, 21, 21, 40);
        GregorianCalendar time2 = (GregorianCalendar)time1.clone();
        time2.set(Calendar.HOUR_OF_DAY, 15);
        time2.set(Calendar.MINUTE, 36);

        assertTrue(time1.after(time2));
        time2.add(Calendar.DAY_OF_MONTH, 1);
        assertTrue(time1.before(time2));
        assertEquals(21, time1.get(Calendar.HOUR_OF_DAY));
        assertEquals(15, time2.get(Calendar.HOUR_OF_DAY));
    }
}