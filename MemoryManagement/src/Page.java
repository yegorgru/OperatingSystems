public class Page
{
    private final int id;
    private int physical;
    private boolean referenced;
    private boolean modified;
    private int memoryTime;
    private int lastTouchTime;
    private final long upperBound;
    private final long lowerBound;

    public Page( int id, boolean referenced, boolean modified, int inMemTime, int lastTouchTime, long upperBound, long lowerBound)
    {
        this.id = id;
        this.referenced = referenced;
        this.modified = modified;
        this.memoryTime = inMemTime;
        this.lastTouchTime = lastTouchTime;
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
        resetPhysical();
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

    public void setReferenced(boolean value) {
        referenced = value;
    }

    public boolean isReferenced() {
        return referenced;
    }

    public void setModified(boolean value) {
        modified = value;
    }

    public boolean isModified() {
        return modified;
    }

    public void setLastTouchTime(int value) {
        lastTouchTime = value;
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
