<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/jspf/taglibs.jspf" %>
<%@ page import="edu.ucar.unidata.rosetta.service.ServerInfoBean" %>
<c:set var="baseUrl" value="${pageContext.request.contextPath}" />
<!DOCTYPE HTML>
    <html>
        <head>
            <title><spring:message code="global.title"/> : Login</title>
            <link rel="shortcut icon" href="<c:out value="${baseUrl}" />/resources/img/logo/favicon.ico" type="image/x-icon" />
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
            <%@ include file="/WEB-INF/views/jspf/css.jspf" %>
        </head>
        <body>
            <%@ include file="/WEB-INF/views/jspf/header.jspf" %>

            <h3>Login</h3>
            <c:choose>
                <c:when test="${error != null}">
                    <p class="error">
                        <b>
                            Authentication Error<br>
                            <c:choose>
                                <c:when test="${error == 'badCredentials'}">
                                    Bad login credentials provided.  Please Try again.
                                </c:when>
                                <c:when test="${error == 'accountDisabled'}">
                                    This account has been disabled.
                                    <spring:message code="fatal.error.message"/>
                                </c:when>
                                <c:otherwise>
                                    <spring:message code="fatal.error.message"/>
                                </c:otherwise>
                            </c:choose>
                        </b>
                    </p>
                </c:when>
            </c:choose>

            <form action="${baseUrl}/j_spring_security_check" method="POST">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                <ul class="format">
                    <li>
                        <label for="userName">
                            Username
                            <input type="text" id="userName" name="userName" value="" />
                        </label>
                    </li>
                    <li>
                        <label for="password">
                            Password
                            <input type="password" id="password" name="password" value="" />
                        </label>
                    </li>
                    <li>
                        <input type="submit" value="Login" />
                    </li>
                </ul>
            </form>

            <%@ include file="/WEB-INF/views/jspf/footer.jspf" %>
        </body>
    </html>

