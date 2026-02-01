package library;

/**
 * Base class for all user types.
 */
public abstract class User implements Identifiable {

    private int id;
    private String name;
    private String password;

    public User(int id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getId() {
        return id;
    }

    /** For persistence (database); not exposed elsewhere. */
    public String getPassword() {
        return password;
    }
}
