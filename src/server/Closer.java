package server;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Closer extends Thread {
    private static final int REFUSAL = 0; // in case of server overload, refusal connection
    private static final int BAN = 1; // in case of prohibited words, bread connection

    private static final String[] answer =
            {"Сервер перегружен, соединение разоравано!",
            "Недопустимая лексика, соединение разоравано!"};

    private final Socket socket;
    private final DataOutputStream dataOutputStream;
    private int answerType; // current case

    Closer(Socket socket) throws IOException {
        this.answerType = REFUSAL;
        this.socket = socket;
        dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    Closer(Socket socket, DataOutputStream dataOutputStream) {
        this.answerType = BAN;
        this.socket = socket;
        this.dataOutputStream = dataOutputStream;
    }

    @Override
    public void run() {
        try {
            dataOutputStream.writeUTF(answer[answerType]);
            dataOutputStream.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
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
}
