package Scheduling;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class FirstComeFirstServed extends SchedulingAlgorithm {
    @Override
    public void run(String logPath) {
        runTime = 0;
        int currentProcessIdx = 0;
        int completed = 0;
        try {
            PrintStream out = new PrintStream(new FileOutputStream(logPath));
            Process currentProcess = processes.get(currentProcessIdx);
            register(out, currentProcessIdx, "");
            while (runTime < options.getRunTime()) {
                switch(currentProcess.getStatus()) {
                    case COMPLETED:
                        completed++;
                        complete(out, currentProcessIdx, "");
                        if (completed == processes.size()) {
                            out.close();
                            return;
                        }
                        for (int i = 0; i < processes.size(); i++) {
                            currentProcess = processes.get(i);
                            if (currentProcess.getCpuDone() < currentProcess.getCpuTime()) {
                                currentProcessIdx = i;
                                break;
                            }
                        }
                        currentProcess = processes.get(currentProcessIdx);
                        register(out, currentProcessIdx, "");
                        break;
                    case BLOCKED:
                        block(out, currentProcessIdx, "");
                        for (int i = 0; i < processes.size(); i++) {
                            currentProcess = processes.get(i);
                            if (currentProcess.getCpuDone() < currentProcess.getCpuTime() && currentProcessIdx != i) {
                                currentProcessIdx = i;
                                break;
                            }
                        }
                        currentProcess = processes.get(currentProcessIdx);
                        register(out, currentProcessIdx, "");
                        break;
                    case RUNNING:
                        break;
                }
                currentProcess.step();
                runTime++;
            }
            out.close();
        } catch (IOException e) {
            System.out.println("Error while running: " + e.getMessage());
            System.exit(-1);
        }
    }

    @Override
    public void init(String filePath) {
        parseConfigFile(filePath);
        for (int i = 0; i < options.getProcessNumber() - processes.size(); i++) {
            processes.add(new Process(generateCpuTime(),i*100));
            i++;
        }
    }

    @Override
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
}
