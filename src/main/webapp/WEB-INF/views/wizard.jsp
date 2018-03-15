<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/jspf/taglibs.jspf" %>
<%@ page import="edu.ucar.unidata.rosetta.service.ServerInfoBean" %>
<!DOCTYPE HTML>
 <html>
  <head>
   <title><spring:message code="global.title"/></title>
   <%@ include file="/WEB-INF/views/jspf/css.jspf" %>
   <%@ include file="/WEB-INF/views/jspf/javascript.jspf" %>
    <c:set var="baseUrl" value="${pageContext.request.contextPath}" />
   <script type="text/javascript">
    $(document).ready(function(){
        $("#FORM").rosettaWizard({ submitButton: 'SaveAccount' })
    });
   </script>
  </head>
  <body>
   <%@ include file="/WEB-INF/views/jspf/header.jspf" %>
   <form id="FORM" action="/rosetta/ffff" method="POST" enctype="multipart/form-data">
    <fieldset>
     <legend><spring:message code="step0.title"/></legend>
     <p><spring:message code="step0.description"/></p>
    </fieldset>

    <fieldset>
     <legend><spring:message code="step1.title"/></legend>
     <p><spring:message code="step1.description"/></p>
    </fieldset>

    <fieldset>
     <legend><spring:message code="step2.title"/></legend>
     <p><spring:message code="step2.description"/></p>
     <button type="button" id="quickSaveButton"/>Quick Save</button>
    </fieldset>

    <fieldset>
     <legend><spring:message code="step3.title"/></legend>
     <p><spring:message code="step3.description"/></p>
     <button type="button" id="quickSaveButton"/>Quick Save</button>
     <button type="button" id="showHeaderButton"/>Show Header</button>
    </fieldset>

    <fieldset>
     <legend><spring:message code="step4.title"/></legend>
     <p><spring:message code="step4.description"/></p>
     <button type="button" id="quickSaveButton"/>Quick Save</button>
     <button type="button" id="showHeaderButton"/>Show Header</button>
    </fieldset>

    <fieldset>
     <legend><spring:message code="step5.title"/></legend>
     <p><spring:message code="step5.description"/></p>
    </fieldset>

    </form> 
   <%@ include file="/WEB-INF/views/jspf/footer.jspf" %>
  </body>
 </html>




