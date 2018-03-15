<%@ include file="/WEB-INF/views/includes/taglibs.jsp" %>
<script type="text/javascript">
    platformMetedataItems = {};
    units = {};
</script>
<% pageContext.setAttribute("newLineChar", "\n"); %>
<!-- TODO: Figure out how to not hardcode these and avoid the 3x duplicated code below Also,
     fix the possibility of adding line breaks to elements in the xml files -->
<c:forEach items="${platformMetadataItems}" var="platformMetadataItem">
	<script type="text/javascript">
        var type = "default";
        var obj = {};
        obj["tagName"] = "<c:out value="${platformMetadataItem.tagName.replaceAll(newLineChar,'')}" />";
        obj["displayName"] = "<c:out value="${platformMetadataItem.displayName.replaceAll(newLineChar,'')}" />";
        <c:choose>
            <c:when test="${platformMetadataItem.isRequired != null}">
                obj["isRequired"] = true;
            </c:when>
            <c:otherwise>
                obj["isRequired"] = false;
            </c:otherwise>
        </c:choose>
        <c:choose>
            <c:when test="${platformMetadataItem.units != null}">
                obj["units"] = "<c:out value="${platformMetadataItem.units.replaceAll(newLineChar,'')}" />";
            </c:when>
            <c:otherwise>
                obj["units"] = false;
            </c:otherwise>
        </c:choose>
            <c:choose>
            <c:when test="${platformMetadataItem.description != null}">
                obj["description"] = "<c:out value="${platformMetadataItem.description.replaceAll(newLineChar,'')}" />";
            </c:when>
            <c:otherwise>
                obj["description"] = false;
            </c:otherwise>
        </c:choose>
        if (!(type in platformMetedataItems)){
            platformMetedataItems[type] = [];
        }
        platformMetedataItems[type].push(obj);
    </script>
</c:forEach>
<c:forEach items="${platformMetadataProfileItems}"
	var="platformMetadataItem">
	<script type="text/javascript">
        var type = "profile";
        var obj = {};
        obj["tagName"] = "<c:out value="${platformMetadataItem.tagName.replaceAll(newLineChar,'')}" />";
        obj["displayName"] = "<c:out value="${platformMetadataItem.displayName.replaceAll(newLineChar,'')}" />";
        <c:choose>
            <c:when test="${platformMetadataItem.isRequired != null}">
                obj["isRequired"] = true;
            </c:when>
            <c:otherwise>
                obj["isRequired"] = false;
            </c:otherwise>
        </c:choose>
        <c:choose>
            <c:when test="${platformMetadataItem.units != null}">
                obj["units"] = "<c:out value="${platformMetadataItem.units.replaceAll(newLineChar,'')}" />";
            </c:when>
            <c:otherwise>
                obj["units"] = false;
            </c:otherwise>
        </c:choose>
            <c:choose>
            <c:when test="${platformMetadataItem.description != null}">
                obj["description"] = "<c:out value="${platformMetadataItem.description.replaceAll(newLineChar,'')}" />";
            </c:when>
            <c:otherwise>
                obj["description"] = false;
            </c:otherwise>
        </c:choose>
        if (!(type in platformMetedataItems)){
            platformMetedataItems[type] = [];
        }
        platformMetedataItems[type].push(obj);
    </script>
</c:forEach>
<c:forEach items="${platformMetadataTrajectoryItems}"
	var="platformMetadataItem">
	<script type="text/javascript">
        var type = "trajectory";
        var obj = {};
        obj["tagName"] = "<c:out value="${platformMetadataItem.tagName.replaceAll(newLineChar,'')}" />";
        obj["displayName"] = "<c:out value="${platformMetadataItem.displayName.replaceAll(newLineChar,'')}" />";
        <c:choose>
            <c:when test="${platformMetadataItem.isRequired != null}">
                obj["isRequired"] = true;
            </c:when>
            <c:otherwise>
                obj["isRequired"] = false;
            </c:otherwise>
        </c:choose>
        <c:choose>
            <c:when test="${platformMetadataItem.units != null}">
                obj["units"] = "<c:out value="${platformMetadataItem.units.replaceAll(newLineChar,'')}" />";
            </c:when>
            <c:otherwise>
                obj["units"] = false;
            </c:otherwise>
        </c:choose>
            <c:choose>
            <c:when test="${platformMetadataItem.description != null}">
                obj["description"] = "<c:out value="${platformMetadataItem.description.replaceAll(newLineChar,'')}" />";
            </c:when>
            <c:otherwise>
                obj["description"] = false;
            </c:otherwise>
        </c:choose>
        if (!(type in platformMetedataItems)){
            platformMetedataItems[type] = [];
        }
        platformMetedataItems[type].push(obj);
    </script>
</c:forEach>
<c:forEach items="${units}" var="unit">
	<script type="text/javascript">
        var options = "";
        <c:forEach items="${unit.value}" var="val">
        options += "<option value=\"<c:out value="${val}" />\"><c:out value="${val}" /></option>";
        </c:forEach>
        units["${unit.name}"] = options;
    </script>
</c:forEach>
<div id="platformMetadataDiv"></div>
