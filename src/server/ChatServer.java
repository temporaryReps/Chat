package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class ChatServer {
    public static final int MAX_CLIENTS_NUMBER = 5;

    private static final int PORT = 8070;
    private static final String PROPERTIES_PATH = "src/server.properties";
    private static final String BANNED_WORDS_SOURCE = "banned";

    private static List<String> bannedWords; // list of prohibited words
    private static int clientsNumber; // current number of connected clients
    private static ServerSocket serverSocket = null;

    public static void main(String[] args) {
        bannedWordsDownload();

        try {
            serverSocket = new ServerSocket(PORT);
            while (true) {
                if (clientsNumber < MAX_CLIENTS_NUMBER) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Accept from: " + socket.getInetAddress());
                    ChatHandler chatHandler = new ChatHandler(socket);
                    chatHandler.start();
                    clientsNumber++;
                    System.out.println(clientsNumber);
                } else {
                    Socket socket = serverSocket.accept();
                    new Closer(socket).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert serverSocket != null;
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * decrement number of clients
     */
    public static void clientsDecrement() {
        if (clientsNumber > 0) {
            clientsNumber--;
        }
        System.out.println(clientsNumber);
    }

    public static List<String> getBannedWords() {
        return bannedWords;
    }

    /**
     * receives data from a file containing a list of words
     */
    private static void bannedWordsDownload() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Properties properties = new Properties();
                try(BufferedInputStream stream =
                            new BufferedInputStream(new FileInputStream(new File(PROPERTIES_PATH)))) {
                    properties.load(stream);

                    String wordsPath = properties.getProperty(BANNED_WORDS_SOURCE);
                    BufferedReader reader = new BufferedReader(new FileReader(new File(wordsPath)));

                    bannedWords = Collections.synchronizedList(new ArrayList<>());
                    String line;
                    while ((line = reader.readLine()) != null) {
                        bannedWords.add(line);
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}