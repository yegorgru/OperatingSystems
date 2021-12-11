import java.util.ArrayList;
import java.util.List;

public class WorkingSetShiftRegisterReplacement {
    private List<Integer> register;
    private int nextIdx;

    public WorkingSetShiftRegisterReplacement(int k) {
        register = new ArrayList<>(k);
        for (int i = 0; i < 10; i++) {
            register.add(0);
        }
        nextIdx = 0;
    }

    public void startPage(int idx) {
        register.set(nextIdx, idx);
        nextIdx = (nextIdx + 1) % register.size();
    }

    public int replacePage(List<Page> pages, int newPage) {
        return 0;
    }
}
