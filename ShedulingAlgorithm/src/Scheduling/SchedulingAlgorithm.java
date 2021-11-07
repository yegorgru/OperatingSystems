package Scheduling;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;

public class SchedulingAlgorithm {
    private Options options;
    private ArrayList<Process> processes;
    private int runTime;

    public void run(String path) {
        runTime = 0;
        int currentProcessIdx = 0;
        int size = processes.size();
        int completed = 0;
        try {
            PrintStream out = new PrintStream(new FileOutputStream(path));
            Process process = processes.get(currentProcessIdx);
            register(out, currentProcessIdx);
            while (runTime < options.getRunTime()) {
                switch(process.getStatus()) {
                    case COMPLETED:
                        completed++;
                        complete(out, currentProcessIdx);
                        if (completed == size) {
                            out.close();
                            return;
                        }
                        for (int i = 0; i < processes.size(); i++) {
                            process = processes.get(i);
                            if (process.getCpuDone() < process.getCpuTime()) {
                                currentProcessIdx = i;
                                break;
                            }
                        }
                        process = processes.get(currentProcessIdx);
                        register(out, currentProcessIdx);
                        break;
                    case BLOCKED:
                        block(out, currentProcessIdx);
                        for (int i = 0; i < processes.size(); i++) {
                            process = processes.get(i);
                            if (process.getCpuDone() < process.cpuTime && currentProcessIdx != i) {
                                currentProcessIdx = i;
                                break;
                            }
                        }
                        process = processes.get(currentProcessIdx);
                        register(out, currentProcessIdx);
                        break;
                    case RUNNING:
                        break;
                }
                process.step();
                runTime++;
            }
            out.close();
        } catch (IOException e) {
            System.out.println("Error while running: " + e.getMessage());
            System.exit(-1);
        }
    }

    public void init(String filePath) {
        options = new Options();
        processes = new ArrayList<>();
        parseConfigFile(filePath);
        for (int i = 0; i < options.getProcessNumber() - processes.size(); i++) {
            processes.add(new Process(generateCpuTime(),i*100));
            i++;
        }
    }

    public void reportResults(String path) {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(path));
            out.println("Scheduling Type: Batch (Nonpreemptive)");
            out.println("Scheduling Name: First-Come First-Served");
            out.println("Simulation Run Time: " + runTime);
            out.println("Mean Deviation: " + options.getMeanDeviation());
            out.println("Standard Deviation: " + options.getStandardDeviation());
            out.println("Process #\t\tCPU Time\t\tIO Blocking\t\tCPU Completed\t\tCPU Blocked");
            for (int i = 0; i < processes.size(); i++) {
                Process process = processes.get(i);
                out.format("%4d (ms)\t\t", i);
                out.format("%4d (ms)\t\t", process.getCpuTime());
                out.format("%4d (ms)\t\t", process.getIoBlocking());
                out.format("%4d (ms)\t\t\t", process.getCpuDone());
                out.println(process.getTimesBlocked() + " times");
            }
            out.close();
        } catch (IOException e) {
            System.out.println("Error while reporting results: " + e.getMessage());
            System.exit(-1);
        }
    }

    private void register(PrintStream out, int idx) {
        Process process = processes.get(idx);
        out.println("Process: " + idx + " registered... (" + process.getCpuTime() + " " +
                process.getIoBlocking() + " " + process.getCpuDone() + "). Timepoint: " + runTime);
    }

    private void block(PrintStream out, int idx) {
        Process process = processes.get(idx);
        out.println("Process: " + idx + " I/O blocked... (" + process.getCpuTime() +
                " " + process.getIoBlocking() + " " + process.getCpuDone() + "). Timepoint: " + runTime);
    }

    private void complete(PrintStream out, int idx) {
        Process process = processes.get(idx);
        out.println("Process: " + idx + " completed... (" + process.getCpuTime() + " " +
                process.getIoBlocking() + " " + process.getCpuDone() + "). Timepoint: " + runTime);
    }

    private void parseConfigFile(String filePath) {
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
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println("Error while parsing file: " + e.getMessage());
            System.exit(-1);
        }
    }

    private int generateCpuTime () {
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

    private static class Utils{
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
