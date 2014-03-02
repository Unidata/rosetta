<!DOCTYPE HTML>
<%@ include file="/WEB-INF/views/includes/taglibs.jsp" %>
<html>
    <head>
        <title>ACADIS Inventory</title>
        <%@ include file="/WEB-INF/views/includes/css.jsp" %>
        <%@ include file="/WEB-INF/views/includes/javascript.jsp" %>
        <script type="text/javascript">
            var data = eval('('+'${dataJson}'+')');
        </script>
        <script type="text/javascript" src="resources/js/acadis.js"></script>
    </head>
        <body>
        <h1>Please choose a file to convert:</h1>
        <br>
        <h3>Project Inventory:</h3>
        <!-- this is what happens upon submit: rosetta/createAcadis  -->
        <div id="inventory"></div>
        <input type="submit" value="Submit" id="getAcadisFile">
    </body>
</html>
