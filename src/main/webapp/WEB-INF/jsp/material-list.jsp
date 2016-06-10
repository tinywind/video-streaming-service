<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>SAVED DATA</title>
    <link rel="stylesheet" type="text/css" href="<c:url value="/webjars/bootstrap/3.3.5/dist/css/bootstrap.css"/>"/>
    <style>
        html {
            padding: 20px;
        }
    </style>
</head>
<body>
<h2>DATA list</h2>

<hr/>

<table style="width: 100%; float: left; min-height: 100px;"
       class="table table-hover table-striped">
    <thead>
    <tr>
        <th>ID</th>
        <th>file name</th>
        <th>created at</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="e" items="${postPage.content}">
        <tr style="cursor: pointer;" onclick="location.href = '/download-csv/${e.id}';">
            <td>${e.id}</td>
            <td>${e.videoName.substring('/video-random-accessible/'.length(), e.videoName.length())}</td>
            <td>${e.createdAt}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<hr/>

<div style="text-align: center;">
    <a href="/material-list?page=0"> << </a>
    <c:forEach var="num" begin="${postPage.number - 5 > 0 ? postPage.number - 5 : 0}"
               end="${postPage.number + 5 < postPage.totalPages -1 ? postPage.number + 5 : postPage.totalPages -1}">
        <c:choose>
            <c:when test="${postPage.number == num}">
                <span> ${num + 1} </span>
            </c:when>
            <c:otherwise>
                <a href="/material-list?page=${num}"> ${num + 1} </a>
            </c:otherwise>
        </c:choose>
    </c:forEach>
    <a href="/material-list?page=${postPage.totalPages - 1}"> >> </a>
</div>

</body>
</html>
