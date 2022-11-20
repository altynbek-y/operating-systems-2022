/** Class Simulator is the main driver for the whole simulation.  It contains
 ** the mainLoop processing loop, as well as various various methods for
 ** gathering and printing statistics and generating debugging output.
 ** All members are static.
 ** The only public members are the methods now(), moreVerbose(), db(), and
 ** tod().
 **/
import java.io.*;
import java.util.*;

public class Simulator {
    // =========== Simulation Parameters =========

    /** The amount of time it takes for a disk operation */
    public static final int DISK_TIME = 20;

    /** The quantum for round-robin scheduling */
    public static int QUANTUM;

    /** The size of a disk block, in bytes */
    public static final int BLOCKSIZE = 1024;

    /** Penalty for starting and stopping a process. */
    public static final int SWAP_OVERHEAD = 2;

    /** Probability of an "unusual" CPU burst (see Job.newBurst()) */
    public static final double ODD_BURST_PROB = 0.2;

    // =========== Simulation Variables and Structures =========

    /** The current simulated time (in ms since start) */
    private static int currentTime;
    public static int now() {
        return currentTime;
    }

    // Devices

    /** Number of devices "installed" */
    private final static int DEVICES = 4;

    /** An array of devices, index by one of the following slot numbers */
    private static Device[] device = new Device[DEVICES];

    private final static int ARRIVALS_SLOT = 0;
    private final static int DISK_SLOT = 1;
    private final static int CPU_SLOT = 2;
    private final static int CLOCK_SLOT = 3;

    /** For debugging: slot names */
    private final static String[] dev_name =
            { "ARRIVAL", "DISK", "CPU", "CLOCK" };

    // Schedulers

    /** Scheduler for the disk */
    private static Scheduler diskScheduler;

    /** Scheduler for the cpu */
    private static Scheduler cpuScheduler;

    /** Types of CPU scheduler */
    public final static int CPU_RR = 1;
    public final static int CPU_SJF = 2;
    public final static int CPU_FCFS = 3;
    public final static int CPU_SRTF = 4;


    // =========== Statistics  =========

    /** The number of job arrivals */
    private static int arrivalCount = 0;

    /** Information about completed Jobs.  This is a vector of objects of
     type JobStats */
    private static Vector completed = new Vector();

    // =========== Main Loop  =========

    /**
     * This is the main program that runs the simulation
     */
    public static void mainLoop(String fname, int cpuSchedType)
            throws FileNotFoundException {

        // Create devices
        Device cpu, disk, clock, arrivals;

        device[ARRIVALS_SLOT] = arrivals =  new JobArrival(fname);
        device[DISK_SLOT]     = disk     =  new Device("Disk");
        device[CPU_SLOT]      = cpu      =  new CPU("CPU");
        device[CLOCK_SLOT]    = clock    =  new Device("Clock");

        // Create schedulers

        diskScheduler = new DiskScheduler();

        pl("Disk block size = " + BLOCKSIZE);
        pl("I/O ops each take " + DISK_TIME + "ms");

        pl("Swap overhead is " + SWAP_OVERHEAD);


        switch (cpuSchedType) {
            case CPU_RR -> {
                pl("CPU scheduling is round-robin with quantum "
                        + QUANTUM + "ms");
                cpuScheduler = new RRScheduler();    // <-- we us here
            }
            case CPU_SJF -> {
                pl("CPU scheduling is Short Job First with quantum "
                        + QUANTUM + "ms");
                cpuScheduler = new SJFScheduler();
            }
            case CPU_FCFS -> {
                pl("CPU scheduling is First Come First Serve");
                cpuScheduler = new FCFSScheduler();
            }
            case CPU_SRTF -> {
                pl("CPU scheduling is Shortest Remaining Time First");
                cpuScheduler = new SRTFScheduler();
            }
        }



        db("********** Simulation starting ************\n\n");

        Stats burstWait = new Stats();  // cpu waiting time per burst
//System.out.println("==========>Current new Stats=" + burstWait+"\n\n\n");

        currentTime = 0;
        // Start the job-arrival "device".  The arguments are ignored.
        arrivals.start(null, 0);

        // Process events until all done
        for (;;) {
            int nextDev = firstInterrupt();
            int nextTime = device[nextDev].nextInterrupt();

            if (nextTime == Integer.MAX_VALUE)
                break;

            currentTime = nextTime;
            Job j = device[nextDev].stop();

            db(nextDev==CLOCK_SLOT ? 3 : 2,
                    "Sim: ", dev_name[nextDev],
                    " interrupt, job ",
                    j == null ? "NULL" : j.toString());
            db(4, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            db(4, "|", disk);
            db(4, "|", cpu);
            db(4, "|", arrivals);
//********            if (verbosity >= 4) {
//System.out.println("===>>Pring here:");
//                diskScheduler.printQueue();
//                cpuScheduler.printQueue();
//********            }
//System.out.println(":Pring here<<===");
            db(4, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            switch (nextDev) {
                case ARRIVALS_SLOT:
                    // New job arrived
                    arrivalCount++;
                    db(1, "New job: ", j);

                    //***********************************************
                    //follow statement is modified, change null into j
                    arrivals.start(j,0); // enable another arrival
                    //**********************************************
                    if (j.state() == Job.READY)
                        j.burstStart = now();
                    schedule(j, cpu);       // send to correct queue
                    break;
                case DISK_SLOT:
                    // Disk operation completed
                    if (j.state() == Job.READY)
                        j.burstStart = now();
                    schedule(j, cpu);       // send to correct queue
                    break;
                case CPU_SLOT:
                    // Running job did a "system call"
                    clock.stop();           // also stop the clock
                    j.stop();               // tell job it has stopped
                    schedule(j, cpu);       // send to correct queue
                    burstWait.record(now()-j.burstStart - j.burst - SWAP_OVERHEAD);
                    break;
                case CLOCK_SLOT:
                    // Timer interrupt
                    int timeLeft = cpu.nextInterrupt() - now();
                    if (cpuScheduler.reschedule( j )) {
                        // Change jobs
                        j = cpu.stop();     // stop current job
                        db(3, "timer preempt ", j);
                        j.stop();           // tell it that it has stopped
                        cpuScheduler.add(j,-1);// add it to queue
                    }
                    else {
                        // Essentially ignore this clock interrupt


                        if (cpu.isBusy()){
                            //clock.start(null, QUANTUM);
                            //*********************************************
                            //following are modified
                            int t = j.getTimeSlice();
                            clock.start(j, t);
                            //*********************************************
                        }
                    }
                    break;
            } // switch

            // Now make sure that no device is idle if there are
            // jobs queued for it.
            if (!cpu.isBusy()) {
                j = cpuScheduler.remove();
                if (j != null) {
                    // Start it running immediately.
                    db(2, "Sim: start ", j, " running");
                    cpu.start(j, j.nextBurst());
                    // Tell job that it has started (for statistics).
                    j.start();
                    // Also (re)start clock
                    if (clock.isBusy())
                        clock.stop();

                    //*******************************
                    //following are modified
                    int t = j.getTimeSlice();
                    clock.start(j, t);
                    //*******************************

                    //clock.start(null, QUANTUM);
                }
            }
            if (!disk.isBusy()) {
                j = diskScheduler.remove();
                if (j != null) {
                    // Start it running immediately.
                    db(2, "Sim: start ", j, " on disk");
                    disk.start(j, DISK_TIME);
                    // Tell job that it has started io (for statistics).
                    j.doIO();
                }
            }
        } // for (;;)

        //        Print statistics

        db("********** Simulation completed ************");

        pl("\n**** Device Statistics\n");

        cpu.printStats();
        disk.printStats();

        pl("\n**** Scheduler Statistics\n");

        pl("Disk queue:"); diskScheduler.printStats();
        pl("CPU queue:"); cpuScheduler.printStats();

        pl("\n**** Job Statistics\n");

        // Accumunlate per-job statistics
        Stats jobIO = new Stats();      // I/O per job
        Stats jobCPU = new Stats();     // cpu per job (ms)
        Stats jobElapsed = new Stats(); // elapsed time per job
        Stats jobPR = new Stats();      // penalty ration per job

        Enumeration jobs = completed.elements();
        while (jobs.hasMoreElements()) {
            JobStats info = (JobStats)jobs.nextElement();

            jobIO.record((double)info.ioUsed);
            jobCPU.record((double)info.cpuUsed);
            jobElapsed.record((double)info.elapsedTime);
            jobPR.record(info.penalty);
        }

        pl("Total Jobs:            " + arrivalCount);
        pl("Total complete time (milisec): " + currentTime);
        pl("Throughput (jobs/sec): " + arrivalCount*1000.0/currentTime);
        pl("Elapsed time:          " + jobElapsed);
        pl("CPU time:              " + jobCPU);
        pl("I/O operations:        " + jobIO);
        pl("Penalty Ratio:         " + jobPR);
        pl("CPU waiting time:      " + burstWait);
    } // mainLoop

    // =========== Debugging and Untility Routines  =========

    /** Utility routine to send a Job to the Disk, CPU, or completion as
     ** appropriate.
     **/
    private static void schedule(Job j, Device cpu) {
        switch (j.state()) {
            case Job.DONE -> {
                // Job has finished; simply collect its statistics
                JobStats stats = j.finish();
                db(1, "Job finished: ", j, " ", stats);
                completed.addElement(stats);
            }
            case Job.READY -> {
                db(2, "Sim: send ", j, " to cpuScheduler");
                int timeLeft = cpu.isBusy() ? (cpu.nextInterrupt() - now()) : -1;
                if (cpuScheduler.add(j, timeLeft)) {
                    j = cpu.stop();         // stop current job
                    db(2, "arrival preempt ", j);
                    j.stop();               // tell it that it has stopped
                    cpuScheduler.add(j, -1);// add it to queue
                }
            }
            case Job.BLOCKED -> {
                db(2, "Sim: send ", j, " to diskScheduler");
                diskScheduler.add(j, -1);
            }
        }
    }

    /** Query the devices to see which one will be the first to interrupt.
     ** Return the slot-number of the winner.
     ** Ties are broken in favor of the lowest-numbered device.
     ** Devices return Integer.MAX_VALUE if they are not running (and so
     ** will never interrupt) so if no device is ready, the result will be 0
     ** (ARRIVALS_SLOT).
     **/
    private static int firstInterrupt() {
        int nextReady = 0;
        int nextTime = device[0].nextInterrupt();
        for (int i = 1; i < DEVICES; i++) {
            int n = device[i].nextInterrupt();
            if (n < nextTime) {
                nextTime = n;
                nextReady = i;
            }
        }
        return nextReady;
    }

    /** Flag to control the verbosity of debugging output. */
    public static int verbosity = 0; // higher values generate more output
    public static void moreVerbose() { verbosity++; }

    /** Trace flag: */
    public static boolean traceFlag = false;

    /** Print msg if verbosity >= level */
    static public void db(int level, String msg) {
        if (verbosity >= level)
            db(msg);
    }

    // We would like to write things like
    //   db(3, "low-level debugging" + someObject);
    // Unfortunately, that would do the expensive call of someObject.toString()
    // and the expensive string concatenation even if verbosity < 3, so
    // that nothing gets printed.  Therefore, we define various overloaded
    // versions of db.
    static public void db(int level, Object o1, Object o2) {
        if (verbosity >= level) db("" + o1 + o2);
    }

    static public void db(int level, Object o1, Object o2, Object o3) {
        if (verbosity >= level) db("" + o1 + o2 + o3);
    }

    static public void db(int level, Object o1, Object o2, Object o3, Object o4) {
        if (verbosity >= level) db("" + o1 + o2 + o3 + o4);
    }

    static public void db(int level, Object o1, Object o2, Object o3,
                          Object o4, Object o5) {
        if (verbosity >= level) db("" + o1 + o2 + o3 + o4 + o5);
    }
    static public void db(int level,
                          Object o1, Object o2, Object o3, Object o4, Object o5, int o6) {
        if (verbosity >= level) db("" + o1 + o2 + o3 + o4 + o5 + o6);
    }

    /** Print msg unconditionally */
    static public void db(String msg) {
        System.out.println(tod(currentTime) + ": " + msg);
    }

    /** Print msg unconditionally without the header */
    static public void pl(String msg) {
        System.out.println(msg);
    }


    /** Crude assertion checking */
    static public void assert1(boolean condition) {
        if (!condition)
            throw new RuntimeException("assertion failed!");
    }

    /** Convert a time-stamp from ms to time of day in the format
     **    h:mm:ss.mmm
     ** This used to be written more "cleanly", but it turned out to
     ** consume a huge fraction of the entire time taken by the program,
     ** so it was re-written to avoid allocating and concatenating Strings
     ** until the very end.
     **
     ** Too bad Java doesn't have sprintf!
     **/
    public static String tod(int time) {
        // hh:mm:ss.mmm
        // 001234567890
        byte[] buf = new byte[11];

        if (time == Integer.MAX_VALUE)
            return "INFINITY";
        int i, next;

        // milliseconds
        buf[10] = (byte)('0' + (time % 10)); time /= 10;
        buf[9]  = (byte)('0' + (time % 10)); time /= 10;
        buf[8]  = (byte)('0' + (time % 10)); time /= 10;
        buf[7]  = (byte)('.');

        // seconds
        buf[6] = (byte)('0' + (time % 10)); time /= 10;
        buf[5] = (byte)('0' + (time % 6)); time /= 6;
        buf[4] = (byte)(':');

        // minutes
        buf[3] = (byte)('0' + (time % 10)); time /= 10;
        buf[2] = (byte)('0' + (time % 6)); time /= 6;
        buf[1] = (byte)(':');

        // hours
        buf[0] = (byte)('0' + (time % 10));

        return new String( buf );
    }

} // Simulator
