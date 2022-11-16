import java.util.ArrayList;

/** A round-robin Scheduler for a CPU.
 ** It maintains a linked list of jobs.
 **
 ** This scheduler is remarkably Simulatorilar to the DiskScheduler.
 **/

public class SRTFScheduler extends Scheduler {
    /** The queue of jobs awaiting service.
     ** If the queue is empty, head = null and the value of tail is undefined.
     **/
    private ArrayList<Job> queue = null;
    private Job head = null;
    private Job tail;

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
        queueChanged(1);    // update queue-length statistics
        return false;               //
    }

    /** Retrieve (and remove) the next job to be served.
     ** Return null if there is no such job.
     **/
    public Job remove() {
        int minBurst = Integer.MAX_VALUE, id = 0;
        Job result = null;
        for(Job process : queue)
        {
            if(process.burst < minBurst)
            {                minBurst = process.burst;

                result = process;
            }
        }
        queue.remove(result);
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
