var stompClient = null;
var recipeLength = -1;
var currentMark = -1;
var viewed = true;

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
        stompClient.subscribe('/ws-pull/oven/view/' + $("#userID").val(), function (request) {
            showView(JSON.parse(request.body));
        });
        //navigationString
        stompClient.subscribe('/ws-pull/oven/navigation/' + $("#userID").val(), function (request) {
        });
        //trigger ping to see the View
        stompClient.send("/ws-push/oven/view/get/" + $("#userID").val());

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


function showView(messages) {
    $("#viewBody").html("");

    if (messages.conversationList !== undefined) {
        //ConversationView
        recipeLength = -1;
        messages.conversationList.forEach(function (message) {
            $("#viewBody").append("<tr><td>" + JSON.stringify(message).replace(/,/g, " , ") + "</td></tr>");

        });
    } else {
        //recipeView
        console.log(messages.selection)
        recipeLength = messages.recipeList.length;


        if (messages.selection.recipeDetailListIndex !== -1) {
            //DETAILLISTVIEW

            selectedRecipeIndex = messages.selection.recipeIndex;
            selectedDetailKey = messages.selection.recipeDetailJSONKey;
            seletedDetailListIndex = messages.selection.recipeDetailListIndex;

            $("#viewBody").append("<tr><td> DetailListView:</td></tr>");
            $("#viewBody").append("<tr><td bgcolor='#FFBBBB'> " + JSON.stringify(messages.recipeList[selectedRecipeIndex][selectedDetailKey][seletedDetailListIndex]) + "</td></tr>");

        } else if (messages.selection.recipeDetailIndex !== -1) {
            //DETAILVIEW

            selectedRecipeIndex = messages.selection.recipeIndex;
            selectedDetailKey = messages.selection.recipeDetailJSONKey;

            $("#viewBody").append("<tr><td> Detail View:</td></tr>");
            $("#viewBody").append("<tr><td bgcolor='#FFBBBB'> " + JSON.stringify(messages.recipeList[selectedRecipeIndex][selectedDetailKey]) + "</td></tr>");
        } else {
            messages.recipeList.forEach(function (message) {
                //LISTVIEW
                if (messages.selection.recipeID === message.id) {
                    $("#viewBody").append("<tr><td bgcolor='#FFAAAA' id='currentDish' >" + JSON.stringify(message).replace(/,/g, " , ") + "</td></tr>");
                } else {
                    $("#viewBody").append("<tr><td>" + JSON.stringify(message).replace(/,/g, " , ") + "</td></tr>");
                }
            });
        }

        //only scroll if we are not in position...
        if (Math.round((recipeLength - 1) * (window.scrollY / window.scrollMaxY)) !== messages.selection.recipeIndex)
            $("html, body").animate({scrollTop: $("#currentDish").offset().top}, 800);
    }
}


function navigate(navigationString) {
    stompClient.send("/ws-push/oven/view/navigation/" + navigationString + "/" + $("#userID").val(), {}, {});
}

function set(index) {
    stompClient.send("/ws-push/oven/view/set/" + index + "/" + $("#userID").val(), {}, {});
}

function changeView(viewNumber) {
    stompClient.send("/ws-push/oven/view/change/" + $("#userID").val() + "/" + viewNumber, {}, {});
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
    $("#down").click(function () {
        navigate("down");
    });
    $("#up").click(function () {
        navigate("up");
    });
    $("#left").click(function () {
        navigate("left");
    });
    $("#right").click(function () {
        navigate("right");
    });
    $("#volDown").click(function () {
        navigate("volDown");
    });
    $("#volUp").click(function () {
        navigate("volUp");
    });
    $("#mute").click(function () {
        navigate("mute");
    });
    $("#action").click(function () {
        navigate("action");
    });
    $("#back").click(function () {
        navigate("back");
    });
    $("#forth").click(function () {
        navigate("forth");
    });
    $("#chatView").click(function () {
        changeView(0);
    });
    $("#recipeView").click(function () {
        changeView(1);
    });
    $("#userID").change(function () {
        disconnect();
        $("#viewBody").html("");
        connect();
    });
    $(window).focus(function () {
        viewed = true;
    });
    $(window).blur(function () {
        viewed = false;
    });
    $(document).scroll(function () {
        //only refresh if we mark another dish than before
        if (currentMark !== Math.round((recipeLength - 1) * (window.scrollY / window.scrollMaxY)) && viewed) {
            currentMark = Math.round((recipeLength - 1) * (window.scrollY / window.scrollMaxY));
            set(currentMark);
        }
    });

});