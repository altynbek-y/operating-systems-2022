/** A CPU is a specific kind of Device.  It differs from the generic device
 ** only in that there is a 1ms delay in starting a process and a 1ms delay
 ** in stopping a process.
 ** The "current job" is always null.
 **/
import java.io.*;
import java.util.*;

public class CPU extends Device {
    // Public interface

    /** Constructor */
    CPU(String name) {
        super(name);
    }

    /** Start the device running.  */
    public void start(Job j, int delay) {
        super.start(j, delay + Simulator.SWAP_OVERHEAD);
    }
} // CPU
