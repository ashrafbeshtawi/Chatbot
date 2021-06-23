package de.dailab.oven.api;

import de.dailab.oven.api_common.chat.ChatResponse;
import de.dailab.oven.api_common.chat.request.ChatInputMessage;
import de.dailab.oven.api_common.chat.request.ChatRequest;
import de.dailab.oven.api_common.chat.request.ChatUserMatch;
import de.dailab.oven.api_common.error.ErrorResponse;
import de.dailab.oven.database.AbstractDatabaseTest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import zone.bot.vici.Language;
import zone.bot.vici.intent.IntentRequest.InputType;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ChatWSTest extends AbstractDatabaseTest {
    private static final String SEND_CHAT = "/ws-push/oven/chat/";
    private static final String SEND_PING = "/ws-push/oven/chat/ping";
    private static final String SUBSCRIBE_CHAT_REQUEST = "/ws-pull/oven/chat/request";
    private static final String SUBSCRIBE_CHAT_RESPONSE = "/ws-pull/oven/chat/response";
    private static final String SUBSCRIBE_CHAT_CONVERSATION = "/ws-pull/oven/chat/conversation";
    private static final String DEFAULT_USER = "/-1";
    private static final String HTTP_CHAT = "/oven/chat/";
    @Value("${local.server.port}")
    private int port;
    private String websocketUrl;
    private String httpUrl;
    private CompletableFuture<Object> completableFutureRequest;
    private CompletableFuture<ErrorResponse> completableFutureErrorResponse;
    private CompletableFuture<ChatResponse> completableFutureResponse;
    private CompletableFuture<LinkedHashMap> completableFutureConversationResponse;

    @Override
    public void initialize() {

    }

    public static void sendHTML(final String urlToRead, final String body) {
        try {
            sleep(5000);
            final URL url = new URL(urlToRead);
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            final String jsonInputString = body;
            final OutputStream os = conn.getOutputStream();
            final byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);


            final BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            final StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }


        } catch (final Exception e) {
        }

    }

    @Before
    public void setup() {
        this.completableFutureRequest = new CompletableFuture<>();
        this.completableFutureErrorResponse = new CompletableFuture<>();
        this.completableFutureResponse = new CompletableFuture<>();
        this.completableFutureConversationResponse = new CompletableFuture<>();
        this.websocketUrl = "ws://localhost:" + this.port + "/IntelliOven-Websocket";
        this.httpUrl = "http://localhost:" + this.port;
    }

    @Test
    public void C00_Get_Request_WS() throws InterruptedException, ExecutionException, TimeoutException {

        //request should be always send back to user

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(5, SECONDS);

        stompSession.subscribe(SUBSCRIBE_CHAT_REQUEST + DEFAULT_USER, new RequestHandler());
        final ChatRequest request = new ChatRequest();

        final ChatInputMessage inputMessage = new ChatInputMessage();
        inputMessage.setMessage("Hello");
        inputMessage.setProbability(1);
        final ChatInputMessage[] inputMessages = new ChatInputMessage[]{inputMessage};
        request.setChatInputMessages(inputMessages);
        request.setInputType(InputType.CHAT);

        final ChatUserMatch chatUserMatch = new ChatUserMatch();
        chatUserMatch.setUserID(-1);
        final ChatUserMatch[] chatUserMatches = new ChatUserMatch[]{chatUserMatch};
        request.setChatUserMatches(chatUserMatches);

        stompSession.send(SEND_CHAT, request);

        final Object response = this.completableFutureRequest.get(50, SECONDS);
        assertTrue(response.toString().contains("Hello"));

    }

    @Test
    public void C00_Get_Request_HTTP() throws InterruptedException, ExecutionException, TimeoutException {
        //request should be always send back to user

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(5, SECONDS);

        stompSession.subscribe(SUBSCRIBE_CHAT_REQUEST + DEFAULT_USER, new RequestHandler());

        sendHTML(this.httpUrl + "" + HTTP_CHAT,
                "{\n" +
                        "    \"chatInputMessages\": [\n" +
                        "        {\n" +
                        "            \"message\": \"Hallo Chatbot\",\n" +
                        "            \"language\": \"de\",\n" +
                        "            \"probability\": 1.0\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"chatUserMatches\": [\n" +
                        "        {\n" +
                        "            \"userID\": -1,\n" +
                        "            \"probability\": 1\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}"
        );

        final Object response = this.completableFutureRequest.get(30, SECONDS);
        assertTrue(response.toString().contains("Chat"));

    }

    @Test
    public void C00_Get_Response_WS() throws InterruptedException, ExecutionException, TimeoutException {

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(5, SECONDS);

        stompSession.subscribe(SUBSCRIBE_CHAT_RESPONSE + DEFAULT_USER, new ChatResponseHandler());
        final ChatRequest request = new ChatRequest();

        final ChatInputMessage inputMessage = new ChatInputMessage();
        inputMessage.setMessage("Hello");
        inputMessage.setProbability(1);
        inputMessage.setLanguage(Language.ENGLISH);
        final ChatInputMessage[] inputMessages = new ChatInputMessage[]{inputMessage};
        request.setChatInputMessages(inputMessages);

        final ChatUserMatch chatUserMatch = new ChatUserMatch();
        chatUserMatch.setUserID(-1);
        final ChatUserMatch[] chatUserMatches = new ChatUserMatch[]{chatUserMatch};
        request.setChatUserMatches(chatUserMatches);
        request.setInputType(InputType.CHAT);

        stompSession.send(SEND_CHAT, request);

        final ChatResponse response = this.completableFutureResponse.get(30, SECONDS);
        assertTrue(response.getResponse().contains("I"));
        assertTrue(response.getResponse().contains("you"));
        //assertEquals("Hello, I am happy to help you.", response.getResponse());
    }

    @Ignore
    @Test
    public void C00_Get_Response_HTTPNoUser() throws InterruptedException, ExecutionException, TimeoutException {

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(5, SECONDS);

        stompSession.subscribe(SUBSCRIBE_CHAT_RESPONSE + "/55", new ChatResponseHandler());

        sendHTML(this.httpUrl + "" + HTTP_CHAT,
                "{\n" +
                        "    \"chatInputMessages\": [\n" +
                        "        {\n" +
                        "            \"message\": \"Hallo Chatbot\",\n" +
                        "            \"language\": \"de\",\n" +
                        "            \"probability\": 1\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"chatUserMatches\": [\n" +
                        "        {\n" +
                        "            \"userID\": 55,\n" +
                        "            \"probability\": 1\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}"
        );

        final ChatResponse response = this.completableFutureResponse.get(30, SECONDS);
        assertEquals("Bitte authentifiziere dich oder erstelle einen neuen Account.", response.getResponse());
    }

    @Test
    public void C00_Get_Response_HTTP_User() throws InterruptedException, ExecutionException, TimeoutException {

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(5, SECONDS);

        stompSession.subscribe(SUBSCRIBE_CHAT_RESPONSE + "/69", new ChatResponseHandler());

        sendHTML(this.httpUrl + "" + HTTP_CHAT,
                "{\n" +
                        "    \"chatInputMessages\": [\n" +
                        "        {\n" +
                        "            \"message\": \"Hallo Chatbot!\",\n" +
                        "            \"language\": \"tr\",\n" +
                        "            \"probability\": 1\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"chatUserMatches\": [\n" +
                        "        {\n" +
                        "            \"userID\": 69,\n" +
                        "            \"probability\": 1\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}"
        );

        final ChatResponse response = this.completableFutureResponse.get(30, SECONDS);
        assertEquals("Sorunuzu tam anlayamadım. Öğrenebilmem için sorunuzu farklı bir şekilde ifade edebilir misiniz?", response.getResponse());
    }

    @Test
    public void C00_Get_Request_WS_User() throws InterruptedException, ExecutionException, TimeoutException {

        //request should be always send back to user

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(5, SECONDS);

        stompSession.subscribe(SUBSCRIBE_CHAT_REQUEST + "/69", new RequestHandler());
        final ChatRequest request = new ChatRequest();

        final ChatInputMessage inputMessage = new ChatInputMessage();
        inputMessage.setMessage("Hallo Test");
        inputMessage.setProbability(1);
        final ChatInputMessage[] inputMessages = new ChatInputMessage[]{inputMessage};
        request.setChatInputMessages(inputMessages);

        final ChatUserMatch chatUserMatch = new ChatUserMatch();
        chatUserMatch.setUserID(69);
        final ChatUserMatch[] chatUserMatches = new ChatUserMatch[]{chatUserMatch};
        request.setChatUserMatches(chatUserMatches);
        request.setInputType(InputType.CHAT);

        stompSession.send(SEND_CHAT, request);

        final Object response = this.completableFutureRequest.get(50, SECONDS);
        assertTrue(response.toString().contains("Hallo"));

    }

    @Test
    public void C00_Get_Request_WS_NegativeUserID() throws InterruptedException, ExecutionException, TimeoutException {

        //request should be always send back to user

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(5, SECONDS);

        stompSession.subscribe(SUBSCRIBE_CHAT_REQUEST + "/1", new RequestHandler());


        final String req = "{\n" +
                "    \"chatInputMessages\": [\n" +
                "        {\n" +
                "            \"message\": \"Hello\",\n" +
                "            \"language\": \"de\",\n" +
                "            \"probability\": 1.0\n" +
                "        }\n" +
                "    ],\n" +
                "    \"chatUserMatches\": [\n" +
                "        {\n" +
                "            \"userID\": 1,\n" +
                "            \"probability\": 1.0\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        final JSONParser parser = new JSONParser();
        try {
            sleep(2000);
            final JSONObject json = (JSONObject) parser.parse(req);
            stompSession.send(SEND_CHAT, json);
            final Object response = this.completableFutureRequest.get(50, SECONDS);
            assertTrue(response.toString().contains("Hello"));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void C01_Ping_OnlyOwnUserMessages() throws InterruptedException, ExecutionException, TimeoutException {

        //check if conversation of 200 is in 100

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(5, SECONDS);

        stompSession.subscribe(SUBSCRIBE_CHAT_CONVERSATION + "/100", new ChatConversationHandler());

        final ChatRequest request = new ChatRequest();

        final ChatInputMessage inputMessage = new ChatInputMessage();
        inputMessage.setMessage("Hello");
        inputMessage.setProbability(1);
        final ChatInputMessage[] inputMessages = new ChatInputMessage[]{inputMessage};
        request.setChatInputMessages(inputMessages);

        final ChatUserMatch chatUserMatch = new ChatUserMatch();
        chatUserMatch.setUserID(200);
        final ChatUserMatch[] chatUserMatches = new ChatUserMatch[]{chatUserMatch};
        request.setChatUserMatches(chatUserMatches);
        request.setInputType(InputType.CHAT);

        stompSession.send(SEND_CHAT, request);
        stompSession.send(SEND_PING + "/100", "");

        final LinkedHashMap response = this.completableFutureConversationResponse.get(30, SECONDS);
        //has to be emtpy because no request was send to this user
        assertEquals(new ArrayList(), response.get("conversationList"));
    }

    @Test
    public void C01_PingUser_ShowAllMessages() throws InterruptedException, ExecutionException, TimeoutException {

        //check if conversation of 100 is in 200

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(5, SECONDS);

        stompSession.subscribe(SUBSCRIBE_CHAT_CONVERSATION + "/101", new ChatConversationHandler());

        final String message = "{\n" +
                "    \"chatInputMessages\": [\n" +
                "        {\n" +
                "            \"message\": \"Hallo Chatbot!\",\n" +
                "            \"language\": \"tr\",\n" +
                "            \"probability\": 1\n" +
                "        }\n" +
                "    ],\n" +
                "    \"chatUserMatches\": [\n" +
                "        {\n" +
                "            \"userID\": 101,\n" +
                "            \"probability\": 1\n" +
                "        }\n" +
                "    ],\n" +
                "    \"inputType\": \"SPEECH\"\n" +
                "}";

        sendHTML(this.httpUrl + "" + HTTP_CHAT, message);

        final LinkedHashMap response = this.completableFutureConversationResponse.get(30, SECONDS);
        //assertTrue(response.get("conversationList").toString().contains("chatInputMessages=[{message=Hallo Chatbot!, language=tr, probability=1.0}]"));
        assertTrue(response.get("conversationList").toString().contains("chatUserMatches=[{userID=101, probability=1.0}]"));
    }

    private List<Transport> createTransportClient() {
        final List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }

    private class ErrorResponseHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(final StompHeaders stompHeaders) {
            return ErrorResponse.class;
        }

        @Override
        public void handleFrame(final StompHeaders stompHeaders, final Object o) {
            ChatWSTest.this.completableFutureErrorResponse.complete((ErrorResponse) o);
        }
    }

    private class ChatResponseHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(final StompHeaders stompHeaders) {
            return ChatResponse.class;
        }

        @Override
        public void handleFrame(final StompHeaders stompHeaders, final Object o) {
            ChatWSTest.this.completableFutureResponse.complete((ChatResponse) o);
        }
    }

    private class ChatConversationHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(final StompHeaders stompHeaders) {
            return LinkedHashMap.class;
        }

        @Override
        public void handleFrame(final StompHeaders stompHeaders, final Object o) {
            ChatWSTest.this.completableFutureConversationResponse.complete((LinkedHashMap) o);
        }
    }


    private class RequestHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(final StompHeaders stompHeaders) {
            return Object.class;
        }

        @Override
        public void handleFrame(final StompHeaders stompHeaders, final Object o) {
            ChatWSTest.this.completableFutureRequest.complete(o);
        }


    }

}
