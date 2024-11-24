
import java.util.HashMap;

public class Commands {
    public static final HashMap<String, Object[]> COMMANDS = new HashMap<>();

    static {
        COMMANDS.put("light", new Object[]{7, "light", "8", "H"});
        COMMANDS.put("fan", new Object[]{6, "fan", "7", "G"});
        COMMANDS.put("pan", new Object[]{6, "fan", "7", "G"});
        COMMANDS.put("hen", new Object[]{6, "fan", "7", "G"});
        COMMANDS.put("and", new Object[]{6, "fan", "7", "G"});
        COMMANDS.put("so good", new Object[]{5, "socket", "6", "F"});
        COMMANDS.put("socket", new Object[]{5, "socket", "6", "F"});
        COMMANDS.put("night", new Object[]{4, "night lamp", "5", "E"});
    }
}
