package de.dailab.oven.api;

import de.dailab.oven.database.AbstractDatabaseTest;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ManageWSTest extends AbstractDatabaseTest {
    private static final String SEND_MANAGE = "/ws-push/oven/manage";
    private static final String SUBSCRIBE_MANAGE = "/ws-pull/oven/manage/0";
    @Value("${local.server.port}")
    private int port;
    private String websocketUrl;
    private String httpUrl;
    private CompletableFuture<Object> completableFuture;


    @Override
    public void initialize() throws Throwable {

    }


    public static void sendHTML(final String method, final String urlToRead, final String body) {
        try {
            sleep(1000);
            final URL url = new URL(urlToRead);
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
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
        this.completableFuture = new CompletableFuture<>();
        this.websocketUrl = "ws://localhost:" + this.port + "/IntelliOven-Websocket";
        this.httpUrl = "http://localhost:" + this.port;
    }


    @Test
    public void MV01_VOLUME_Allowed() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        final String[] commands = {"UP", "DOWN", "MUTE", "UNMUTE"};
        for (final String command : commands) {
            sleep(1000);
            final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());

            final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
            }).get(20, SECONDS);

            stompSession.subscribe(SUBSCRIBE_MANAGE, new RequestHandler());

            this.completableFuture = new CompletableFuture<>();
            stompSession.send(SEND_MANAGE + "/volume/command/" + command, "");

            final Object response = this.completableFuture.get(20, SECONDS);
            assertEquals("{message=" + command + ", responseType=VOLUME}", response.toString());
        }
    }

    @Test
    public void MV01_VOLUME_NotCommand() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(20, SECONDS);

        stompSession.subscribe(SUBSCRIBE_MANAGE, new RequestHandler());

        stompSession.send(SEND_MANAGE + "/volume/command/upp", "");

        final Object response = this.completableFuture.getNow(0);
        assertEquals(0, response);
    }

    @Test
    public void MV02_VOLUME_SET_ALLOWED() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(20, SECONDS);

        stompSession.subscribe(SUBSCRIBE_MANAGE, new RequestHandler());

        stompSession.send(SEND_MANAGE + "/volume/set/50", "");

        final Object response = this.completableFuture.getNow(0);
        assertEquals(0, response);
    }

    @Test
    public void MV01_VOLUME_SET_NotAllowed_String() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(20, SECONDS);

        stompSession.subscribe(SUBSCRIBE_MANAGE, new RequestHandler());

        stompSession.send(SEND_MANAGE + "/volume/set/50L", "");

        final Object response = this.completableFuture.getNow(0);
        assertEquals(0, response);
    }

    @Test
    public void MV01_VOLUME_SET_NotAllowed_Int() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(20, SECONDS);

        stompSession.subscribe(SUBSCRIBE_MANAGE, new RequestHandler());

        stompSession.send(SEND_MANAGE + "/volume/set/101", "");

        final Object response = this.completableFuture.getNow(0);
        assertEquals(0, response);
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
            ManageWSTest.this.completableFuture.complete(o);
        }
    }

}
