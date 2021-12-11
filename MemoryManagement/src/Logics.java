import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Logger;

public class Logics {
    private static final Logger logger = Logger.getLogger(Logics.class.getName());

    private final Options options;

    private Application ui;

    private final List<Page> pages = new ArrayList<>();
    private List<Instruction> instructions;

    private final List<Integer> shiftRegister = new ArrayList<>();
    private int currentRegisterBit = 0;

    private int runs = 0;
    private final Set<Integer> loadedPhysicalPages = new TreeSet<>();
    private int currentProcess = -1;
    private final TreeMap<Integer, TreeSet<Integer>> processWorkingSetMap = new TreeMap<>();

    public Logics(Options options) {
        this.options = options;
    }

    public void init(Application ui, String commandsPath) {
        this.ui = ui;
        initPages();
        instructions = Parser.parseCommandsFile(options, commandsPath);
        for(int i = 0; i < options.getSizeOfShiftRegister(); i++) {
            shiftRegister.add(-1);
        }
    }

    public void run() {
        step();
        while (runs != instructions.size()) {
            try {
                Thread.sleep(options.getDelay());
            }
            catch(InterruptedException e) {
                logger.severe("Unexpected exception: " + e.getMessage());
                System.exit(-1);
            }
            step();
        }
    }

    public boolean step() {
        Instruction instruction = instructions.get(runs);
        ui.instructionValueLabel.setText(instruction.getName());
        ui.addressValueLabel.setText(Long.toString(instruction.getAddr(), options.getAddressRadix()));
        int idx = getPageIdxByAddr(instruction.getAddr());
        ui.paintPage(pages.get(idx));
        if(instruction.getProcessId() != currentProcess) {
            unloadProcess();
            loadProcess(instruction.getProcessId());
        }
        if (ui.pageFaultValueLabel.getText().equals("YES")) {
            ui.pageFaultValueLabel.setText("NO");
        }
        if (instruction.getName().equals("READ")) {
            Page page = pages.get(getPageIdxByAddr(instruction.getAddr()));
            String message = "READ " + Long.toString(instruction.getAddr(), options.getAddressRadix());
            processPageOperation(instruction, page, message);
            page.setReferenced(true);
        }
        if (instruction.getName().equals("WRITE")) {
            Page page = pages.get(getPageIdxByAddr(instruction.getAddr()));
            String message = "WRITE " + Long.toString(instruction.getAddr(), options.getAddressRadix());
            processPageOperation(instruction, page, message);
            page.setModified(true);
        }
        ui.paintPage(pages.get(getPageIdxByAddr(instruction.getAddr())));
        for (int i = 0; i < options.getVirtualPageMaxIdx(); i++) {
            Page page = pages.get(i);
            if (page.hasPhysical()) {
                page.setMemoryTime(page.getMemoryTime() + 10);
                page.setLastTouchTime(page.getLastTouchTime() + 10);
            }
        }
        ui.timeValueLabel.setText(runs*10 + " (ms)");
        ui.processValueLabel.setText("" + instruction.getProcessId());
        runs++;
        return instructions.size() == runs;
    }

    public List<Page> getPages() {
        return pages;
    }

    private int getPageIdxByAddr(long memoryAddr) {
        int pageIdx = (int)(memoryAddr / options.getBlock());
        if(pageIdx < 0 || pageIdx > options.getVirtualPageMaxIdx()) {
            return -1;
        }
        return pageIdx;
    }

    private void initPages() {
        for (int i = 0; i <= options.getVirtualPageMaxIdx(); i++) {
            long high = (options.getBlock() * (i + 1))-1;
            long low = options.getBlock() * i;
            pages.add(new Page(i, false, false, 0, 0, high, low));
        }
    }

    private void unloadProcess() {
        if(currentProcess != -1) {
            for(int i = 0; i < pages.size(); i++) {
                ui.removeFromWorkingSet(i);
                Page page = pages.get(i);
                page.setModified(false);
                page.setReferenced(false);
            }
        }
        for(int i = 0; i < shiftRegister.size(); i++) {
            shiftRegister.set(i, -1);
        }
        currentRegisterBit = 0;
    }

    private void loadProcess(int process) {
        currentProcess = process;
        if(!processWorkingSetMap.containsKey(currentProcess)) {
            processWorkingSetMap.put(currentProcess, new TreeSet<>());
        }
        int bit = 0;
        for(int workingPage : processWorkingSetMap.get(currentProcess)) {
            loadPage(workingPage);
            shiftRegister.set(bit, workingPage);
            bit++;
        }
        currentRegisterBit = bit % shiftRegister.size();
    }

    private void processPageOperation(Instruction instruction, Page page, String message) {
        updateShiftRegister(page.getId());
        if (!page.hasPhysical()) {
            message += " ... page fault";
            loadPage(getPageIdxByAddr(instruction.getAddr()));
            ui.pageFaultValueLabel.setText("YES");
        }
        else {
            page.setLastTouchTime(0);
            message += " ... okay";
        }
        log(message);
    }

    private void loadPage(int newPageIdx) {
        Page nextPage = pages.get(newPageIdx);
        nextPage.setLastTouchTime(0);
        nextPage.setMemoryTime(0);
        nextPage.setReferenced(false);
        nextPage.setModified(false);
        ui.addToWorkingSet(newPageIdx);
        if(nextPage.hasPhysical()) {
            return;
        }
        int newPhysicalPageIdx = -1;
        if(loadedPhysicalPages.size() < options.getPhysicalPages()) {
            for(int i = 0; i < options.getPhysicalPages(); i++) {
                if(!loadedPhysicalPages.contains(i)) {
                    newPhysicalPageIdx = i;
                    break;
                }
            }
        }
        else {
            int oldPageIdx = -1;
            for(Page page : pages) {
                if(page.hasPhysical() && !processWorkingSetMap.get(currentProcess).contains(page.getId())) {
                    oldPageIdx = page.getId();
                    break;
                }
            }
            if(oldPageIdx == -1) {
                for(int i = 0; i < shiftRegister.size(); i++) {
                    int idx = shiftRegister.get(i);
                    if(!processWorkingSetMap.get(currentProcess).contains(idx) || idx == -1) {
                        continue;
                    }
                    boolean isLater = false;
                    for(int j = i + 1; j < shiftRegister.size(); j++) {
                        if(shiftRegister.get(j) == idx) {
                            isLater = true;
                            break;
                        }
                    }
                    if(!isLater) {
                        oldPageIdx = idx;
                        break;
                    }
                }
            }
            Page oldPage = pages.get(oldPageIdx);
            ui.removePhysicalPage(oldPageIdx);
            ui.removeFromWorkingSet(oldPageIdx);
            processWorkingSetMap.get(currentProcess).remove(oldPageIdx);
            newPhysicalPageIdx = oldPage.getPhysical();
            oldPage.setMemoryTime(0);
            oldPage.setLastTouchTime(0);
            oldPage.setReferenced(false);
            oldPage.setModified(false);
            oldPage.resetPhysical();
        }
        loadedPhysicalPages.add(newPhysicalPageIdx);
        nextPage.setPhysical(newPhysicalPageIdx);
        ui.addPhysicalPage(nextPage.getPhysical(), newPageIdx);
    }

    private void updateShiftRegister(int pageIdx) {
        if(!processWorkingSetMap.get(currentProcess).contains(pageIdx)) {
            ui.addToWorkingSet(pageIdx);
            processWorkingSetMap.get(currentProcess).add(pageIdx);
        }
        int prevIdx = shiftRegister.get(currentRegisterBit);
        shiftRegister.set(currentRegisterBit, pageIdx);
        if(prevIdx != -1 && !shiftRegister.contains(prevIdx)) {
            ui.removeFromWorkingSet(prevIdx);
            processWorkingSetMap.get(currentProcess).remove(prevIdx);
        }
        currentRegisterBit = (currentRegisterBit + 1) % shiftRegister.size();
    }

    private void log(String message) {
        if (options.isFileLog()) {
            printLogInFile(message);
        }
        if (options.isStdoutLog()) {
            logger.info(message);
        }
    }

    private void printLogInFile(String message)
    {
        try {
            Files.write(Paths.get(options.getFileLogPath()),
                    (System.getProperty("line.separator") + message).getBytes(),
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            logger.severe("Unexpected exception: " + e.getMessage());
        }
    }
}
