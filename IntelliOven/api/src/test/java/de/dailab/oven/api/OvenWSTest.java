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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OvenWSTest extends AbstractDatabaseTest {
    private static final String SEND_OVEN = "/ws-push/oven/oven";
    private static final String SUBSCRIBE_OVEN = "/ws-pull/oven/oven";
    @Value("${local.server.port}")
    private int port;
    private String websocketUrl;
    private CompletableFuture<Object> completableFuture;


    @Override
    public void initialize() throws Throwable {

    }

    @Before
    public void setup() {
        this.completableFuture = new CompletableFuture<>();
        this.websocketUrl = "ws://localhost:" + this.port + "/IntelliOven-Websocket";
    }


    @Test
    public void O01_GetOvenStatus() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(20, SECONDS);

        stompSession.subscribe(SUBSCRIBE_OVEN, new RequestHandler());

        this.completableFuture = new CompletableFuture<>();
        stompSession.send(SEND_OVEN + "/get", "");

        final Object response = this.completableFuture.get(20, SECONDS);
        assertTrue(response.toString().contains("CELSIUS"));
        assertTrue(response.toString().contains("lightOn"));
        assertTrue(response.toString().contains("off"));

    }

    @Test
    public void O02_SetProgram() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(20, SECONDS);

        stompSession.subscribe(SUBSCRIBE_OVEN, new RequestHandler());

        this.completableFuture = new CompletableFuture<>();

        final String req = "{\n" +
                "\"ovenMode\": \"clean\",\n" +
                "\"temperature\":{\n" +
                "\"temperatureUnit\": \"FAHRENHEIT\",\n" +
                "\"temp\": -20\n" +
                "}\n" +
                "}";

        final JSONParser parser = new JSONParser();
        try {
            final JSONObject json = (JSONObject) parser.parse(req);
            stompSession.send(SEND_OVEN + "/setProgram", json);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        final Object response = this.completableFuture.get(20, SECONDS);
        assertTrue(response.toString().contains("FAHRENHEIT"));
        assertTrue(response.toString().contains("-20"));
        assertTrue(response.toString().contains("off"));
    }

    @Test
    public void O02_SetProgramFail() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(20, SECONDS);

        stompSession.subscribe(SUBSCRIBE_OVEN, new RequestHandler());

        this.completableFuture = new CompletableFuture<>();
        final String req = "{\n" +
                "\"ovenMode\": \"notavaiable\",\n" +
                "\"temperature\":{\n" +
                "\"temperatureUnit\": \"FAHRENHEIT\",\n" +
                "\"temp\": -20\n" +
                "}\n" +
                "}";

        final JSONParser parser = new JSONParser();
        try {
            final JSONObject json = (JSONObject) parser.parse(req);
            stompSession.send(SEND_OVEN + "/setProgram", json);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        final Object response = this.completableFuture.get(20, SECONDS);
        System.out.println(response);
        assertTrue(response.toString().contains("error"));
        assertTrue(response.toString().contains("422"));
        assertTrue(response.toString().contains("true"));
    }

    private List<Transport> createTransportClient() {
        final List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }

    private class RequestHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(final StompHeaders stompHeaders) {
            return Object.class;
        }

        @Override
        public void handleFrame(final StompHeaders stompHeaders, final Object o) {
            OvenWSTest.this.completableFuture.complete(o);
        }
    }

}
