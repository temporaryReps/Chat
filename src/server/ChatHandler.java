package server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ChatHandler extends Thread {
    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private static List<ChatHandler> handlers =
            Collections.synchronizedList(new ArrayList<>(ChatServer.MAX_CLIENTS_NUMBER));
    private static List<String> bannedWords = ChatServer.getBannedWords();

    public ChatHandler(Socket socket) throws IOException {
        this.socket = socket;
        dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    @Override
    public void run() {
        handlers.add(this);

        try {
            while (true) { //TODO flag
                System.out.println("run");
                String message = dataInputStream.readUTF();
                if (checkWords(message)) {
                    ChatServer.clientsDecrement();
                    return;
                }
                broadcast(message);
            }
        } catch (IOException e) {
            ChatServer.clientsDecrement();
            e.printStackTrace();
        } finally {
            handlers.remove(this);
            try {
                dataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * checks if the message contains prohibited words
     * @param message received message
     * @return true if the message contains prohibited words
     */
    private boolean checkWords(String message) {
        String[] messageWords = message.split(" ");
        for (String word : bannedWords) {
            for (int i = 0; i < messageWords.length; i++) {
                if (word.equals(messageWords[i])) {
                    Closer closer = new Closer(socket, dataOutputStream);
                    closer.start();
                    try {
                        //wait until closer is completed
                        closer.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void broadcast(String message) {
        synchronized (handlers) {
            Iterator<ChatHandler> iterator = handlers.iterator();
            while (iterator.hasNext()) {
                ChatHandler chatHandler = iterator.next();
                sendMessage(message, chatHandler);
            }
        }
    }

    /**
     * Sends message to particular client
     * @param message which will send
     * @param chatHandler which is binding with client
     */
    private void sendMessage(String message, ChatHandler chatHandler) {
        try {
            chatHandler.dataOutputStream.writeUTF(message);
            chatHandler.dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}