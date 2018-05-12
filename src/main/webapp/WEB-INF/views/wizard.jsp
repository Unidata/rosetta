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
                $.metadata.setType("attr", "validate");
                var maxUploadSize = 1243000;
            </script>
        </head>
        <body>
            <%@ include file="/WEB-INF/views/jspf/header.jspf" %>

            <c:choose>
                <c:when test="${not empty currentStep}">

                <form id="FORM" action="/rosetta/step${currentStep}" method="POST" enctype="multipart/form-data">

                    <nav>
                        <c:choose>
                            <c:when test="${fn:length(steps) gt 0}">
                                <ul id="steps">
                                    <c:forEach items="${steps}" var="step">
                                        <%-- Assign the variables associated with current step. --%>
                                        <c:if test="${currentStep eq step.id}">
                                            <c:set var="currentStepTitle" value="${step.title}" />
                                            <c:set var="currentStepView" value="${step.view}" />
                                        </c:if>


                                        <%-- Create left nav step menu. --%>
                                        <li id="step${step.id}"
                                                <c:if test="${currentStep eq step.id}">
                                                    class="current"
                                                </c:if>
                                        >
                                            ${step.title}
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:when>
                            <c:otherwise>
                                <p class="error">Unable to load wizard navigation.</p>
                            </c:otherwise>
                        </c:choose>
                    </nav>

                    <section>
                         <div id="step${currentStep}">
                            <h3>${currentStepTitle}</h3>
                            <c:choose>
                                 <c:when test="${currentStepView eq 'cfType'}">
                                     <%@ include file="/WEB-INF/views/jspf/cfType.jspf" %>
                                 </c:when>
                                 <c:when test="${currentStepView eq 'fileUpload'}">
                                     <%@ include file="/WEB-INF/views/jspf/fileUpload.jspf" %>
                                 </c:when>
                                 <c:when test="${currentStepView eq 'headerLinesAndDelimiters'}">
                                     <%@ include file="/WEB-INF/views/jspf/headerLinesAndDelimiters.jspf" %>
                                 </c:when>
                                 <c:when test="${currentStepView eq 'variableMetadata'}">
                                     <%@ include file="/WEB-INF/views/jspf/variableMetadata.jspf" %>
                                 </c:when>
                                 <c:when test="${currentStepView eq 'generalMetadata'}">
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




