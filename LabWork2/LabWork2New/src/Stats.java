/** A Stats object accumulates statistics about a serious of values. */
import java.io.*;
import java.util.*;

public class Stats {
    private int count;
    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;
    private double sum = 0;

    public void record(double d) {
        count++;
        sum += d;
        if (d < min) min = d;
        if (d > max) max = d;
    }
    public String toString() {
        return
                "min " + min
                        + "\tmax " + max
                        + "\tmean " + sum/count;
    }
} // Stats