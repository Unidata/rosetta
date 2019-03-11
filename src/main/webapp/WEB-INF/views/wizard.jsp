<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/jspf/common/taglibs.jspf" %>
<%--
View handling user-related tasks. Loads jspf according to provided action value.
The default index page gets redirected to here.
--%>
<!DOCTYPE HTML>
  <html>
    <head>
      <title><spring:message code="global.title"/></title>
      <link rel="shortcut icon" href="<c:out value="${baseUrl}" />/resources/img/logo/favicon.ico"
      type="image/x-icon"/>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
      <%@ include file="/WEB-INF/views/jspf/common/javascript.jspf" %>
      <%@ include file="/WEB-INF/views/jspf/common/css.jspf" %>
      <script type="text/javascript">
        //var platformMetadata = [];
        //var globalMetadata = [];
        //var publisherInfo = [];

      </script>
    </head>
    <body>
      <%@ include file="/WEB-INF/views/jspf/common/header.jspf" %>

      <c:choose>
        <c:when test="${not empty currentStep}">
          <form:form id ="FORM" action="${baseUrl}/${currentStep}" modelAttribute="${command}"
                    method="POST" enctype="multipart/form-data">
            <nav>
              <ul id="steps">
                <li id="${currentStep}"
                    <c:if test="${currentStep eq 'cfType'}">class="current"</c:if>>
                  Basic Information
                </li>
                <li id="${currentStep}"
                  <c:if test="${currentStep eq 'fileUpload'}">class="current"</c:if>>
                    Upload Data File
                </li>
                <c:if test="${customFileAttributesStep}">
                  <li id="${currentStep}"
                    <c:if test="${currentStep eq 'customFileTypeAttributes'}">class="current"</c:if>>
                      Specify Custom File Type Attributes
                  </li>
                  <li id="${currentStep}"
                    <c:if test="${currentStep eq 'variableMetadata'}">class="current"</c:if>>
                      Specify Variable Attributes
                  </li>
                </c:if>
                <li id="${currentStep}"
                  <c:if test="${currentStep eq 'globalMetadata'}">class="current"</c:if>>
                    Specify Global Attributes
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
                  <c:when test="${currentStep eq 'globalMetadata'}">
                    <%@ include file="/WEB-INF/views/jspf/globalMetadata.jspf" %>
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
          </form:form>
        </c:when>
        <c:otherwise>
          <p class="error">
              Unable to load the Rosetta wizard.
              <spring:message code="fatal.error.message"/>
          </p>
        </c:otherwise>
      </c:choose>
      <%@ include file="/WEB-INF/views/jspf/common/footer.jspf" %>
    </body>
  </html>