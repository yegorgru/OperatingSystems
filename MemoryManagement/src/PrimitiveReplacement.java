import java.util.*;

public class PrimitiveReplacement {

    public int replacePage(List<Page> pages) {
        int replacePageIdx = -1;
        for (int i = 0; i < pages.size(); i++) {
            Page page = pages.get(i);
            if (page.hasPhysical()) {
                replacePageIdx = i;
                break;
            }
        }
        return replacePageIdx;
    }
}
