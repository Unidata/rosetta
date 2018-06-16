<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/jspf/taglibs.jspf" %>
<%-- View handling user-related tasks. Loads jspf according to provided action value.  --%>
<!DOCTYPE HTML>
<html>
<head>
    <title>
        <spring:message code="global.title"/> :
        <c:choose>
            <c:when test="${action eq 'createUser'}">
                <spring:message code="user.create"/>
            </c:when>
            <c:when test="${action eq 'register'}">
                <spring:message code="user.register"/>
            </c:when>
            <c:when test="${action eq 'editUser'}">
                <spring:message code="user.edit"/>
            </c:when>
            <c:when test="${action eq 'deleteUser'}">
                <spring:message code="user.delete"/>
            </c:when>
            <c:when test="${action eq 'viewUser'}">
                <spring:message code="user.view"/>
            </c:when>
            <c:when test="${action eq 'resetPassword'}">
                <spring:message code="user.password"/>
            </c:when>
            <c:otherwise>
                <spring:message code="user.list"/>
            </c:otherwise>
        </c:choose>
    </title>
    <link rel="shortcut icon" href="<c:out value="${baseUrl}" />/resources/img/logo/favicon.ico" type="image/x-icon" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/WEB-INF/views/jspf/css.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/jspf/header.jspf" %>
<c:choose>
    <c:when test="${not empty action}">
        <c:choose>
            <c:when test="${action eq 'createUser' || action eq 'register'}">
                <%-- create user or user registration  --%>
                <%@ include file="/WEB-INF/views/jspf/user/createUser.jspf" %>
            </c:when>
            <c:when test="${action eq 'editUser'}">
                <%-- edit existing user  --%>
                <%@ include file="/WEB-INF/views/jspf/user/editUser.jspf" %>
            </c:when>
            <c:when test="${action eq 'deleteUser'}">
                <%-- delete user  --%>
                <%@ include file="/WEB-INF/views/jspf/user/deleteUser.jspf" %>
            </c:when>
            <c:when test="${action eq 'viewUser'}">
                <%-- view user account details --%>
                <%@ include file="/WEB-INF/views/jspf/user/viewUser.jspf" %>
            </c:when>
            <c:when test="${action eq 'resetPassword'}">
                <%-- reset user password --%>
                <%@ include file="/WEB-INF/views/jspf/user/resetPassword.jspf" %>
            </c:when>
            <c:otherwise>
                <%-- view all users --%>
                <%@ include file="/WEB-INF/views/jspf/user/listUsers.jspf" %>
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:otherwise>
        <p class="error">Unable to view user account information.  <spring:message code="fatal.error.message"/></p>
    </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/views/jspf/footer.jspf" %>
</body>
</html>