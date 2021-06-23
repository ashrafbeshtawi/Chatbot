var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#table").show();
    } else {
        $("#table").hide();
    }
    $("#message").html("");
}

function connect() {
    var socket = new SockJS(window.location.origin + '/IntelliOven-Websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        //message requests
        stompClient.subscribe('/ws-pull/oven/chat/request/' + $("#userID").val(), function (request) {
            chatRequest(JSON.parse(request.body));
        });
        //message responses
        stompClient.subscribe('/ws-pull/oven/chat/response/' + $("#userID").val(), function (response) {
            chatResponse(JSON.parse(response.body));
        });
        //broadcast
        stompClient.subscribe('/ws-pull/oven/chat/response/0', function (response) {
            chatResponse(JSON.parse(response.body));
        });
        //whole conversation
        stompClient.subscribe('/ws-pull/oven/chat/conversation/' + $("#userID").val(), function (response) {
            chatConversation(JSON.parse(response.body));
        });
        //trigger ping to see the conversation
        stompClient.send("/ws-push/oven/chat/ping/" + $("#userID").val());

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

function searchDish() {
    stompClient.send("/ws-push/oven/chat/", {}, JSON.stringify(
        {
            "chatInputMessages":
                [
                    {
                        "message": $("#name").val(),
                        "language": $("#lang").val(),
                        "probability": 1,
                        "distance": 1
                    }
                ],
            "chatUserMatches":
                [
                    {
                        "userID": $("#userID").val(),
                        "probability": 1,
                        "distance": 1
                    }
                ]
        }
    ));
}


function chatRequest(message) {
    $("#message").append("<tr><td>Request: " + JSON.stringify(message) + "</td></tr>");
}

function chatResponse(message) {
    $("#message").append("<tr><td>Response: " + JSON.stringify(message) + "</td></tr>");
}

function chatConversation(messages) {
    $("#conversation").html("");
    messages.conversationList.forEach(function (message) {
        $("#conversation").append("<tr><td>" + JSON.stringify(message) + "</td></tr>");
    });
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
        searchDish();
    });
    $("#userID").change(function () {
        disconnect();
        $("#message").html("");
        $("#conversation").html("");
        connect();
    });
});
