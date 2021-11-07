package Scheduling;

public class Process {
    public int cpuTime;
    public int ioBlocking;
    public int cpuDone;
    public int ioCounter;
    public int timesBlocked;

    public Process(int cpuTime, int ioBlocking) {
        this.cpuTime = cpuTime;
        this.ioBlocking = ioBlocking;
        this.cpuDone = 0;
        this.ioCounter = 0;
        this.timesBlocked = 0;
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

    public int getCpuDone() {
        return cpuDone;
    }

    public Status getStatus() {
        if (cpuDone == cpuTime) {
            return Status.COMPLETED;
        }
        if (ioBlocking == ioCounter) {
            timesBlocked++;
            ioCounter = 0;
            return Status.BLOCKED;
        }
        return Status.RUNNING;
    }

    public void step() {
        cpuDone++;
        if (ioBlocking > 0) {
            ioCounter++;
        }
    }

    public enum Status {
        COMPLETED,
        BLOCKED,
        RUNNING
    }
}

