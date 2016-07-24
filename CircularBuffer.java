import javax.swing.text.MutableAttributeSet;
import java.util.Arrays;

/**
 * @author Caleb Flynn <flynn37@purdue.edu>
 * @version Nov 2, 2015
 * @lab 809
 * @author Ali Khan <khan133@purdue.edu>
 * @version Nov15,2015
 * @lab 814
 */

public class CircularBuffer {
    private int i = 0;
    private String[] messages;
    private int maxMessages;
    private int count = 0;

    public CircularBuffer(int size) {
        this.maxMessages = size;
        messages = new String[maxMessages];
    }

    public void put(String message) {
        if (i == maxMessages)
            i = 0;
        if (!message.isEmpty())
            messages[i++] = String.format("%04d", count++) + ") " + message;
    }

    public String[] getNewest(int numMessages) {
        if (numMessages < 0)
            return null;
        int numReturned = 0;
        if (count < maxMessages)
            numReturned = Math.min(count, numMessages);
        else
            numReturned = Math.min(numMessages, maxMessages);
        String[] send = new String[numReturned];
        if (numMessages == 0)
            return send;
        int temp = i - 1;
        for (int j = numReturned - 1; j >= 0; j--) {
            send[j] = messages[temp--];
            if (temp == -1)
                temp = maxMessages - 1;
        }
        return send;
    }

    public static void main(String[] args) {
        CircularBuffer c = new CircularBuffer(10);
        for (int i = 0; i < 15; i++) {
            c.put("a");
        }
        System.out.println(Arrays.toString(c.getNewest(50)));
    }
}
