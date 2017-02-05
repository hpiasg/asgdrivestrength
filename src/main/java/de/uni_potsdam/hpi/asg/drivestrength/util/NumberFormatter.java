package de.uni_potsdam.hpi.asg.drivestrength.util;

import java.util.Locale;

public class NumberFormatter {
    public static String spaced(int aNumber) {
        return String.format(Locale.US, "%,d", aNumber).replace(',', ' ');
    }
    
    public static String spaced(double aNumber) {
        return String.format(Locale.US, "%,f", aNumber).replace(',', ' ');
    }
}
