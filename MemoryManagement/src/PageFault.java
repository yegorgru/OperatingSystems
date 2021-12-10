import java.util.*;

public class PageFault {

    public static void replacePage (List<Page> pages, int virtPageNum , int replacePageNum , ControlPanel controlPanel ) {
        int count = 0;
        int oldestPage = -1;
        int oldestTime = 0;
        int firstPage = -1;
        boolean mapped = false;

        while (!mapped || count != virtPageNum ) {
            Page page = pages.get(count);
            if (page.hasPhysical()) {
                if (firstPage == -1) {
                    firstPage = count;
                }
                if (page.getMemoryTime() > oldestTime) {
                    oldestTime = page.getMemoryTime();
                    oldestPage = count;
                    mapped = true;
                }
            }
            count++;
            if (count == virtPageNum ) {
                mapped = true;
            }
        }
        if (oldestPage == -1) {
            oldestPage = firstPage;
        }
        Page page = pages.get(oldestPage);
        Page nextpage = pages.get(replacePageNum);
        controlPanel.removePhysicalPage(oldestPage);
        nextpage.setPhysical(page.getPhysical());
        controlPanel.addPhysicalPage(nextpage.getPhysical(), replacePageNum);
        page.setMemoryTime(0);
        page.setLastTouchTime(0);
        page.setRead(false);
        page.setWrite(false);
        page.resetPhysical();
    }
}
