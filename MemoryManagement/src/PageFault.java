import java.util.*;

public class PageFault {

    public static void replacePage ( Vector mem , int virtPageNum , int replacePageNum , ControlPanel controlPanel )
    {
        int count = 0;
        int oldestPage = -1;
        int oldestTime = 0;
        int firstPage = -1;
        boolean mapped = false;

        while ( ! (mapped) || count != virtPageNum ) {
            Page page = ( Page ) mem.elementAt( count );
            if ( page.physical != -1 ) {
                if (firstPage == -1) {
                    firstPage = count;
                }
                if (page.inMemTime > oldestTime) {
                    oldestTime = page.inMemTime;
                    oldestPage = count;
                    mapped = true;
                }
            }
            count++;
            if ( count == virtPageNum ) {
                mapped = true;
            }
        }
        if (oldestPage == -1) {
            oldestPage = firstPage;
        }
        Page page = ( Page ) mem.elementAt( oldestPage );
        Page nextpage = ( Page ) mem.elementAt( replacePageNum );
        controlPanel.removePhysicalPage( oldestPage );
        nextpage.physical = page.physical;
        controlPanel.addPhysicalPage( nextpage.physical , replacePageNum );
        page.inMemTime = 0;
        page.lastTouchTime = 0;
        page.R = false;
        page.M = false;
        page.physical = -1;
    }
}
