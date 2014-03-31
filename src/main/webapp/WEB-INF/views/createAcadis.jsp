<!DOCTYPE HTML>
<%@ include file="/WEB-INF/views/includes/taglibs.jsp" %>
<%@ page import="edu.ucar.unidata.rosetta.service.ServerInfoBean"%>
<html>
<head>
    <title><spring:message code="global.title"/></title>
    <%@ include file="/WEB-INF/views/includes/css.jsp" %>
    <link type="text/css" rel="stylesheet" href="resources/css/createAcadis.css" />
    <link type="text/css" rel="stylesheet" href="resources/img/acadis/integrate_acadis.css" />
    <%@ include file="/WEB-INF/views/includes/javascript.jsp" %>
    <script type="text/javascript" src="resources/js/createAcadis.js"></script>
    <script type="text/javascript">
        var platformMetadata = [];
        var generalMetadata = [];
        var publisherInfo = [];
        $.metadata.setType("attr", "validate");

        // incoming dataset id in json format
        var data = eval('('+'${dataJson}'+')');
    </script>
</head>
<body>
<a href="https://www.aoncadis.org/home.htm">
<div id="banner"></div> 
</a>
<!-- end of acadis banner wrapper -->
<h1 id="title"><spring:message code="global.title"/></h1>
<form id="FORM" action="/rosetta/upload" method="POST" enctype="multipart/form-data">

    <div id="step0" title="Please choose a file to convert">
        <%@include file="/WEB-INF/views/jspf/selectAcadisFile.jspf" %>
    </div>

    <div id="step1" title="<spring:message code="step0.title"/>">
        <h5><spring:message code="step0.description"/></h5>
        <%@include file="/WEB-INF/views/jspf/selectPlatform.jspf" %>
    </div>

    <div id="step2" title="<spring:message code="step2.title"/>">
        <h5><spring:message code="step2.description"/></h5>
        <%@include file="/WEB-INF/views/jspf/specifyHeaderLines.jspf" %>
    </div>

    <div id="step3" title="<spring:message code="step3.title"/>">
        <h5><spring:message code="step3.description"/></h5>
        <%@include file="/WEB-INF/views/jspf/specifyDelimiters.jspf" %>
    </div>

    <div id="step4" title="<spring:message code="step4.title"/>">
        <h5><spring:message code="step4.description"/></h5>
        <%@include file="/WEB-INF/views/jspf/specifyVariableMetadata.jspf" %>
    </div>

    <div id="step5" title="<spring:message code="step5.title"/>">
        <h5><spring:message code="step5.description"/></h5>
        <%@include file="/WEB-INF/views/jspf/specifyPlatformMetadata.jspf" %>
    </div>

    <div id="step6" title="<spring:message code="step6.title"/>">
        <h5><spring:message code="step6.description"/></h5>
        <%@include file="/WEB-INF/views/jspf/specifyGeneralMetadata.jspf" %>
    </div>

    <div id="step7" title="<spring:message code="step7.title"/>">
        <h5><spring:message code="step7.description"/></h5>
        <%@include file="/WEB-INF/views/jspf/convertAndDownload.jspf" %>
    </div>

    <div id="step8" title="<spring:message code="step8.title"/>">
        <h5><spring:message code="step8.description"/></h5>
        <%@include file="/WEB-INF/views/jspf/publish.jspf" %>
    </div>

    <%@include file="/WEB-INF/views/jspf/footerAcadis.jspf" %>
</form>
</body>
</html>
