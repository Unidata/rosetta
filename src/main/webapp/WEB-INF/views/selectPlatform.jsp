<%@ include file="/WEB-INF/views/includes/taglibs.jsp" %>
<label for="cfType" class="error"></label>
<c:choose>
    <c:when test="${fn:length(platforms) gt 0}">
        <ul>
            <c:forEach items="${platforms}" var="platform">
                <li>
                    <label>
                        <img src="<c:out value="${platform.img}" />"
                             alt="<c:out value="${platform.name}" />">
                        <input type="radio" name="cfType" value="<c:out value="${platform.type}" />"
                               validate="required:true"/><c:out value="${platform.name}"/>
                    </label>
                </li>
            </c:forEach>
        </ul>
    </c:when>
    <c:otherwise>
        <!-- insert error handling -->
    </c:otherwise>
</c:choose>
