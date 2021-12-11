import java.lang.Thread;
import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class Parser extends Thread
{
    private static final Logger log = Logger.getLogger(Parser.class.getName());

    public static Options parseConfigFile(String configPath)
    {
        Options options = new Options();
        options.setConfigPath(configPath);
        if (options.getConfigPath() != null)
        {
            File configFile = new File (options.getConfigPath());
            try
            {
                Scanner scanner = new Scanner(configFile);
                boolean wasNumpages = false;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if(line.startsWith("numpages")) {
                        wasNumpages = true;
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
                            options.setVirtualPageMaxIdx(vPageNum);
                        }
                    }
                    if (line.startsWith("physical_pages")) {
                        StringTokenizer st = new StringTokenizer(line);
                        st.nextToken();
                        options.setPhysicalPages(Utils.stringToInt(st.nextToken()));
                    }
                    if (line.startsWith("k ")) {
                        StringTokenizer st = new StringTokenizer(line);
                        st.nextToken();
                        options.setSizeOfShiftRegister(Utils.stringToInt(st.nextToken()));
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
                        if(wasNumpages) {
                            log.warning("If pagesize option is not default, it must be before numpages option. Value ignored");
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
                        if (radix < 0 || radix > 20)
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
        return options;
    }

    public static List<Instruction> parseCommandsFile(Options options, String commandsPath) {
        options.setCommandPath(commandsPath);
        List<Instruction> instructions = new ArrayList<>();
        File commandsFile = new File (options.getCommandPath());
        try {
            Scanner scanner = new Scanner(commandsFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("PROCESS")) {
                    StringTokenizer st = new StringTokenizer(line);
                    String tmp = st.nextToken();
                    int processId = Utils.stringToInt(st.nextToken());
                    String command = st.nextToken();
                    if (!command.equals("READ") && !command.equals("WRITE") && !command.equals("RELOAD")) {
                        log.severe("Unknown command: " + command);
                        System.exit(-1);
                    }
                    if(command.equals("RELOAD")) {
                        instructions.add(new Instruction(command, 0, processId));
                        continue;
                    }
                    tmp = st.nextToken();
                    if (tmp.startsWith("random")) {
                        instructions.add(new Instruction(command, ThreadLocalRandom.current().nextLong(options.getAddressLimit()), processId));
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
                        instructions.add(new Instruction(command, addr, processId));
                    }
                }
            }
        } catch (IOException ex) {
            log.severe("Unexpected exception: " + ex.getMessage());
            System.exit(-1);
        }
        if(instructions.size() == 0)
        {
            log.severe("No instructions present for execution.");
            System.exit(-1);
        }
        return instructions;
    }
}
