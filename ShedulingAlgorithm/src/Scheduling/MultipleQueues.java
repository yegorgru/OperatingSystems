package Scheduling;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;

public class MultipleQueues extends SchedulingAlgorithm {
    private ConcurrentSkipListMap<Integer, ArrayList<Integer>> queues;

    @Override
    public void run(String logPath) {
        runTime = 0;
        int currentProcessQueuePosition = 0;
        int currentPriority = 0;
        int completed = 0;
        try {
            PrintStream out = new PrintStream(new FileOutputStream(logPath));
            int currentProcessIdx = queues.get(currentPriority).get(currentProcessQueuePosition);
            Process currentProcess = processes.get(currentProcessIdx);
            register(out, currentProcessIdx, "Priority: " + currentPriority);
            while (runTime < options.getRunTime()) {
                switch(currentProcess.getStatus()) {
                    case COMPLETED:
                        completed++;
                        complete(out, currentProcessIdx, "Priority: " + currentPriority);
                        if (completed == processes.size()) {
                            out.close();
                            return;
                        }
                        queues.get(currentPriority).remove(currentProcessQueuePosition);
                        if(queues.get(currentPriority).size() == 0) {
                            while(queues.get(currentPriority).size() == 0) {
                                ++currentPriority;
                            }
                            currentProcessQueuePosition = 0;
                        }
                        else {
                            currentProcessQueuePosition = currentProcessQueuePosition %
                                    queues.get(currentPriority).size();
                        }
                        currentProcessIdx = queues.get(currentPriority).get(currentProcessQueuePosition);
                        currentProcess = processes.get(currentProcessIdx);
                        register(out, currentProcessIdx, "Priority: " + currentPriority);
                        break;
                    case BLOCKED:
                        block(out, currentProcessIdx, "Priority: " + currentPriority);
                        currentProcessQueuePosition = (currentProcessQueuePosition + 1) %
                                queues.get(currentPriority).size();
                        currentProcessIdx = queues.get(currentPriority).get(currentProcessQueuePosition);
                        currentProcess = processes.get(currentProcessIdx);
                        register(out, currentProcessIdx, "Priority: " + currentPriority);
                        break;
                    case RUNNING:
                        if(currentProcess.getSessionCounter() ==
                                (int) Math.pow(2, currentPriority) * options.getQuantum())
                        {
                            interrupt(out, currentProcessIdx, "Priority: " + currentPriority);
                            currentProcess.interrupt();
                            queues.get(currentPriority).remove(currentProcessQueuePosition);
                            if(!queues.containsKey(currentPriority + 1)) {
                                queues.put(currentPriority+1, new ArrayList<>());
                            }
                            queues.get(currentPriority+1).add(currentProcessIdx);
                            if(queues.get(currentPriority).size() == 0) {
                                ++currentPriority;
                                currentProcessQueuePosition = 0;
                            }
                            else {
                                currentProcessQueuePosition = currentProcessQueuePosition %
                                        queues.get(currentPriority).size();
                            }
                            currentProcessIdx = queues.get(currentPriority).get(currentProcessQueuePosition);
                            currentProcess = processes.get(currentProcessIdx);
                            register(out, currentProcessIdx, "Priority: " + currentPriority);
                        }
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
        queues = new ConcurrentSkipListMap<>();
        queues.put(0, new ArrayList<>());
        for(int i = 0; i < processes.size(); i++) {
            queues.get(0).add(i);
        }
    }

    @Override
    public void reportResults(String path) {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(path));
            out.println("Scheduling Type: Interactive (Preemptive)");
            out.println("Scheduling Name: Multiple queues");
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
