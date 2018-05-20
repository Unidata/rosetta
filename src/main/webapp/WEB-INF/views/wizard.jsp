<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/jspf/taglibs.jspf" %>
<%@ page import="edu.ucar.unidata.rosetta.service.ServerInfoBean" %>
<c:set var="baseUrl" value="${pageContext.request.contextPath}" />
<!DOCTYPE HTML>
    <html>
        <head>
            <title><spring:message code="global.title"/></title>
            <script>
                var baseUrl = '<c:out value="${baseUrl}" />';
            </script>

            <%@ include file="/WEB-INF/views/jspf/css.jspf" %>
            <%@ include file="/WEB-INF/views/jspf/javascript.jspf" %>

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

                <form id="FORM" action="/rosetta/${currentStep}" method="POST" enctype="multipart/form-data">
                    <nav>
                        <ul id="steps">
                            <li id="${currentStep}" <c:if test="${currentStep eq 'cfType'}">class="current"</c:if>>
                                Basic Information
                            </li>
                            <li id="${currentStep}" <c:if test="${currentStep eq 'fileUpload'}">class="current"</c:if>>
                                Upload Data File
                            </li>
                            <c:if test="${not empty data.dataFileType && data.dataFileType eq 'Custom_File_Type'}">
                                <li id="${currentStep}" <c:if test="${currentStep eq 'customFileTypeAttributes'}">class="current"</c:if>>
                                    Specify Custom File Type Attributes
                                </li>
                            </c:if>
                            <li id="${currentStep}" <c:if test="${currentStep eq 'variableMetadata'}">class="current"</c:if>>
                                Specify Variable Attributes
                            </li>
                            <li id="${currentStep}" <c:if test="${currentStep eq 'generalMetadata'}">class="current"</c:if>>
                                Specify General Information
                            </li>
                            <li id="${currentStep}" <c:if test="${currentStep eq 'convertAndDownload'}">class="current"</c:if>>
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
                                     <%@ include file="/WEB-INF/views/jspf/generalMetadata.jspf" %>
                                 </c:when>
                                 <c:otherwise>
                                     <%@ include file="/WEB-INF/views/jspf/convertAndDownload.jspf" %>
                                 </c:otherwise>
                            </c:choose>
                         </div>
                    </section>

                </c:when>
                <c:otherwise>
                    <p class="error">Unable to load the Rosetta wizard.  <spring:message code="fatal.error.message"/></p>
                </c:otherwise>
            </c:choose>

            <%@ include file="/WEB-INF/views/jspf/footer.jspf" %>
        </body>
    </html>




