public class Utils {

    public static final int BYTES_BLOCK_SIZE = 512;
    public static final int NUM_MAX_BLOCKS = 65536;
    public static final int NUM_MAX_INODE_BLOCKS = 10;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";

    enum Action {
        READ(4),
        WRITE(2),
        EXECUTE(1);

        private final int value;

        Action(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}
