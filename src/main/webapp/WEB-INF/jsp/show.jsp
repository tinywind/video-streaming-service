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
<h2>Streaming Show</h2>

<div id="fileList"></div>
<div>
    <video id="player" controls="controls" autoplay="autoplay" poster="http://placehold.it/640x360" width="640"
           <%--height="400"--%> <%--loop="loop"--%> <%--muted="muted"--%>
           src="/video/Mike%20Tompkins%20-%20Dynamite%20-%20Taio%20Cruz%20-%20A%20Cappella%20Cover%20-%20Just%20Voice%20and%20Mouth.mp4">
    </video>
</div>

<div id="scripts">
    <script type="text/javascript" src="<c:url value="/webjars/jquery/2.1.4/dist/jquery.min.js"/>"></script>
    <script type="text/javascript">
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

        $(window).load(function () {
            var fileList = $("#fileList");
            var player = $("#player");
            ajaxData("/list").done(function (data) {
                for (var i = 0; i < data.length; i++) {
                    fileList.append($("<div/>", {
                        'class': 'video-link',
                        text: data[i].fileName,
                        click: function () {
                            console.log($(this).text());
                            player.attr('src', '/video/' + $(this).text());
//                            player.attr('src', 'http://techslides.com/demos/sample-videos/small.mp4');
                        }
                    }));
                }
            }).fail(function (e) {
                fileList.text("error[" + e.status + "]: " + e.statusText);
            });
        });
    </script>
</div>
</body>
</html>