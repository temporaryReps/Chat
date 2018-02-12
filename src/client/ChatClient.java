package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

public class ChatClient extends JFrame implements Runnable {
    private static final String SITE = "localhost";
    private static final String PORT = "8070";
    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private JTextArea outTextArea;
    private JTextField inTextField;
    private JPanel panel;

    private ChatClient(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        super("Client");
        this.socket = socket;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;

        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel();
        panel.setLayout(new BorderLayout(3, 3));
        add(BorderLayout.SOUTH, panel);

        outTextArea = new JTextArea();
        add(outTextArea);

        inTextField = new JTextField();

        JButton button = new JButton("Send");

        panel.add(inTextField);
        panel.add(BorderLayout.EAST, button);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                try {
                    dataOutputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        inTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        setVisible(true);
        inTextField.requestFocus();
        new Thread(this).start();
    }

    private void sendMessage() {
        try {
            dataOutputStream.writeUTF(inTextField.getText());
            dataOutputStream.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        inTextField.setText("");
    }

    public static void main(String[] args) {
        Socket socket = null;
        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;

        try {
            socket = new Socket(SITE, Integer.parseInt(PORT));
            dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            new ChatClient(socket, dataInputStream, dataOutputStream);

        } catch (IOException e) {
            e.printStackTrace();
            if (dataInputStream != null) {
                try {
                    dataInputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            while (true) { //TODO flag
                String line = dataInputStream.readUTF();
                outTextArea.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            inTextField.setVisible(false);
            validate();
        }
    }
}