package Scheduling;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

public class SchedulingAlgorithm {
    private Options options;
    private ArrayList<SProcess> processes;
    private static Results result = new Results("null","null",0);

    public void run() {
        int compTime = 0;
        int currentProcess = 0;
        int previousProcess = 0;
        int size = processes.size();
        int completed = 0;
        String resultsFile = "Summary-Processes";

        result.schedulingType = "Batch (Nonpreemptive)";
        result.schedulingName = "First-Come First-Served";
        try {
            PrintStream out = new PrintStream(new FileOutputStream(resultsFile));
            SProcess process = processes.get(currentProcess);
            out.println("Process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
            while (compTime < options.getRunTime()) {
                if (process.cpudone == process.cputime) {
                    completed++;
                    out.println("Process: " + currentProcess + " completed... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
                    if (completed == size) {
                        result.compuTime = compTime;
                        out.close();
                        return;
                    }
                    for (int i = size - 1; i >= 0; i--) {
                        process = processes.get(i);
                        if (process.cpudone < process.cputime) {
                            currentProcess = i;
                        }
                    }
                    process = processes.get(currentProcess);
                    out.println("Process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
                }
                if (process.ioblocking == process.ionext) {
                    out.println("Process: " + currentProcess + " I/O blocked... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
                    process.numblocked++;
                    process.ionext = 0;
                    previousProcess = currentProcess;
                    for (int i = size - 1; i >= 0; i--) {
                        process = processes.get(i);
                        if (process.cpudone < process.cputime && previousProcess != i) {
                            currentProcess = i;
                        }
                    }
                    process = processes.get(currentProcess);
                    out.println("Process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
                }
                process.cpudone++;
                if (process.ioblocking > 0) {
                    process.ionext++;
                }
                compTime++;
            }
            out.close();
        } catch (IOException e) {
            System.out.println("Error while running: " + e.getMessage());
            System.exit(-1);
        }
        result.compuTime = compTime;
    }

    public void init(String filePath) {
        options = new Options();
        processes = new ArrayList<>();
        parseConfigFile(filePath);
        for (int i = 0; i < options.getProcessNumber() - processes.size(); i++) {
            double X = Utils.generateCoef();
            X = X * options.getStandardDeviation();
            int cpuTime = (int) X + options.getMeanDeviation();
            processes.add(new SProcess(cpuTime,i*100,0,0,0));
            i++;
        }
    }

    public void reportResults(String fileName) {
        try {
            //BufferedWriter out = new BufferedWriter(new FileWriter(resultsFile));
            PrintStream out = new PrintStream(new FileOutputStream(fileName));
            out.println("Scheduling Type: " + result.schedulingType);
            out.println("Scheduling Name: " + result.schedulingName);
            out.println("Simulation Run Time: " + result.compuTime);
            out.println("Mean: " + options.getMeanDeviation());
            out.println("Standard Deviation: " + options.getStandardDeviation());
            out.println("Process #\tCPU Time\tIO Blocking\tCPU Completed\tCPU Blocked");
            for (int i = 0; i < processes.size(); i++) {
                SProcess process = processes.get(i);
                out.print(i);
                if (i < 100) { out.print("\t\t"); } else { out.print("\t"); }
                out.print(process.cputime);
                if (process.cputime < 100) { out.print(" (ms)\t\t"); } else { out.print(" (ms)\t"); }
                out.print(process.ioblocking);
                if (process.ioblocking < 100) { out.print(" (ms)\t\t"); } else { out.print(" (ms)\t"); }
                out.print(process.cpudone);
                if (process.cpudone < 100) { out.print(" (ms)\t\t"); } else { out.print(" (ms)\t"); }
                out.println(process.numblocked + " times");
            }
            out.close();
        } catch (IOException e) {
            System.out.println("Error while reporting results: " + e.getMessage());
            System.exit(-1);
        }
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
        String line;
        int ioBlocking = 0;
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            while ((line = in.readLine()) != null) {
                if (line.startsWith("numprocess")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    options.setProcessNumber(Utils.stoi(st.nextToken()));
                }
                if (line.startsWith("meandev")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    options.setMeanDeviation(Utils.stoi(st.nextToken()));
                }
                if (line.startsWith("standdev")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    options.setStandardDeviation(Utils.stoi(st.nextToken()));
                }
                if (line.startsWith("process")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    ioBlocking = Utils.stoi(st.nextToken());
                    double X = Utils.generateCoef();
                    X = X * options.getStandardDeviation();
                    int cpuTime = (int) X + options.getMeanDeviation();
                    processes.add(new SProcess(cpuTime, ioBlocking, 0, 0, 0));
                }
                if (line.startsWith("runtime")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    options.setRunTime(Utils.stoi(st.nextToken()));
                }
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Error while parsing file: " + e.getMessage());
            System.exit(-1);
        }
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

        public static double generateCoef () {
            Random generator = new Random();
            while(true) {
                double U = generator.nextDouble();
                double V = generator.nextDouble();
                double X =  Math.sqrt((8/Math.E)) * (V - 0.5)/U;
                if ((X * X) <= (5 - 4 * Math.exp(.25) * U) ||
                        (X * X) >= (4 * Math.exp(-1.35) / U + 1.4) ||
                        (X * X) < (-4 * Math.log(U))
                ) {
                    continue;
                }
                return X;
            }
        }
    }
}
