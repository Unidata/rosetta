<%@ include file="/WEB-INF/views/includes/taglibs.jsp" %>
<c:choose>
    <c:when test="${fn:length(globalMetadataItems) gt 0}">
        <ul>
            <c:forEach items="${globalMetadataItems}" var="globalMetadataItem">
                <script type="text/javascript">
                    var obj = {};
                    obj["tagName"] = "<c:out value="${globalMetadataItem.tagName}" />";
                    obj["displayName"] = "<c:out value="${globalMetadataItem.displayName}" />";
                    <c:choose>
                    <c:when test="${globalMetadataItem.isRequired != null}">
                    obj["isRequired"] = true;
                    </c:when>
                    <c:otherwise>
                    obj["isRequired"] = false;
                    </c:otherwise>
                    </c:choose>
                    <c:choose>
                    <c:when test="${globalMetadataItem.units != null}">
                    obj["units"] = true;
                    </c:when>
                    <c:otherwise>
                    obj["units"] = false;
                    </c:otherwise>
                    </c:choose>
                    generalMetadata.push(obj);
                </script>
                <li>
                    <label>
                        <c:choose>
                            <c:when test="${globalMetadataItem.isRequired}">
                                *
                            </c:when>
                        </c:choose>
                        <c:out value="${globalMetadataItem.displayName}"/>
                        <c:choose>
                            <c:when test="${globalMetadataItem.description != null}">
                                <img src="resources/img/help.png"
                                     alt="<c:out value="${globalMetadataItem.description}" />"/>
                            </c:when>
                        </c:choose>
                        <br/>
                        <input type='checkbox' name="${globalMetadataItem.tagName}"> is a regex
                        <br/>
                        <input type="text" name="<c:out value="${globalMetadataItem.tagName}" />"
                               value=""/>
                    </label>
                    <label class="error"></label>
                </li>
            </c:forEach>
            <div id="containerForCustomAttributes"/>
        </ul>
    </c:when>
    <c:otherwise>
        <!-- insert error handling -->
    </c:otherwise>
</c:choose>
