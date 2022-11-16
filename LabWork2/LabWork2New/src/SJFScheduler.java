import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/** A round-robin Scheduler for a CPU.
 ** It maintains a linked list of jobs.
 **
 ** This scheduler is remarkably Simulatorilar to the DiskScheduler.
 **/

public class SJFScheduler extends Scheduler {
    /** The queue of jobs awaiting service.
     ** If the queue is empty, jobsList = null or jobsList.size()==0.
     **/
    private ArrayList<Job> queue = null;

    /** Add a new job wanting service.
     ** The second argument is the amount of time remaining before the
     ** job currently using the device will finish (-1 if the device is idle).
     ** Return TRUE if this scheduler would like to preempt the current
     ** job.
     **/
    public boolean add(Job j, int timeLeft) {
        if (queue == null) {
            queue = new ArrayList<>();
        }
        queue.add(j);
        queue.sort(Comparator.comparingInt(o -> o.burst));   // Sort list of jobs
        queueChanged(1);    // update queue-length statistics
        return false;       // SJFS scheduler only preempts on clock interrupts
    }

    /** Retrieve (and remove) the next job to be served.
     ** Return null if there is no such job.
     **/
    public Job remove() {
        if (queue == null || queue.size() == 0)
            return null;
        Job result = queue.get(0);
        queue.remove(0);
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
        return (queue != null);
    }

    /** For debugging: print the queue of waiting jobs */
    public void printQueue() {
        if (queue == null || queue.size() == 0)
            Simulator.db("| CPU queue: empty");
        else {
            Simulator.db("| CPU queue:");
            for (Job job : queue) {
                Simulator.db("|    " + job);
            }
        }
    }

    /** For debugging: a concise version of the queue of waiting jobs */
    private String queueString() {
        if (queue == null || queue.size()==0)
            return "[]";
        StringBuilder res = new StringBuilder("[");
        for (Job job : queue) {
            res.append(" ").append(job);
        }
        return res + "]";
    }
} // RRScheduler
