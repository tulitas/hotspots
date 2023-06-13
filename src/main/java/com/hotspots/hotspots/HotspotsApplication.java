package com.hotspots.hotspots;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@SpringBootApplication
public class HotspotsApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(HotspotsApplication.class, args);
        System.out.println("Run APP");
        int port = 5000;

        System.out.println("port: " + port);

        try (ServerSocket serverSocket = new ServerSocket(port);
             Socket socket = serverSocket.accept();
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            ObjectMapper objectMapper = new ObjectMapper();

            String packet;
            while ((packet = reader.readLine()) != null) {
//                System.out.println("Received packet: " + packet);

                try {
                    if (isJSONValid(packet)) {
                        JsonNode jsonNode = objectMapper.readTree(packet);

                        // Извлекаем нужные данные из jsonNode
                        JsonNode appEuiNode = jsonNode.get("app_eui");
                        JsonNode balanceNode = null;
                        JsonNode nonceNode = null;
                        JsonNode dcNode = jsonNode.get("dc");
                        if (dcNode != null) {
                            balanceNode = dcNode.get("balance");
                            if (balanceNode != null) {
                                int balance = balanceNode.asInt();
                            }
                            nonceNode = dcNode.get("nonce");
                            if (nonceNode != null) {
                                int nonce = nonceNode.asInt();
                            }
                        }

                        JsonNode latNode = null;
                        JsonNode lonNode = null;
                        JsonNode speedNode = null;
                        JsonNode decodedNode = jsonNode.get("decoded");
                        if (decodedNode != null) {
                            JsonNode payloadNode = jsonNode.get("decoded").get("payload");
                            if (payloadNode != null) {
                                latNode = payloadNode.get("Lat");
                                lonNode = payloadNode.get("Lon");
                                speedNode = payloadNode.get("Speed");
                            }
                        }

                        int hotSpotsChannel = 0;
                        double hotSpotsFrecuency = 0;
                        long hotSpotsReportedAt = 0;
                        LocalDateTime reportedDateTime = null;
                        JsonNode hotspotsNode = jsonNode.get("hotspots");
                        if (hotspotsNode != null && hotspotsNode.isArray() && hotspotsNode.size() > 0) {
                            JsonNode hotspotNode = hotspotsNode.get(0);
                            JsonNode channelNode = hotspotNode.get("channel");
                            if (channelNode != null && channelNode.isInt()) {
                                hotSpotsChannel = channelNode.asInt();
                            }
                            JsonNode frequencyNode = hotspotNode.get("frequency");
                            if (frequencyNode != null && frequencyNode.isDouble()) {
                                 hotSpotsFrecuency = frequencyNode.asDouble();
                            }
                            JsonNode reportedAtNode = hotspotNode.get("reported_at ");
                            if (reportedAtNode != null) {
                                hotSpotsReportedAt = reportedAtNode.asLong();
                                 reportedDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(hotSpotsReportedAt), ZoneId.systemDefault());

                            }
                        }

                        // Проверяем наличие элементов и извлекаем значения
                        String appEui = (appEuiNode != null) ? appEuiNode.asText() : null;
                        int balance = (balanceNode != null) ? balanceNode.asInt() : 0;
                        int nonce = (nonceNode != null) ? nonceNode.asInt() : 0;
                        int channel = hotSpotsChannel;
                        double frequency = hotSpotsFrecuency;
                        LocalDateTime time = reportedDateTime;
                        String lat = (latNode != null) ? latNode.asText() : null;
                        String lon = (lonNode != null) ? lonNode.asText() : null;
                        String speed = (speedNode != null) ? speedNode.asText() : null;

                        // Используйте значения переменных по вашему усмотрению
                        if (appEui != null && balance != 0 && nonce != 0 && lat != null && lon != null && speed != null) {
                            System.out.println("app_eui: " + appEui);
                            System.out.println("balance: " + balance);
                            System.out.println("nonce: " + nonce);
                            System.out.println("Lat: " + lat);
                            System.out.println("Lon: " + lon);
                            System.out.println("Speed: " + speed);
                            System.out.println("channel: " + channel);
                            System.out.println("frequency: " + frequency);
                            System.out.println("time: " + time);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isJSONValid(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(json);
            return true;
        } catch (JsonParseException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

}
