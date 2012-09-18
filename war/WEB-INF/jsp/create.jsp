<!DOCTYPE HTML>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
 <html>
  <head>
   <title>&rho;&zeta;&eta;&tau;&alpha;</title>
   <link type="text/css" rel="stylesheet" href="resources/css/jquery-ui.css" />
   <link type="text/css" rel="stylesheet" href="resources/css/jWizard.base.css" />
   <link type="text/css" rel="stylesheet" href="resources/css/upload.css" />
   <link type="text/css" rel="stylesheet" href="resources/css/SlickGrid/slick.grid.css" />
   <link type="text/css" rel="stylesheet" href="resources/css/SlickGrid/slick.headermenu.css"/>
   <link type="text/css" rel="stylesheet" href="resources/css/SlickGrid/slick.headerbuttons.css"/>
   <link type="text/css" rel="stylesheet" href="resources/css/SlickGrid/examples.css" />



<style type="text/css">

  body {
      font-size: 10pt;
  }


  input {
    margin-right: 20px;
  }

  .hideMe {
    display:none !important;
  }

  .disabled {
    color: #CFCFCF !important;
    border: 1px solid #D3D3D3;
    background: #E6E6E6 url('resources/img/ui-bg_glass_75_e6e6e6_1x400.png') 50% 50% repeat-x; 
    font-weight: normal; 
    padding: .5em 1em;
    margin-left: 1em;
  }

  .columnDisabled {
    color: #CFCFCF !important;
    background: #E6E6E6 !important;
  }

  label.error {
    color: #FF0000;
  }

  .ui-autocomplete {
    font-size: 8pt;
    max-height: 300px;
    overflow-y: auto;
    /* prevent horizontal scrollbar */
    overflow-x: hidden;
    /* add padding to account for vertical scrollbar */
    padding-right: 20px;
  }


  .jw-menu li {
    padding: .25em .5em;
    margin-bottom: .25em;
    cursor: pointer;
    white-space:nowrap;
    border: none !important;
  }


  .jw-menu .jw-active {
    background: #FFFFFF url('resources/img/step.png') 0 5px no-repeat !important; 
    padding-left: 20px; 
  }

  .jw-menu .jw-current {
    background: #FFFFFF url('resources/img/current.png') 0 5px no-repeat !important; 
    padding-left: 20px; 
    font-weight: bold;
  }

  .jw-menu .jw-inactive {
    background: #FFFFFF url('resources/img/step.png') 0 5px no-repeat !important; 
    padding-left: 20px; 
  }



  .slick-header-button {
    background-image: none !important; 
    margin: 0 0 0 2px;
    padding: 0;
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 16px;
    height: 20px;
    cursor: pointer;
    display: inline-block;
    cursor: pointer;
  }

  .todo {
    background: transparent url('resources/img/todo.png') 0 2px no-repeat !important; 
  }

  .done {
    background: transparent url('resources/img/tick.png') 0 2px no-repeat !important; 
  }
  .metadata span.slick-column-name {
    position: absolute !important;
    left: 20px;
  }

  ul {
    width: 100%;
    list-style: none;
    margin: 0 0 10px 0;
    padding: 0;
    float: left;
    position: relative; 
  }

  ul li {
    width: 33%;
    display: inline;
    margin: 2px 0 0 0;
    padding: 0;   
    float: left; 
  }




  .max ol{
    width: 350px;
    list-style: none;
    margin: 0 0 10px 0;
    padding: 0;
    float: left;
    position: relative; 
  }

   .max ol li {
    width: 90%;
    display: block;
    margin: 2px 0 0 0;
    padding: 0;   
    float: left; 
  }

  .max ol li input{
     float: right; 
     width: 175px;
  }

  .max ol li textarea{
     float: right; 
     width: 200px;
  }

  .max {
     width: 750px;
  }

  .left {
     float: left; 
  }

  .right {
     float: right; 
  }


  #dialog {
    font-size: 9pt;
  }

  #dialog label {
    font-size: 8pt;
    margin: 0;
    padding: 0 10px 0 0; 
  }

  #dialog ul {
    width: 100%;
    list-style: none;
    margin: 0 0 10px 0;
    padding: 0;
    float: left;
    position: relative; 
  }

  #dialog ul li {
    width: 33%;
    display: inline;
    margin: 2px 0 0 0;
    padding: 0;   
    float: left; 
  }

  #dialog ol {
    width: 100%;
    list-style: none;
    margin: 0 0 10px 0;
    padding: 0;
    float: left;
    position: relative; 
  }

  #dialog ol li {
    width: 90%;
    display: block;
    margin: 2px 0 0 0;
    padding: 0;   
    float: left; 
  }

  #dialog ol li input{
     float: right; 
     width: 175px;
  }

  #dialog img {
    margin: 0 10px 0 0;
    padding: 0;   
  }

  #dialog .error {
    display: block;   
  }

.jw-header {
    position: absolute;
    left: 240px;
    top: 75px;
	height: 20px;
   padding: 0;
   margin: 0;
   background: none;
   border: none;
}

.jw-footer {
    position: absolute;
    left: 0px;
    top: 350px;
   padding: 0;
   margin: 0 0 0 10px;
   background: none;
   border: none;
}

.jw-header h2 {
		margin: 0;
		padding: .3em 0;
   font-size: 11pt;
}

	.jw-menu {
		border-right: 1px solid #CFCFCF;
	}

.jw-steps-wrap {
   overflow: auto;
   padding: 0 0 0 20px;
   margin: 40px 0 0 0;
  
}

</style> 

<script type="text/javascript">
	
$.metadata.setType("attr", "validate");

</script>


  </head>
  <body> 
   <h1>&rho;&zeta;&eta;&tau;&alpha;</h1>
   <form id="FORM" action="/pzhta/upload" method="POST" enctype="multipart/form-data">


    <div id="step1" title="Select Data Source"> 
     <label for="cfType" class="error"></label> 
     <div class="max">
      <ul>
       <li><label>Station<input type="radio" name="cfType" value="Station" validate="required:true"/></label></li>
       <li><label>Moored Buoy<input type="radio" name="cfType" value="Moored Buoy" validate="required:true"/></label></li>
       <li><label>Radiosonde<input type="radio" name="cfType" value="Radiosonde" validate="required:true"/></label></li>
       <li><label>Wind Profiler<input type="radio" name="cfType" value="Wind Profile" validate="required:true"/></label></li>
       <li><label>Aircraft<input type="radio" name="cfType" value="Aircraft" validate="required:true"/></label></li>
       <li><label>Ship<input type="radio" name="cfType" value="Ship" validate="required:true"/></label></li>
       <li><label>Vehicle<input type="radio" name="cfType" value="Vehicle" validate="required:true"/></label></li>
       <li><label>Dropsonde<input type="radio" name="cfType" value="Dropsonde" validate="required:true"/></label></li>
      </ul>
     </div>
    </div>

    <div id="step2" title="Upload ASCII File">
	<input id="file" name="file" type="file" value=""/>
	<input type="button" id="upload" value="Upload" />
	<p><span id="progress" class="progress">0%</span>  <button id="clearFileUpload" type="button" class="hideMe">Clear file upload</button></p>
        <label for="file" class="error"></label>
    </div> 

    <div id="step3" title="Specify Header Lines">
     <p><span id="uniqueId"></span></p>
     <h5>Indicate which lines are header lines:</h5>
     <label for="myGrid" class="error"></label>
     <div id="myGrid"></div>
    </div> 

    <div id="step4" title="Specify Delimiters">
     <p><span id="uniqueId"></span></p>
     <h5>Please specify delimiters used:</h5>
        <label for="delimiter" class="error"></label>
        <label>Tab <input type="checkbox" name="delimiter" id="delimiter" value="Tab" validate="required:true"/></label>
        <label>Comma <input type="checkbox" name="delimiter" id="delimiter" value="Comma" validate="required:true"/></label>
        <label>Space <input type="checkbox" name="delimiter" id="delimiter" value="Space" validate="required:true"/></label>    
        <label>Semicolon <input type="checkbox" name="delimiter" id="delimiter" value="Semicolon" validate="required:true"/></label> 
        <label>Double Quote <input type="checkbox" name="delimiter" id="delimiter" value="Double Quote" validate="required:true"/></label>    
        <label>Single Quote <input type="checkbox" name="delimiter" id="delimiter" value="Single Quote" validate="required:true"/></label>
        <label>Other <input type="checkbox" name="delimiter" id="delimiter" value="Other" validate="required:true"/> </label> <input type="text" id="otherDelimiter" name="otherDelimiter" size="1" maxlength="1" class="hideMe"/><br />

     
    </div> 

    <div id="step5" title="Specify Variable Names">
     <label for="myGrid" class="error"></label>    
     <div id="myGrid"></div>
    </div> 

    <div id="step6" title="Specify Variable Units">
     <label for="myGrid" class="error"></label>    
     <div id="myGrid"></div>
    </div> 

    <div id="step7" title="Specify Variable Metadata">
     <label for="myGrid" class="error"></label>    
     <div id="myGrid"></div>
     <div id="dialog"></div>
    </div> 

    <div id="step8" title="Specify Station Information">
     <p>* = required</p>
      <div class="max">
       <div class="left">
        <ol>
         <li><label for="station_name" class="error"></label><label>Station Name* <input type="text" name="station_name" value="" validate="required:true"/></label></li>             
         <li><label for="latitude" class="error"></label><label>Latitude* <input type="text" name="latitude" value="" validate="required:true"/></label></li>
         <li><label for="lat_units" class="error"></label><label>Latitude Units* <input type="text" name="lat_units" value="" validate="required:true"/></label></li>
         <li><label for="longitude" class="error"></label><label>Longitude* <input type="text" name="longitude" value="" validate="required:true"/></label></li>
         <li><label for="lon_units" class="error"></label><label>Longitude Units* <input type="text" name="lon_units" value="" validate="required:true"/></label></li>
         <li><label for="altitude" class="error"></label><label>Altitude* <input type="text" name="altitude" value="" validate="required:true"/></label></li>
         <li><label for="alt_units" class="error"></label><label>Altitude Units* <input type="text" name="alt_units" value="" validate="required:true"/></label></li>
        </ol>
       </div>
      </div>
    </div> 




    <div id="step9" title="Specify Global Metadata">
     <p>* = required</p>
      <div class="max">
       <div class="left">
        <ol>
         <li><label for="title" class="error"></label><label>Title* <input type="text" name="title" value="" validate="required:true"/></label></li>
         <li><label for="institution" class="error"></label><label>Institution* <input type="text" name="institution" value="" validate="required:true"/></label></li>
         <li><label>Processor <input type="text" name="processor" value=""/></label></li>
         <li><label>Version <input type="text" name="version" value=""/></label></li>
         <li><label>Source <input type="text" name="source" value=""/></label></li>
        </ol>
       </div>
       <div class="right">
        <ol>
         <li><label for="description" class="error"></label><label>Description* <textarea rows="2" cols="20" name="description" validate="required:true"></textarea></label></li>
         <li><label>Comment <textarea rows="2" cols="20" name="comment"></textarea></label></li>
         <li><label>History <textarea rows="2" cols="20" name="history"></textarea></label></li>
         <li><label>References <textarea rows="2" cols="20" name="references"></textarea></label></li>
        </ol>
       </div>
      </div>
    </div> 

    <div id="step10" title="Download Files">
      <div class="max">
       <ol></ol>
      </div>
    </div>
   </form>





   <script type="text/javascript" src="resources/js/jquery/jquery-1.7.2.min.js"></script>
   <script type="text/javascript" src="resources/js/jquery/jquery-ui-1.8.20.custom.min.js"></script>

  <!-- validation -->
   <script type="text/javascript" src="resources/js/jquery/jquery.metadata.js" ></script>
   <script type="text/javascript" src="resources/js/jquery/jquery.validate.js" ></script>
  <!-- jWizard -->
   <script type="text/javascript" src="resources/js/jquery/jWizard/jquery.jWizard.js"></script>
   <script type="text/javascript" src="resources/js/main.js"></script>
  <!-- file upload -->
   <script type="text/javascript" src="resources/js/uploader.js"></script>

  <!-- slick grid -->
  <script src="resources/js/jquery/jquery.event.drag-2.0.min.js"></script>
  <script src="resources/js/jquery/jquery.jsonp-1.1.0.min.js"></script>
  <script src="resources/js/jquery/SlickGrid/slick.checkboxselectcolumn.js"></script>
  <script src="resources/js/jquery/SlickGrid/slick.rowselectionmodel.js"></script>
  <script src="resources/js/jquery/SlickGrid/slick.core.js"></script>
  <script src="resources/js/jquery/SlickGrid/slick.grid.js"></script>
  <script src="resources/js/jquery/SlickGrid/slick.dataview.js"></script>
  <script src="resources/js/jquery/SlickGrid/slick.headermenu.js"></script>
  <script src="resources/js/jquery/SlickGrid/slick.headerbuttons.js"></script>
  <script src="resources/js/jquery/SlickGrid/slick.cellselectionmodel.js"></script>
  <script src="resources/js/jquery/SlickGrid/slick.cellrangeselector.js"></script>
  <script src="resources/js/jquery/SlickGrid/slick.cellrangedecorator.js"></script>
  <script src="resources/js/parse.js"></script>

  <!-- auto complete, dialog & effects -->
  <script type="text/javascript" src="resources/js/jquery/jquery.ui.core.js"></script>
  <script type="text/javascript" src="resources/js/jquery/jquery.ui.widget.js"></script>
  <script type="text/javascript" src="resources/js/jquery/jquery.ui.position.js"></script>
  <script type="text/javascript" src="resources/js/jquery/jquery.ui.autocomplete.js"></script>
  <script type="text/javascript" src="resources/js/jquery/jquery.ui.mouse.js"></script>
  <script type="text/javascript" src="resources/js/jquery/jquery.ui.button.js"></script>
  <script type="text/javascript" src="resources/js/jquery/jquery.ui.draggable.js"></script>
  <script type="text/javascript" src="resources/js/jquery/jquery.ui.resizable.js"></script>
  <script type="text/javascript" src="resources/js/jquery/jquery.ui.dialog.js"></script>
  <script type="text/javascript" src="resources/js/jquery/jquery.effects.core.js"></script>
  <script type="text/javascript" src="resources/js/jquery/jquery.bgiframe-2.1.2.js"></script>

  </body>
 </html>
