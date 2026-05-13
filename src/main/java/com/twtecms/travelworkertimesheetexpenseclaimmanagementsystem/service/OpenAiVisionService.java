package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.ReceiptAnalysisResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class OpenAiVisionService {

    private static final String OPENAI_RESPONSES_URL = "https://api.openai.com/v1/responses";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.vision.model:gpt-4.1-mini}")
    private String visionModel;

    public OpenAiVisionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public ReceiptAnalysisResponse analyzeReceipt(MultipartFile file, String category) {
        validateRequest(file);

        try {
            String dataUrl = createDataUrl(file);
            String requestBody = buildRequestBody(dataUrl, category);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_RESPONSES_URL))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("OpenAI Vision API returned status " + response.statusCode());
            }

            String outputText = extractOutputText(response.body());
            return parseAnalysis(outputText);
        } catch (IOException e) {
            throw new RuntimeException("Could not analyze receipt image", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Receipt analysis was interrupted", e);
        }
    }

    private void validateRequest(MultipartFile file) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("OPENAI_API_KEY is not configured on the backend");
        }

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("A receipt image is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new RuntimeException("AI receipt analysis supports image uploads only");
        }
    }

    private String createDataUrl(MultipartFile file) throws IOException {
        String contentType = file.getContentType() == null ? "image/jpeg" : file.getContentType();
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        return "data:" + contentType + ";base64," + base64Image;
    }

    private String buildRequestBody(String imageDataUrl, String category) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", visionModel);
        root.putObject("text").putObject("format").put("type", "json_object");

        ArrayNode input = root.putArray("input");
        ObjectNode message = input.addObject();
        message.put("role", "user");

        ArrayNode content = message.putArray("content");
        ObjectNode text = content.addObject();
        text.put("type", "input_text");
        text.put("text", buildPrompt(category));

        ObjectNode image = content.addObject();
        image.put("type", "input_image");
        image.put("image_url", imageDataUrl);
        image.put("detail", "high");

        return objectMapper.writeValueAsString(root);
    }

    private String buildPrompt(String category) {
        return """
                Read this South African receipt/slip for an expense claim category: %s.
                Extract only facts visible on the receipt. Return strict JSON only with:
                receiptTime: 24-hour HH:mm if visible, otherwise null.
                amount: final total amount in South African rand as a number, otherwise null.
                mealType: breakfast if time is before 12:00, lunch if time is from 12:00 to before 16:00, supper if time is 16:00 or later, otherwise null.
                rawText: short relevant receipt text used for the decision.
                message: one short sentence explaining what was found or what is missing.
                Do not include markdown.
                """.formatted(category == null || category.isBlank() ? "Unknown" : category);
    }

    private String extractOutputText(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode output = root.path("output");
        if (output.isArray()) {
            for (JsonNode outputItem : output) {
                JsonNode content = outputItem.path("content");
                if (!content.isArray()) {
                    continue;
                }
                for (JsonNode contentItem : content) {
                    JsonNode text = contentItem.path("text");
                    if (text.isTextual() && !text.asText().isBlank()) {
                        return text.asText();
                    }
                }
            }
        }

        JsonNode outputText = root.path("output_text");
        if (outputText.isTextual() && !outputText.asText().isBlank()) {
            return outputText.asText();
        }

        throw new RuntimeException("OpenAI Vision API response did not include receipt analysis text");
    }

    private ReceiptAnalysisResponse parseAnalysis(String outputText) throws IOException {
        JsonNode root = objectMapper.readTree(stripCodeFence(outputText));
        ReceiptAnalysisResponse response = new ReceiptAnalysisResponse();
        response.setReceiptTime(normalizeReceiptTime(root.path("receiptTime")));
        response.setAmount(root.path("amount").isNumber() ? root.path("amount").asDouble() : null);
        response.setMealType(root.path("mealType").isTextual() ? root.path("mealType").asText() : null);
        response.setRawText(root.path("rawText").isTextual() ? root.path("rawText").asText() : null);
        response.setMessage(root.path("message").isTextual() ? root.path("message").asText() : "Receipt analysis completed.");
        return response;
    }

    private String normalizeReceiptTime(JsonNode receiptTimeNode) {
        if (!receiptTimeNode.isTextual() || receiptTimeNode.asText().isBlank()) {
            return null;
        }

        String value = receiptTimeNode.asText().trim();
        try {
            return LocalTime.parse(value).format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception ignored) {
            return value;
        }
    }

    private String stripCodeFence(String value) {
        String trimmed = value.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }

        return trimmed
                .replaceFirst("^```(?:json)?\\s*", "")
                .replaceFirst("\\s*```$", "")
                .trim();
    }
}
