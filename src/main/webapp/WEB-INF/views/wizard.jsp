<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/jspf/taglibs.jspf" %>
<%@ page import="edu.ucar.unidata.rosetta.service.ServerInfoBean" %>
<!DOCTYPE HTML>
 <html>
  <head>
   <title><spring:message code="global.title"/></title>
   <link rel="shortcut icon" href="${baseUrl}/favicon.ico" type="image/x-icon" />
   <c:set var="baseUrl" value="${pageContext.request.contextPath}" />
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

   <script type="text/javascript">
    $(document).ready(function(){
        $("#FORM").rosettaWizard({ submitButton: 'SaveAccount' })
    });
   </script>
  </head>
  <body>
   <%@ include file="/WEB-INF/views/jspf/header.jspf" %>
   <form id="FORM" action="/rosetta/convert" method="POST" enctype="multipart/form-data">
    <fieldset>
     <legend><spring:message code="step0.title"/></legend>
     <p><spring:message code="step0.description"/></p>
     <%@ include file="/WEB-INF/views/jspf/selectPlatform.jspf" %>
    </fieldset>

    <fieldset>
     <legend><spring:message code="step1.title"/></legend>
     <p><spring:message code="step1.description"/></p>
     <%@ include file="/WEB-INF/views/jspf/uploadFile.jspf" %>
    </fieldset>

    <fieldset>
     <legend><spring:message code="step2.title"/></legend>
     <p><spring:message code="step2.description"/></p>
     <%@ include file="/WEB-INF/views/jspf/specifyHeaderLines.jspf" %>
     <button type="button" id="quickSaveButton" class="nonNav"/>Quick Save</button>
    </fieldset>

    <fieldset>
     <legend><spring:message code="step3.title"/></legend>
     <p><spring:message code="step3.description"/></p>
     <%@ include file="/WEB-INF/views/jspf/specifyVariableMetadata.jspf" %>
     <button type="button" id="quickSaveButton" class="nonNav"/>Quick Save</button>
     <button type="button" id="showHeaderButton" class="nonNav"/>Show Header</button>
    </fieldset>

    <fieldset>
     <legend><spring:message code="step4.title"/></legend>
     <p><spring:message code="step4.description"/></p>
     <%@ include file="/WEB-INF/views/jspf/specifyGeneralMetadata.jspf" %>
     <button type="button" id="quickSaveButton" class="nonNav"/>Quick Save</button>
     <button type="button" id="showHeaderButton" class="nonNav"/>Show Header</button>
    </fieldset>

    <fieldset>
     <legend><spring:message code="step5.title"/></legend>
     <p><spring:message code="step5.description"/></p>
     <%@ include file="/WEB-INF/views/jspf/convertAndDownload.jspf" %>
    </fieldset>

    </form> 
   <%@ include file="/WEB-INF/views/jspf/footer.jspf" %>
  </body>
 </html>




