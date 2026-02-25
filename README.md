# project2-team21
-----------------------------------------------------------------------------------------------------------------------
HOW TO LINK CREDENTIALS:
- under the frontend folder directory, create a file called Credentials.java
- populate it with the following:

package frontend;

public class Credentials {
    public static final String username = "USERNAME";
    public static final String password = "PASSWORD";
}

// EDIT THE USERNAME AND PASSWORD FIELDS TO ACTUAL CREDENTIALS IN THE Credentials.java ON YOUR LOCAL COMPUTER
-----------------------------------------------------------------------------------------------------------------------
HOW TO RUN APP:
- compile using command: javac -cp ".;postgresql-42.2.8.jar" frontend/*.java
- run using command: java -cp ".;postgresql-42.2.8.jar" frontend.App
-----------------------------------------------------------------------------------------------------------------------
