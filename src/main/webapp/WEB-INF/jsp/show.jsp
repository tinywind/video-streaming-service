<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Hello Millky</title>
    <link rel="stylesheet" type="text/css" href="<c:url value="/webjars/bootstrap/3.3.5/dist/css/bootstrap.css"/>"/>
    <style>
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
        }

        .dir {
            font-style: italic;
        }
    </style>
</head>
<body>
<h2>Streaming Show ( location: <span id="pathInfo"></span> )</h2>

<div id="fileList"></div>
<div>
    <video id="player" controls="controls" autoplay="autoplay" poster="http://placehold.it/640x360" width="640"
    <%--height="400"--%> loop="loop" <%--muted="muted"--%>>
    </video>
</div>

<div>
    <a id="helpLink" href="#help">설명보기</a>
</div>

<div id="help" style="display: none; padding: 2em; background: white; width: 30em; border-radius: 5px;">
    <h2>사용방법 (전부 아직 미구현)</h2>
    <div>방향키 왼쪽: 5초 전으로</div>
    <div>방향키 오른쪽: 5초 앞으로</div>
    <div>방향키 위: 재생 속도 10% 빠르게</div>
    <div>방향키 아래: 재생 속도 10% 느리게</div>
    <div>Space bar: 비디오 재생/중지</div>
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
            helpLink.leanModal({closeButton: ".lean-overlay"});
            loadDirFiles();
        });
    </script>
</div>
</body>
</html>