public class User {

    private static int idProvider = 0;
    private final int id;
    private final String name;

    public User(String name) {
        this.id = idProvider++;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getId ()
    {
        return id;
    }

    public static void resetIdProvider() {
        idProvider = 0;
    }
}
