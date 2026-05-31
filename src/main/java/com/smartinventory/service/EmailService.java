package com.smartinventory.service;

import com.smartinventory.config.AppConfig;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Properties;

public class EmailService {
    private final Properties config = AppConfig.load("email.properties");
    private static String lastError = "";

    public boolean isEnabled() {
        return Boolean.parseBoolean(config.getProperty("mail.enabled", "false"));
    }

    public boolean send(String subject, String body) {
        lastError = "";
        if (!isEnabled()) {
            lastError = "Email is disabled in email.properties";
            return false;
        }
        String host = config.getProperty("mail.smtp.host", "");
        int port = Integer.parseInt(config.getProperty("mail.smtp.port", "465"));
        boolean ssl = Boolean.parseBoolean(config.getProperty("mail.smtp.ssl", "true"));
        boolean trustAll = Boolean.parseBoolean(config.getProperty("mail.trustAll", "false"));
        String username = config.getProperty("mail.username", "").trim();
        String password = config.getProperty("mail.password", "").replaceAll("\\s+", "");
        String from = config.getProperty("mail.from", username).trim();
        String to = config.getProperty("mail.to", "").trim();
        if (host.isBlank() || from.isBlank() || to.isBlank()) {
            lastError = "Missing mail host, from, or to address";
            return false;
        }

        try (Socket socket = ssl
                ? socketFactory(trustAll).createSocket(host, port)
                : new Socket(host, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            expect(reader);
            command(writer, reader, "EHLO smartinventory.local");
            if (!username.isBlank()) {
                command(writer, reader, "AUTH LOGIN");
                command(writer, reader, Base64.getEncoder().encodeToString(username.getBytes(StandardCharsets.UTF_8)));
                command(writer, reader, Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)));
            }
            command(writer, reader, "MAIL FROM:<" + from + ">");
            command(writer, reader, "RCPT TO:<" + to + ">");
            command(writer, reader, "DATA");
            writer.write("From: " + from + "\r\n");
            writer.write("To: " + to + "\r\n");
            writer.write("Subject: " + subject + "\r\n");
            writer.write("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
            writer.write(body.replace("\n.", "\n.."));
            writer.write("\r\n.\r\n");
            writer.flush();
            expect(reader);
            command(writer, reader, "QUIT");
            return true;
        } catch (IOException ex) {
            lastError = ex.getMessage();
            return false;
        }
    }

    public static String lastError() {
        return lastError;
    }

    private SSLSocketFactory socketFactory(boolean trustAll) throws IOException {
        if (!trustAll) {
            return (SSLSocketFactory) SSLSocketFactory.getDefault();
        }
        try {
            TrustManager[] trustManagers = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustManagers, new SecureRandom());
            return context.getSocketFactory();
        } catch (Exception ex) {
            throw new IOException("Could not create SMTP SSL context", ex);
        }
    }

    private void command(BufferedWriter writer, BufferedReader reader, String command) throws IOException {
        writer.write(command);
        writer.write("\r\n");
        writer.flush();
        expect(reader);
    }

    private void expect(BufferedReader reader) throws IOException {
        String line;
        do {
            line = reader.readLine();
            if (line == null) {
                throw new EOFException("SMTP server closed the connection");
            }
        } while (line.length() > 3 && line.charAt(3) == '-');
        char code = line.charAt(0);
        if (code != '2' && code != '3') {
            throw new IOException("SMTP error: " + line);
        }
    }
}
