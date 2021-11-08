package Scheduling;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;

public abstract class SchedulingAlgorithm {
    protected Options options;
    protected int runTime;
    protected ArrayList<Process> processes;

    public abstract void run(String logPath);

    public abstract void init(String filePath);

    public abstract void reportResults(String path);

    public SchedulingAlgorithm() {
        options = new Options();
        processes = new ArrayList<>();
    }

    protected void register(PrintStream out, int idx, String message) {
        Process process = processes.get(idx);
        out.println("Process: " + idx + " registered... (" + process.getCpuTime() + " " +
                process.getIoBlocking() + " " + process.getCpuDone() + "). Timepoint: " + runTime + " " + message);
    }

    protected void block(PrintStream out, int idx, String message) {
        Process process = processes.get(idx);
        out.println("Process: " + idx + " I/O blocked... (" + process.getCpuTime() +
                " " + process.getIoBlocking() + " " + process.getCpuDone() + "). Timepoint: " + runTime + " " +
                message);
    }

    protected void complete(PrintStream out, int idx, String message) {
        Process process = processes.get(idx);
        out.println("Process: " + idx + " completed... (" + process.getCpuTime() + " " +
                process.getIoBlocking() + " " + process.getCpuDone() + "). Timepoint: " + runTime + " " + message);
    }

    protected void interrupt(PrintStream out, int idx, String message) {
        Process process = processes.get(idx);
        out.println("Process: " + idx + " reached the limit... (" + process.getCpuTime() + " " +
                process.getIoBlocking() + " " + process.getCpuDone() + "). Timepoint: " + runTime + " " + message);
    }

    protected int generateCpuTime () {
        Random generator = new Random();
        double cpuTime = options.getMeanDeviation();
        if(generator.nextBoolean()) {
            cpuTime += options.getStandardDeviation() * generator.nextDouble();
        }
        else {
            cpuTime -= options.getStandardDeviation() * generator.nextDouble();
        }
        if(cpuTime <= 0) {
            cpuTime = Math.abs(cpuTime) + 1;
        }
        return (int) cpuTime;
    }

    protected void parseConfigFile(String filePath) {
        File file = new File(filePath);
        if (!(file.exists())) {
            System.out.println("Scheduling: error, file '" + file.getName() + "' does not exist.");
            System.exit(-1);
        }
        if (!(file.canRead())) {
            System.out.println("Scheduling: error, read of " + file.getName() + " failed.");
            System.exit(-1);
        }
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("numprocess")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    options.setProcessNumber(Utils.stoi(st.nextToken()));
                }
                else if (line.startsWith("meandev")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    options.setMeanDeviation(Utils.stoi(st.nextToken()));
                }
                else if (line.startsWith("standdev")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    options.setStandardDeviation(Utils.stoi(st.nextToken()));
                }
                else if (line.startsWith("process")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    int ioBlocking = Utils.stoi(st.nextToken());
                    processes.add(new Process(generateCpuTime(), ioBlocking));
                }
                else if (line.startsWith("runtime")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    options.setRunTime(Utils.stoi(st.nextToken()));
                }
                else if (line.startsWith("quantum")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    options.setQuantum(Utils.stoi(st.nextToken()));
                }
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println("Error while parsing file: " + e.getMessage());
            System.exit(-1);
        }
    }

    protected static class Utils{
        public static int stoi (String s) {
            int i = 0;
            try {
                i = Integer.parseInt(s.strip());
            } catch (NumberFormatException e) {
                System.out.println("NumberFormatException: " + e.getMessage());
            }
            return i;
        }
    }
}
