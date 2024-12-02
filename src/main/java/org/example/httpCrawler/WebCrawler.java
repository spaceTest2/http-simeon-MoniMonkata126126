package org.example.httpCrawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpHeaders;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

@Slf4j
public class WebCrawler {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Input URL: ");
        String inputUrl = scanner.nextLine();
        scanner.close();

        if (!isValidURL(inputUrl)) {
            log.warn("Invalid URL: {}", inputUrl);
            return;
        }

        try {
            crawl(inputUrl);
        } catch (Exception e) {
            log.error("Error when handling URL: {}", e.getMessage());
        }
    }

    private static boolean isValidURL(String url) {
        try {
            new URI(url).toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void crawl(String url) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();
        String statusMessage = handleStatusCode(statusCode);
        log.info("URL: {}", url);
        log.info("Status code: {}", statusCode);
        log.info("Status message: {}", statusMessage);

        if (statusCode == 200) {
            log.info("Page content: {}", response.body());
            HttpHeaders headers = response.headers();
            headers.map().forEach((key, values) -> {
                if ("set-cookie".equalsIgnoreCase(key)) {
                    values.forEach(value -> log.info("Cookie: {}", value));
                }
            });
        }

        saveResultToJson(url, statusCode);
    }

    private static String handleStatusCode(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 404 -> "Not Found";
            case 403 -> "Forbidden";
            case 500 -> "Internal Server Error";
            default -> "Unknown Status";
        };
    }

    private static void saveResultToJson(String url, int statusCode) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("url", url);
        result.put("status_code", statusCode);

        objectMapper.writeValue(new File("result.json"), result);
        log.info("Results written in result.json");
    }
}
