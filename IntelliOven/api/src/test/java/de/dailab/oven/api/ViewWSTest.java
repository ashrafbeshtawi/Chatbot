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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ViewWSTest extends AbstractDatabaseTest {
    private static final String SEND_VIEW = "/ws-push/oven/view";
    private static final String SUBSCRIBE_VIEW = "/ws-pull/oven/view";
    private static final String DEFAULT_USER = "/-100";
    @Value("${local.server.port}")
    private int port;
    private String websocketUrl;
    private String httpUrl;
    private CompletableFuture<LinkedHashMap> completableFutureConversation;

    @Override
    public void initialize() throws Throwable {

    }

    public static void sendHTML(final String method, final String urlToRead, final String body) {
        try {
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
        this.completableFutureConversation = new CompletableFuture<>();
        this.websocketUrl = "ws://localhost:" + this.port + "/IntelliOven-Websocket";
        this.httpUrl = "http://localhost:" + this.port;
    }

    //get Empty View (*)

    //Navigation Test on empty View (*)

    //weird navigation (*)

    //Change to Recipe View (*)

    //fill Recipe via RecipeAPI (*)
    // --> check if RecipeView  is filled (*)

    //navigate up and down on list --> same element (*)
    //go right twice (*)
    //navigate up and down (*)
    //go left twice (*)

    //change to ChatView

    //fill Chat via chatAPI
    //--> check if chatview is filled
    //navigate in all directions


    @Test
    public void V01_GET_EMPTY_VIEW() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(20, SECONDS);

        stompSession.subscribe(SUBSCRIBE_VIEW + DEFAULT_USER, new RequestHandler());

        stompSession.send(SEND_VIEW + "/get" + DEFAULT_USER, "");

        final LinkedHashMap response = this.completableFutureConversation.get(20, SECONDS);
        assertEquals("{conversationList=[]}", response.toString());
    }

    @Test
    public void V01_NAVIGATION_NORMAL_WEIRD_EMPTY_VIEW() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {
        final String[] navigations = {"up", "down", "left", "right", "weird"};

        for (final String navigation : navigations) {

            final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());

            final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
            }).get(20, SECONDS);

            stompSession.subscribe(SUBSCRIBE_VIEW + DEFAULT_USER, new RequestHandler());

            stompSession.send(SEND_VIEW + "/navigation/" + navigation + DEFAULT_USER, "");

            assertTrue(stompSession.isConnected());
        }
    }

//    @Test
    public void V03_4_NAVIGATE_THROUGH_RECIPE_VIEW() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        final long user = 83;

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(5, SECONDS);

        stompSession.subscribe(SUBSCRIBE_VIEW + "/" + user, new RequestHandler());

        stompSession.send(SEND_VIEW + "/change/" + user + "/1", "");

        LinkedHashMap response = this.completableFutureConversation.get(20, SECONDS);
        System.out.println(response);
        assertTrue(response.toString().contains("recipeList=[]"));
        assertTrue(response.toString().contains("selection="));
        assertTrue(response.get("selection").toString().contains("recipeID=-1"));
        assertTrue(response.get("selection").toString().contains("recipeIndex=-1"));
        assertTrue(response.get("selection").toString().contains("recipeDetailListIndex=-1"));
        assertTrue(response.get("selection").toString().contains("recipeDetailJSONKey="));
        assertTrue(response.get("selection").toString().contains("recipeDetailIndex=-1"));

        //send to RecipeAPI
        this.completableFutureConversation = new CompletableFuture<>();
        final String message = "{\n" +
                "                   \"userID\":" + user + ",\n" +
                "                   \"recipeFilter\": {\n" +
                "                       \"recipeName\":\"c\",\n" +
                "                       \"recipeLanguages\":[\"tr\"],\n" +
                "                       \"maxNumberOfRecipesToParsePerLanguage\":2\n" +
                "                   },\n" +
                "                    \"persons\": 5\n" +
                "                }";
        sendHTML("POST", this.httpUrl + "/oven/recipe/get", message);

        response = this.completableFutureConversation.get(30, SECONDS);
        //We have to find 3 Dishes. If Test fails please change maxNumberOfRecipesToParsePerLanguage to 1-3
        System.out.println(response);
        assertEquals(2, response.toString().split("categories=").length - 1);
        assertTrue(response.get("selection").toString().contains("recipeIndex=0"));
        assertTrue(response.get("selection").toString().contains("recipeDetailIndex=-1"));
        assertTrue(response.get("selection").toString().contains("recipeDetailJSONKey="));
        assertTrue(response.get("selection").toString().contains("recipeDetailListIndex=-1"));

        //Navigate up
        this.completableFutureConversation = new CompletableFuture<>();
        sendHTML("POST", this.httpUrl + "/oven/view/navigation/up/" + user, "");

        //should be other ID
        response = this.completableFutureConversation.get(30, SECONDS);
        assertFalse(response.get("selection").toString().contains("recipeIndex=0"));
        assertTrue(response.get("selection").toString().contains("recipeDetailIndex=-1"));

        //navigate down
        this.completableFutureConversation = new CompletableFuture<>();
        sendHTML("POST", this.httpUrl + "/oven/view/navigation/down/" + user, "");

        //should be as before
        response = this.completableFutureConversation.get(20, SECONDS);
        assertTrue(response.get("selection").toString().contains("recipeIndex=0"));
        assertTrue(response.get("selection").toString().contains("recipeDetailIndex=-1"));

        //go right twice
        this.completableFutureConversation = new CompletableFuture<>();
        sendHTML("POST", this.httpUrl + "/oven/view/navigation/right/" + user, "");
        sendHTML("POST", this.httpUrl + "/oven/view/navigation/right/" + user, "");

        //should be a detailed view (0) of recipeIndex 0. (name)
        response = this.completableFutureConversation.get(20, SECONDS);
        assertTrue(response.get("selection").toString().contains("recipeIndex=0"));
        assertTrue(response.get("selection").toString().contains("recipeDetailIndex=0"));
        assertTrue(response.get("selection").toString().contains("recipeDetailJSONKey=name"));
        assertTrue(response.get("selection").toString().contains("recipeDetailListIndex=-1"));


        //set selection to ID 1
        sleep(1000);
        this.completableFutureConversation = new CompletableFuture<>();
        sendHTML("PUT", this.httpUrl + "/oven/view/set/1/" + user, "");

        //should be a detailed view (0) of recipeIndex 1.
        response = this.completableFutureConversation.get(20, SECONDS);
        assertTrue(response.get("selection").toString().contains("recipeIndex=1"));
        assertTrue(response.get("selection").toString().contains("recipeDetailIndex=0"));
        assertTrue(response.get("selection").toString().contains("recipeDetailJSONKey=name"));

        //set selection (back) to ID 0
        this.completableFutureConversation = new CompletableFuture<>();
        sendHTML("PUT", this.httpUrl + "/oven/view/set/0/" + user, "");

        //should be a detailed view (0) of recipeIndex 1.
        response = this.completableFutureConversation.get(20, SECONDS);
        assertTrue(response.get("selection").toString().contains("recipeIndex=0"));
        assertTrue(response.get("selection").toString().contains("recipeDetailIndex=0"));
        assertTrue(response.get("selection").toString().contains("recipeDetailJSONKey=name"));

        //Navigate up
        this.completableFutureConversation = new CompletableFuture<>();
        sendHTML("POST", this.httpUrl + "/oven/view/navigation/up/" + user, "");

        //should be other ID
        response = this.completableFutureConversation.get(20, SECONDS);
        assertTrue(response.get("selection").toString().contains("recipeIndex=0"));
        assertTrue(response.get("selection").toString().contains("recipeDetailIndex=2"));
        assertTrue(response.get("selection").toString().contains("recipeDetailJSONKey=instructions"));
        assertTrue(response.get("selection").toString().contains("recipeDetailListIndex=-1"));

        //two right to see the DetailListView
        this.completableFutureConversation = new CompletableFuture<>();
        sendHTML("POST", this.httpUrl + "/oven/view/navigation/right/" + user, "");
        sendHTML("POST", this.httpUrl + "/oven/view/navigation/right/" + user, "");

        //should be a DetailedListView (0) of recipeIndex 0., first Listindex (see first instruction step)
        response = this.completableFutureConversation.get(20, SECONDS);
        assertTrue(response.get("selection").toString().contains("recipeIndex=0"));
        assertTrue(response.get("selection").toString().contains("recipeDetailIndex=2"));
        assertTrue(response.get("selection").toString().contains("recipeDetailJSONKey=instructions"));
        assertTrue(response.get("selection").toString().contains("recipeDetailListIndex=0"));

        //Navigate up twice - should be another step
        sleep(1000);
        this.completableFutureConversation = new CompletableFuture<>();
        sendHTML("POST", this.httpUrl + "/oven/view/navigation/up/" + user, "");
        sendHTML("POST", this.httpUrl + "/oven/view/navigation/up/" + user, "");

        //should be a DetailedListView (0) of recipeIndex 0., something Listindex
        response = this.completableFutureConversation.get(20, SECONDS);
        assertTrue(response.get("selection").toString().contains("recipeIndex=0"));
        assertTrue(response.get("selection").toString().contains("recipeDetailIndex=2"));
        assertTrue(response.get("selection").toString().contains("recipeDetailJSONKey=instructions"));
        assertFalse(response.get("selection").toString().contains("recipeDetailListIndex=0"));

        //set selection to ID 1
        sleep(1000);
        this.completableFutureConversation = new CompletableFuture<>();
        sendHTML("PUT", this.httpUrl + "/oven/view/set/1/" + user, "");

        //should be a detailedListview (0) of recipeIndex 1., instructions step 0
        response = this.completableFutureConversation.get(20, SECONDS);
        assertTrue(response.get("selection").toString().contains("recipeIndex=1"));
        assertTrue(response.get("selection").toString().contains("recipeDetailIndex=2"));
        assertTrue(response.get("selection").toString().contains("recipeDetailJSONKey=instructions"));
        assertTrue(response.get("selection").toString().contains("recipeDetailListIndex=0"));

        //set back to 0
        this.completableFutureConversation = new CompletableFuture<>();
        sendHTML("PUT", this.httpUrl + "/oven/view/set/0/" + user, "");

        //should be a detailedListview (0) of recipeIndex 1., instructions step 0
        response = this.completableFutureConversation.get(20, SECONDS);
        assertTrue(response.get("selection").toString().contains("recipeIndex=0"));
        assertTrue(response.get("selection").toString().contains("recipeDetailIndex=2"));
        assertTrue(response.get("selection").toString().contains("recipeDetailJSONKey=instructions"));
        assertTrue(response.get("selection").toString().contains("recipeDetailListIndex=0"));

        //Navigate down twice - should be the 3. step (index 2)
        this.completableFutureConversation = new CompletableFuture<>();
        sendHTML("POST", this.httpUrl + "/oven/view/navigation/down/" + user, "");
        sendHTML("POST", this.httpUrl + "/oven/view/navigation/down/" + user, "");

        //should be a DetailedListView (0) of recipeIndex 0., something Listindex
        response = this.completableFutureConversation.get(20, SECONDS);
        assertTrue(response.get("selection").toString().contains("recipeIndex=0"));
        assertTrue(response.get("selection").toString().contains("recipeDetailIndex=2"));
        assertTrue(response.get("selection").toString().contains("recipeDetailJSONKey=instructions"));
        assertFalse(response.get("selection").toString().contains("recipeDetailListIndex=2"));

        //go left
        sleep(1000);
        this.completableFutureConversation = new CompletableFuture<>();
        sendHTML("POST", this.httpUrl + "/oven/view/navigation/left/" + user, "");

        //should be a DetailedView (0) of recipeIndex 0.
        response = this.completableFutureConversation.get(20, SECONDS);
        assertTrue(response.get("selection").toString().contains("recipeIndex=0"));
        assertTrue(response.get("selection").toString().contains("recipeDetailIndex=2"));
        assertTrue(response.get("selection").toString().contains("recipeDetailJSONKey=instructions"));
        assertTrue(response.get("selection").toString().contains("recipeDetailListIndex=-1"));

        //go up, we are at ingredients (the middle)
        this.completableFutureConversation = new CompletableFuture<>();
        sendHTML("POST", this.httpUrl + "/oven/view/navigation/up/" + user, "");

        //should be a DetailedView (0) of recipeIndex 0.
        response = this.completableFutureConversation.get(20, SECONDS);
        assertTrue(response.get("selection").toString().contains("recipeIndex=0"));
        assertTrue(response.get("selection").toString().contains("recipeDetailIndex=1"));
        assertTrue(response.get("selection").toString().contains("recipeDetailJSONKey=ingredients"));
        assertTrue(response.get("selection").toString().contains("recipeDetailListIndex=-1"));

        //go down twice (we are at name again)
        sendHTML("POST", this.httpUrl + "/oven/view/navigation/down/" + user, "");
        sleep(1000);
        this.completableFutureConversation = new CompletableFuture<>();
        sendHTML("POST", this.httpUrl + "/oven/view/navigation/down/" + user, "");

        //should be a DetailedView (0) of recipeIndex 0.
        response = this.completableFutureConversation.get(20, SECONDS);
        System.out.println(response.get("selection"));
        assertTrue(response.get("selection").toString().contains("recipeIndex=0"));
        assertTrue(response.get("selection").toString().contains("recipeDetailIndex=0"));
        assertTrue(response.get("selection").toString().contains("recipeDetailJSONKey=name"));
        assertTrue(response.get("selection").toString().contains("recipeDetailListIndex=-1"));


        //go left twice
        this.completableFutureConversation = new CompletableFuture<>();
        sendHTML("POST", this.httpUrl + "/oven/view/navigation/left/" + user, "");
        sendHTML("POST", this.httpUrl + "/oven/view/navigation/left/" + user, "");

        //should be normal view of Recipe 0
        response = this.completableFutureConversation.get(20, SECONDS);
        assertTrue(response.get("selection").toString().contains("recipeIndex=0"));
        assertTrue(response.get("selection").toString().contains("recipeDetailIndex=-1"));

        //set selection to ID 1
        sleep(1000);
        this.completableFutureConversation = new CompletableFuture<>();
        sendHTML("PUT", this.httpUrl + "/oven/view/set/1/" + user, "");

        //should be a ListView (0) of recipeIndex 1.
        response = this.completableFutureConversation.get(20, SECONDS);
        assertTrue(response.get("selection").toString().contains("recipeIndex=1"));
        assertTrue(response.get("selection").toString().contains("recipeDetailIndex=-1"));
        assertTrue(response.get("selection").toString().contains("recipeDetailJSONKey="));

    }

    @Test
    public void V04() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        final long user = 160;

        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
        }).get(20, SECONDS);

        stompSession.subscribe(SUBSCRIBE_VIEW + DEFAULT_USER, new RequestHandler());

        stompSession.send(SEND_VIEW + "/set/0" + DEFAULT_USER, "");

        final Object response = this.completableFutureConversation.getNow(null);
        assertEquals(null, response);
    }


    private List<Transport> createTransportClient() {
        final List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }

    private class RequestHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(final StompHeaders stompHeaders) {
            return LinkedHashMap.class;
        }

        @Override
        public void handleFrame(final StompHeaders stompHeaders, final Object o) {
            ViewWSTest.this.completableFutureConversation.complete((LinkedHashMap) o);
        }
    }

}
