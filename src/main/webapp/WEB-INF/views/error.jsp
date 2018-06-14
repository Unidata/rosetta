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

            <h3><spring:message code="fatal.error.title"/></h3>
            <p><spring:message code="fatal.error.message"/></p>


            <p><c:out value="${message}"/></p>

            <%@ include file="/WEB-INF/views/jspf/footer.jspf" %>
        </body>
    </html>



