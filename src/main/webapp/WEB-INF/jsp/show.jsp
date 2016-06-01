<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Hello Millky</title>
    <style>
        .video-link {
            cursor: pointer;
        }

        .video-link:focus, .video-link:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
<h2>Streaming Show ( <span id="pathInfo"></span> )</h2>

<div id="fileList"></div>
<div>
    <video id="player" controls="controls" autoplay="autoplay" poster="http://placehold.it/640x360" width="640"
    <%--height="400"--%> <%--loop="loop"--%> <%--muted="muted"--%>>
    </video>
</div>

<div id="scripts">
    <script type="text/javascript" src="<c:url value="/webjars/jquery/2.1.4/dist/jquery.min.js"/>"></script>
    <script type="text/javascript">
        var currentPath = "";
        var pathInfo = $("#pathInfo");
        var fileList = $("#fileList");
        var player = $("#player");

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
            ajaxData("/list/" + path).done(function (data) {
                fileList.empty();
                currentPath = path;
                pathInfo.text(currentPath);
                fileList.append($("<div/>", {
                    'class': 'video-link',
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
                        'class': 'video-link',
                        text: data[i].fileName,
                        'data-file': "" + data[i].file,
                        click: function () {
                            var path = (currentPath + "/" + $(this).text()).replace(/[/][/]/gi, "/");
                            if ($(this).attr('data-file') == 'true') {
                                player.attr('src', '/video-random-accessible/' + path);
                            } else {
                                loadDirFiles(path);
                            }
                        }
                    }));
                }
                if (data.length == 0)
                    fileList.append($("<i/>", {text: "파일 목록이 없습니다."}));
            }).fail(function (e) {
                fileList.text("error[" + e.status + "]: " + e.statusText);
                alert("파일 목록 불러오기 실패");
            });
        }

        $(window).load(function () {
            loadDirFiles();
        });
    </script>
</div>
</body>
</html>