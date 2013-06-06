<!DOCTYPE HTML>
<%@ include file="/WEB-INF/views/includes/taglibs.jsp" %>
<%@ page import="edu.ucar.unidata.rosetta.service.ServerInfoBean"%>
 <html>
  <head>
   <title><spring:message code="global.title"/></title>
<%@ include file="/WEB-INF/views/includes/css.jsp" %>
    <link type="text/css" rel="stylesheet" href="resources/css/restore.css" />
    <%@ include file="/WEB-INF/views/includes/javascript.jsp" %>
    <script type="text/javascript" src="resources/js/restore.js"></script>
  </head>

  <body> 
   <h1><spring:message code="global.title"/></h1>
   <form id="FORM" action="/rosetta/upload" method="POST" enctype="multipart/form-data">

    <div id="step0" title="<spring:message code="step0.template.title"/>">
     <h5><spring:message code="step0.template.description"/></h5>
     <label for="file" class="error"></label>
     <input id="file" name="file" type="file" value=""/>
     <input type="button" id="upload" value="Upload" class="hideMe"/>
	   <p><span id="progress" class="progress">0%</span>  <button id="clearFileUpload" type="button" class="hideMe">Clear file upload</button></p>
     <div id="notice"></div>
    </div>

     <div id="step1" title="<spring:message code="step1.template.title"/>">
       <h5><spring:message code="step1.template.description"/></h5>
       <label for="file" class="error"></label>
       <a href="create">Continue...</a><br>
       <div id="notice"></div>
     </div>

   </form>
   <p>
   <table>
     <tr>
       <td> <img src="<spring:message code="global.logo.path"/>" alt="<spring:message code="global.logo.alt"/>" align="middle"/></td>
       <td>
         <i><spring:message code="global.footer"/>
           <br>
           Version : <%=ServerInfoBean.getVersion()%>
           <br>
           Build Date: <%=ServerInfoBean.getBuildDate()%> </i>
       </td>
     </tr>
   </table>
   <br>
   <a href="<spring:message code="unidata.banner.url"/>">
     <img src="<spring:message code="unidata.banner.path"/>" alt="<spring:message code="unidata.banner.alt"/>" align="left"/>
   </a>
   </p>
  </body>
 </html>