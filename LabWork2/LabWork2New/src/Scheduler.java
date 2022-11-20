/** A Scheduler schedules jobs seeking service from a Device.
 ** The class Scheduler itself is "abstract", meaning that there are no
 ** instance of Scheduler, only of its subclasses.
 ** This Simulatorulatorulation has three Schedulers:  DiskScheduler, RRScheduler, and
 ** JobScheduler.
 ** The base class takes care of maintaining statistics on queue lengths.
 ** The subclasses do all the actual work.
 **/
import java.io.*;
import java.util.*;

public abstract class Scheduler {
    /** Add a new job wanting service.
     ** The second argument is the amount of time remaining before the
     ** job currently using the device will finish (-1 if the device is idle).
     ** Return TRUE if this scheduler would like to preempt the current
     ** job.
     **/
    public abstract boolean add(Job j, int timeLeft);

    /** Retrieve (and remove) the next job to be served.
     ** Return null if there is no such job.
     **/
    public abstract Job remove();

    /** This method is called when there is a clock interrupt.
     ** It returns true if there is a reason to stop the current process
     ** and run another one instead.
     ** The argument is the amount of time left until the current job
     ** finishes service on the device.
     **/
    public abstract boolean reschedule(Job job);

    /** For debugging: print the queue of waiting jobs */
    public abstract void printQueue();

    /**
     * This method should be called by a subclass whenever the
     * queue length changes.
     */
    protected void queueChanged(int amount) {
        int now = Simulator.now();
        lenSum += qlen * (now - lastChanged);
        queueHist[Math.min(qlen,MAXQUEUE)] += now - lastChanged;
        lastChanged = now;
        qlen += amount;
        if (qlen > maxLen)
            maxLen = qlen;
    }

    /** Print statistics about the history of this queue. */
    public void printStats() {
        int now = Simulator.now();
        Simulator.pl("\tmax " + maxLen + " average " + (lenSum/(double)now));
        Simulator.pl("\tlength\tms\t% of total");
        for (int i=0; i<=Math.min(maxLen, MAXQUEUE); i++) {
            Simulator.pl(
                    "\t" + (i==MAXQUEUE ? ">=" : "")
                            + i + '\t'
                            + queueHist[i] + '\t'
                            + (100 * queueHist[i] / (double)now));
        }
    }

    // Statistics about the queue length

    /** The max queue length we tally in the histogram */
    private static final int MAXQUEUE = 20;

    /** The current queue length */
    private int qlen = 0;

    /** The last time the queue length changed */
    private int lastChanged = 0;

    /** The maximum queue length */
    private int maxLen = 0;

    /** The time integral of queue length */
    private int lenSum = 0;

    /** The amount of time the queue had a given length */
    private int[] queueHist = new int[MAXQUEUE+1];

} // Scheduler