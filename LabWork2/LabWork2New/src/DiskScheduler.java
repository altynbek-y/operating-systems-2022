/** A FCFS scheduler for the disk.
 ** It maintains a Simulatorple queue of jobs.
 **/
import java.io.*;
import java.util.*;

public class DiskScheduler extends Scheduler {
    /** The queue of jobs awaiting service.
     ** If the queue is empty, head = null and the value of tail is undefined.
     **/
    private Job head = null;
    private Job tail;

    /** Add a new job wanting service.
     ** The second argument is the amount of time remaining before the
     ** job currently using the device will finish (-1 if the device is idle).
     ** Return TRUE if this scheduler would like to preempt the current
     ** job.
     **/
    public boolean add(Job j, int timeLeft) {
        if (head == null) {
            head = j;
        }
        else {
            tail.next = j;
        }
        tail = j;
        j.next = null;
        queueChanged(1);    // update queue-length statistics
        return false;       // no preemption
    }

    /** Retrieve (and remove) the next job to be served.
     ** Return null if there is no such job.
     **/
    public Job remove() {
        if (head == null)
            return null;
        Job result = head;
        head = head.next;
        queueChanged(-1);   // update queue-length statistics
        return result;
    }

    /** This method is called when there is a clock interrupt, and just
     ** after a call to schedule().
     ** It returns true if there is a reason to stop the current process
     ** and run another one instead.
     ** The argument is the amount of time left until the current job
     ** finishes service on the device.
     **/
    public boolean reschedule(Job job) {
        return false; // no preemption
    }

    /** For debugging: print the queue of waiting jobs */
    public void printQueue() {
        if (head == null)
            Simulator.db("| Disk queue: empty");
        else {
            Simulator.db("| Disk queue:");
            for (Job j = head; j != null; j = j.next) {
                Simulator.db("|    " + j);
            }
        }
    }
} // DiskScheduler