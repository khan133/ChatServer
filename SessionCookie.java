/**
 * @author Caleb Flynn <flynn37@purdue.edu>
 * @version Nov 2, 2015
 * @lab 809
 * @author Ali Khan <khan133@purdue.edu>
 * @version Nov15,2015
 * @lab 814
 */
//project12
import java.util.*;

public class SessionCookie {
    public static ArrayList<Long> cookies = new ArrayList<>();
    private long id;
    public static int timeoutLength = 300;
    public long previousTime; //holds the time at which an action happens
    public long currentTime;
    public SessionCookie(long id) {
        this.id = id;
        this.previousTime = System.currentTimeMillis();
        cookies.add(id);
    }

    public void updateTimeOfActivity() {
        this.previousTime = System.currentTimeMillis();
    }

    public long getID() {
        return this.id;
    }

    public boolean hasTimedOut() {
        currentTime = System.currentTimeMillis();
        if (currentTime - previousTime > (long) (timeoutLength * 1000)) {
            return true;
        } else
            return false;
    }
}
