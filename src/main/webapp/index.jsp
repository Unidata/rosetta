<!DOCTYPE HTML>
<%@ page import="edu.ucar.unidata.rosetta.service.ServerInfoBean" %>
 <html>
  <head>
   <title>Rosetta</title>
   <%@ include file="/WEB-INF/views/includes/css.jsp" %>
   <%@ include file="/WEB-INF/views/includes/javascript.jsp" %>
  </head>
  <body>
    <%@ include file="/WEB-INF/views/jspf/header.jspf" %>
     <%-- landing page & intro --%>
     <div class="left nodec">
      <h3>What would you like to do?</h3>
      <form id="FORM" action="/rosetta/template" method="POST">
        <ul class="template">
         <li><input type="radio" name="name" id="create"/><label for="create">Create a new template</label></li>
         <li><input type="radio" name="name" id="createRegex"/><label for="createRegexp">Create a new template using regular expressions to mine the data file header</label></li>
         <li><input type="radio" name="name" id="restore"/><label for="restore">Upload, modify, and use an existing template</label></li>
         <li><input type="radio" name="name" id="autoConvert"/><label for="autoConvert">Autoconvert a new data file and transform automatically</label></li>
         <li><input type="radio" name="name" id="augmentMetadata"/><label for="augmentMetadata">Augment the metadata of a known format type and transform automatically</label></li>
         <li><input type="radio" name="name" disabled id="autoTransform"/><label for="autoTransform">Upload template and new data file, transform automatically</label></li>
        </ul>
       <input type="submit" value="Get Started!" class="button"/>
      </form>
     </div>

     <div class="right">
      <h3>About Rosetta</h3>
      <p><i>Rosetta is Beta software under active development, use at your own risk.</i></p>
      <img src="resources/img/logo/rosetta-150x150.png" alt="Rosetta" class="right nodec"/>
      <p>Welcome to Rosetta, a data transformation tool. Rosetta is a web-based service that provides an easy, wizard-based interface for data collectors to transform their datalogger generated ASCII output into Climate and Forecast (CF) compliant netCDF files. These files will contain the metadata describing what data is contained in the file, the instruments used to collect the data, and other critical information that otherwise may be lost in one of many dreaded README files.</p>
      <p>In addition, with the understanding that the observational community does appreciate the ease of use of ASCII files, methods for transforming the netCDF back into a user defined CSV or spreadsheet formats are also incorporated into Rosetta.</p>
      <p> We hope that Rosetta will be of value to the science community users who have needs for transforming the data they have collected or stored in non-standard formats.</p>
      <p> Rosetta is currently under continued further development, and ready for beta testing.</p>
    </div>
   <%@ include file="/WEB-INF/views/jspf/footer.jspf" %>
  </body>
 </html>




