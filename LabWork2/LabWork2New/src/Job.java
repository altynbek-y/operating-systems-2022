/** A Job represents one customer of services.  It records the job's current
 ** state as well as statistics about its lifetime.  It generally spends its
 ** lifetime moving form Server to Server.
 **
 ** One Job is created from each line of the input trace file.
 **
 ** The resources required by a Job are characterized by an amount `cpuNeeded'
 ** of CPU time (all times are in milliseconds) and a number `ioNeeded' of I/O
 ** operations, each representing one disk transfer of Simulator.BLOCKSIZE bytes of
 ** data.  The CPU time is assumed to be distributed as evenly as possible into
 ** ioNeeded "bursts" of cpu time, each followed by an I/O operation (unless the
 ** job does no I/O at all).
 **
 ** Note the terminology:  A "burst" is the amount of computing a Job wants
 ** to do before choosing to do I/O.  Depending on the CPU scheduing algorithm
 ** in force, these bursts may be divided into "quanta" (plural of "quantum").
 **/
import java.io.*;
import java.util.*;

public class Job {


    /***********************************/
    //the followings are added by Lei Li and Zongyong Zheng


    /**
     *member:  priority, default value is -1
     */
    private int priority = -1;  //set job initial priority -1

    /**
     *get a job's priority
     */
    public int getPriority(){
        return priority;
    }

    /**
     *set priority
     */
    public void setPriority(int p){
        priority = p;
    }

    /**
     *member : time slice, default value is QUANTUM.
     */
    private int timeSlice = Simulator.QUANTUM;

    /**
     *get time sclice
     */
    public int getTimeSlice(){
        return timeSlice;
    }

    /**
     *set time slice
     */
    public void setTimeSlice(int t){
        timeSlice = t;
    }


    //*********************************************************







    /** Name and serial number of this job (for debugging) */
    private String name;
    private int serial;

    /** Trace of significant events in this job's lifetime
     ** (only if Simulator.traceFlag)
     **/
    private Vector trace = new Vector();

    /** Link for linking into queues */
    Job next;

    // Job parameters set up at arrival time.

    /** Arrival time in ms from start of Simulatorulation */
    int arrivalTime;

    /** Total CPU time (ms) required by this job */
    private int cpuNeeded;

    /** Total number of I/O operations required by this job */
    private int ioNeeded;

    // Remaining resources not yet consumed.

    /** number of I/O operations left to do */
    private int ioRemaining;

    /** total amount of CPU time (in ms) left to consume */
    private int cpuRemaining;

    /** size of current burst */
    public int burst;

    /** amount in current burst (until next I/O op) remaining */
    private int burstRemaining;

    /** time when current burst started (job "arrived" at the cpu queue) */
    public int burstStart;

    /** time when this Jobs last started using the CPU */
    private int lastStart;

    /** random number generator used by newBurst */
    Random rand = new Random(0);


    /**
     * Constructor.  Create a Job from a line of the trace file
     */
    public Job(BufferedReader traceFile) throws EOFException {
//System.out.println("============> Enter Job.java at : " + lastSerial);
        serial = lastSerial++;
        // Use a loop to skip over ill-formed lines, if necessary
        for (;;) {
            String line = null;
            try {
                line = traceFile.readLine();
                if (line == null)
                    break;
                StringTokenizer tokens = new StringTokenizer(line);
                /* Each line should have four fields:
                1) the job name
                2) start time, in 1/100 second since midnight
                3) cpu time required (in seconds, as a float)
                4) io count in bytes
                */
                name = tokens.nextToken().intern();

                arrivalTime = 10 * Integer.parseInt(tokens.nextToken());

                double cpuTime = Double.parseDouble(tokens.nextToken());
                // Convert to milliseconds
                cpuNeeded = (int)(1000 * cpuTime);
                cpuRemaining = cpuNeeded;

                int bytesIO = Integer.parseInt(tokens.nextToken());
                // Convert to blocks of I/O, rounding up.
                ioNeeded = (bytesIO + Simulator.BLOCKSIZE-1) / Simulator.BLOCKSIZE;
                ioRemaining = ioNeeded;

                newBurst();
//System.out.println("==============> burstRemaining ="+ burstRemaining);

                /* I comment this, useless, which is just for error
                   protection. */
                if (tokens.countTokens() > 0) {
                    Simulator.pl(line + ": too many words");
                    continue;
                }
                if (cpuTime + bytesIO == 0) {
                    // silently ignore null jobs!
                    continue;
                }
                return;
            }
            catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            }
            catch (NumberFormatException e) {
                Simulator.pl(line + ": ill-formed number "+e.getMessage());
                continue;
            }
            catch (NoSuchElementException e) {
                Simulator.pl(line + ": not enough tokens");
                continue;
            }
        } // for (;;)
        throw new EOFException();
    } // constructor



    /** for debugging: a string version of this job */
    public String toString() {
        if (Simulator.verbosity > 2) {
            // Format is
            //   name:jobnumber[arrival-time; cpu; io]
            // where
            //   cpu = total/remaining/burst-remaining
            // and
            //   io = total/remaining
            return name + ':' + serial + '['+ Simulator.tod(arrivalTime) + ';'
                    + cpuNeeded + '/' + cpuRemaining + '/' + burstRemaining + ';'
                    + ioNeeded + '/' + ioRemaining + ']';
        }
        if (Simulator.verbosity > 0)
            return name + '/' + serial;
        return name;
    }




    /** Counter used for assigning serial numbers to Jobs (for debugging
     ** output).
     **/
    private static int lastSerial = 0;

    // Possible states of a Job
    /** Job has completed */
    public static final int DONE = 0;
    /** Job needs to do an I/O operation next */
    public static final int BLOCKED = 1;
    /** Job is ready to run */
    public static final int READY = 2;

    /** What is the current state of this Job? */
    public int state() {
        if (burstRemaining > 0)
            return READY;
        if (ioRemaining > 0)
            return BLOCKED;
        return DONE;
    }

    /** Called when this Job is started running on the CPU */
    public void start() {
        lastStart = Simulator.now();
        if (Simulator.traceFlag)
            trace.addElement(Simulator.tod(Simulator.now()) + ": start");
    }

    /** Called when this Job is removed from the CPU */
    public void stop() {
        // The total amount of cpu consumed is the amount of elapsed time
        // since this job was started, less swap overhead.
        int used = Simulator.now() - lastStart - Simulator.SWAP_OVERHEAD;
        Simulator.assert1(cpuRemaining >= used); // debug
        Simulator.assert1(burstRemaining >= used); // debug
        cpuRemaining -= used;
        burstRemaining -= used;
        if (Simulator.traceFlag)
            trace.addElement(Simulator.tod(Simulator.now()) + ": stop");
    }

    /** Return the amount of CPU time remaining for this job until
     ** it next does I/O, or completes.
     **/
    public int nextBurst() {
        return burstRemaining;
    }

    /** Called when this Job does an I/O operation */
    public void doIO() {
        ioRemaining--;
        newBurst();

        //*************************
        //set priority and timeSlice of a job when it finishes doIO
        setPriority(-1);
        setTimeSlice(Simulator.QUANTUM);
        //**************************
        if (Simulator.traceFlag)
            trace.addElement(Simulator.tod(Simulator.now()) + ": do I/O");
    }

    /** This job is finished.  Return some summary statistics */
    public JobStats finish() {
        if (Simulator.traceFlag) {
            trace.addElement(Simulator.tod(Simulator.now()) + ": finish");
            Enumeration elts = trace.elements();
            Simulator.db("Job " + name + ':');
            while (elts.hasMoreElements())
                Simulator.db("   " +elts.nextElement());
        }
        return new JobStats(
                name,
                cpuNeeded,
                ioNeeded,
                Simulator.now() - arrivalTime);
    }

    /**
     * Recaculate the amount of CPU to be used by this job before the next
     * I/O operation:  the amount of cpu time remaining divided by the
     * number of I/O operations remaining, rounded up.  This spreads the
     * cpu time as even as possible in to bursts, each preceding one I/O.
     * The case ioRemaining==0 only arises if the job does no I/O at all.
     * To give a little variety, we occasionally create an unusual burst
     * that is x*b in length, where b is the "normal" burst length (as
     * just described) and x is a random number uniformly distributed between
     * 0 and 2.
     * In any case, the result is limited by the amount of CPU time remaining.
     */
    private void newBurst() {
        if (ioRemaining <= 1)
            burst = cpuRemaining;
        else {
            double b = cpuRemaining/(double)ioRemaining;
            if (rand.nextDouble() < Simulator.ODD_BURST_PROB)
                b *= 2 * rand.nextDouble();
            burst = Math.min(cpuRemaining, (int)Math.ceil(b));
        }
        burstRemaining = burst;
    }
} // Job