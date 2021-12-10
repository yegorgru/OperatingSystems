import java.lang.Thread;
import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class Kernel extends Thread
{
    private static final Logger log = Logger.getLogger(Kernel.class.getName());
    private final Options options;

    private final List<Page> pages = new ArrayList<>();
    private final List<Instruction> instructions = new ArrayList<>();

    public Kernel() {
        options = new Options();
    }

    public Options getOptions() {
        return options;
    }

    public List<Page> getPages() {
        return pages;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public int getPageIdxByAddr(long memoryAddr)
    {
        int pageIdx = (int)(memoryAddr / options.getBlock());
        if(pageIdx < 0 || pageIdx > options.getVirtualPageNum()) {
            return -1;
        }
        return pageIdx;
    }

    public void init(String commandsPath, String configPath) {
        options.setCommandPath(commandsPath);
        options.setConfigPath(configPath);
        parseConfigFile();
        parseCommandsFile();
    }

    public void initPages() {
        for (int i = 0; i <= options.getVirtualPageNum(); i++) {
            long high = (options.getBlock() * (i + 1))-1;
            long low = options.getBlock() * i;
            pages.add(new Page(i, -1, false, false, 0, 0, high, low));
        }
    }

    public void parseConfigFile()
    {
        if (options.getConfigPath() != null)
        {
            File configFile = new File (options.getConfigPath());
            try
            {
                Scanner scanner = new Scanner(configFile);
                boolean wasPage = false;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if(line.startsWith("numpages")) {
                        if(wasPage) {
                            log.warning("If numpages option is not default, it must be before memset options. Value ignored");
                        }
                        else {
                            StringTokenizer st = new StringTokenizer(line);
                            st.nextToken();
                            if (!st.hasMoreTokens()) {
                                log.warning("Undefined numpages");
                            }
                            else {
                                int vPageNum = Utils.stringToInt(st.nextToken()) - 1;
                                if (vPageNum < 2 || vPageNum > 63) {
                                    log.warning("Number of pages is out of range. Used value 63");
                                    //System.exit(-1);
                                    vPageNum = 63;
                                }
                                options.setVirtualPageNum(vPageNum);
                            }
                        }
                    }
                    if (line.startsWith("memset")) {
                        wasPage = true;
                        if(pages.size() == 0) {
                            options.updateAddressLimit();
                            initPages();
                        }
                        StringTokenizer st = new StringTokenizer(line);
                        st.nextToken();
                        while (st.hasMoreTokens())
                        {
                            int id = Utils.stringToInt(st.nextToken());
                            String tmp = st.nextToken();
                            int physical;
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
                            Page page = pages.get(id);
                            page.setPhysical(physical);
                            page.setRead(R);
                            page.setWrite(M);
                            page.setMemoryTime(inMemTime);
                            page.setLastTouchTime(lastTouchTime);
                        }
                    }
                    if (line.startsWith("log_stdout")) {
                        options.setStdoutLog(true);
                    }
                    if (line.startsWith("log_file")) {
                        StringTokenizer st = new StringTokenizer(line);
                        options.setFileLog(true);
                        st.nextToken();
                        if(st.hasMoreTokens()) {
                            options.setFileLogPath(st.nextToken());
                        }
                        else {
                            options.setFileLogPath("log.txt");
                        }
                        File file = new File(options.getFileLogPath());
                        file.delete();
                        file.createNewFile();
                    }
                    if (line.startsWith("pagesize")) {
                        if(wasPage) {
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
                    if (line.startsWith("addressradix")) {
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
                    if (line.startsWith("delay")) {
                        StringTokenizer st = new StringTokenizer(line);
                        String tmp = st.nextToken();
                        tmp = st.nextToken();
                        int delay = Utils.stringToInt(tmp);
                        if(delay <= 50) {
                            log.warning("delay is too small. Used value 2000");
                            //System.exit(-1);
                            delay = 2000;
                        }
                        options.setDelay(delay);
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
        File commandsFile = new File (options.getCommandPath());
        try {
            Scanner scanner = new Scanner(commandsFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("READ") || line.startsWith("WRITE")) {
                    String command = "";
                    if (line.startsWith("READ")) {
                        command = "READ";
                    }
                    if (line.startsWith("WRITE")) {
                        command = "WRITE";
                    }
                    StringTokenizer st = new StringTokenizer(line);
                    String tmp = st.nextToken();
                    tmp = st.nextToken();
                    if (tmp.startsWith("random")) {
                        instructions.add(new Instruction(command, ThreadLocalRandom.current().nextLong(options.getAddressLimit())));
                    } else {
                        long addr;
                        if (tmp.startsWith("bin")) {
                            addr = Long.parseLong(st.nextToken(), 2);
                        } else if (tmp.startsWith("oct")) {
                            addr = Long.parseLong(st.nextToken(), 8);
                        } else if (tmp.startsWith("hex")) {
                            addr = Long.parseLong(st.nextToken(), 16);
                        } else {
                            addr = Long.parseLong(tmp);
                        }
                        if (0 > addr || addr > options.getAddressLimit()) {
                            log.warning(addr + " address is out of range. Used value 0");
                            //System.exit(-1);
                            addr = 0;
                        }
                        instructions.add(new Instruction(command, addr));
                    }
                }
            }
        } catch (IOException ex) {

        }
        if(instructions.size() == 0)
        {
            log.severe("No instructions present for execution.");
            System.exit(-1);
        }
        int mapCount = 0;
        Set<Integer> physicalPages = new TreeSet<>();
        for (int i = 0; i < options.getVirtualPageNum(); i++) {
            Page page = pages.get(i);
            if (page.hasPhysical()) {
                mapCount++;
                if(physicalPages.contains(page.getPhysical())) {
                    log.severe("Duplicate physical pages");
                    System.exit(-1);
                }
                physicalPages.add(page.getPhysical());
            }
        }
        if (mapCount < (options.getVirtualPageNum() +1 ) / 2) {
            for (int i = 0; i < options.getVirtualPageNum(); i++) {
                Page page = pages.get(i);
                if (!page.hasPhysical()) {
                    page.setPhysical(i);
                    mapCount++;
                    if(mapCount == (options.getVirtualPageNum() + 1 ) / 2) {
                        break;
                    }
                }
            }
        }
    }

    public Page getPage(int pageNum)
    {
        return pages.get(pageNum);
    }

    public void reset() {
        pages.clear();
        instructions.clear();
        parseConfigFile();
        parseCommandsFile();
    }
}
