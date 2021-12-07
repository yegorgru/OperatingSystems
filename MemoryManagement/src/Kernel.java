import java.lang.Thread;
import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class Kernel extends Thread
{
    private static final Logger log = Logger.getLogger(Kernel.class.getName());
    private static int virtualPageNum = 63;

    private String output = null;
    private static final String lineSeparator = System.getProperty("line.separator");
    private String commandPath;
    private String configPath;
    private ControlPanel controlPanel ;
    private Vector memVector = new Vector();
    private Vector instructVector = new Vector();
    private String status;
    private boolean doStdoutLog = false;
    private boolean doFileLog = false;
    public int runs;
    public int runcycles;
    public long block = (int) Math.pow(2,12);
    public static byte addressradix = 10;

    public void initPages() {
        for (int i = 0; i <= virtualPageNum; i++)
        {
            long high = (block * (i + 1))-1;
            long low = block * i;
            memVector.addElement(new Page(i, -1, false, false, 0, 0, high, low));
        }
    }

    public void parseConfigFile(String commandsPath, String configPath)
    {
        //File f = new File(commandsFile);
        this.commandPath = commandsPath;        //!!
        //configFile = configFile;
        //String line;
        //String tmp = null;
        //String command = "";
        int i = 0;
        int j = 0;
        //int id = 0;
        int physical_count = 0;
        int map_count = 0;
        double power = 14;
        long high = 0;
        long low = 0;
        long addr = 0;
        long addressLimit = (block * virtualPageNum+1)-1;

        if (configPath != null)
        {
            File configFile = new File (configPath);
            try
            {
                Scanner scanner = new Scanner(configFile);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if(line.startsWith("numpages")) {
                        if(memVector.size() != 0) {
                            log.info("If numpages option is not default, it must be before memset options. Value ignored");
                        }
                        else {
                            StringTokenizer st = new StringTokenizer(line);
                            if (!st.hasMoreTokens()) {
                                log.severe("Undefined numpages");
                            }
                            st.nextToken();
                            virtualPageNum = Utils.stringToInt(st.nextToken()) - 1;
                            if (virtualPageNum < 2 || virtualPageNum > 63) {
                                log.severe("Number of pages is out of range");
                                System.exit(-1);
                            }
                            addressLimit = (block * virtualPageNum + 1) - 1;
                            initPages();
                        }
                    }
                    if (line.startsWith("memset")) {
                        if(memVector.size() == 0) {
                            initPages();
                        }
                        StringTokenizer st = new StringTokenizer(line);
                        st.nextToken();
                        while (st.hasMoreTokens())
                        {
                            int id = Utils.stringToInt(st.nextToken());
                            String tmp = st.nextToken();
                            int physical = 0;
                            if(tmp.startsWith("x"))
                            {
                                physical = -1;
                            }
                            else
                            {
                                physical = Utils.stringToInt(tmp);
                            }
                            if (0 > id || id > virtualPageNum || -1 > physical || physical > ((virtualPageNum - 1) / 2))
                            {
                                log.severe(id + " page id is invalid. Used default value 0.");
                                //System.exit(-1);
                                id = 0;
                            }
                            boolean R = Utils.stringToBoolean(st.nextToken());
                            boolean M = Utils.stringToBoolean(st.nextToken());
                            int inMemTime = Utils.stringToInt(st.nextToken());
                            if (inMemTime < 0)
                            {
                                log.info(inMemTime + " inMemTime value is invalid. Used default value 0.");
                                //System.exit(-1);
                                inMemTime = 0;
                            }
                            int lastTouchTime = Utils.stringToInt(st.nextToken());
                            if (lastTouchTime < 0)
                            {
                                log.info(lastTouchTime + " lastTouchTime value is invalid. Used default value 0.");
                                //System.exit(-1);
                                lastTouchTime = 0;
                            }
                            Page page = (Page) memVector.elementAt(id);
                            page.physical = physical;
                            page.R = R;
                            page.M = M;
                            page.inMemTime = inMemTime;
                            page.lastTouchTime = lastTouchTime;
                        }
                    }
                    if (line.startsWith("enable_logging"))
                    {
                        StringTokenizer st = new StringTokenizer(line);
                        doStdoutLog = true;
                    }
                    if (line.startsWith("log_file"))
                    {
                        StringTokenizer st = new StringTokenizer(line);
                        doFileLog = true;
                        output = st.nextToken();
                        String tmp = "";
                    }
                    if (line.startsWith("pagesize"))
                    {
                        if(memVector.size() != 0) {
                            log.info("If pagesize option is not default, it must be before memset options. Value ignored");
                        }
                        else {
                            StringTokenizer st = new StringTokenizer(line);
                            while (st.hasMoreTokens())
                            {
                                String tmp = st.nextToken();
                                tmp = st.nextToken();
                                if (tmp.startsWith( "power" ) )
                                {
                                    block = (int) Math.pow(2, Integer.parseInt(st.nextToken()));
                                }
                                else
                                {
                                    block = Long.parseLong(tmp,10);
                                }
                                addressLimit = (block * virtualPageNum+1)-1;
                            }
                            if (block < 64) {
                                log.info("Pagesize is too small. Used value 64.");
                                block = 64;
                            }
                            else if(block > Math.pow(2,26)) {
                                log.info("Pagesize is too small. Used value 2^26.");
                                block = (long) Math.pow(2,26);
                            }
                        }
                    }
                    if (line.startsWith("addressradix"))
                    {
                        StringTokenizer st = new StringTokenizer(line);
                        String tmp = st.nextToken();
                        tmp = st.nextToken();
                        addressradix = Byte.parseByte(tmp);
                        if (addressradix < 0 || addressradix > 20 )
                        {
                            log.info("addressradix is out of range. Use value 10");
                            //System.exit(-1);
                            addressradix = 10;
                        }
                    }
                }
                scanner.close();
            } catch (IOException ex) {
                log.severe("Unexpected exception: " + ex.getMessage());
                System.exit(-1);
            }
        }
        File f = new File ( commandsPath );
        try
        {
            DataInputStream in = new DataInputStream(new FileInputStream(f));
            String line;
            while ((line = in.readLine()) != null)
            {
                if (line.startsWith("READ") || line.startsWith("WRITE"))
                {
                    String command = "";
                    if (line.startsWith("READ"))
                    {
                        command = "READ";
                    }
                    if (line.startsWith("WRITE"))
                    {
                        command = "WRITE";
                    }
                    StringTokenizer st = new StringTokenizer(line);
                    String tmp = st.nextToken();
                    tmp = st.nextToken();
                    if (tmp.startsWith("random"))
                    {
                        instructVector.addElement(new Instruction(command, ThreadLocalRandom.current().nextLong(addressLimit)));
                    }
                    else
                    {
                        if ( tmp.startsWith( "bin" ) )
                        {
                            addr = Long.parseLong(st.nextToken(),2);
                        }
                        else if ( tmp.startsWith( "oct" ) )
                        {
                            addr = Long.parseLong(st.nextToken(),8);
                        }
                        else if ( tmp.startsWith( "hex" ) )
                        {
                            addr = Long.parseLong(st.nextToken(),16);
                        }
                        else
                        {
                            addr = Long.parseLong(tmp);
                        }
                        if (0 > addr || addr > addressLimit)
                        {
                            System.out.println("MemoryManagement: " + addr + ", Address out of range in " + commandsPath);
                            System.exit(-1);
                        }
                        instructVector.addElement(new Instruction(command,addr));
                    }
                }
            }
            in.close();
        } catch (IOException e) { /* Handle exceptions */ }
        runcycles = instructVector.size();
        if ( runcycles < 1 )
        {
            System.out.println("MemoryManagement: no instructions present for execution.");
            System.exit(-1);
        }
        if ( doFileLog )
        {
            File trace = new File(output);
            trace.delete();
        }
        runs = 0;
        for (i = 0; i < virtualPageNum; i++)
        {
            Page page = (Page) memVector.elementAt(i);
            if ( page.physical != -1 )
            {
                map_count++;
            }
            for (j = 0; j < virtualPageNum; j++)
            {
                Page tmp_page = (Page) memVector.elementAt(j);
                if (tmp_page.physical == page.physical && page.physical >= 0)
                {
                    physical_count++;
                }
            }
            if (physical_count > 1)
            {
                System.out.println("MemoryManagement: Duplicate physical page's in " + configPath);
                System.exit(-1);
            }
            physical_count = 0;
        }
        if ( map_count < ( virtualPageNum +1 ) / 2 )
        {
            for (i = 0; i < virtualPageNum; i++)
            {
                Page page = (Page) memVector.elementAt(i);
                if ( page.physical == -1 && map_count < ( virtualPageNum + 1 ) / 2 )
                {
                    page.physical = i;
                    map_count++;
                }
            }
        }
        for (i = 0; i < virtualPageNum; i++)
        {
            Page page = (Page) memVector.elementAt(i);
            if (page.physical == -1)
            {
                controlPanel.removePhysicalPage( i );
            }
            else
            {
                controlPanel.addPhysicalPage( i , page.physical );
            }
        }
        for (i = 0; i < instructVector.size(); i++)
        {
            high = block * virtualPageNum;
            Instruction instruct = ( Instruction ) instructVector.elementAt( i );
            if ( instruct.addr < 0 || instruct.addr > high )
            {
                System.out.println("MemoryManagement: Instruction (" + instruct.inst + " " + instruct.addr + ") out of bounds.");
                System.exit(-1);
            }
        }
    }

    public void setControlPanel(ControlPanel newControlPanel)
    {
        controlPanel = newControlPanel ;
    }

    public void getPage(int pageNum)
    {
        Page page = ( Page ) memVector.elementAt( pageNum );
        controlPanel.paintPage( page );
    }

    private void printLogFile(String message)
    {
        String line;
        String temp = "";

        File trace = new File(output);
        if (trace.exists())
        {
            try
            {
                DataInputStream in = new DataInputStream( new FileInputStream( output ) );
                while ((line = in.readLine()) != null) {
                    temp = temp + line + lineSeparator;
                }
                in.close();
            }
            catch ( IOException e )
            {
                /* Do nothing */
            }
        }
        try
        {
            PrintStream out = new PrintStream( new FileOutputStream( output ) );
            out.print( temp );
            out.print( message );
            out.close();
        }
        catch (IOException e)
        {
            /* Do nothing */
        }
    }

    public void run()
    {
        step();
        while (runs != runcycles)
        {
            try
            {
                Thread.sleep(2000);
            }
            catch(InterruptedException e)
            {
                /* Do nothing */
            }
            step();
        }
    }

    public void step()
    {
        int i = 0;

        Instruction instruct = ( Instruction ) instructVector.elementAt( runs );
        controlPanel.instructionValueLabel.setText( instruct.inst );
        controlPanel.addressValueLabel.setText( Long.toString( instruct.addr , addressradix ) );
        getPage( Virtual2Physical.pageNum( instruct.addr , virtualPageNum , block ) );
        if ( controlPanel.pageFaultValueLabel.getText() == "YES" )
        {
            controlPanel.pageFaultValueLabel.setText( "NO" );
        }
        if ( instruct.inst.startsWith( "READ" ) )
        {
            Page page = ( Page ) memVector.elementAt( Virtual2Physical.pageNum( instruct.addr , virtualPageNum , block ) );
            if ( page.physical == -1 )
            {
                if ( doFileLog )
                {
                    printLogFile( "READ " + Long.toString(instruct.addr , addressradix) + " ... page fault" );
                }
                if ( doStdoutLog )
                {
                    System.out.println( "READ " + Long.toString(instruct.addr , addressradix) + " ... page fault" );
                }
                PageFault.replacePage( memVector , virtualPageNum , Virtual2Physical.pageNum( instruct.addr , virtualPageNum , block ) , controlPanel );
                controlPanel.pageFaultValueLabel.setText( "YES" );
            }
            else
            {
                page.R = true;
                page.lastTouchTime = 0;
                if ( doFileLog )
                {
                    printLogFile( "READ " + Long.toString( instruct.addr , addressradix ) + " ... okay" );
                }
                if ( doStdoutLog )
                {
                    System.out.println( "READ " + Long.toString( instruct.addr , addressradix ) + " ... okay" );
                }
            }
        }
        if ( instruct.inst.startsWith( "WRITE" ) )
        {
            Page page = ( Page ) memVector.elementAt( Virtual2Physical.pageNum( instruct.addr , virtualPageNum , block ) );
            if ( page.physical == -1 )
            {
                if ( doFileLog )
                {
                    printLogFile( "WRITE " + Long.toString(instruct.addr , addressradix) + " ... page fault" );
                }
                if ( doStdoutLog )
                {
                    System.out.println( "WRITE " + Long.toString(instruct.addr , addressradix) + " ... page fault" );
                }
                PageFault.replacePage( memVector , virtualPageNum , Virtual2Physical.pageNum( instruct.addr , virtualPageNum , block ) , controlPanel );          controlPanel.pageFaultValueLabel.setText( "YES" );
            }
            else
            {
                page.M = true;
                page.lastTouchTime = 0;
                if ( doFileLog )
                {
                    printLogFile( "WRITE " + Long.toString(instruct.addr , addressradix) + " ... okay" );
                }
                if ( doStdoutLog )
                {
                    System.out.println( "WRITE " + Long.toString(instruct.addr , addressradix) + " ... okay" );
                }
            }
        }
        for ( i = 0; i < virtualPageNum; i++ )
        {
            Page page = ( Page ) memVector.elementAt( i );
            if ( page.R && page.lastTouchTime == 10 )
            {
                page.R = false;
            }
            if ( page.physical != -1 )
            {
                page.inMemTime = page.inMemTime + 10;
                page.lastTouchTime = page.lastTouchTime + 10;
            }
        }
        runs++;
        controlPanel.timeValueLabel.setText( Integer.toString( runs*10 ) + " (ns)" );
    }

    public void reset() {
        memVector.removeAllElements();
        instructVector.removeAllElements();
        controlPanel.statusValueLabel.setText("STOP");
        controlPanel.timeValueLabel.setText("0");
        controlPanel.instructionValueLabel.setText("NONE");
        controlPanel.addressValueLabel.setText("NULL");
        controlPanel.pageFaultValueLabel.setText("NO");
        controlPanel.virtualPageValueLabel.setText("x");
        controlPanel.physicalPageValueLabel.setText("0");
        controlPanel.RValueLabel.setText( "0");
        controlPanel.MValueLabel.setText("0");
        controlPanel.inMemTimeValueLabel.setText("0");
        controlPanel.lastTouchTimeValueLabel.setText("0");
        controlPanel.lowValueLabel.setText("0");
        controlPanel.highValueLabel.setText("0");
        parseConfigFile(commandPath, configPath);
    }
}
