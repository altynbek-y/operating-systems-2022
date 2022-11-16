/** A round-robin Scheduler for a CPU.
 ** It maintains a linked list of jobs.
 **
 ** This scheduler is remarkably Simulatorilar to the DiskScheduler.
 **/
import java.io.*;
import java.util.*;

public class RRScheduler extends Scheduler {
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
        } else {
            tail.next = j;
        }
        tail = j;
        j.next = null;
        queueChanged(1);    // update queue-length statistics
        return false;               // RR scheduler only preempts on clock interrupts
    }

    /** Retrieve (and remove) the next job to be served.
     ** Return null if there is no such job.
     **/
    public Job remove() {
        if (head == null)
            return null;
        Job result = head;
        head = head.next;
        //result.burst -= result.getTimeSlice();
        queueChanged(-1);   // update queue-length statistics
        return result;
    }

    /** This method is called when there is a clock interrupt.
     ** It returns true if there is a reason to stop the current process
     ** and run another one instead.
     ** The argument is the amount of time left until the current job
     ** finishes service on the device.
     ** It is ignored for RR scheduling (we preempt if and only if there
     ** is some other job to run).
     **/
    public boolean reschedule(Job job) {
        return (head != null);
    }

    /** For debugging: print the queue of waiting jobs */
    public void printQueue() {
        if (head == null)
            Simulator.db("| CPU queue: empty");
        else {
            Simulator.db("| CPU queue:");
            for (Job j = head; j != null; j = j.next) {
                Simulator.db("|    " + j);
            }
        }
    }

    /** For debugging: a concise version of the queue of waiting jobs */
    private String queueString() {
        Job j = head;
        if (j == null)
            return "[]";
        String res = "[" + j;
        for (j = j.next; j != null; j = j.next) {
            res += " " + j;
        }
        return res + "]";
    }
} // RRScheduler
