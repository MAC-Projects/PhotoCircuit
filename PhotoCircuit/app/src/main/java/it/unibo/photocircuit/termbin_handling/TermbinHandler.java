package it.unibo.photocircuit.termbin_handling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TermbinHandler {
    public static String send(String data) throws IOException {
        String url;
        try (Socket socket = new Socket("termbin.com", 9999)) {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(data);

            InputStreamReader reader = new InputStreamReader(socket.getInputStream());
            StringBuilder urlSb = new StringBuilder();

            int ch;
            while ((ch = reader.read()) > 0) {
                urlSb.append((char)ch);
            }
            url = urlSb.toString();
        }
        return url;
    }
}
