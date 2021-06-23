package de.dailab.oven.api;

import de.dailab.oven.api_common.error.ErrorResponse;
import de.dailab.oven.api_common.recipe.RecipeResponse;
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
import static org.junit.Assert.assertTrue;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RecipeWSTest extends AbstractDatabaseTest {
    private static final String SEND_RECIPE_GET = "/ws-push/oven/recipe/get";
    private static final String SUBSCRIBE_RECIPE = "/ws-pull/oven/recipe";
    private static final String HTTP_RECIPE_GET = "/oven/recipe/get";
    @Value("${local.server.port}")
    private int port;
    private String websocketUrl;
    private String httpUrl;
    private CompletableFuture<RecipeResponse> completableFutureResponse;
    private CompletableFuture<ErrorResponse> completableFutureErrorResponse;


    @Override
    public void initialize() {

    }


    public static void getHTML(final String urlToRead, final String body) {
        try {
            sleep(1000);
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
        this.completableFutureResponse = new CompletableFuture<>();
        this.completableFutureErrorResponse = new CompletableFuture<>();
        this.websocketUrl = "ws://localhost:" + this.port + "/IntelliOven-Websocket";
        this.httpUrl = "http://localhost:" + this.port;
    }


    //TODO R00_Get_Request_HTTP

    //TODO check if recipe filter is empty, WS

    //TODO check if recipe filter is empty, HTTP

    @Test
    public void R00_Get_Response_WS_RecipeIdNoMatch_ContenBasedRec() throws InterruptedException, ExecutionException, TimeoutException {
//
//        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
//        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
//
//        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
//        }).get(1, SECONDS);
//
//        stompSession.subscribe(SUBSCRIBE_RECIPE + "/45", new ErrorResponseHandler());
//
//        final String req = "{" +
//                "\"userID\":45," +
//                "\"recipeFilter\":{\"recipeLanguages\":[\"de\"]," +
//                "\"recipeName\":\"chicken\"," +
//                "\"requiredCategories\":[\"VEGAN\"]," +
//                "\"possibleCategories\":[]," +
//                "\"excludedCategories\":[\"MEAT\"]," +
//                "\"requiredIngredients\":[\"cheese\"]," +
//                "\"possibleIngredients\":[\"mozerella\"]," +
//                "\"excludedIngredients\":[\"ham\"]," +
//                "\"possibleAuthors\":[\"John Doe\",\"Hendrik\"]," +
//                "\"excludedAuthors\":[\"Sahin\"]," +
//                "\"cookedWithin\":700.0," +
//                "\"isFoodLabel\":\"GREEN\"," +
//                "\"recipeId\":100," +
//                "\"maxNumberOfRecipesToParsePerLanguage\":100," +
//                "\"originialServings\":-1}," +
//                "\"contentBasedRecommendation\":true," +
//                "\"persons\":5" +
//                "}";
//
//        final JSONParser parser = new JSONParser();
//        try {
//            final JSONObject json = (JSONObject) parser.parse(req);
//            stompSession.send(SEND_RECIPE_GET, json);
//        } catch (final Exception e) {
//            e.printStackTrace();
//        }

//        final ErrorResponse response = this.completableFutureErrorResponse.get(30, SECONDS);
        //recommender fails...
//        System.out.println(response.getMessage());
//        assertTrue(response.getMessage().contains("Id 100 doesn't match to a recipe"));
    }

    @Test
    public void R00_Get_Response_WS_RecipeIdNoMatch_collRec() throws InterruptedException, ExecutionException, TimeoutException {

//        final WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
//        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
//
//        final StompSession stompSession = stompClient.connect(this.websocketUrl, new StompSessionHandlerAdapter() {
//        }).get(1, SECONDS);
//
//        stompSession.subscribe(SUBSCRIBE_RECIPE + "/45", new ErrorResponseHandler());
//
//        final String req = "{" +
//                "\"userID\":45," +
//                "\"recipeFilter\":{\"recipeLanguages\":[\"de\"]," +
//                "\"recipeName\":\"chicken\"," +
//                "\"requiredCategories\":[\"VEGAN\"]," +
//                "\"possibleCategories\":[]," +
//                "\"excludedCategories\":[\"MEAT\"]," +
//                "\"requiredIngredients\":[{\"name\": \"cheese\", \"language\": \"en\"}]," +
//                "\"possibleIngredients\":[{\"name\": \"mozerella\", \"language\": \"en\"}]," +
//                "\"excludedIngredients\":[{\"name\": \"ham\", \"language\": \"en\"}]," +
//                "\"possibleAuthors\":[\"John Doe\",\"Hendrik\"]," +
//                "\"excludedAuthors\":[\"Sahin\"]," +
//                "\"cookedWithin\":700.0," +
//                "\"isFoodLabel\":\"GREEN\"," +
//                "\"recipeId\":100," +
//                "\"maxNumberOfRecipesToParsePerLanguage\":100," +
//                "\"originialServings\":-1}," +
//                "\"collaborativeRecommendation\":true," +
//                "\"persons\":5" +
//                "}";
//
//        final JSONParser parser = new JSONParser();
//        try {
//            final JSONObject json = (JSONObject) parser.parse(req);
//            stompSession.send(SEND_RECIPE_GET, json);
//        } catch (final Exception e) {
//            e.printStackTrace();
//        }
//
//        final ErrorResponse response = this.completableFutureErrorResponse.get(30, SECONDS);
//        System.out.println(response.getMessage());
//        assertTrue(response.getMessage().contains("Id 100 doesn't match to a recipe"));
    }

    private List<Transport> createTransportClient() {
        final List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }

    private class ResponseHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(final StompHeaders stompHeaders) {
            return RecipeResponse.class;
        }

        @Override
        public void handleFrame(final StompHeaders stompHeaders, final Object o) {
            RecipeWSTest.this.completableFutureResponse.complete((RecipeResponse) o);
        }
    }

    private class ErrorResponseHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(final StompHeaders stompHeaders) {
            return ErrorResponse.class;
        }

        @Override
        public void handleFrame(final StompHeaders stompHeaders, final Object o) {
            RecipeWSTest.this.completableFutureErrorResponse.complete((ErrorResponse) o);
        }
    }

}
