/** Main program for Programming assignment: LabWork2.
 **  Simulate CPU scheduling.
 **  This version does RR and calls skeletons for the other schedulers.
 **/
import java.util.Scanner;
public class LabWok2 {

    public static void main(String[] args) throws Exception
    {
        int cpuSched = Simulator.CPU_RR;
        Scanner input = new Scanner(System.in);
     //   System.out.print("1 for v and 0 for !v::");
     //   int vCond = input.nextInt();
     //   if(vCond==1)
     //       Simulator.moreVerbose();

     //   System.out.print("Trace Flag: 1 - yes, 0 - no::");
     //   int traceFlagCond = input.nextInt();
     //   if(traceFlagCond==1)
     //       Simulator.traceFlag = true;    // <--

        System.out.print("Quantum Time:: ");
        int quantum = input.nextInt();
        Simulator.QUANTUM = quantum;       // <--

        System.out.print("Scheduling Algorithm: 1 - FCFS, 2 - SJF, 3 - SRTF, 4 - RR:: ");
        int schedAlgorithm = input.nextInt();
        switch (schedAlgorithm) {
            case 1:
                cpuSched = Simulator.CPU_FCFS;
                break;
            case 2:
                cpuSched = Simulator.CPU_SJF;
                break;
            case 3:
                cpuSched = Simulator.CPU_SRTF;
                break;
            case 4:
                cpuSched = Simulator.CPU_RR;
                break;
            default:
                throw new Exception("Unknown CPU scheduler type");
        }
        System.out.print("Enter File Name:: ");
        String fName = input.next();
     //   System.out.println(fName);
        Simulator.mainLoop(fName , cpuSched);         // <--`
    }

    static private void pl(String msg) {
        System.out.println( msg );
    }
} // LabWork2
