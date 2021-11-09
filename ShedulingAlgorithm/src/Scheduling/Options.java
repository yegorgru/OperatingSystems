package Scheduling;

public class Options {
    private int processNumber;
    private int meanDeviation;
    private int standardDeviation;
    private int runTime;
    private int quantum;
    private int increasing;

    public Options() {
        runTime = 1000;
        standardDeviation = 100;
        meanDeviation = 1000;
        processNumber = 5;
        increasing = 0;
    }

    public void setProcessNumber(int value) {
        processNumber = value;
    }

    public int getProcessNumber() {
        return processNumber;
    }

    public void setMeanDeviation(int value) {
        meanDeviation = value;
    }

    public int getMeanDeviation() {
        return meanDeviation;
    }

    public void setStandardDeviation(int value) {
        standardDeviation = value;
    }

    public int getStandardDeviation() {
        return standardDeviation;
    }

    public void setRunTime(int value) {
        runTime = value;
    }

    public int getRunTime() {
        return runTime;
    }

    public void setQuantum(int value) {
        quantum = value;
    }

    public int getQuantum() {
        return quantum;
    }

    public void setIncreasing(int value) {
        increasing = value;
    }

    public int getIncreasing() {
        return increasing;
    }
}
