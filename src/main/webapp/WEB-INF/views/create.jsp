<!DOCTYPE HTML>
<%@ include file="/WEB-INF/views/includes/taglibs.jsp" %>
<%@ page import="edu.ucar.unidata.rosetta.service.ServerInfoBean"%>
 <html>
  <head>
   <title><spring:message code="global.title"/></title>
<%@ include file="/WEB-INF/views/includes/css.jsp" %>
    <link type="text/css" rel="stylesheet" href="resources/css/create.css" />
<%@ include file="/WEB-INF/views/includes/javascript.jsp" %>
    <script type="text/javascript" src="resources/js/create.js"></script>

    <script type="text/javascript">
	    var platformMetadata = [];
	    var generalMetadata = [];
        var publisherInfo = [];
        $.metadata.setType("attr", "validate");
    </script>
 
  </head>
  <body> 
   <h1><spring:message code="global.title"/></h1>
   <form id="FORM" action="/rosetta/upload" method="POST" enctype="multipart/form-data">

    <div id="step0" title="<spring:message code="step0.title"/>">     
     <h5><spring:message code="step0.description"/></h5>  
     <label for="cfType" class="error"></label> 
     <c:choose>
      <c:when test="${fn:length(platforms) gt 0}">
       <ul>
        <c:forEach items="${platforms}" var="platform">
         <li>
          <label>
           <img src="<c:out value="${platform.img}" />" alt="<c:out value="${platform.name}" />">
           <input type="radio" name="cfType" value="<c:out value="${platform.type}" />" validate="required:true"/><c:out value="${platform.name}" />
          </label>          
         </li>
        </c:forEach>
       </ul>
      </c:when>
      <c:otherwise>
       <!-- insert error handling -->
      </c:otherwise>
     </c:choose>
    </div>

    <div id="step1" title="<spring:message code="step1.title"/>">
     <h5><spring:message code="step1.description"/></h5>
     <label for="file" class="error"></label>
     <input id="file" name="file" type="file" value=""/>
     <input type="button" id="upload" value="Upload" />
	 <p><span id="progress" class="progress">0%</span>  <button id="clearFileUpload" type="button" class="hideMe">Clear file upload</button></p>
     <div id="notice"></div>
    </div> 

    <div id="step2" title="<spring:message code="step2.title"/>">
     <h5><spring:message code="step2.description"/></h5>
     <label for="headerLineGrid" class="error"></label>
     <div id="headerLineGrid" class="rosettaGrid"></div>
    </div> 

    <div id="step3" title="<spring:message code="step3.title"/>">
     <h5><spring:message code="step3.description"/></h5>  
     <label for="delimiter" class="error"></label> 
     <c:choose>
      <c:when test="${fn:length(delimiters) gt 0}">
       <ul>
        <c:forEach items="${delimiters}" var="delimiter">
         <li>
          <label>
           <input type="checkbox" name="delimiter" id="delimiter" value="<c:out value="${delimiter.name}" />" validate="required:true"/>
           <c:out value="${delimiter.name}" />
          </label>          
         </li>
        </c:forEach>
         <li>
          <label>
           <input type="checkbox" name="delimiter" id="delimiter" value="Other" validate="required:true"/> Other   
          </label> 
          <label>
           <input type="text" id="otherDelimiter" name="otherDelimiter" size="1" maxlength="1" class="hideMe"/> 
          </label>
         </li>
       </ul>
      </c:when>
      <c:otherwise>
       <!-- insert error handling -->
      </c:otherwise>
     </c:choose>   
   <!--  <div id="delimiterGrid" class="rosettaGrid"></div>  -->
    </div> 

    <div id="step4" title="<spring:message code="step4.title"/>">
     <h5><spring:message code="step4.description"/></h5>
     <label for="variableGrid" class="error"></label>    
     <div id="variableGrid" class="rosettaGrid"></div>
     <div id="dialog"></div>
    </div> 

    <div id="step5" title="<spring:message code="step5.title"/>">
     <h5><spring:message code="step5.description"/></h5>
     <c:choose>
      <c:when test="${fn:length(platformMetadataItems) gt 0}">
       <ul>
        <c:forEach items="${platformMetadataItems}" var="platformMetadataItem">    
         <script type="text/javascript">
             var obj = {};
             obj["tagName"] = "<c:out value="${platformMetadataItem.tagName}" />";
             obj["displayName"] = "<c:out value="${platformMetadataItem.displayName}" />";
             obj["isRequired"] = <c:out value="${platformMetadataItem.isRequired}" />;
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
                  obj["units"] = true;
              </c:when>
              <c:otherwise>
                  obj["units"] = false;
              </c:otherwise>
             </c:choose>
             platformMetadata.push(obj);
         </script>  
         <li>
          <label>
           <c:choose>
            <c:when test="${platformMetadataItem.isRequired}">
             *
            </c:when>
           </c:choose>
           <c:out value="${platformMetadataItem.displayName}" />
           <c:choose>
            <c:when test="${platformMetadataItem.description != null}">
              <img src="resources/img/help.png" alt="<c:out value="${platformMetadataItem.description}" />"/>
            </c:when>
           </c:choose>
           <br/>
           <input type="text" name="<c:out value="${platformMetadataItem.tagName}" />" value="" />
           <c:choose>
            <c:when test="${platformMetadataItem.units != null}">
             <c:choose>
              <c:when test="${fn:length(units) gt 0}">
               <c:forEach items="${units}" var="unit">
                <c:choose>
                 <c:when test="${platformMetadataItem.units == unit.name}">
                  <select name="<c:out value="${platformMetadataItem.tagName}" />Units">
                   <c:forEach items="${unit.value}" var="val">
                    <option value="<c:out value="${val}" />"><c:out value="${val}" /></option>
                   </c:forEach>
                  </select>
                 </c:when>
                </c:choose>
               </c:forEach>
              </c:when>
              <c:otherwise>
               <c:out value="${platformMetadataItem.displayName}" /> Units *
               <input type="text" name="<c:out value="${platformMetadataItem.tagName}" />Units" value="" />
              </c:otherwise>
             </c:choose> 
            </c:when>
           </c:choose>
          </label>   
          <label for="<c:out value="${platformMetadataItem.tagName}" />" class="error"></label>        
         </li>
        </c:forEach>
       </ul>
      </c:when>
      <c:otherwise>
       <!-- insert error handling -->
      </c:otherwise>
     </c:choose> 
    </div> 

    <div id="step6" title="<spring:message code="step6.title"/>">
     <h5><spring:message code="step6.description"/></h5>
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
           <c:out value="${globalMetadataItem.displayName}" />
           <c:choose>
            <c:when test="${globalMetadataItem.description != null}">
              <img src="resources/img/help.png" alt="<c:out value="${globalMetadataItem.description}" />"/>
            </c:when>
           </c:choose>
           <br/>
           <input type="text" name="<c:out value="${globalMetadataItem.tagName}" />" value="" />
          </label>     
          <label for="<c:out value="${globalMetadataItem.tagName}" />" class="error"></label>       
         </li>
        </c:forEach>
       </ul>
      </c:when>
      <c:otherwise>
       <!-- insert error handling -->
      </c:otherwise>
     </c:choose> 
    </div> 

    <div id="step7" title="<spring:message code="step7.title"/>">
     <h5><spring:message code="step7.description"/></h5>
     <ul id="download">
     </ul>
    </div>

    <div id="step8" title="<spring:message code="step8.title"/>">
        <h5><spring:message code="step8.description"/></h5>
        <select id="publisherName">
            <c:forEach items="${publishers}" var="publisher">
                <option> <c:out value="${publisher.pubName}" /> </option>
            </c:forEach>
        </select>
        <form>
            <br>
            User Name: <input type="text" id="userName"><br>
            Password: <input type="password" id="userPassword">
            <br>
        </form>
        <br>
        <input type="button" id="publish" value="Publish" />
        <div id="notice"></div>
    </div>

   </form>
   <p>
     <table>
       <tr>
         <td> <img src="<spring:message code="global.logo.path"/>" alt="<spring:message code="global.logo.alt"/>" align="middle"/></td>
         <td>
           <i><spring:message code="global.footer"/>
           <br>
           Version : <%=ServerInfoBean.getVersion()%>
           <br>
           Build Date: <%=ServerInfoBean.getBuildDate()%> </i>
         </td>
       </tr>
     </table>
   </p>
  </body>
 </html>
