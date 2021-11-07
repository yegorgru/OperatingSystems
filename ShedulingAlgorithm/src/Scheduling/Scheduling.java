package Scheduling;// This file contains the main() function for the Scheduling
// simulation.  Init() initializes most of the variables by
// reading from a provided file.  SchedulingAlgorithm.Run() is
// called from main() to run the simulation.  Summary-Results
// is where the summary results are written, and Summary-Processes
// is where the process scheduling summary is written.

// Created by Alexander Reeder, 2001 January 06

public class Scheduling {

    public static void main(String[] args) {
        SchedulingAlgorithm algorithm = new SchedulingAlgorithm();
        if (args.length != 1) {
            System.out.println("Usage: 'java Scheduling <INIT FILE>'");
            System.exit(-1);
        }
        System.out.println("Init...");
        algorithm.init(args[0]);
        System.out.println("Running processes...");
        algorithm.run();
        algorithm.reportResults("Summary-Results");
        System.out.println("Completed.");
    }
}

