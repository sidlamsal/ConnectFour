import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class spike {

    private static HashMap<String, String> readLogsFromFile(String filename) {
        HashMap<String, String> readLogs = new HashMap<>();

        try {
            String logsString = new String(Files.readAllBytes(Paths.get(filename)));

            if (logsString.length() != 0) {
                String[] logsList = logsString.split("\n");
                for (int i = 0; i < logsList.length; i++) {
                    String[] usernameAndPassword = logsList[i].split(", ");
                    readLogs.put(usernameAndPassword[0], usernameAndPassword[1]);
                }
            }

        } catch (IOException e) {
            System.out.println(e);
        }

        return readLogs;
    }

    public static void appendStringToFile(String filePath, String content) throws IOException {
        Files.write(Paths.get(filePath), content.getBytes(), StandardOpenOption.APPEND);
    }

    private static void updateLogsInFile(String filename, HashMap<String, String> logs) {
        try {
            Files.write(Paths.get(filename), new byte[0]);

            for (Map.Entry<String, String> entry : logs.entrySet()) {
                String logString = entry.getKey() + ", " + entry.getValue();
                appendStringToFile(filename, logString);
            }

        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {

        String fileName = "C4V4\\C4Logs.txt";

        HashMap<String, String> example = readLogsFromFile(fileName);

        updateLogsInFile(fileName, example);

    }
}
