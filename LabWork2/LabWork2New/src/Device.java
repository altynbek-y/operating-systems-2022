/** A Device can be running or stopped.  Once started, it will "interrupt"
 ** at some time in the future.
 **
 ** Since this is a Simulatorulation, interrupts are a bit funny:
 ** Each device indicates the Simulatorulation time when the interrupt will occur
 ** and Simulator.mainLoop() polls the devices, see which one is next to interrupt.
 ** It then advances its current time to the time of the interrupt,
 ** stops the device, and does what the "interrupt handler" for the device
 ** would do.
 **
 ** In the current version of the Simulatorulation, the Disk, Clock, and CPU
 ** devices are all instances of Device.  Job arrivals are represented by
 ** a pseudo-device JobArrival, which extends Device.
 **/
import java.io.*;
import java.util.*;

public class Device {
    /** The name of the device (for diagnostic output) */
    private String name;

    /** Trace of significant events in this device's lifetime
     ** (only if Simulator.traceFlag)
     **/
    private Vector trace = new Vector();

    /** Is the device currently running? */
    private boolean running = false;

    /** The current job, if running. */
    private Job currentJob;

    /** When will it next interrupt? (MAX_VALUE if not running) */
    private int nextInterruptTime = Integer.MAX_VALUE;

    /** When did it last start running?  (meaningless if not running) */
    private int startedAt;

    /** What is the total amount of time it has been running? */
    private int totalTime = 0;

    // Public interface

    /** Constructor */
    Device(String name) {
//System.out.println("=============> Enter Device.java: ["+name+"]");
        this.name = name;
    }

    /** For debugging, information about this device */
    public String toString() {
        return name
                + (running
                ? (": job " + currentJob
                + ", started at " + Simulator.tod(startedAt)
                + ", next interrupt at " + Simulator.tod(nextInterruptTime))
                : ": idle"
        )
                + ", " + totalTime + " ms";
    }

    /**
     * Start the device running job j. For generic devices,
     * the amt argument is the amount of time to run,  in ms.
     * Specific devices may interpret the argument differently.
     */
    public void start(Job j, int amt) {
//System.out.println("Job:"+j+"  amount of time to run:["+amt+"ms]");
        Simulator.db(3, "Start device ", name,
                " job ", j == null ? "NULL" : j.toString(),
                " delay ", amt);
        if (running)
            throw new RuntimeException(
                    name + ": Attempt to start device that is already running");
        if (Simulator.traceFlag)
            trace.addElement(Simulator.tod(Simulator.now()) + ": start " + j);
        running = true;
        currentJob = j;
        startedAt = Simulator.now();
        nextInterruptTime = startedAt + amt;
    }

    /** Stop the device, and return the Job currently running.  */
    public Job stop() {
        Simulator.db(3, "Stop device ", name,
                " job ", currentJob == null ? "NULL" : currentJob.toString(),
                " elapsed ", Simulator.now() - startedAt);
        if (!running)
            throw new RuntimeException(
                    name + ": Attempt to stop device that is not running");
        if (Simulator.traceFlag)
            trace.addElement(Simulator.tod(Simulator.now()) + ": stop");
        running = false;
        nextInterruptTime = Integer.MAX_VALUE;
        totalTime += Simulator.now() - startedAt;
        return currentJob;
    }

    /** Retrieve the time when the next interrupt will occur */
    public final int nextInterrupt() { // Ok whether running or not.
        return nextInterruptTime;
    }

    /** Is the device currently runing? */
    public boolean isBusy() {
        return running;
    }

    /** Print information about this device */
    public void printStats() {
        Simulator.pl( name + ": utilization = "
                + (100*totalTime / (double)Simulator.now()) + '%'
                + (running ? " (running)" : ""));
        if (Simulator.traceFlag) {
            trace.addElement(Simulator.tod(Simulator.now()) + ": finish");
            Enumeration elts = trace.elements();
            Simulator.pl("Device " + name + ':');
            while (elts.hasMoreElements())
                Simulator.pl("   " +elts.nextElement());
        }
    }
} // Device