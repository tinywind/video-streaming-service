<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>DATA PROCESSING</title>
    <link rel="stylesheet" type="text/css" href="<c:url value="/webjars/bootstrap/3.3.5/dist/css/bootstrap.css"/>"/>
    <style>
        html {
            padding: 20px;
        }

        .video-link {
            cursor: pointer;
            display: block;
            line-height: 20px;
        }

        .video-link:focus, .video-link:hover {
            text-decoration: underline;
        }

        .video-link:before {
            width: 30px;
            display: inline-block;
        }

        .file {
            color: darkblue;
            font-weight: bold;
            padding-left: 1em;
        }

        .dir {
            font-style: italic;
        }

        @keyframes fadeinout {
            from {
                opacity: 0;
            }
            10% {
                opacity: 1;
            }
            70% {
                opacity: 1;
            }
            to {
                opacity: 0;
            }
        }

        #playerComment {
            position: absolute;
            width: 100%;
            padding-left: 1em;
            padding-right: 1em;
            top: 2em;
            min-height: 1px;
            white-space: nowrap;
            overflow: hidden;
            font-weight: bold;
            color: orange;
            opacity: 0;
        }

        #playerTitle {
            position: absolute;
            width: 100%;
            padding-left: 1em;
            padding-right: 1em;
            top: 0.5em;
            min-height: 1px;
            white-space: nowrap;
            overflow: hidden;
            font-weight: bold;
            color: indigo;
        }

        #progressContainer {
            position: absolute;
            width: 96%;
            left: 2%;
            bottom: 0;
            opacity: 0.5;
        }

        .ani-blank {
            animation: fadeinout 0.7s;
        }

        article {
            width: 1.5em;
            float: left;
            text-align: center;
            padding-left: 0.1em;
            padding-right: 0.1em;
            height: 8em;
        }

        article .key {
            font-weight: bold;
            color: firebrick;
            padding-bottom: 0.5em;
        }

        article .timestamp {
            -ms-transform: rotate(90deg); /* IE 9 */
            -webkit-transform: rotate(90deg); /* Chrome, Safari, Opera */
            transform: rotate(90deg);
        }
    </style>
</head>
<body style="min-width: 640px;">
<h2>EYE DATA RECORDING ( location: <span id="pathInfo"></span> )</h2>

<div id="fileList" style="height: 100px; overflow-x: hidden; overflow-y: auto;"></div>

<hr/>
<div class="row">
    <div class="col-xs-6">
        <button class="btn btn-default form-control" id="helpLink" data-href="#help"><b>KEY INSTRUCTION</b></button>
    </div>
    <div class="col-xs-6">
        <button class="btn btn-info form-control" id="send-data"><b>SAVE TO CSV</b></button>
    </div>
</div>
<hr/>

<div style="width: 100%;">
    <div style="position: relative; overflow: hidden;">
        <div id="playerContainer" style="position: absolute; width: 100%; top: 0; left: 0;">
            <video id="player" style="width: 100%;" autoplay="autoplay"
                   poster="http://placehold.it/640x360"
            <%--loop="loop"--%> <%--height="400"--%> <%--muted="muted"--%> <%--controls="controls"--%>>
            </video>
        </div>
        <div id="playerPadding" style="position: relative; width: 100%; padding-bottom: 60%;">
            <div id="playerTitle"></div>
            <div id="playerComment"></div>
            <div class="progress" id="progressContainer">
                <div id="playerProgress" class="progress-bar progress-bar-info progress-bar-striped"
                     role="progressbar" <%--aria-valuenow="70"--%>
                     aria-valuemin="0" aria-valuemax="100" style="width:0; transition-duration: 0.1s;"></div>
            </div>
        </div>
    </div>
    <div style="text-align: center;">
        <button id="restart" title="Restart button" class="btn glyphicon glyphicon-repeat"></button>
        &nbsp;&nbsp;&nbsp;
        <button id="rew" title="Rewind button" class="btn glyphicon glyphicon-backward"></button>
        <button id="play" title="Play button" class="btn glyphicon glyphicon-stop"></button>
        <button id="fwd" title="Forward button" class="btn glyphicon glyphicon-forward"></button>
        &nbsp;&nbsp;&nbsp;
        <button id="slower" title="Slower playback button"
                class="btn glyphicon glyphicon-fast-backward"></button>
        <button id="normal" title="Reset playback rate button" class="btn glyphicon glyphicon-refresh"></button>
        <button id="faster" title="Faster playback button"
                class="btn glyphicon glyphicon-fast-forward"></button>
        &nbsp;&nbsp;&nbsp;
        <button id="volDn" title="Volume down button" class="btn glyphicon glyphicon-volume-down"></button>
        <button id="volUp" title="Volume up button" class="btn glyphicon glyphicon-volume-up"></button>
        <button id="mute" title="Mute button" class="btn glyphicon glyphicon-volume-off"></button>
    </div>
    <br/>
</div>

<hr/>
<div id="keyLog" class="clearfix" style="width: 100%; overflow-x: auto; min-height: 1px;"></div>
<hr/>

<div id="help" style="display: none; padding: 2em; background: white; width: 30em; border-radius: 5px;">
    <h2>사용방법</h2>
    <div>방향키 왼쪽: 5초 전으로</div>
    <div>방향키 오른쪽: 5초 앞으로</div>
    <div>방향키 위: 재생 속도 10% 빠르게</div>
    <div>방향키 아래: 재생 속도 10% 느리게</div>
    <div>Space bar: 비디오 재생/중지</div>
    <div>backspace: 마지막 로그 삭제</div>
    <div>+: 화면 비율 확대</div>
    <div>-: 화면 비율 축소</div>
    <div>=: 화면 비율 100%</div>
    <div>ctrl + 방향키: 화면 중심 이동</div>
    <div>Q: 시선 좌측 상단</div>
    <div>W: 시선 상단</div>
    <div>E: 시선 좌측 상단</div>
    <div>A: 시선 좌측</div>
    <div>S: 시선 가운데</div>
    <div>D: 시선 우측</div>
    <div>Z: 시선 좌측 하단</div>
    <div>X: 시선 하단</div>
    <div>C: 시선 우측 하단</div>
</div>

<div id="scripts">
    <script type="text/javascript" src="<c:url value="/webjars/jquery/2.1.4/dist/jquery.min.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/!resources/library/jquery.blockUI-2.70.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/!resources/js/jquery.leanModal-1.1.replace.js"/>"></script>
    <script type="text/javascript">
        var currentPath = "";
        var pathInfo = $("#pathInfo");
        var fileList = $("#fileList");
        var playerTitle = $("#playerTitle");
        var playerContainer = $("#playerContainer");
        var playerPadding = $("#playerPadding");
        var playerProgress = $("#playerProgress");
        var player = $("#player");
        var player0 = player[0];
        var helpLink = $("#helpLink");
        var help = $("#help");
        var keyLog = $("#keyLog");
        var comment = $("#playerComment");

        var playerRew = $("#rew");
        var playerFwd = $("#fwd");
        var playerSlower = $("#slower");
        var playerFaster = $("#faster");
        var playerPlay = $("#play");

        function createPlayer() {
            player.bind('progress', function () {
                adjustVideoContainerRatio();
                playerProgress.css({width: ((player0.currentTime / player0.duration) * 100) + "%"})
            });

            if (player0.canPlayType) {
                playerRew.click(function (e) {
                    e.stopPropagation();
                    setTime(-3);
                });
                playerFwd.click(function (e) {
                    e.stopPropagation();
                    setTime(3);
                });
                playerSlower.click(function (e) {
                    e.stopPropagation();
                    player0.playbackRate -= .25;
                    showComment("playbackRate: " + player0.playbackRate);
                });
                playerFaster.click(function (e) {
                    e.stopPropagation();
                    player0.playbackRate += .25;
                    showComment("playbackRate: " + player0.playbackRate);
                });
                playerPlay.click(function (e) {
                    e.stopPropagation();
                    var button = $(e.target);
                    if (player0.paused) {
                        player0.play();
                        button.removeClass('glyphicon-play').addClass('glyphicon-stop');
                    } else {
                        player0.pause();
                        button.removeClass('glyphicon-stop').addClass('glyphicon-play');
                    }
                });

                document.getElementById("restart").addEventListener("click", function () {
                    setTime(0);
                }, false);
                document.getElementById("normal").addEventListener("click", function () {
                    player0.playbackRate = 1;
                    showComment("playbackRate: " + player0.playbackRate);
                }, false);
                document.getElementById("mute").addEventListener("click", function (e) {
                    if (player0.muted) {
                        player0.muted = false;
                        $(e.target).addClass('glyphicon-volume-off').removeClass('glyphicon-bullhorn');
                    } else {
                        player0.muted = true;
                        $(e.target).addClass('glyphicon-bullhorn').removeClass('glyphicon-volume-off');
                    }
                    showComment("sound " + (player0.muted ? "off" : "on"));
                }, false);
                document.getElementById("volDn").addEventListener("click", function () {
                    setVol(-.1); // down by 10%
                }, false);
                document.getElementById("volUp").addEventListener("click", function () {
                    setVol(.1);  // up by 10%
                }, false);

                function setVol(value) {
                    var vol = player0.volume;
                    vol += value;
                    if (vol >= 0 && vol <= 1) {
                        player0.volume = vol;
                    } else {
                        player0.volume = (vol < 0) ? 0 : 1;
                    }
                    showComment("volume: " + player0.volume);
                }

                player0.addEventListener("error", function (err) {
                    showComment("err: " + err);
                }, true);
            }

            function setTime(tValue) {
                try {
                    player0.currentTime = tValue + (tValue == 0 ? 0 : player0.currentTime);
                    showComment(player0.currentTime + "s");
                } catch (err) {
                    showComment("err: " + "Video content might not be loaded");
                }
            }
        }

        function showComment(text) {
            comment.text(text).removeClass('ani-blank');
            comment[0].offsetWidth = comment[0].offsetWidth;
            comment.addClass('ani-blank');
        }

        function ajaxData(url, data) {
            var deferred = $.Deferred();
            console.log(url);
            console.log(data);
            $.ajax({
                url: url,
                method: 'post',
                data: data,
                dataType: 'json',
                success: function (data) {
                    console.log(data);
                    deferred.resolve(data);
                },
                error: function (result) {
                    deferred.reject(result);
                }
            });
            return deferred.promise();
        }

        $("#send-data").click(function () {
            var articleList = keyLog.find("article");
//            if (player.attr("src") == "" || player.attr("src") == null || trList.length <= 0)
//                return;

            var form = $("<form/>", {
                action: "/save",
                method: "post"
            });

            form.append($("<input/>", {
                name: "videoName",
                value: player.attr("src")
            }));

            for (var i = 0; i < articleList.length; i++) {
                form.append($("<input/>", {
                    name: "materialDataList[" + i + "].key",
                    value: $(articleList[i]).find(".key").text()
                })).append($("<input/>", {
                    name: "materialDataList[" + i + "].timestamp",
                    value: $(articleList[i]).find(".timestamp").text()
                }));
            }

            console.log(form);
            form.submit();
        });

        function loadDirFiles(path) {
            if (path == null)
                path = "";
            fileList.block();
            ajaxData("/list/" + path).done(function (data) {
                fileList.empty();
                currentPath = path;
                pathInfo.text(currentPath);
                fileList.append($("<div/>", {
                    'class': 'video-link glyphicon dir glyphicon-folder-open',
                    text: "<<MOVE TO THE PREVIOUS FOLDER>>",
                    click: function () {
                        if (currentPath == "" || currentPath == "/") {
                            loadDirFiles();
                            return;
                        }

                        var lastIndex = currentPath.lastIndexOf("/");
                        if (lastIndex == currentPath.length - 1)
                            lastIndex = currentPath.substr(0, currentPath.length - 1).lastIndexOf("/");
                        console.log(currentPath.substr(0, lastIndex));
                        loadDirFiles(currentPath.substr(0, lastIndex));
                    }
                }));
                for (var i = 0; i < data.length; i++) {
                    fileList.append($("<div/>", {
                        'class': 'video-link glyphicon ' + (data[i].file ? 'file glyphicon-film' : 'dir glyphicon-folder-open'),
                        text: data[i].fileName,
                        'data-file': "" + data[i].file,
                        click: function () {
                            var path = (currentPath + "/" + $(this).text()).replace(/[/][/]/gi, "/");
                            if ($(this).hasClass('file')) {
                                player.attr('src', '/video-random-accessible/' + path);
                                keyLog.empty();
                                playerContainer.css({left: 0, top: 0});
                                playerTitle.text(path)
                            } else {
                                loadDirFiles(path);
                            }
                        }
                    }));
                }
                if (data.length == 0)
                    fileList.append($("<i/>", {text: "NO MP4 EXISTS"}));
                fileList.unblock();
            }).fail(function (e) {
                fileList.text("error[" + e.status + "]: " + e.statusText);
                alert("FAILED TO CREATE FILE LIST");
                fileList.unblock();
            });
        }

        function addArticle(timestamp, key) {
            keyLog.append(
                    $('<article/>')
                            .append($('<div/>', {'class': 'key', text: key}))
                            .append($('<div/>', {'class': 'timestamp', text: timestamp}))
            );
        }

        function removeArticle() {
            keyLog.find('article:last').remove();
        }

        var videoRatio = 1;

        function adjustVideoContainerRatio() {
            var ratio = player0.videoHeight / player0.videoWidth;
//            console.log("ratio:" + ratio);
            if (!isNaN(ratio))
                playerPadding.css({'padding-bottom': (ratio * 100) + '%'});
        }

        $(window).load(function () {
            createPlayer();
            helpLink.leanModal({closeButton: ".lean-overlay"});
            loadDirFiles();

            $(document).bind('keyup keydown keypress', function (e) {
                e.stopPropagation();
                e.preventDefault();
            });

            $(document).keydown(function (e) {
//                console.log(e);
                var eyeTrackingKeys = ['q', 'w', 'e', 'a', 's', 'd', 'z', 'x', 'c'];
                var arrowKeys = ['arrowleft', 'arrowright', 'arrowup', 'arrowdown'];
                var key = e.key.toLowerCase();
                if (player0.canPlayType) {
                    if (key == " ") {
                        playerPlay.click();
                    } else if (!player0.paused) {
                        if ($.inArray(key, eyeTrackingKeys) >= 0) {
                            addArticle(player0.currentTime, key);
                        } else if ($.inArray(key, arrowKeys) >= 0) {
                            if (e.ctrlKey) {
                                var left = parseInt(playerContainer.css('left'));
                                var top = parseInt(playerContainer.css('top'));
                                var width = parseInt(playerPadding.css('width'));
                                var height = parseInt(playerPadding.css('height'));
                                var videoWidth = parseInt(playerContainer.css('width'));
                                var videoHeight = parseInt(playerContainer.css('height'));
                                var INTERVAL = 5;
                                if (key == "arrowleft") {
                                    var last = left - INTERVAL;
                                    if (last <= 0 && width <= videoWidth + last)
                                        playerContainer.css({left: last + 'px'});
                                } else if (key == "arrowright") {
                                    last = left + INTERVAL;
                                    if (last <= 0 && width <= videoWidth + last)
                                        playerContainer.css({left: last + 'px'});
                                } else if (key == "arrowup") {
                                    last = top - INTERVAL;
                                    if (last <= 0 && height <= videoHeight + last)
                                        playerContainer.css({top: last + 'px'});
                                } else if (key == "arrowdown") {
                                    last = top + INTERVAL;
                                    if (last <= 0 && height <= videoHeight + last)
                                        playerContainer.css({top: last + 'px'});
                                }
                            } else {
                                if (key == "arrowleft") {
                                    playerRew.click();
                                } else if (key == "arrowright") {
                                    playerFwd.click();
                                } else if (key == "arrowup") {
                                    playerFaster.click();
                                } else if (key == "arrowdown") {
                                    playerSlower.click();
                                }
                            }
                        } else if (key == '+') {
                            videoRatio += 0.1;
                            playerContainer.css({width: videoRatio * 100 + '%'});
                        } else if (key == '-') {
                            videoRatio = videoRatio - 0.1 < 1 ? 1 : videoRatio - 0.1;
                            playerContainer.css({width: videoRatio * 100 + '%'});
                        } else if (key == '=') {
                            playerContainer.css({left: 0, top: 0, width: '100%'});
                        } else if (key == "backspace") {
                            removeArticle();
                        }
                    }
                }
            });
        });
    </script>
</div>
</body>
</html>
