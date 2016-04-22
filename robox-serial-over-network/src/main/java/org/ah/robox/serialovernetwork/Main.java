package org.ah.robox.serialovernetwork;

import java.io.File;
import java.net.Socket;

public class Main {

    public static void main(String[] args) throws Exception {
        File roboxNetworkProperties = new File("robox.network.properties");
        roboxNetworkProperties.createNewFile();

        boolean found = false;

        if (args.length > 0) {
            for (String a : args) {
                if (a.startsWith("serialproxy://")) {
                    String[] split = a.substring(14).split(":");
                    String address = split[0];
                    int port = Integer.parseInt(split[1]);

                    try {
                        Socket socket = new Socket(address, port);
                        socket.getOutputStream();
                        socket.close();
                        System.out.println(a);
                        found = true;
                    } catch (Exception e) {
                    }
                }
            }
        }
        if (!found) {
            System.out.println("NOT_CONNECTED");
        }
    }

}
