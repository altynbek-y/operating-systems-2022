/** A JobStats object is used to record statistics about a Job that has
 ** completed.
 **/
public class JobStats {
    /** Name of the job */
    String name;

    /** Total cpu used (in ms) */
    int cpuUsed;

    /** Total I/O operations */
    int ioUsed;

    /** Total elapsed time from start to end */
    int elapsedTime;

    /** Penalty ratio (elapsed over sum of I/O time and CPU time) */
    double penalty;

    JobStats(String name, int cpuUsed, int ioUsed, int elapsedTime) {
        this.name = name;
        this.cpuUsed = cpuUsed;
        this.ioUsed = ioUsed;
        this.elapsedTime = elapsedTime;
        penalty = elapsedTime/(double)(Simulator.DISK_TIME*ioUsed + cpuUsed);
    }

    public String toString() {
        return
                "cpu " + cpuUsed
                        + " io " + ioUsed
                        + " elapsed " + elapsedTime
                        + " penalty " + penalty;
    }
}
