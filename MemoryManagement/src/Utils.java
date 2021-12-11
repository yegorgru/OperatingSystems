import java.util.logging.Logger;

public class Utils {
    private static final Logger log = Logger.getLogger(Utils.class.getName());

    static public int stringToInt(String s) {
        int i = 0;
        try {
            i = Integer.parseInt(s.strip());
        } catch (NumberFormatException ex) {
            log.severe(ex.getMessage());
        }
        return i;
    }
}

