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
        stompClient.subscribe('/ws-pull/oven/user', function (request) {
            showUsers(JSON.parse(request.body));
        });
        searchUser();
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

function searchUser() {
    stompClient.send("/ws-push/oven/user/get", {}, JSON.stringify(
        {
            "userID": $("#userID").val(),
            "userName": $("#userName").val()

        }
    ));
}

function showUsers(messages) {
    $("#users").html("");
    messages.userList.forEach(function (message) {
        $("#users").append("<tr><td>" + JSON.stringify(message) + "</td></tr>");
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
    $("#searchUser").click(function () {
        searchUser();
    });
});
