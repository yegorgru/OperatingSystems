import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

public class ControlPanel extends Frame
{
    private Options options;
    private final Button runButton = new Button("run");
    private final Button stepButton = new Button("step");
    private final Button resetButton = new Button("reset");
    private final Button exitButton = new Button("exit");
    private final ArrayList<Button> pageButtons = new ArrayList<>();

    private final Label statusValueLabel = new Label("STOP" , Label.LEFT) ;
    private final Label timeValueLabel = new Label("0" , Label.LEFT) ;
    private final Label instructionValueLabel = new Label("NONE" , Label.LEFT) ;
    private final Label addressValueLabel = new Label("NULL" , Label.LEFT) ;
    private final Label pageFaultValueLabel = new Label("NO" , Label.LEFT) ;
    private final Label virtualPageValueLabel = new Label("x" , Label.LEFT) ;
    private final Label physicalPageValueLabel = new Label("0" , Label.LEFT) ;
    private final Label RValueLabel = new Label("0" , Label.LEFT) ;
    private final Label MValueLabel = new Label("0" , Label.LEFT) ;
    private final Label inMemTimeValueLabel = new Label("0" , Label.LEFT) ;
    private final Label lastTouchTimeValueLabel = new Label("0" , Label.LEFT) ;
    private final Label lowValueLabel = new Label("0" , Label.LEFT) ;
    private final Label highValueLabel = new Label("0" , Label.LEFT);
    private final Label processValueLabel = new Label(null , Label.LEFT);

    ArrayList<Label> labels = new ArrayList<>();

    private final List<Page> pages = new ArrayList<>();
    private List<Instruction> instructions;

    private final List<Integer> shiftRegister = new ArrayList<>();
    private int currentRegisterBit = 0;

    private int runs = 0;
    private final Set<Integer> loadedPhysicalPages = new TreeSet<>();
    private int currentProcess = -1;
    private final TreeMap<Integer, TreeSet<Integer>> processWorkingSetMap = new TreeMap<>();
    private static final Logger logger = Logger.getLogger(ControlPanel.class.getName());

    private final Parser parser = new Parser();

    public ControlPanel(String title) {
        super(title);
    }

    public void init(String commandsPath, String configPath) {
        options = parser.parseConfigFile(configPath);
        initPages();
        instructions = parser.parseCommandsFile(options, commandsPath);
        for(int i = 0; i <= options.getVirtualPageMaxIdx(); i++) {
            pageButtons.add(new Button("page " + i));
            labels.add(new Label(null, Label.CENTER));
        }

        setLayout(null);
        setBackground(Color.white);
        setForeground(Color.black);
        setSize(635 , 545 );
        setFont(new Font("Courier", Font.PLAIN, 12));

        for(int i = 0; i < options.getSizeOfShiftRegister(); i++) {
            shiftRegister.add(-1);
        }

        runButton.setForeground(Color.blue);
        runButton.setBackground(Color.lightGray);
        runButton.setBounds(0,25,70,15);
        add(runButton);

        stepButton.setForeground( Color.blue );
        stepButton.setBackground( Color.lightGray );
        stepButton.setBounds(70,25,70,15);
        add(stepButton);

        resetButton.setForeground(Color.blue);
        resetButton.setBackground(Color.lightGray);
        resetButton.setBounds(140,25,70,15);
        add(resetButton);

        exitButton.setForeground(Color.blue);
        exitButton.setBackground(Color.lightGray);
        exitButton.setBounds(210,25,70,15);
        add(exitButton);

        int bound = (pageButtons.size() + 1) / 2;
        for(int i = 0; i < pageButtons.size(); i++) {
            Button button = pageButtons.get(i);
            button.setBounds(i < bound ? 0 : 140, (i%bound+2)*15+25, 70, 15);
            button.setForeground(Color.magenta);
            button.setBackground(Color.lightGray);
            add(button);

            Label label = labels.get(i);
            label.setBounds( i < bound ? 70 : 210, (2 + i%bound)*15+25, 60, 15 );
            label.setForeground( Color.red );
            label.setFont(new Font( "Courier", Font.PLAIN, 10));
            add(label);
        }

        statusValueLabel.setBounds(45,25,100,15);
        add(statusValueLabel);

        timeValueLabel.setBounds(345,15+25,100,15);
        add(timeValueLabel);

        instructionValueLabel.setBounds(385,45+25,100,15);
        add(instructionValueLabel);

        addressValueLabel.setBounds(385,60+25,230,15);
        add(addressValueLabel);

        pageFaultValueLabel.setBounds(385,90+25,100,15);
        add(pageFaultValueLabel);

        virtualPageValueLabel.setBounds(395,120+25,200,15);
        add(virtualPageValueLabel);

        physicalPageValueLabel.setBounds(395,135+25,200,15);
        add(physicalPageValueLabel);

        RValueLabel.setBounds(395,150+25,200,15);
        add(RValueLabel);

        MValueLabel.setBounds( 395,165+25,200,15);
        add(MValueLabel);

        inMemTimeValueLabel.setBounds(395,180+25,200,15);
        add(inMemTimeValueLabel);

        lastTouchTimeValueLabel.setBounds(395,195+25,200,15);
        add(lastTouchTimeValueLabel);

        lowValueLabel.setBounds(395,210+25,230,15);
        add(lowValueLabel);

        highValueLabel.setBounds(395,225+25,230,15);
        add(highValueLabel);

        processValueLabel.setBounds(395,250+25,230,15);
        add(processValueLabel);

        Label virtualOneLabel = new Label("virtual" , Label.CENTER) ;
        virtualOneLabel.setBounds(0,15+25,70,15);
        add(virtualOneLabel);

        Label virtualTwoLabel = new Label( "virtual" , Label.CENTER) ;
        virtualTwoLabel.setBounds(140,15+25,70,15);
        add(virtualTwoLabel);

        Label physicalOneLabel = new Label( "physical" , Label.CENTER) ;
        physicalOneLabel.setBounds(70,15+25,70,15);
        add(physicalOneLabel);

        Label physicalTwoLabel = new Label( "physical" , Label.CENTER) ;
        physicalTwoLabel.setBounds(210,15+25,70,15);
        add(physicalTwoLabel);

        Label statusLabel = new Label("status: " , Label.LEFT) ;
        statusLabel.setBounds(285,25,65,15);
        add(statusLabel);

        Label timeLabel = new Label("time: " , Label.LEFT) ;
        timeLabel.setBounds(285,15+25,50,15);
        add(timeLabel);

        Label instructionLabel = new Label("instruction: " , Label.LEFT) ;
        instructionLabel.setBounds(285,45+25,100,15);
        add(instructionLabel);

        Label addressLabel = new Label("address: " , Label.LEFT) ;
        addressLabel.setBounds(285,60+25,85,15);
        add(addressLabel);

        Label pageFaultLabel = new Label("page fault: " , Label.LEFT) ;
        pageFaultLabel.setBounds(285,90+25,100,15);
        add(pageFaultLabel);

        Label virtualPageLabel = new Label("virtual page: " , Label.LEFT) ;
        virtualPageLabel.setBounds(285,120+25,110,15);
        add(virtualPageLabel);

        Label physicalPageLabel = new Label("physical page: " , Label.LEFT) ;
        physicalPageLabel.setBounds(285,135+25,110,15);
        add(physicalPageLabel);

        Label RLabel = new Label("referenced: ", Label.LEFT);
        RLabel.setBounds(285,150+25,110,15);
        add(RLabel);

        Label MLabel = new Label("modified: " , Label.LEFT);
        MLabel.setBounds(285,165+25,110,15);
        add(MLabel);

        Label inMemTimeLabel = new Label("in memory time: " , Label.LEFT) ;
        inMemTimeLabel.setBounds(285,180+25,110,15);
        add(inMemTimeLabel);

        Label lastTouchTimeLabel = new Label("last touch time: " , Label.LEFT) ;
        lastTouchTimeLabel.setBounds(285,195+25,110,15);
        add(lastTouchTimeLabel);

        Label lowLabel = new Label("low: " , Label.LEFT) ;
        lowLabel.setBounds(285,210+25,110,15);
        add(lowLabel);

        Label highLabel = new Label("high: " , Label.LEFT) ;
        highLabel.setBounds(285,225+25,110,15);
        add(highLabel);

        Label processLabel = new Label("PROCESS: " , Label.LEFT) ;
        processLabel.setBounds(285,250+25,110,15);
        add(processLabel);

        setVisible(true);
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

    public void step() {
        Instruction instruction = instructions.get(runs);
        instructionValueLabel.setText(instruction.getName());
        if(instruction.getName().equals("RELOAD")) {
            if(currentProcess != instruction.getProcessId()) {
                logger.severe("Can't reload not current process");
                System.exit(-1);
            }
            log("RELOAD PROCESS " + instruction.getProcessId());
            unloadProcess();
            loadProcess(currentProcess);
        }
        else {
            addressValueLabel.setText( Long.toString(instruction.getAddr(), options.getAddressRadix()));
            int idx = getPageIdxByAddr(instruction.getAddr());
            paintPage(idx);
            if(instruction.getProcessId() != currentProcess) {
                unloadProcess();
                loadProcess(instruction.getProcessId());
            }
            if (pageFaultValueLabel.getText().equals("YES")) {
                pageFaultValueLabel.setText("NO");
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
            paintPage(getPageIdxByAddr(instruction.getAddr()));
            for (int i = 0; i < options.getVirtualPageMaxIdx(); i++) {
                Page page = pages.get(i);
                if (page.isReferenced() && page.getLastTouchTime() == 10) {
                    page.setReferenced(false);
                }
                if (page.hasPhysical()) {
                    page.setMemoryTime(page.getMemoryTime() + 10);
                    page.setLastTouchTime(page.getLastTouchTime() + 10);
                }
            }
            timeValueLabel.setText(runs*10 + " (ms)");
            processValueLabel.setText("" + instruction.getProcessId());
        }
        runs++;
    }

    private void processPageOperation(Instruction instruction, Page page, String message) {
        updateShiftRegister(page.getId());
        if (!page.hasPhysical())
        {
            message += " ... page fault";
            loadPage(getPageIdxByAddr(instruction.getAddr()));
            pageFaultValueLabel.setText("YES");
        }
        else {
            page.setLastTouchTime(0);
            message += " ... okay";
        }
        log(message);
    }

    public void paintPage(int idx) {
        Page page = pages.get(idx);
        virtualPageValueLabel.setText(Integer.toString(page.getId()));
        physicalPageValueLabel.setText( Integer.toString(page.getPhysical()));
        RValueLabel.setText(page.isReferenced() ? "1" : "0");
        MValueLabel.setText(page.isModified() ? "1" : "0");
        inMemTimeValueLabel.setText( Integer.toString(page.getMemoryTime()));
        lastTouchTimeValueLabel.setText( Integer.toString(page.getLastTouchTime()));
        lowValueLabel.setText(Long.toString(page.getLowerBound(), options.getAddressRadix()));
        highValueLabel.setText(Long.toString(page.getUpperBound(), options.getAddressRadix()));
    }

    public void setStatus(String status) {
        statusValueLabel.setText(status);
    }

    public boolean action(Event e, Object arg)
    {
        if ( e.target == runButton ) {
            setStatus( "RUN" );
            runButton.setEnabled(false);
            stepButton.setEnabled(false);
            resetButton.setEnabled(false);
            run();
            setStatus("STOP");
            resetButton.setEnabled(true);
            return true;
        }
        else if (e.target == stepButton) {
            setStatus("STEP");
            step();
            if (instructions.size() == runs) {
                stepButton.setEnabled(false);
                runButton.setEnabled(false);
            }
            setStatus("STOP");
            return true;
        }
        else if (e.target == resetButton) {
            reset();
            return true;
        }
        else if ( e.target == exitButton ) {
            System.exit(0);
            return true;
        }
        for(int i = 0; i < pageButtons.size(); i++) {
            if(e.target == pageButtons.get(i)) {
                paintPage(i);
                return true;
            }
        }
        return false;
    }

    private void reset() {
        statusValueLabel.setText("STOP");
        timeValueLabel.setText("0");
        instructionValueLabel.setText("NONE");
        addressValueLabel.setText("NULL");
        pageFaultValueLabel.setText("NO");
        virtualPageValueLabel.setText("x");
        physicalPageValueLabel.setText("0");
        RValueLabel.setText( "0");
        MValueLabel.setText("0");
        inMemTimeValueLabel.setText("0");
        lastTouchTimeValueLabel.setText("0");
        lowValueLabel.setText("0");
        highValueLabel.setText("0");
        runs = 0;
        runButton.setEnabled(true);
        stepButton.setEnabled(true);
        processValueLabel.setText(null);
        for(int i = 0; i <= options.getVirtualPageMaxIdx(); i++) {
            uiRemovePhysicalPage(i);
            uiRemoveFromWorkingSet(i);
        }
        loadedPhysicalPages.clear();
        unloadProcess();
        pages.clear();
        instructions.clear();
        options = parser.parseConfigFile(options.getConfigPath());
        instructions = parser.parseCommandsFile(options, options.getCommandPath());
    }

    private void updateShiftRegister(int pageIdx) {
        if(!processWorkingSetMap.get(currentProcess).contains(pageIdx)) {
            uiAddToWorkingSet(pageIdx);
            processWorkingSetMap.get(currentProcess).add(pageIdx);
        }
        int prevIdx = shiftRegister.get(currentRegisterBit);
        shiftRegister.set(currentRegisterBit, pageIdx);
        if(prevIdx != -1 && !shiftRegister.contains(prevIdx)) {
            uiRemoveFromWorkingSet(prevIdx);
            processWorkingSetMap.get(currentProcess).remove(prevIdx);
        }
        currentRegisterBit = (currentRegisterBit + 1) % shiftRegister.size();
    }

    private void uiAddPhysicalPage(int physicalPage, int virtualPage) {
        labels.get(virtualPage).setText("page " + physicalPage);
    }

    private void uiRemovePhysicalPage(int physicalPage) {
        labels.get(physicalPage).setText(null);
    }

    private void uiAddToWorkingSet(int pageIdx) {
        pageButtons.get(pageIdx).setBackground(Color.yellow);
    }

    private void uiRemoveFromWorkingSet(int pageIdx) {
        pageButtons.get(pageIdx).setBackground(Color.lightGray);
    }

    private void unloadProcess() {
        if(currentProcess != -1) {
            for(int i = 0; i < pages.size(); i++) {
                uiRemoveFromWorkingSet(i);
                uiRemovePhysicalPage(i);
                Page page = pages.get(i);
                page.resetPhysical();
                page.setModified(false);
                page.setReferenced(false);
                page.setMemoryTime(0);
                page.setLastTouchTime(0);
            }
            loadedPhysicalPages.clear();
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
        int physical = 0;
        for(int i : processWorkingSetMap.get(currentProcess)) {
            uiAddToWorkingSet(i);
            loadedPhysicalPages.add(physical);
            uiAddPhysicalPage(physical, i);
            Page page = pages.get(i);
            page.setLastTouchTime(0);
            page.setPhysical(physical);
            page.setMemoryTime(0);
            shiftRegister.set(physical, i);
            physical++;
        }
        currentRegisterBit = physical % shiftRegister.size();
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

    private void loadPage(int newPageIdx) {
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
            int oldPageIdx = new WorkingSetShiftRegisterAlgorithm().replacePage(pages,newPageIdx);
            Page oldPage = pages.get(oldPageIdx);
            uiRemovePhysicalPage(oldPageIdx);
            newPhysicalPageIdx = oldPage.getPhysical();
            oldPage.setMemoryTime(0);
            oldPage.setLastTouchTime(0);
            oldPage.setReferenced(false);
            oldPage.setModified(false);
            oldPage.resetPhysical();
            loadedPhysicalPages.remove(oldPageIdx);
        }
        Page nextPage = pages.get(newPageIdx);
        loadedPhysicalPages.add(newPhysicalPageIdx);
        nextPage.setPhysical(newPhysicalPageIdx);
        uiAddPhysicalPage(nextPage.getPhysical(), newPageIdx);
    }

    private void log(String message) {
        if (options.isFileLog()) {
            printLogInFile(message);
        }
        if (options.isStdoutLog()) {
            logger.info(message);
        }
    }

    private void initPages() {
        for (int i = 0; i <= options.getVirtualPageMaxIdx(); i++) {
            long high = (options.getBlock() * (i + 1))-1;
            long low = options.getBlock() * i;
            pages.add(new Page(i, false, false, 0, 0, high, low));
        }
    }

    private int getPageIdxByAddr(long memoryAddr) {
        int pageIdx = (int)(memoryAddr / options.getBlock());
        if(pageIdx < 0 || pageIdx > options.getVirtualPageMaxIdx()) {
            return -1;
        }
        return pageIdx;
    }

}
