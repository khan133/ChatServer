/**
 * @author Caleb Flynn <flynn37@purdue.edu>
 * @version Nov 2, 2015
 * @lab 809
 * @author Ali Khan <khan133@purdue.edu>
 * @version Nov15,2015
 * @lab 814
 */
public class User {
    private String username;
    private String password;
    private SessionCookie cookie;

    public User(String username, String password, SessionCookie cookie) {
        this.username = username;
        this.password = password;
        this.cookie = cookie;
    }

    public String getName() {
        return username;
    }

    public boolean checkPassword(String password) {
        if (this.password.equals(password))
            return true;
        return false;
    }

    public SessionCookie getCookie() {
        return cookie;
    }

    public void setCookie(SessionCookie cookie) {
        this.cookie = cookie;
    }
}
