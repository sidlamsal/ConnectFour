# ConnectFour
Server-client implementation of Connect Four using Java and JavaScript.

This file contains instructions on how to download and execute a Connect Four application built on a client-server paradigm, which supports concurrent games with 2 players each.

## Instructions to Download

1. Visit the website at the URL https://github.com/sidlamsal/ConnectFour/tree/main/C4V3. Here, you will see a list of files created for the Connect Four game. Download the following three files:
   - C4Client.java
   - C4Server.java
   - C4Game.java
   - C4CheckGame.java.

3. To download these files, click on the file name, which will take you to the page where the particular file will be displayed. In the top right corner, click on the button that says "Download raw file" when hovered over. Repeat this step for each file mentioned in the previous step.

## Instructions to Execute

Note that this step assumes that your device has a functioning java virtual machine (JVM), as it makes use of the javac command that can read the source files and compile them into class files to run on the JVM. To check if you have the javac command installed properly, open a terminal window and type javac -version. If this does not return a version number, visit the oracle website to download the Java Development Kit (JDK) at the following URL: https://www.oracle.com/java/technologies/downloads/.

1. Navigate into the directories containing the downloaded files via a command line terminal, and compile both the server and the client files via their respective commands.
```
javac C4Client.java 
javac C4Server.java
```

2. Execute the server file using the following command. 
```
java C4Server
```

This command allows the server to start running on the localhost machine at port number 12345.

3. Execute the client file twice - in two different terminals - using the following command.
```
java C4Client localhost 12345
java C4Client localhost 12345
```

At the moment, we are assuming that the server and client are running on the same machine (localhost). There have been varying levels of success running the server on a separate machine and connecting different players from different machines. For best results, we suggest running both the server and clients on the same machine as we have detailed above.

Now, the two clients have connected to a running server, a thread has been created for the game and the game can commence.
