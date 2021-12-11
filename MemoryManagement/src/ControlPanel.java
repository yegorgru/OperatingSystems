import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

public class ControlPanel extends Frame
{
    Kernel kernel;
    Options options;
    Button runButton = new Button("run");
    Button stepButton = new Button("step");
    Button resetButton = new Button("reset");
    Button exitButton = new Button("exit");
    ArrayList<Button> pageButtons = new ArrayList<>();

    Label statusValueLabel = new Label("STOP" , Label.LEFT) ;
    Label timeValueLabel = new Label("0" , Label.LEFT) ;
    Label instructionValueLabel = new Label("NONE" , Label.LEFT) ;
    Label addressValueLabel = new Label("NULL" , Label.LEFT) ;
    Label pageFaultValueLabel = new Label("NO" , Label.LEFT) ;
    Label virtualPageValueLabel = new Label("x" , Label.LEFT) ;
    Label physicalPageValueLabel = new Label("0" , Label.LEFT) ;
    Label RValueLabel = new Label("0" , Label.LEFT) ;
    Label MValueLabel = new Label("0" , Label.LEFT) ;
    Label inMemTimeValueLabel = new Label("0" , Label.LEFT) ;
    Label lastTouchTimeValueLabel = new Label("0" , Label.LEFT) ;
    Label lowValueLabel = new Label("0" , Label.LEFT) ;
    Label highValueLabel = new Label("0" , Label.LEFT);
    Label processValueLabel = new Label(null , Label.LEFT);

    ArrayList<Label> labels = new ArrayList<>();

    private int runs = 0;
    private final Set<Integer> loadedPhysicalPages = new TreeSet<>();
    private static final Logger log = Logger.getLogger(ControlPanel.class.getName());

    public ControlPanel(String title, Kernel kernel) {
        super(title);
        this.kernel = kernel;
        this.options = kernel.getOptions();
        for(int i = 0; i <= options.getVirtualPageMaxIdx(); i++) {
            pageButtons.add(new Button("page " + i));
            labels.add(new Label(null, Label.CENTER));
        }
    }

    public void init() {
        setLayout(null);
        setBackground(Color.white);
        setForeground(Color.black);
        setSize(635 , 545 );
        setFont(new Font("Courier", Font.PLAIN, 12));

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
        while (runs != kernel.getInstructions().size()) {
            try {
                Thread.sleep(options.getDelay());
            }
            catch(InterruptedException e) {

            }
            step();
        }
    }

    public void step() {
        Instruction instruction = kernel.getInstructions().get(runs);
        instructionValueLabel.setText(instruction.getName());
        addressValueLabel.setText( Long.toString(instruction.getAddr(), options.getAddressRadix()));
        int idx = kernel.getPageIdxByAddr(instruction.getAddr());
        paintPage(idx);
        if (pageFaultValueLabel.getText().equals("YES")) {
            pageFaultValueLabel.setText("NO");
        }
        if (instruction.getName().startsWith("READ")) {
            Page page = kernel.getPage(kernel.getPageIdxByAddr(instruction.getAddr()));
            String message = "READ " + Long.toString(instruction.getAddr(), options.getAddressRadix());
            processPageOperation(instruction, page, message);
            page.setReferenced(true);
        }
        if (instruction.getName().startsWith("WRITE")) {
            Page page = kernel.getPage(kernel.getPageIdxByAddr(instruction.getAddr()));
            String message = "WRITE " + Long.toString(instruction.getAddr(), options.getAddressRadix());
            processPageOperation(instruction, page, message);
            page.setModified(true);
        }
        paintPage(kernel.getPageIdxByAddr(instruction.getAddr()));
        for (int i = 0; i < options.getVirtualPageMaxIdx(); i++) {
            Page page = kernel.getPage(i);
            if (page.isReferenced() && page.getLastTouchTime() == 10) {
                page.setReferenced(false);
            }
            if (page.hasPhysical()) {
                page.setMemoryTime(page.getMemoryTime() + 10);
                page.setLastTouchTime(page.getLastTouchTime() + 10);
            }
        }
        runs++;
        timeValueLabel.setText(runs*10 + " (ms)");
        processValueLabel.setText("" + instruction.getProcessId());
    }

    private void processPageOperation(Instruction instruction, Page page, String message) {
        if (!page.hasPhysical())
        {
            message += " ... page fault";
            loadPage(kernel.getPageIdxByAddr(instruction.getAddr()));
            pageFaultValueLabel.setText("YES");
        }
        else {
            page.setLastTouchTime(0);
            message += " ... okay";
        }
        if (options.isFileLog()) {
            printLogInFile(message);
        }
        if (options.isStdoutLog()) {
            log.info(message);
        }
    }

    public void paintPage(int idx) {
        Page page = kernel.getPage(idx);
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

    public void addPhysicalPage( int pageNum , int physicalPage )
    {
        labels.get(physicalPage).setText("page " + pageNum);
    }

    public void removePhysicalPage(int physicalPage)
    {
        labels.get(physicalPage).setText(null);
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
            if (kernel.getInstructions().size() == runs) {
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
        kernel.reset();
        runButton.setEnabled(true);
        stepButton.setEnabled(true);
        processValueLabel.setText(null);
        for(int i = 0; i <= options.getVirtualPageMaxIdx(); i++) {
            removePhysicalPage(i);
        }
        loadedPhysicalPages.clear();
    }

    private void printLogInFile(String message)
    {
        try {
            Files.write(Paths.get(options.getFileLogPath()),
                    (System.getProperty("line.separator") + message).getBytes(),
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {

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
            int oldPageIdx = new WorkingSetShiftRegisterReplacement(100).replacePage(kernel.getPages(),newPageIdx);
            Page oldPage = kernel.getPages().get(oldPageIdx);
            removePhysicalPage(oldPageIdx);
            newPhysicalPageIdx = oldPage.getPhysical();
            oldPage.setMemoryTime(0);
            oldPage.setLastTouchTime(0);
            oldPage.setReferenced(false);
            oldPage.setModified(false);
            oldPage.resetPhysical();
            loadedPhysicalPages.remove(oldPageIdx);
        }
        Page nextPage = kernel.getPages().get(newPageIdx);
        loadedPhysicalPages.add(newPhysicalPageIdx);
        nextPage.setPhysical(newPhysicalPageIdx);
        addPhysicalPage(nextPage.getPhysical(), newPageIdx);
    }
}
