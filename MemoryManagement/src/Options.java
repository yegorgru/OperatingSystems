public class Options {
    private String commandPath;
    private String configPath;
    private int addressRadix;
    private long block;
    private int virtualPageNum;
    private long addressLimit;
    private boolean fileLog;
    private boolean stdoutLog;

    public Options() {
        addressRadix = 10;
        block = (long) Math.pow(2,12);
        virtualPageNum = 63;
        addressLimit = (block * (virtualPageNum+1))-1;
        fileLog = false;
        stdoutLog = false;
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

    void setVirtualPageNum(int value) {
        virtualPageNum = value;
    }

    int getVirtualPageNum() {
        return virtualPageNum;
    }

    void updateAddressLimit() {
        //(block * virtualPageNum+1)-1
        addressLimit = (block * (virtualPageNum+1))-1;
    }

    void setAddressLimit(long value) {
        addressLimit = value;
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
}
