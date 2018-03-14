<!DOCTYPE HTML>
<%@ page import="edu.ucar.unidata.rosetta.service.ServerInfoBean" %>
 <html>
  <head>
   <title>Rosetta</title>
   <%@ include file="/WEB-INF/views/includes/css.jsp" %>
   <%@ include file="/WEB-INF/views/includes/javascript.jsp" %>
   <script type="text/javascript">
    $(document).ready(function(){
        $("#FORM").rosettaWizard({ submitButton: 'SaveAccount' })
    });
   </script>
  </head>
  <body>
    <%@ include file="/WEB-INF/views/jspf/header.jspf" %>
    <form id="FORM" action="/rosetta/upload" method="POST" enctype="multipart/form-data">

     <%-- landing page & intro --%>
     <fieldset>
       <legend>Account information</legend>
        <%@ include file="/WEB-INF/views/jspf/mainMenu.jspf" %>
     </fieldset>

     <fieldset>
        <legend>two</legend>
        <-- input fields -->
     </fieldset>

     <fieldset>
        <legend>three</legend>
        <-- input fields -->
     </fieldset>

     <fieldset>
        <legend>four</legend>
        <-- input fields -->
     </fieldset>
     <input type="button" value="Quick Save" id="quickSaveButton" onclick="quickSave()" alt="Quick Save"/><br/>
    </form> 

   <%@ include file="/WEB-INF/views/jspf/footer.jspf" %>
  </body>
 </html>




