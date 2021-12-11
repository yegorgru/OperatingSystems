public class Instruction
{
    private final String name;
    private final long addr;
    private final int processId;

    public Instruction(String name, long addr, int processId)
    {
        this.name = name;
        this.addr = addr;
        this.processId = processId;
    }

    String getName() {
        return name;
    }

    long getAddr() {
        return addr;
    }

    int getProcessId() {
        return processId;
    }
}
