package org.ah.robox.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.ah.robox.comms.PrinterChannel;

class PrinterConnection implements Runnable {
    private ProxyCommand proxy;
    private ServerSocket serverSocket;
    private boolean stopped;
    private PrinterChannel channel;
    private Thread thread;
    private InputStream printerIn;
    private OutputStream printerOut;

    PrinterConnection(ProxyCommand proxy, PrinterChannel channel) throws IOException {
        this.proxy = proxy;
        this.channel = channel;
        serverSocket = new ServerSocket(0);
        serverSocket.setSoTimeout(500);
    }

    public void closeConnection() {
        stopped = true;
        try {
            printerIn.close();
        } catch (Exception ignore) {
        }
        try {
            printerOut.close();
        } catch (Exception ignore) {
        }
        try {
            channel.close();
        } catch (Exception ignore) {
        }
        proxy.closePrinterConnection(this);
    }

    public void start() {
        try {
            if (!channel.isOpen()) {
                channel.open();
            }

            printerIn = channel.getInputStream();
            printerOut = channel.getOutputStream();
        } catch (IOException e) {
            closeConnection();
            return;
        }

        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    public int getLocalPort() {
        return serverSocket.getLocalPort();
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                Socket socket = serverSocket.accept();
                Client client = new Client(this, socket, printerIn, printerOut);
                client.start();
            } catch (SocketTimeoutException ignore) {
            } catch (IOException e) {
                closeConnection();
                e.printStackTrace();
            }
        }
    }
}