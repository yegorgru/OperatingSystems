package Scheduling;

import java.io.File;
import java.io.FileOutputStream;
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
    protected String type;
    protected String name;

    public abstract void run(String logPath);

    public abstract void init(String filePath);
    
    public void reportResults(String path) {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(path));
            out.println("Scheduling Type: " + type);
            out.println("Scheduling Name: " + name);
            out.println("Simulation Run Time: " + runTime);
            out.println("Mean Deviation: " + options.getAverageDuration());
            out.println("Standard Deviation: " + options.getDeviation());
            out.println("Process #\t\tCPU Time\t\tIO Blocking\t\tCPU Completed\t\tCPU Blocked\t\tInterrupted");
            for (int i = 0; i < processes.size(); i++) {
                Process process = processes.get(i);
                out.format("%4d (ms)\t\t", i);
                out.format("%4d (ms)\t\t", process.getCpuTime());
                out.format("%4d (ms)\t\t", process.getIoBlocking());
                out.format("%4d (ms)\t\t\t", process.getCpuDone());
                out.format("%4d (ms)\t\t", process.getTimesBlocked());
                out.println(process.getTimesInterrupted());
            }
            out.close();
        } catch (IOException e) {
            System.out.println("Error while reporting results: " + e.getMessage());
            System.exit(-1);
        }
    }

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
        double cpuTime = options.getAverageDuration();
        if(generator.nextBoolean()) {
            cpuTime += options.getDeviation() * generator.nextDouble();
        }
        else {
            cpuTime -= options.getDeviation() * generator.nextDouble();
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
                else if (line.startsWith("average_duration")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    options.setAverageDuration(Utils.stoi(st.nextToken()));
                }
                else if (line.startsWith("deviation")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    options.setDeviation(Utils.stoi(st.nextToken()));
                }
                else if (line.startsWith("process")) {
                    StringTokenizer st = new StringTokenizer(line);
                    ArrayList<Integer> ioBlocking = new ArrayList<>();
                    st.nextToken();
                    while(st.hasMoreTokens()) {
                        ioBlocking.add(Utils.stoi(st.nextToken()));
                    }
                    processes.add(new Process(generateCpuTime(), ioBlocking));
                    if(options.getResetSessionCounter()) {
                        processes.get(processes.size() - 1).setResetSessionCounter(true);
                    }
                }
                else if (line.startsWith("runtime")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    options.setRunTime(Utils.stoi(st.nextToken()));
                }
                else if (line.startsWith("quantum_increase_base")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    options.setQuantumIncreaseBase(Utils.stoi(st.nextToken()));
                }
                else if (line.startsWith("quantum")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    options.setQuantum(Utils.stoi(st.nextToken()));
                }
                else if (line.startsWith("priority_increasing")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    options.setIncreasingPriority(Utils.stoi(st.nextToken()));
                }
                else if (line.startsWith("reset_session_counter")) {
                    options.setResetSessionCounter(true);
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
