/** JobArrival is a phony kind of device that "interrupts" when a new
 ** job arrives in the system.
 ** It works by reading a trace file that indicates the starting times
 ** and resource requirements of jobs that were actually run on a real
 ** system.
 **/
import java.io.*;
import java.util.*;

public class JobArrival extends Device {
    /** The name of the trace file */
    private String fileName;

    /** The trace file itself */
    private BufferedReader traceFile;

    /** The constructor just opens the trace file.
     ** The next line is read whenever the device is started.
     **/
    JobArrival(String fname) {
        super("JobArrivals");
        fileName = fname;
        try {
            traceFile = new BufferedReader(new FileReader(fname));
        }
        catch (FileNotFoundException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    /**
     * Read the next (or first) job description from the input file,
     * skipping molformed lines, and make it the current job, with a
     * Simulatorulated interrupt occurring at the arrival time of the job.
     *
     * If no more valid lines appear in the file, schedule an interrupt
     * at time inifinity.
     */
    public void start(Job j, int dummy) {   // args are ignored
        try {
            j = new Job(traceFile);
            super.start(j, j.arrivalTime - Simulator.now());
        }
        catch (EOFException e) {
            super.start(null, Integer.MAX_VALUE - Simulator.now());
        }
    }
} // JobArrival
