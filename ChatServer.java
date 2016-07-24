import java.util.*;
//final

/**
 * <b> CS 180 - Project 4 - Chat Server Skeleton </b>
 * <p>
 * <p>
 * This is the skeleton code for the ChatServer Class. This is a private chat
 * server for you and your friends to communicate.
 *
 * @author Caleb Flynn <flynn37@purdue.edu>
 * @version Nov 2, 2015
 * @lab 809
 * @author Ali Khan <khan133@purdue.edu>
 * @version Nov15,2015
 * @lab 814
 */

public class ChatServer {
    private ArrayList<User> users = new ArrayList<User>();
    private int maxMessages;
    private int numMessages = 0;
    CircularBuffer buffer;
    String names;

    public ChatServer(User[] users, int maxMessages) {
        buffer = new CircularBuffer(maxMessages);
        //generate random num for cookie id
        long num = genCookie();
        //create root user, password: cs180, and random sessionCookie
        this.users.add(new User("root", "cs180", null));

        for (int i = 0; i < users.length; i++) {
            this.users.add(users[i]); //add each user from array to arraylist
        }
        this.maxMessages = maxMessages;
    }

    public long genCookie() {
        int num;
        Random r = new Random();
        while (true) {
            num = r.nextInt(10000);
            boolean duplicate = false;
            for (int i = 0; i < SessionCookie.cookies.size(); i++) {
                if (SessionCookie.cookies.get(i) == num) {
                    duplicate = true;

                }
            }
            if (duplicate == false) {
                break;
            }
        }
        return (long) num;
    }

    /**
     * This method begins server execution.
     */
    public void run() {
        boolean verbose = false;
        System.out.printf("The VERBOSE option is off.\n\n");
        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.printf("Input Server Request: ");
            String command = in.nextLine();

            // this allows students to manually place "\r\n" at end of command
            // in prompt
            command = replaceEscapeChars(command);

            if (command.startsWith("kill"))
                break;

            if (command.startsWith("verbose")) {
                verbose = !verbose;
                System.out.printf("VERBOSE has been turned %s.\n\n", verbose ? "on" : "off");
                continue;
            }

            String response;
            try {
                response = parseRequest(command);
            } catch (IndexOutOfBoundsException ex) {
                response = MessageFactory.makeErrorMessage(MessageFactory.UNKNOWN_ERROR,
                        String.format("An exception of %s occurred.", ex.getMessage()));
            }

            // change the formatting of the server response so it prints well on
            // the terminal (for testing purposes only)
            if (response.startsWith("SUCCESS\t"))
                response = response.replace("\t", "\n");

            // print the server response
            if (verbose)
                System.out.printf("response:\n");
            System.out.printf("\"%s\"\n\n", response);
        }

        in.close();
    }

    /**
     * Replaces "poorly formatted" escape characters with their proper values.
     * For some terminals, when escaped characters are entered, the terminal
     * includes the "\" as a character instead of entering the escape character.
     * This function replaces the incorrectly inputed characters with their
     * proper escaped characters.
     *
     * @param str - the string to be edited
     * @return the properly escaped string
     */
    private static String replaceEscapeChars(String str) {
        str = str.replace("\\r", "\r");
        str = str.replace("\\n", "\n");
        str = str.replace("\\t", "\t");

        return str;
    }

    /**
     * Determines which client command the request is using and calls the
     * function associated with that command.
     *
     * @param request - the full line of the client request (CRLF included)
     * @return the server response
     */
    public String parseRequest(String request) {
        request = replaceEscapeChars(request);
        //contains array starting with the command and containing parameters for methods below
        String[] parsedRequest = request.split("\t");

        //command must be upper case
        if (!parsedRequest[0].equals(parsedRequest[0].toUpperCase())) {
            return MessageFactory.makeErrorMessage(11);
        }
        //must contain "\r\n" at end
        if (!parsedRequest[parsedRequest.length - 1].endsWith("\r\n")) {
            return MessageFactory.makeErrorMessage(MessageFactory.FORMAT_COMMAND_ERROR);
        } else {
            //removes "\r\n" from args
            int index = parsedRequest[parsedRequest.length - 1].indexOf("\r\n");
            parsedRequest[parsedRequest.length - 1] =
                    parsedRequest[parsedRequest.length - 1].substring(0, index);
        }


        //if statements to check command
        if (parsedRequest[0].equals("ADD-USER")) {
            if (parsedRequest.length != 4) {
                return MessageFactory.makeErrorMessage(10);
            }
            return addUser(parsedRequest);
        }
        if (parsedRequest[0].equals("USER-LOGIN")) {
            if (parsedRequest.length != 3) {
                return MessageFactory.makeErrorMessage(10);
            }
            return userLogin(parsedRequest);
        }
        if (parsedRequest[0].equals("POST-MESSAGE")) {
            if (parsedRequest.length != 3) {
                return MessageFactory.makeErrorMessage(10);
            }
            String name = ""; //TODO: figure out name.
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i) == null)
                    continue;
                if (users.get(i).getCookie() == null)
                    continue;
                if (Long.toString(users.get(i).getCookie().getID()).equals(parsedRequest[1]))
                    name = users.get(i).getName();
            }
            return postMessage(parsedRequest, name); //name is current user
        }
        if (parsedRequest[0].equals("GET-MESSAGES")) {
            if (parsedRequest.length != 3) {
                return MessageFactory.makeErrorMessage(MessageFactory.FORMAT_COMMAND_ERROR);
            }
            return getMessages(parsedRequest);
        }

        return MessageFactory.makeErrorMessage(MessageFactory.UNKNOWN_COMMAND_ERROR);
    }

    public String addUser(String[] args) {
        String cookieId = args[1];
        String username = args[2];
        String password = args[3];


        boolean cookieMatch = false;
        //update current user cookie id
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getCookie() != null && cookieId.equals(Long.toString(users.get(i).getCookie().getID()))) {
                //if cookie id sent from args = current user's cookie id
                //update cookie id
                cookieMatch = true;
                users.get(i).getCookie().updateTimeOfActivity();
            }
        }
        if (!cookieMatch) {
            return MessageFactory.makeErrorMessage(MessageFactory.LOGIN_ERROR);
        }

        //errors
        //if user already exists
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);

            if (users.get(i).getName().equals(username)) {
                return MessageFactory.makeErrorMessage(MessageFactory.USER_ERROR);
            }
        }

        //username and password must be alphanumerical values [A-Za-z0-9].
        for (int i = 0; i < username.length(); i++) {
            int ascii = (int) username.charAt(i); //convert char at index i to ascii format
            //if it is not (0-9 or A-Z or a-z) -> return error
            if (!((ascii >= 48 && ascii <= 57) || (ascii >= 65 && ascii <= 90) || (ascii >= 97 && ascii <=
                    122))) {
                return MessageFactory.makeErrorMessage(MessageFactory.INVALID_VALUE_ERROR);
            }
        }
        for (int i = 0; i < password.length(); i++) {
            int ascii = (int) password.charAt(i); //convert char at index i to ascii format
            //if it is not (0-9 or A-Z or a-z) -> return error
            if (!((ascii >= 48 && ascii <= 57) || (ascii >= 65 && ascii <= 90) || (ascii >= 97 && ascii <=
                    122))) {
                return MessageFactory.makeErrorMessage(MessageFactory.INVALID_VALUE_ERROR);
            }
        }
        //username:1 to 20 characters in length (inclusive).
        if (username.length() < 1 || username.length() > 20)
            return MessageFactory.makeErrorMessage(MessageFactory.INVALID_VALUE_ERROR);
        //password:4 to 40 characters in length (inclusive).
        if (password.length() < 4 || password.length() > 40)
            return MessageFactory.makeErrorMessage(MessageFactory.INVALID_VALUE_ERROR);


        //creates new user with no cookie. generate a cookie later in the user login
        User newUser = new User(username, password, null);
        users.add(newUser);
        return "SUCCESS\r\n";
    }

    public String userLogin(String[] args) {
        String username = args[1];
        String password = args[2];
        names = username;
        //errors
        //the user must already have been created earlier through addUser
        //the password must be correct
        //find username/password in users arrayList
        int index = 0;
        boolean usernamefound = false;
        boolean passwordfound = false;
        boolean fullfound = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getName().equals(username)) {
                usernamefound = true;
                if (users.get(i).checkPassword(password)) {
                    if (users.get(i).getCookie() == null) {
                        index = i; //index of user with given username
                        fullfound = true;
                        break;
                    }
                    if (users.get(i).getCookie() != null)
                        return MessageFactory.makeErrorMessage(25);
                }

            }
        }
        if (!usernamefound)
            return MessageFactory.makeErrorMessage(20);
        if (!fullfound) {
            return MessageFactory.makeErrorMessage(21);
        }

        //the given user shouldn't already be authenticated (the SessionCookie associated should be null)
        if (users.get(index).getCookie() != null) {
            return MessageFactory.makeErrorMessage(MessageFactory.INVALID_VALUE_ERROR);
        }

        //generates a new SessionCookie for the user to indicate that he is now connected.
        long cook = genCookie();
        users.get(index).setCookie(new SessionCookie(cook));

        //make sure cookie id is 4 digits
        String idcookie = String.format("%04d", cook);
        return "SUCCESS\t" + idcookie + "\r\n";
    }

    public String postMessage(String[] args, String name) {
        String cookieID = args[1];
        try {
            boolean cookieExists = false;
            int count = 0;
            long cookieee = Long.parseLong(cookieID);
            boolean name1 = false;
            int i;
            String message = args[2].trim();
            if (message.equals("")) {
                return MessageFactory.makeErrorMessage(24);
            }
            for (i = 0; i < users.size(); i++) {
                if (users.get(i) == null)
                    continue;
                if (users.get(i).getCookie() == null)
                    continue;
                if (Long.toString(users.get(i).getCookie().getID()).equals(args[1])) {
                    count++;
                    break;
                }
            }
            if (count == 0 || users.get(i).getCookie() == null)
                return MessageFactory.makeErrorMessage(23);
            if (i == users.size())
                return MessageFactory.makeErrorMessage(20);
            if (users.get(i).getCookie().hasTimedOut()) {
                users.get(i).setCookie(null);
                return MessageFactory.makeErrorMessage(05);
            }

            buffer.put(name + ": " + args[2]);
            users.get(i).getCookie().updateTimeOfActivity();
            return "SUCCESS\r\n";

            //this error is prob wrong
        } catch (NumberFormatException e) {
            return MessageFactory.makeErrorMessage(24);
        }
    }

    public String getMessages(String[] args) {
        try {
            int flag = 0;
            int i;
            for (i = 0; i < users.size(); i++) {
                if (users.get(i) == null)
                    continue;
                if (users.get(i).getCookie() == null)
                    continue;
                if (Long.toString(users.get(i).getCookie().getID()).equals(args[1])) {
                    if (users.get(i).getCookie().hasTimedOut()) {
                        users.get(i).setCookie(null);
                        return MessageFactory.makeErrorMessage(5);
                    }
                    flag++;
                }
            }

            if (flag == 0)
                return MessageFactory.makeErrorMessage(23);
            //if no usernames with that name, return 23
            //try catch
            if (Integer.parseInt(args[2]) < 1)
                return MessageFactory.makeErrorMessage(24);
            String numMessages = args[2];
            if (!numMessages.matches("[0-9]+"))
                return MessageFactory.makeErrorMessage(24);
            int numMessages1 = Integer.parseInt(args[2]);
            String[] newestMessages = buffer.getNewest(numMessages1);
            String str = "";
            for (String str1 : newestMessages) {
                str = str + "\t" + str1;
            }

            return "SUCCESS" + str + "\r\n";
        } catch (NumberFormatException e) {
            return MessageFactory.makeErrorMessage(24);
        }
    }
}
