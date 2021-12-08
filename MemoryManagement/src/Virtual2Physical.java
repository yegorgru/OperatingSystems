
public class Virtual2Physical
{
    public static int pageNum (long memaddr , int numpages , long block)
    {
        for (int i = 0; i <= numpages; i++)
        {
            long low = block * i;
            long high = block * ( i + 1 );
            if ( low <= memaddr && memaddr < high )
            {
                return i;
            }
        }
        return -1;
    }
}
