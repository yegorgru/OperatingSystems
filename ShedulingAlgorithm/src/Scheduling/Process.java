package Scheduling;

public class Process {
    private final int cpuTime;
    private final int ioBlocking;
    private int cpuDone;
    private int ioCounter;
    private int sessionCounter;
    private int timesBlocked;
    private int timesInterrupted;

    public Process(int cpuTime, int ioBlocking) {
        this.cpuTime = cpuTime;
        this.ioBlocking = ioBlocking;
        this.cpuDone = 0;
        this.ioCounter = 0;
        this.sessionCounter = 0;
        this.timesBlocked = 0;
        this.timesInterrupted = 0;
    }

    public int getCpuTime() {
        return cpuTime;
    }

    public int getIoBlocking() {
        return ioBlocking;
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
        if (ioBlocking == ioCounter) {
            timesBlocked++;
            ioCounter = 0;
            sessionCounter = 0;
            return Status.BLOCKED;
        }
        return Status.RUNNING;
    }

    public void step() {
        cpuDone++;
        ioCounter++;
        sessionCounter++;
    }

    public enum Status {
        COMPLETED,
        BLOCKED,
        RUNNING
    }
}

