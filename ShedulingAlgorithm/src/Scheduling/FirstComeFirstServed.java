package Scheduling;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class FirstComeFirstServed extends SchedulingAlgorithm {
    FirstComeFirstServed() {
        type = "Batch (Nonpreemptive)";
        name = "First Come First Served";
    }

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
            ArrayList<Integer> ioBlocking = new ArrayList<>();
            ioBlocking.add((i+1)*100);
            processes.add(new Process(generateCpuTime(),ioBlocking));
            i++;
        }
    }
}
