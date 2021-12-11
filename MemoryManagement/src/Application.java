import java.awt.*;
import java.util.*;

public class Application extends Frame
{
    private Options options;
    private Logics logics;
    private final Button runButton = new Button("run");
    private final Button stepButton = new Button("step");
    private final Button resetButton = new Button("reset");
    private final Button exitButton = new Button("exit");
    private final ArrayList<Button> pageButtons = new ArrayList<>();

    private final Label statusValueLabel = new Label("STOP" , Label.LEFT) ;
    final Label timeValueLabel = new Label("0" , Label.LEFT) ;
    final Label instructionValueLabel = new Label("NONE" , Label.LEFT) ;
    final Label addressValueLabel = new Label("NULL" , Label.LEFT) ;
    final Label pageFaultValueLabel = new Label("NO" , Label.LEFT) ;
    private final Label virtualPageValueLabel = new Label("x" , Label.LEFT) ;
    private final Label physicalPageValueLabel = new Label("0" , Label.LEFT) ;
    private final Label RValueLabel = new Label("0" , Label.LEFT) ;
    private final Label MValueLabel = new Label("0" , Label.LEFT) ;
    private final Label inMemTimeValueLabel = new Label("0" , Label.LEFT) ;
    private final Label lastTouchTimeValueLabel = new Label("0" , Label.LEFT) ;
    private final Label lowValueLabel = new Label("0" , Label.LEFT) ;
    private final Label highValueLabel = new Label("0" , Label.LEFT);
    final Label processValueLabel = new Label(null , Label.LEFT);

    ArrayList<Label> labels = new ArrayList<>();

    public Application(String title) {
        super(title);
    }

    public void init(String commandsPath, String configPath) {
        options = Parser.parseConfigFile(configPath);
        logics = new Logics(options);
        logics.init(this, commandsPath);
        for(int i = 0; i <= options.getVirtualPageMaxIdx(); i++) {
            pageButtons.add(new Button("page " + i));
            labels.add(new Label(null, Label.CENTER));
        }

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

    public void paintPage(Page page) {
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
            logics.run();
            setStatus("STOP");
            resetButton.setEnabled(true);
            return true;
        }
        else if (e.target == stepButton) {
            setStatus("STEP");
            if(logics.step()) {
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
                paintPage(logics.getPages().get(i));
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
        runButton.setEnabled(true);
        stepButton.setEnabled(true);
        processValueLabel.setText(null);
        for(int i = 0; i <= options.getVirtualPageMaxIdx(); i++) {
            uiRemovePhysicalPage(i);
            uiRemoveFromWorkingSet(i);
        }
        options = Parser.parseConfigFile(options.getConfigPath());
        logics.reset();
    }

    public void uiAddPhysicalPage(int physicalPage, int virtualPage) {
        labels.get(virtualPage).setText("page " + physicalPage);
    }

    public void uiRemovePhysicalPage(int physicalPage) {
        labels.get(physicalPage).setText(null);
    }

    public void uiAddToWorkingSet(int pageIdx) {
        pageButtons.get(pageIdx).setBackground(Color.yellow);
    }

    public void uiRemoveFromWorkingSet(int pageIdx) {
        pageButtons.get(pageIdx).setBackground(Color.lightGray);
    }
}
