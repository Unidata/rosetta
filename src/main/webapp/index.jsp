<!DOCTYPE HTML>
<%@ page import="edu.ucar.unidata.rosetta.service.ServerInfoBean"%>
 <html>
  <head>
    <style>
      h1 {font-size:250%;}
      h2 {font-size:200%;}
      p {font-size:150%;}
      td {
        text-align:left;
        vertical-align:bottom;
        padding:15px;
      }
    </style>
    <title>Rosetta</title>
  </head>
  <body>
  <p>
  <table>
    <tr>
      <td>
        <img src="resources/img/icon/rosetta_lighter_text.jpg" HEIGHT="75%" alt="&rho;&zeta;&eta;&tau;&alpha; &rarr; Rosetta">
      </td>
      <td>
        <h1>What would you like to do?</h1>
        <p>
        <a href="create"><img src="resources/img/add.png" alt="Works!" /></a>
        Create a new template
        <br>
        <a href="restore"><img src="resources/img/add.png" alt="Works!" /></a>
        Upload, modify, and use an existing template
        <br>
        <img src="resources/img/remove.png" alt="Future Work." />
        Upload template and new data file, transform automatically
        </p>
      </td>
    </tr>
    <tr>
      <td>
        <i>
        Version : <%=ServerInfoBean.getVersion()%>
        <br>
        Build Date: <%=ServerInfoBean.getBuildDate()%> </i>
        </i>
      </td>
    </tr>
  </table>
  </p>
</body>
</html>