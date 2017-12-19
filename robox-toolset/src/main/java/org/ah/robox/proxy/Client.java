package org.ah.robox.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

class Client {

    private PrinterConnection printerConnection;
    private Thread readerThread;
    private Thread writerThread;
    private Socket socket;
    private InputStream socketIn;
    private OutputStream socketOut;
    private InputStream printerIn;
    private OutputStream printerOut;

    private boolean startWriterThread = true;

    public Client(PrinterConnection printerConnection, Socket socket, InputStream printerIn, OutputStream printerOut) {
        this.printerConnection = printerConnection;
        this.socket = socket;
        this.printerIn = printerIn;
        this.printerOut = printerOut;
    }

    public void start() {
        try {
            socketIn = socket.getInputStream();
            socketOut = socket.getOutputStream();
        } catch (IOException e) {
            close();
            return;
        }

        ProxyCommand.logger.fine("Opened connection " + socket.getRemoteSocketAddress());

        readerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String socketDesc = socket.toString();
                ProxyCommand.logger.finest("Started read thread for  " + socketDesc);
                while (socket != null && !socket.isClosed()) {
                    try {
                        int r = socketIn.read();

                        if (r > 0) {
                            if (startWriterThread) {
                                writerThread.start();
                                startWriterThread = false;
                            }
                            int a = socketIn.available();
                            ProxyCommand.logger.finer("<(" + a + ")");
                            byte[] readBuffer = new byte[a + 1];
                            readBuffer[0] = (byte) r;
                            int total = 1;
                            if (a > 0) {
                                int actual = socketIn.read(readBuffer, 1, a);
                                total = total + actual;
                            }

                            try {
                                printerOut.write(readBuffer, 0, total);
                            } catch (IOException e) {
                                close();
                                printerConnection.closeConnection();
                            }
                        } else {
                            ProxyCommand.logger.finer("<(EOF)");
                            close();
                        }
                    } catch (IOException e) {
                        close();
                    }
                }
                ProxyCommand.logger.finest("Closed read thread for  " + socketDesc);
            }
        });
        readerThread.setDaemon(true);

        writerThread = new Thread(new Runnable() {

            @Override
            public void run() {
                String socketDesc = socket.toString();
                ProxyCommand.logger.finest("Started write thread for  " + socketDesc);
                while (socket != null && !socket.isClosed()) {
                    try {
                        int bytes = printerIn.available();
                        if (bytes > 0) {
                            ProxyCommand.logger.finer(">(" + bytes + ")");
                            byte[] buffer = new byte[bytes];

                            int actual = printerIn.read(buffer, 0, bytes);

                            try {
                                socketOut.write(buffer, 0, actual);
                            } catch (IOException e) {
                                close();
                            }
                        } else {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ignore) { }
                        }
                    } catch (IOException e) {
                        close();
                        printerConnection.closeConnection();
                    }
                }
                ProxyCommand.logger.finest("Closed write thread for  " + socketDesc);
            }
        });
        writerThread.setDaemon(true);

        readerThread.start();
        // writerThread.start();
    }

    protected synchronized void close() {
        if (socket != null) {
            try {
                socket.close();
                ProxyCommand.logger.fine("Closing connection " + socket.getRemoteSocketAddress());
            } catch (Exception ignore) {
            }

            try {
                readerThread.interrupt();
            } catch (Exception ignore) {
            }
            try {
                writerThread.interrupt();
            } catch (Exception ignore) {
            }

            readerThread = null;
            writerThread = null;

            socket = null;
        }
    }
}