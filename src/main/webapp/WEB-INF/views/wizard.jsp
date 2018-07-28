<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/jspf/taglibs.jspf" %>
<%-- View handling user-related tasks. Loads jspf according to provided action value.  The default index page gets redirected to here. --%>
<!DOCTYPE HTML>
<html>
<head>
    <title><spring:message code="global.title"/></title>
    <link rel="shortcut icon" href="<c:out value="${baseUrl}" />/resources/img/logo/favicon.ico"
          type="image/x-icon"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <%@ include file="/WEB-INF/views/jspf/resources.jspf" %>
    <script type="text/javascript">
      var platformMetadata = [];
      var generalMetadata = [];
      var publisherInfo = [];
    </script>
</head>
<body>
<%@ include file="/WEB-INF/views/jspf/header.jspf" %>

<c:choose>
<c:when test="${not empty currentStep}">

<form id="FORM" action="${baseUrl}/${currentStep}" method="POST" enctype="multipart/form-data">
    <nav>
        <ul id="steps">
            <li id="${currentStep}" <c:if test="${currentStep eq 'cfType'}">class="current"</c:if>>
                Basic Information
            </li>
            <li id="${currentStep}"
                <c:if test="${currentStep eq 'fileUpload'}">class="current"</c:if>>
                Upload Data File
            </li>
            <c:if test="${currentStep eq 'customFileTypeAttributes' || currentStep eq 'variableMetadata'}">
                <c:if test="${not empty data.dataFileType && data.dataFileType eq 'Custom_File_Type'}">
                    <li id="${currentStep}"
                        <c:if test="${currentStep eq 'customFileTypeAttributes'}">class="current"</c:if>>
                        Specify Custom File Type Attributes
                    </li>
                    <li id="${currentStep}"
                        <c:if test="${currentStep eq 'variableMetadata'}">class="current"</c:if>>
                        Specify Variable Attributes
                    </li>
                </c:if>
            </c:if>
            <li id="${currentStep}"
                <c:if test="${currentStep eq 'generalMetadata'}">class="current"</c:if>>
                Specify General Information
            </li>
            <li id="${currentStep}"
                <c:if test="${currentStep eq 'convertAndDownload'}">class="current"</c:if>>
                Download Converted File
            </li>
        </ul>
    </nav>

    <section>
        <div id="${currentStep}">

            <c:choose>
                <c:when test="${currentStep eq 'cfType'}">
                    <%@ include file="/WEB-INF/views/jspf/cfType.jspf" %>
                </c:when>
                <c:when test="${currentStep eq 'fileUpload'}">
                    <%@ include file="/WEB-INF/views/jspf/fileUpload.jspf" %>
                </c:when>
                <c:when test="${currentStep eq 'customFileTypeAttributes'}">
                    <%@ include file="/WEB-INF/views/jspf/customFileTypeAttributes.jspf" %>
                </c:when>
                <c:when test="${currentStep eq 'variableMetadata'}">
                    <%@ include file="/WEB-INF/views/jspf/variableMetadata.jspf" %>
                </c:when>
                <c:when test="${currentStep eq 'generalMetadata'}">
                    <c:choose>
                        <c:when test="${not empty data.platform && data.platform eq 'eTag'}">
                            <script>
                              var metadataType = "oiip";
                            </script>
                            <%@ include file="/WEB-INF/views/jspf/oiipGeneralMetadata.jspf" %>
                        </c:when>
                        <c:otherwise>
                            <script>
                              var metadataType = "general";
                            </script>
                            <%@ include file="/WEB-INF/views/jspf/generalMetadata.jspf" %>
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:when test="${currentStep eq 'convertAndDownload'}">
                    <%@ include file="/WEB-INF/views/jspf/convertAndDownload.jspf" %>
                </c:when>
                <c:otherwise>
                    <%@ include file="/WEB-INF/views/jspf/cfType.jspf" %>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    </c:when>
    <c:otherwise>
    <p class="error">Unable to load the Rosetta wizard. <spring:message
            code="fatal.error.message"/></p>
    </c:otherwise>
    </c:choose>

    <%@ include file="/WEB-INF/views/jspf/footer.jspf" %>
</body>
</html>




