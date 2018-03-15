<%@ include file="/WEB-INF/views/includes/taglibs.jsp" %>
        <img id="parsing-img" src="resources/img/pacman.gif">
        <div id="finished-parsing"></div>

        <h5><Add or Change the Metadata/></h5>
        <c:choose>
            <c:when test="${fn:length(etagGlobalMetadataItems) gt 0}">
                <ul class="globalMetadata">
                    <c:forEach items="${etagGlobalMetadataItems}" var="etagGlobalMetadataItem">
                        <script type="text/javascript">
                            var obj = {};
                            obj["tagName"] = "<c:out value="${etagGlobalMetadataItem.tagName}" />";
                            obj["displayName"] = "<c:out value="${etagGlobalMetadataItem.displayName}" />";
                            <c:choose>
                            <c:when test="${etagGlobalMetadataItem.isRequired != null}">
                            obj["isRequired"] = true;
                            </c:when>
                            <c:otherwise>
                            obj["isRequired"] = false;
                            </c:otherwise>
                            </c:choose>
                            <c:choose>
                            <c:when test="${etagGlobalMetadataItem.units != null}">
                            obj["units"] = true;
                            </c:when>
                            <c:otherwise>
                            obj["units"] = false;
                            </c:otherwise>
                            </c:choose>
                            generalMetadata.push(obj);
                        </script>
                        <li>
                            <c:choose>
                                <c:when test="${etagGlobalMetadataItem.isRequired}">
                                    <lable class="empty required">
                                </c:when>
                                <c:otherwise>
                                    <lable class="empty">
                                </c:otherwise>
                            </c:choose>
                                <c:choose>
                                    <c:when test="${etagGlobalMetadataItem.isRequired}">
                                        *
                                    </c:when>
                                </c:choose>
                                <c:out value="${etagGlobalMetadataItem.displayName}"/>
                                <c:choose>
                                    <c:when test="${etagGlobalMetadataItem.description != null}">
                                        <img src="resources/img/help.png"
                                             alt="<c:out value="${etagGlobalMetadataItem.description}" />"/>
                                    </c:when>
                                </c:choose>
                                <br/>
                                <input type="text" name="<c:out value="${etagGlobalMetadataItem.tagName}" />"
                                       value=""/>
                            </label>
                            <label class="error"></label>
                        </lable>
                    </c:forEach>
                    <div id="containerForCustomAttributes"/>
                </ul>
            </c:when>
            <c:otherwise>
                <!-- insert error handling -->
            </c:otherwise>
        </c:choose>
