import java.lang.Thread;
import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class Kernel extends Thread
{
    private static final Logger log = Logger.getLogger(Kernel.class.getName());
    private Options options;

    private String output = null;
    private static final String lineSeparator = System.getProperty("line.separator");
    //private ControlPanel controlPanel ;
    private Vector memVector = new Vector();
    private Vector instructVector = new Vector();
    //public int runs;
    //public int runcycles;

    public Kernel() {
        options = new Options();
    }

    public Options getOptions() {
        return options;
    }

    public Vector getPages() {
        return memVector;
    }

    public Vector getInstructions() {
        return instructVector;
    }

    public void init(String commandsPath, String configPath) {
        options.setCommandPath(commandsPath);
        options.setConfigPath(configPath);
        parseConfigFile();
        parseCommandsFile();
    }

    public void initPages() {
        for (int i = 0; i <= options.getVirtualPageNum(); i++)
        {
            long high = (options.getBlock() * (i + 1))-1;
            long low = options.getBlock() * i;
            memVector.addElement(new Page(i, -1, false, false, 0, 0, high, low));
        }
    }

    public void parseConfigFile()
    {
        //File f = new File(commandsFile);
        //configFile = configFile;
        //String line;
        //String tmp = null;
        //String command = "";
        //int id = 0;
        //long addressLimit = (options.getBlock() * options.getVirtualPageNum()+1)-1;

        if (options.getConfigPath() != null)
        {
            File configFile = new File (options.getConfigPath());
            try
            {
                Scanner scanner = new Scanner(configFile);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if(line.startsWith("numpages")) {
                        if(memVector.size() != 0) {
                            log.warning("If numpages option is not default, it must be before memset options. Value ignored");
                        }
                        else {
                            StringTokenizer st = new StringTokenizer(line);
                            if (!st.hasMoreTokens()) {
                                log.warning("Undefined numpages");
                            }
                            else {
                                st.nextToken();
                                int vPageNum = Utils.stringToInt(st.nextToken()) - 1;
                                if (vPageNum < 2 || vPageNum > 63) {
                                    log.warning("Number of pages is out of range. Used value 63");
                                    //System.exit(-1);
                                    vPageNum = 63;
                                }
                                options.setVirtualPageNum(vPageNum);
                                options.updateAddressLimit();
                                initPages();
                            }
                        }
                    }
                    if (line.startsWith("memset")) {
                        if(memVector.size() == 0) {
                            options.updateAddressLimit();
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
                            if (0 > id || id > options.getVirtualPageNum() ||
                                    -1 > physical || physical > ((options.getVirtualPageNum() - 1) / 2))
                            {
                                log.warning(id + " page id is invalid. Used default value 0.");
                                //System.exit(-1);
                                id = 0;
                            }
                            boolean R = Utils.stringToBoolean(st.nextToken());
                            boolean M = Utils.stringToBoolean(st.nextToken());
                            int inMemTime = Utils.stringToInt(st.nextToken());
                            if (inMemTime < 0)
                            {
                                log.warning(inMemTime + " inMemTime value is invalid. Used default value 0.");
                                //System.exit(-1);
                                inMemTime = 0;
                            }
                            int lastTouchTime = Utils.stringToInt(st.nextToken());
                            if (lastTouchTime < 0)
                            {
                                log.warning(lastTouchTime + " lastTouchTime value is invalid. Used default value 0.");
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
                        options.setStdoutLog(true);
                    }
                    if (line.startsWith("log_file"))
                    {
                        StringTokenizer st = new StringTokenizer(line);
                        options.setFileLog(true);
                        output = st.nextToken();
                    }
                    if (line.startsWith("pagesize"))
                    {
                        if(memVector.size() != 0) {
                            log.warning("If pagesize option is not default, it must be before memset options. Value ignored");
                        }
                        else {
                            StringTokenizer st = new StringTokenizer(line);
                            String tmp = st.nextToken();
                            tmp = st.nextToken();
                            if (tmp.startsWith( "power" ) )
                            {
                                options.setBlock((int) Math.pow(2, Integer.parseInt(st.nextToken())));
                            }
                            else
                            {
                                options.setBlock(Long.parseLong(tmp,10));
                            }
                            if (options.getBlock() < 64) {
                                log.warning("Pagesize is too small. Used value 64.");
                                options.setBlock(64);
                            }
                            else if(options.getBlock() > Math.pow(2,26)) {
                                log.warning("Pagesize is too big. Used value 2^26.");
                                options.setBlock((long) Math.pow(2,26));
                            }
                            options.updateAddressLimit();
                        }
                    }
                    if (line.startsWith("addressradix"))
                    {
                        StringTokenizer st = new StringTokenizer(line);
                        String tmp = st.nextToken();
                        tmp = st.nextToken();
                        int radix = Utils.stringToInt(tmp);
                        if (radix < 0 || radix > 20 )
                        {
                            log.warning("addressradix is out of range. Used value 10");
                            //System.exit(-1);
                            radix = 10;
                        }
                        options.setAddressRadix(radix);
                    }
                }
                scanner.close();
            } catch (IOException ex) {
                log.severe("Unexpected exception: " + ex.getMessage());
                System.exit(-1);
            }
        }
    }

    public void parseCommandsFile() {
        //int physical_count = 0;
        //int map_count = 0;
        //double power = 14;
        //long high = 0;
        //long low = 0;
        //long addr = 0;
        File f = new File (options.getCommandPath());
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
                        instructVector.addElement(new Instruction(command, ThreadLocalRandom.current().nextLong(options.getAddressLimit())));
                    }
                    else
                    {
                        long addr;
                        if (tmp.startsWith( "bin" ))
                        {
                            addr = Long.parseLong(st.nextToken(),2);
                        }
                        else if (tmp.startsWith( "oct" ))
                        {
                            addr = Long.parseLong(st.nextToken(),8);
                        }
                        else if (tmp.startsWith( "hex" ))
                        {
                            addr = Long.parseLong(st.nextToken(),16);
                        }
                        else
                        {
                            addr = Long.parseLong(tmp);
                        }
                        if (0 > addr || addr > options.getAddressLimit())
                        {
                            log.warning(addr + " address is out of range. Used value 0");
                            //System.exit(-1);
                            addr = 0;
                        }
                        instructVector.addElement(new Instruction(command,addr));
                    }
                }
            }
            in.close();
        } catch (IOException e) {
            log.warning("Unexpected exception: " + e.getMessage());
        }
        if(instructVector.size() == 0)
        {
            log.severe("No instructions present for execution.");
            System.exit(-1);
        }
        if(options.isFileLog())
        {
            File trace = new File(output);
            trace.delete();
        }
        //runs = 0;
        int map_count = 0;
        int physical_count = 0;
        for (int i = 0; i < options.getVirtualPageNum(); i++) {
            Page page = (Page) memVector.elementAt(i);
            if (page.physical != -1) {
                map_count++;
            }
            for (int j = 0; j < options.getVirtualPageNum(); j++) {
                Page tmp_page = (Page) memVector.elementAt(j);
                if (tmp_page.physical == page.physical && page.physical >= 0)
                {
                    physical_count++;
                }
            }
            if (physical_count > 1) {
                log.severe("Duplicate physical pages");
                System.exit(-1);
            }
            physical_count = 0;
        }
        if (map_count < (options.getVirtualPageNum() +1 ) / 2) {
            for (int i = 0; i < options.getVirtualPageNum(); i++) {
                Page page = (Page) memVector.elementAt(i);
                if (page.physical == -1 && map_count < (options.getVirtualPageNum() + 1 ) / 2 ) {
                    page.physical = i;
                    map_count++;
                }
            }
        }

        /*for (int i = 0; i < instructVector.size(); i++)
        {
            //high = options.getBlock() * options.getVirtualPageNum();
            Instruction instruct = (Instruction)  instructVector.elementAt(i);
            if (instruct.addr < 0 || instruct.addr > options.getAddressLimit())
            {
                log.severe("Instruction (" + instruct.inst + " " + instruct.addr + ") out of bounds.");
                System.exit(-1);
            }
        }*/
    }

    public void getPage(int pageNum)
    {
        Page page = ( Page ) memVector.elementAt( pageNum );
        //controlPanel.paintPage( page );
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

    public void reset() {
        memVector.removeAllElements();
        instructVector.removeAllElements();
        parseConfigFile();
        parseCommandsFile();
    }
}
