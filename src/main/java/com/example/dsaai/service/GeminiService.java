package com.example.dsaai.service;

import com.example.dsaai.model.DsaQuestionResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String API_URL;

    private final RestTemplate restTemplate;

    public GeminiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public DsaQuestionResponse generateQuestion(String level) {

        String prompt =
                "Generate a unique DSA word problem suitable for competitive programming.\n" +
                "Difficulty: " + level + "\n" +
                "Respond ONLY in JSON with keys: title, problem_statement, constraints, example_input, example_output, topic.\n" +
                "Keep problem structured, realistic and logical.";

        JSONObject reqBody = new JSONObject()
                .put("contents", new JSONArray()
                        .put(new JSONObject()
                                .put("parts", new JSONArray()
                                        .put(new JSONObject().put("text", prompt))
                                )
                        )
                );

        String finalUrl = API_URL + "?key=" + apiKey;

                String rawResponse;
                try {
                        rawResponse = restTemplate.postForObject(finalUrl, reqBody.toString(), String.class);
                } catch (HttpClientErrorException e) {
                        String body = e.getResponseBodyAsString();
                        throw new IllegalStateException("Generative API request failed (status=" + e.getStatusCode() + ").\n" +
                                        "Response body: " + body + "\n" +
                                        "This usually means the configured model is not available or the request is invalid.\n" +
                                        "Run ListModels with your API key to see supported models and methods, then update `gemini.api.url` accordingly.");
                }

                JSONObject json;
                try {
                        json = new JSONObject(rawResponse);
                } catch (org.json.JSONException e) {
                        throw new IllegalStateException("Failed to parse response from Generative API as JSON.\n" +
                                        "Raw response:\n" + rawResponse + "\n" +
                                        "This usually means the API returned an error, HTML, or a different schema than expected.\n" +
                                        "Check `gemini.api.url`, your API key, and the model's supported methods (ListModels).", e);
                }

                String textResponse;
                try {
                        textResponse = json
                                        .getJSONArray("candidates")
                                        .getJSONObject(0)
                                        .getJSONObject("content")
                                        .getJSONArray("parts")
                                        .getJSONObject(0)
                                        .getString("text");
                } catch (org.json.JSONException e) {
                        throw new IllegalStateException("Response JSON didn't match expected structure (candidates[0].content.parts[0].text).\n" +
                                        "Full response:\n" + json.toString(2) + "\n" +
                                        "Adjust parsing based on the model/method used.", e);
                }

                JSONObject parsed;
                // sanitize model output: strip markdown/code fences and surrounding backticks
                String cleaned = textResponse == null ? "" : textResponse.trim();
                if (cleaned.startsWith("```")) {
                        // remove leading fence line (e.g. ```json) and trailing ``` if present
                        int firstNewline = cleaned.indexOf('\n');
                        if (firstNewline != -1) {
                                cleaned = cleaned.substring(firstNewline + 1).trim();
                        }
                        if (cleaned.endsWith("```")) {
                                cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
                        }
                }
                // remove single-line backticks
                if (cleaned.startsWith("`") && cleaned.endsWith("`")) {
                        cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
                }

                try {
                        parsed = new JSONObject(cleaned);
                } catch (org.json.JSONException e1) {
                        // try extracting the first JSON object block between the first '{' and the last '}'
                        int firstBrace = cleaned.indexOf('{');
                        int lastBrace = cleaned.lastIndexOf('}');
                        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                                String maybeJson = cleaned.substring(firstBrace, lastBrace + 1);
                                try {
                                        parsed = new JSONObject(maybeJson);
                                } catch (org.json.JSONException e2) {
                                        throw new IllegalStateException("The model output could not be parsed as JSON after sanitization.\n" +
                                                        "Sanitized output:\n" + cleaned + "\n" +
                                                        "Attempted extraction:\n" + maybeJson + "\n" +
                                                        "Ensure the prompt forces the model to respond with valid JSON (no surrounding text or markdown).", e2);
                                }
                        } else {
                                throw new IllegalStateException("The model output is not valid JSON as requested.\n" +
                                                "Model returned (sanitized):\n" + cleaned + "\n" +
                                                "Ensure the prompt forces the model to respond with valid JSON (and no extra text).", e1);
                        }
                }

                String title = getStringFrom(parsed, "title");
                String problemStatement = getStringFrom(parsed, "problem_statement");
                String constraintsStr = getStringFrom(parsed, "constraints");
                String exampleInput = getStringFrom(parsed, "example_input");
                String exampleOutput = getStringFrom(parsed, "example_output");
                String topic = getStringFrom(parsed, "topic");

                return new DsaQuestionResponse(
                                title,
                                problemStatement,
                                constraintsStr,
                                exampleInput,
                                exampleOutput,
                                topic
                );
    }

        private String getStringFrom(JSONObject obj, String key) {
                if (!obj.has(key) || obj.isNull(key)) return "";
                Object val = obj.get(key);
                if (val instanceof JSONArray) {
                        JSONArray arr = (JSONArray) val;
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < arr.length(); i++) {
                                if (i > 0) sb.append("\n");
                                Object item = arr.get(i);
                                sb.append(item == null ? "" : item.toString());
                        }
                        return sb.toString();
                }
                return obj.optString(key, "");
        }
}
