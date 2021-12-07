public class Page
{
    public int id;
    public int physical;
    public boolean R;
    public boolean M;
    public int inMemTime;
    public int lastTouchTime;
    public long high;
    public long low;

    public Page( int id, int physical, boolean R, boolean M, int inMemTime, int lastTouchTime, long high, long low )
    {
        this.id = id;
        this.physical = physical;
        this.R = R;
        this.M = M;
        this.inMemTime = inMemTime;
        this.lastTouchTime = lastTouchTime;
        this.high = high;
        this.low = low;
    }

}
