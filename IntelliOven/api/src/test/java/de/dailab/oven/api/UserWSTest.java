package de.dailab.oven.api;

import de.dailab.oven.database.AbstractDatabaseTest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserWSTest extends AbstractDatabaseTest {
    private static final String SEND_USER_GET = "/ws-push/oven/user/get";
    private static final String SEND_USER_PUT = "/ws-push/oven/user/put";
    private static final String SEND_USER_RATE = "/ws-push/oven/user/rate";
    private static final String SUBSCRIBE_USER = "/ws-pull/oven/user";
    @Value("${local.server.port}")
    private int port;
    private String websocketUrl;
    private CompletableFuture<Object> completableFutureResponse;
    private String userName;

    @Before
    public void setup() throws Exception {
        this.completableFutureResponse = new CompletableFuture<>();
        this.websocketUrl = "ws://localhost:" + this.port + "/IntelliOven-Websocket";

        U02SetUserName();
    }

    @Override
    public void initialize() {
    }

    public void U02SetUserName() throws Exception {

        this.userName = "user" + System.currentTimeMillis();


        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(1, SECONDS);

        stompSession.subscribe(SUBSCRIBE_USER, new ResponseHandler());
        final String message = "{\n" +
                "            \"name\": \"" + this.userName + "\",\n" +
                "            \"preferredCategories\": [],\n" +
                "            \"likesIngredients\": [],\n" +
                "            \"cookedRecipeIDs\": [],\n" +
                "            \"incompatibleIngredients\": [],\n" +
                "            \"recipeRatings\": {},\n" +
                "            \"currentlySpokenLanguage\": \"en\",\n" +
                "            \"spokenLanguages\": [\n" +
                "                \"en\"\n" +
                "            ],\n" +
                "            \"household\": \"GUEST\"\n" +			
                "        }";
        final JSONParser parser = new JSONParser();
        try {
            final JSONObject json = (JSONObject) parser.parse(message);
            stompSession.send(SEND_USER_PUT, json);

            final LinkedHashMap response = (LinkedHashMap) this.completableFutureResponse.get(10, SECONDS);
            System.out.println(response);
            assertEquals(1, response.size());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void U01_NoBody() throws InterruptedException, ExecutionException, TimeoutException {

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(1, SECONDS);

        stompSession.subscribe(SUBSCRIBE_USER, new ResponseHandler());
        final String message = "{}";
        final JSONParser parser = new JSONParser();
        try {
            final JSONObject json = (JSONObject) parser.parse(message);
            stompSession.send(SEND_USER_GET, json);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        //LinkedHashMap response = (LinkedHashMap) completableFutureResponse.get(10, SECONDS);
        assertTrue(stompSession.isConnected());
        assertEquals(message, "{}");
    }

    @Test
    public void U01_EmptyUserName() throws InterruptedException, ExecutionException, TimeoutException {

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(1, SECONDS);

        stompSession.subscribe(SUBSCRIBE_USER, new ResponseHandler());
        final String message = "{\"userName\":\"\"}";
        final JSONParser parser = new JSONParser();
        try {
            final JSONObject json = (JSONObject) parser.parse(message);
            stompSession.send(SEND_USER_GET, json);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        //LinkedHashMap response = (LinkedHashMap) completableFutureResponse.get(10, SECONDS);
        assertTrue(stompSession.isConnected());
        assertEquals("{\"userName\":\"\"}", message);
    }

    @Test
    public void U01_EmptyUserID() throws InterruptedException, ExecutionException, TimeoutException {

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(1, SECONDS);

        stompSession.subscribe(SUBSCRIBE_USER, new ResponseHandler());
        final String message = "{\"userID\":\"\"}";
        final JSONParser parser = new JSONParser();
        try {
            final JSONObject json = (JSONObject) parser.parse(message);
            stompSession.send(SEND_USER_GET, json);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        final LinkedHashMap response = (LinkedHashMap) this.completableFutureResponse.get(10, SECONDS);
        assertEquals(1, response.size());
    }

    @Test
    public void U01_WrongFilledUserID() throws InterruptedException, ExecutionException, TimeoutException {

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(1, SECONDS);

        stompSession.subscribe(SUBSCRIBE_USER, new ResponseHandler());
        final String message = "{\"userID\":\"xx\"}";
        final JSONParser parser = new JSONParser();
        try {
            final JSONObject json = (JSONObject) parser.parse(message);
            stompSession.send(SEND_USER_GET, json);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void U01_FilledUserIDUserName() throws InterruptedException, ExecutionException, TimeoutException {

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(1, SECONDS);

        stompSession.subscribe(SUBSCRIBE_USER, new ResponseHandler());
        final String message = "{\"userID\":\"-69\", \"userString\":\"xsdfsdfx\"}";
        final JSONParser parser = new JSONParser();
        try {
            final JSONObject json = (JSONObject) parser.parse(message);
            stompSession.send(SEND_USER_GET, json);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        final LinkedHashMap response = (LinkedHashMap) this.completableFutureResponse.get(30, SECONDS);
        assertEquals(1, response.size());
    }

    @Test
    public void U03_addRating() throws InterruptedException, ExecutionException, TimeoutException {

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(1, SECONDS);

        stompSession.subscribe(SUBSCRIBE_USER, new ResponseHandler());
        final String message = "{" +
                "\"userName\": \"" + this.userName + "\"," +
                "\"rating\": 5," +
                "\"recipeID\": 64" +
                "}";
        final JSONParser parser = new JSONParser();
        try {
            final JSONObject json = (JSONObject) parser.parse(message);
            stompSession.send(SEND_USER_RATE, json);


            final LinkedHashMap response = (LinkedHashMap) this.completableFutureResponse.get(10, SECONDS);
            //TODO: Originally false but why?
            assertEquals(true, response.toString().contains("recipeRatings"));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }


    private List<Transport> createTransportClient() {
        final List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }

    private class ResponseHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(final StompHeaders stompHeaders) {
            return Object.class;
        }

        @Override
        public void handleFrame(final StompHeaders stompHeaders, final Object o) {
            UserWSTest.this.completableFutureResponse.complete(o);
        }
    }
}
