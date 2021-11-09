package Scheduling;

import java.util.ArrayList;

public class Process {
    private final int cpuTime;
    private  final ArrayList<Integer> ioBlocking;
    private int currentIoBlock;
    private int cpuDone;
    private int ioCounter;
    private int sessionCounter;
    private int timesBlocked;
    private int timesInterrupted;
    private boolean resetSessionCounter;

    public Process(int cpuTime, ArrayList<Integer> ioBlocking) {
        this.cpuTime = cpuTime;
        this.ioBlocking = ioBlocking;
        this.currentIoBlock = 0;
        this.cpuDone = 0;
        this.ioCounter = 0;
        this.sessionCounter = 0;
        this.timesBlocked = 0;
        this.timesInterrupted = 0;
        this.resetSessionCounter = false;
    }

    public int getCpuTime() {
        return cpuTime;
    }

    public int getIoBlocking() {
        return ioBlocking.get(currentIoBlock);
    }

    public int getTimesBlocked() {
        return timesBlocked;
    }

    public int getTimesInterrupted() {
        return timesInterrupted;
    }

    public int getCpuDone() {
        return cpuDone;
    }

    public int getSessionCounter() {
        return sessionCounter;
    }

    public void interrupt() {
        sessionCounter = 0;
        timesInterrupted++;
    }

    public Status getStatus() {
        if (cpuDone == cpuTime) {
            return Status.COMPLETED;
        }
        if (ioBlocking.get(currentIoBlock) == ioCounter) {
            currentIoBlock = (currentIoBlock + 1) % ioBlocking.size();
            timesBlocked++;
            ioCounter = 0;
            if(resetSessionCounter) {
                sessionCounter = 0;
            }
            return Status.BLOCKED;
        }
        return Status.RUNNING;
    }

    public void step() {
        cpuDone++;
        ioCounter++;
        sessionCounter++;
    }

    public void setResetSessionCounter(boolean value) {
        resetSessionCounter = value;
    }

    public enum Status {
        COMPLETED,
        BLOCKED,
        RUNNING
    }
}

