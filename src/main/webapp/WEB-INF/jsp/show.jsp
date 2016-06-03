<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Hello Millky</title>
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

        #player-comment {
            position: absolute;
            top: 5px;
            left: 5px;
            width: 600px;
            min-height: 1px;
            white-space: nowrap;
            overflow: hidden;
            font-weight: bold;
            color: orange;
            opacity: 0;
        }

        .ani-blank {
            animation: fadeinout 0.7s;
        }
    </style>
</head>
<body>
<div>
    <a id="helpLink" href="#help">설명보기</a>
</div>
<h2>Streaming Show ( location: <span id="pathInfo"></span> )</h2>

<div id="fileList"></div>

<hr/>

<div class="row">
    <div class="col-xs-12">
        <div style="width: 640px; float: left;">
            <div style="position: relative;">
                <div id="player-comment"></div>
                <video id="player" style="width: 100%;" autoplay="autoplay"
                       poster="http://placehold.it/640x360"
                       loop="loop" <%--height="400"--%> <%--muted="muted"--%> controls="controls">
                </video>
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
        <div style="width: 1em; float: left; min-height: 1px;"></div>
        <div style="width: 320px; float: left; min-height: 100px; background: red;" id="keyLog">
        </div>
    </div>
</div>

<div id="help" style="display: none; padding: 2em; background: white; width: 30em; border-radius: 5px;">
    <h2>사용방법 (전부 아직 미구현)</h2>
    <div>방향키 왼쪽: 5초 전으로</div>
    <div>방향키 오른쪽: 5초 앞으로</div>
    <div>방향키 위: 재생 속도 10% 빠르게</div>
    <div>방향키 아래: 재생 속도 10% 느리게</div>
    <div>Space bar: 비디오 재생/중지</div>
    <div>backspace: 마지막 로그 삭제</div>
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
        var player = $("#player");
        var helpLink = $("#helpLink");
        var help = $("#help");
        var comment = $("#player-comment");
        var actor = $("#lead-fadeinout");

        var playerRew = $("#rew");
        var playerFwd = $("#fwd");
        var playerSlower = $("#slower");
        var playerFaster = $("#faster");
        var playerPlay = $("#play");

        function createPlayer() {
            var player0 = player[0];
            if (player0.canPlayType) {
                playerRew.click(function (e) {
                    e.stopPropagation();
                    setTime(-5);
                });
                playerFwd.click(function (e) {
                    e.stopPropagation();
                    setTime(5);
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
                    //  test for range 0 - 1 to avoid exceptions
                    if (vol >= 0 && vol <= 1) {
                        // if valid value, use it
                        player0.volume = vol;
                    } else {
                        // otherwise substitute a 0 or 1
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
                    text: "<<이전 디렉토리로 이동>>",
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
                            } else {
                                loadDirFiles(path);
                            }
                        }
                    }));
                }
                if (data.length == 0)
                    fileList.append($("<i/>", {text: "파일 목록이 없습니다."}));
                fileList.unblock();
            }).fail(function (e) {
                fileList.text("error[" + e.status + "]: " + e.statusText);
                alert("파일 목록 불러오기 실패");
                fileList.unblock();
            });
        }

        $(window).load(function () {
            createPlayer();
            helpLink.leanModal({closeButton: ".lean-overlay"});
            loadDirFiles();

            $(document).bind('keyup keydown keypress', function (e) {
                e.stopPropagation();
                e.preventDefault();
            });

            $(document).keyup(function (e) {
//                console.log(e);
                var arrowKeys = ['q', 'w', 'e', 'a', 's', 'd', 'z', 'x', 'c'];
                var player0 = player[0];
                if (player0.canPlayType && !player0.paused) {
                    var key = e.key.toLowerCase();

                    if (key == "arrowleft") {
                        playerRew.click();
                    } else if (key == "arrowright") {
                        playerFwd.click();
                    } else if (key == "arrowup") {
                        playerFaster.click();
                    } else if (key == "arrowdown") {
                        playerSlower.click();
                    } else if (key == "enter") {
                        playerPlay.click();
                    } else if (key == "backspace") {

                    } else if ($.inArray(key, arrowKeys) >= 0) {
                        console.log(key);
                    }
                }
            });
        });
    </script>
</div>
</body>
</html>