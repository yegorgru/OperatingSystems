public class Instruction
{
    private final String name;
    private final long addr;

    public Instruction(String name, long addr)
    {
        this.name = name;
        this.addr = addr;
    }

    String getName() {
        return name;
    }

    long getAddr() {
        return addr;
    }
}
