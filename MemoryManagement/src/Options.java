public class Options {
    private String commandPath;
    private String configPath;
    private int addressRadix;
    private long block;
    private int virtualPageMaxIdx;
    private long addressLimit;
    private boolean fileLog;
    private boolean stdoutLog;
    private String fileLogPath;
    private int delay;
    private int physicalPages;

    public Options() {
        addressRadix = 10;
        block = (long) Math.pow(2,12);
        virtualPageMaxIdx = 63;
        addressLimit = (block * (virtualPageMaxIdx+1))-1;
        fileLog = false;
        stdoutLog = false;
        physicalPages = 32;
    }

    void setCommandPath(String path) {
        this.commandPath = path;
    }

    String getCommandPath() {
        return commandPath;
    }

    void setConfigPath(String path) {
        this.configPath = path;
    }

    String getConfigPath() {
        return configPath;
    }

    void setAddressRadix(int value) {
        addressRadix = value;
    }

    int getAddressRadix() {
        return addressRadix;
    }

    void setBlock(long value) {
        block = value;
    }

    long getBlock() {
        return block;
    }

    void setVirtualPageMaxIdx(int value) {
        virtualPageMaxIdx = value;
    }

    int getVirtualPageMaxIdx() {
        return virtualPageMaxIdx;
    }

    void updateAddressLimit() {
        addressLimit = (block * (virtualPageMaxIdx+1))-1;
    }

    long getAddressLimit() {
        return addressLimit;
    }

    void setFileLog(boolean value) {
        fileLog = value;
    }

    boolean isFileLog() {
        return fileLog;
    }

    void setStdoutLog(boolean value) {
        stdoutLog = value;
    }

    boolean isStdoutLog() {
        return stdoutLog;
    }

    void setFileLogPath(String value) {
        fileLogPath = value;
    }

    String getFileLogPath() {
        return fileLogPath;
    }

    void setDelay(int value) {
        delay = value;
    }

    int getDelay() {
        return delay;
    }

    void setPhysicalPages(int value) {
        physicalPages = value;
    }

    int getPhysicalPages() {
        return physicalPages;
    }
}
