var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#fridge").show();
    } else {
        $("#fridge").hide();
    }
    $("#ingredients").html("");
}

function connect() {
    var socket = new SockJS(window.location.origin + '/IntelliOven-Websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/ws-pull/fridge', function (ingredientList) {
            showIngredients(JSON.parse(ingredientList.body));
        });
        removeIngredient()
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

function addIngredient() {
    stompClient.send("/ws-push/fridge/add", {}, JSON.stringify({'name': $("#nameAdd").val()}));
}

function removeIngredient() {
    stompClient.send("/ws-push/fridge/remove", {}, JSON.stringify({'name': $("#nameRemove").val()}));
}

function showIngredients(message) {
    $("#ingredients").html("");
    message.forEach(function (ingredient) {
        $("#ingredients").append("<tr><td>" + JSON.stringify(ingredient.name) + "</td></tr>");
    })
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
    $("#sendRemove").click(function () {
        removeIngredient();
    });
});