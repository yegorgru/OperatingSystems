public class Page
{
    private int id;
    private int physical;
    private boolean read;
    private boolean write;
    private int memoryTime;
    private int lastTouchTime;
    private long upperBound;
    private long lowerBound;

    public Page( int id, int physical, boolean read, boolean write, int inMemTime, int lastTouchTime, long upperBound, long lowerBound)
    {
        this.id = id;
        this.physical = physical;
        this.read = read;
        this.write = write;
        this.memoryTime = inMemTime;
        this.lastTouchTime = lastTouchTime;
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
    }

    public int getId() {
        return id;
    }

    public boolean hasPhysical() {
        return physical != -1;
    }

    public void setPhysical(int value) {
        physical = value;
    }

    public void resetPhysical() {
        physical = -1;
    }

    public int getPhysical() {
        return physical;
    }

    public void setRead(boolean value) {
        read = value;
    }

    public boolean isRead() {
        return read;
    }

    public void setWrite(boolean value) {
        write = value;
    }

    public boolean isWrite() {
        return write;
    }

    public void setLastTouchTime(int value) {
        lastTouchTime = 0;
    }

    public int getLastTouchTime() {
        return lastTouchTime;
    }

    public void setMemoryTime(int value) {
        memoryTime = value;
    }

    public int getMemoryTime() {
        return memoryTime;
    }

    public long getUpperBound() {
        return upperBound;
    }

    public long getLowerBound() {
        return lowerBound;
    }
}
