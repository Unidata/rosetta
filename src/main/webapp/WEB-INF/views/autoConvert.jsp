<!DOCTYPE HTML>
<%@ include file="/WEB-INF/views/includes/taglibs.jsp" %>
<%@ page import="edu.ucar.unidata.rosetta.service.ServerInfoBean" %>
<html>
<head>
    <title><spring:message code="global.title"/></title>
    <%@ include file="/WEB-INF/views/includes/css.jsp" %>
    <link type="text/css" rel="stylesheet" href="resources/css/create.css"/>
    <%@ include file="/WEB-INF/views/includes/javascript.jsp" %>
    <script type="text/javascript" src="resources/js/autoConvert.js"></script>
    <script type="text/javascript">
        var platformMetadata = [];
        var generalMetadata = [];
        var publisherInfo = [];
        $.metadata.setType("attr", "validate");
        var maxUploadSize = ${maxUploadSize};
    </script>
</head>
<body>
<h1><spring:message code="global.title"/></h1>
<form id="FORM" action="/rosetta/upload" method="POST" enctype="multipart/form-data">
    <div id="step0" title="<spring:message code="step1.title"/>">
        <h5><spring:message code="step1.description"/></h5>
        <%@include file="/WEB-INF/views/jspf/uploadKnownFile.jspf" %>
    </div>

    <div id="step1" title="Select known file type">
        <h5>"What type of file did you upload?"</h5>
        <%@include file="/WEB-INF/views/jspf/selectKnownFileType.jspf" %>
    </div>

    <div id="step2" title="<spring:message code="step7.title"/>">
        <h5><spring:message code="step7.description"/></h5>
        <%@include file="/WEB-INF/views/jspf/convertAndDownload.jspf" %>
    </div>

    <%@include file="/WEB-INF/views/jspf/footerNoQs.jspf" %>
</form>
</body>
</html>
