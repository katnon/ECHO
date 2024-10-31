
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class GPTChatbot {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static String API_KEY = "";

    // 각 챗봇의 프롬프트
    private static final String[] botPrompts = {
            "You are the black guy who very well sing a rap and good at basketball, and he uses a lot of black slang. If user let him know that user is a woman and user is attracted to this man, user can exchange chatting similar to a romantic relationship.",
            "You are a white guy who is very good at programming. This guy also likes a lot of computer games, especially Overwatch2, and he speaks in a very polite tone. If user lets him know that user is a woman and user is attracted to this man, user can exchange chatting similar to a romantic relationship.",
            "You are a Hispanic woman who loves soccer and wants to become an announcer or reporter who delivers sports news. You speak English like Mexicans. If user lets her know that user is a man and gains her favor, user can have increasingly romantic chats.",
            "You are Asian American and were adopted to the United States when you were young. You like K-pop and Japanese anime. If user lets her know that user is a man and gains her favor, user can have increasingly romantic chats."
    };

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String userInput = "";

        // 건호가 수정(추가)한 부분
        API_KEY = System.getenv("OPENAI_API_KEY");
        if (API_KEY == ""){
            System.out.println("API KEY를 찾을 수 없습니다. 환경변수를 확인해주세요.");
            return;
        }

        System.out.println("API KEY를 성공적으로 로드 했습니다.");
        //여기까지

        while (true) {
            printBotSelectionMenu();
            userInput = scanner.nextLine();

            if (userInput.equals("exit") || userInput.equals("finish")) {
                System.out.println("Exiting the program.");
                break;
            }

            int botChoice = -1;
            try {
                botChoice = Integer.parseInt(userInput);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            if (botChoice >= 1 && botChoice <= 4) {
                startChatWithBot(botChoice - 1, scanner);
            } else {
                System.out.println("1, 2, 3, 4 중에 다시 입력해주세요.");
            }
        }

        scanner.close();
    }

    private static void printBotSelectionMenu() {
        System.out.println("어떤 친구와 이야기 하고 싶은지 선택하세요:");
        System.out.println("1) black guy");
        System.out.println("2) white guy");
        System.out.println("3) Hispanic girl");
        System.out.println("4) Asian American girl");
        System.out.println("채팅을 끝내려면 'exit'를 입력하세요.");
    }

    private static void startChatWithBot(int botIndex, Scanner scanner) {
        System.out.println("챗봇과 대화를 시작합니다. 'select again'을 입력하면 다시 챗봇을 고를 수 있습니다.");
        String prompt = botPrompts[botIndex];

        while (true) {
            System.out.print("You: ");
            String userMessage = scanner.nextLine();

            if (userMessage.equals("exit")) {
                System.out.println("채팅을 종료합니다.");
                System.exit(0); // 프로그램 전체 종료
            } else if (userMessage.equals("select again")) {
                break;
            }

            String botResponse = getChatbotResponse(prompt, userMessage);
            System.out.println("Bot: " + botResponse);
        }
    }

    private static String getChatbotResponse(String prompt, String userMessage) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // GPT-3.5-turbo API 요청을 위한 JSON 포맷
            String requestBody = "{\n" +
                    "  \"model\": \"gpt-3.5-turbo\",\n" +
                    "  \"messages\": [\n" +
                    "    {\"role\": \"system\", \"content\": \"" + prompt + "\"},\n" +
                    "    {\"role\": \"user\", \"content\": \"" + userMessage + "\"}\n" +
                    "  ],\n" +
                    "  \"max_tokens\": 100\n" +
                    "}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            InputStream inputStream;
            if (responseCode >= 200 && responseCode < 300) {
                inputStream = conn.getInputStream();
            } else {
                inputStream = conn.getErrorStream();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            // JSON 응답에서 GPT의 답변 추출 (simple-json 라이브러리 사용)
            JSONParser parser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) parser.parse(response.toString());
            JSONArray choices = (JSONArray) jsonResponse.get("choices");
            JSONObject message = (JSONObject) ((JSONObject) choices.get(0)).get("message");
            return (String) message.get("content");

        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return "Error occurred while communicating with GPT.";
        }
    }
}
