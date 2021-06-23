var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#recipe").show();
    } else {
        $("#recipe").hide();
    }
    $("#ovenBody").html("");
}

function connect() {
    var socket = new SockJS(window.location.origin + '/IntelliOven-Websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/ws-pull/oven/recipe/' + $("#userID").val(), function (ingredientList) {
            getReturn(JSON.parse(ingredientList.body));
        });
    });
}

connect();

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function getReturn(message) {

    $("#ovenBody").html("");

    if (!message.error) {

        $("#ovenBody").append("<tr><td>" + message.recipeList.length + " Recipes found.</td></tr>");

        message.recipeList.forEach(function (recipe) {
            $("#ovenBody").append("<tr><td>" + JSON.stringify(recipe).replace(/,/g, " , ") + "</td></tr>");
        })
    } else {
        $("#ovenBody").append("<tr><td>Error: " + message.message + "</td></tr>");
    }
}

function addIngredient() {
    stompClient.send("/ws-push/oven/recipe/get", {}, JSON.stringify({
        'userID': $("#userID").val(),
        'recipeFilter': {
            'recipeName': $("#nameAdd").val(),
            "recipeLanguages": [
                $("#lang").val()
            ]
        },
        "contentBasedRecommendation": $("#recommend").val() === "content",
        "collaborativeRecommendation": $("#recommend").val() === "collaborative"
    }));
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#connect").click(function () {
        connect();
    });
    $("#disconnect").click(function () {
        disconnect();
    });
    $("#sendAdd").click(function () {
        addIngredient();
    });
    $("#userID").change(function () {
        disconnect();
        $("#ovenBody").html("");
        connect();
    });
});
