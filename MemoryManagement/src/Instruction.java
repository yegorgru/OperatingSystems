public class Instruction
{
    private String name;
    private long addr;

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
