package Scheduling;

public class Scheduling {
    public static void main(String[] args) {
        SchedulingAlgorithm algorithm = new SchedulingAlgorithm();
        if (args.length != 1) {
            System.out.println("Usage: 'java Scheduling <CONFIG FILE>'");
            System.exit(-1);
        }
        System.out.println("Init...");
        algorithm.init(args[0]);
        System.out.println("Running processes...");
        algorithm.run("Summary-Processes.txt");
        algorithm.reportResults("Summary-Results.txt");
        System.out.println("Completed.");
    }
}

