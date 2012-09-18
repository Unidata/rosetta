$(document).ready(function($) {
    var faux = '<div id="faux" class="ui-corner-all disabled" style="display: inline-block;">Next</div>';

    var $p = $('#progress'),
        $e = $('#step2'),
        $u = $("#upload"),
	up = new uploader($('#file').get(0), {
	    url:'/',
	    progress:function(ev){ console.log('progress'); $p.html(((ev.loaded/ev.total)*100)+'%'); $p.css('width','50%'); },
	    error:function(ev){ console.log('error'); $e.find("label.error").text("Error!  No file uploaded!"); return false; },
	    success:function(data){ console.log('success'); $u.addClass("hideMe");  $p.html('100%'); $p.effect("fade", 1000, progressBarCallback); addToSession("uniqueId", data); }
	});



    $w = $("#FORM");

    $w.validate();

    $w.jWizard({
        menuEnable: true,
        buttons : {
            finishType : "button",
            cancelHide: true
        },

        // The 4 functions below are callbacks... they are the last function to be executed before deciding whether or not to proceed
        cancel : function(event, ui) {
            $w.jWizard("firstStep");
        },
        previous : function(event, ui) {
          // insert code here
        },
        next : function(event, ui) {
          // insert code here
        },
        finish : function(event, ui) {
          $.post("parse", { uniqueId: sessionStorage.getItem('uniqueId'), fileName: sessionStorage.getItem('fileName'), otherDelimiter:  sessionStorage.getItem('otherDelimiter'), headerLineNumbers:  sessionStorage.getItem('headerLineNumbers'), delimiters:  sessionStorage.getItem('delimiters'), done: "true"},  
                   
                "text");
        }
    })

    /** The bindings below are event handlers, they will all be executed before proceeding to the callback */
    /** ui = {
            type: "previous|next|first|last|manual",
            currentStepIndex: [int],
            nextStepIndex: [int]
        }; */



    /* 
     EVENT HANDLER: Handling custom navigation through the wizard     
     */
    .bind("jwizardchangestep", function (event, ui) {
        // "manual" is always triggered by the user, never jWizard itself
        if (ui.type !== "manual") {          
           $("#faux").remove();
           $(".jw-button-next").addClass("hideMe").after(faux);

            // using currentStepIndex, we can intercept the user when they are *done* with a particular step
            switch (ui.currentStepIndex) {  
               
                
                case 0: // Select CF Type
                    // Validation: user must pick a CF type 
                    $userInput = $(".jw-step").find("input:checked");                   
                    if ($userInput.length < 1) {
                        $(".jw-step:eq(" + ui.currentStepIndex + ")").find("label.error").text("You Must specify a CF Type");
                        return false;
                    } else {
                        addToSession("cfType", $(".jw-step").find("input:checked").val());
                    }
                    break;


                case 1:  // Upload ASCII file
                    // Validation: file must be uploaded
                    if (ui.type == "next") {
                        if (!sessionStorage.uniqueId) {
                            $(".jw-step:eq(" + ui.currentStepIndex + ")").find("label.error").text("You need to upload an ascii file to continue");
                            return false;
                        } 
                    }   
                    break;

                case 2: // Specify Header Lines
                    // Validation: header lines must be specified
                    if (ui.type == "next") {
                        if (!sessionStorage.headerLineNumbers) {
                            $(".jw-step:eq(" + ui.currentStepIndex + ")").find("label.error").text("You need to specify which lines are header lines to continue");
                            return false;
                        } 
                    }   
                    break;

                case 3:  // Specify Delimiters
                    // Validation: must have at least one delimiter specified 
                    if (ui.type == "next") {
                        if (!sessionStorage.delimiters) {
                            $(".jw-step:eq(" + ui.currentStepIndex + ")").find("label.error").text("You need to specify at least one delimiter to continue");
                            return false;
                        } 
                        // if "Other" is specified for a delimiter, make sure they input something
                        if (sessionStorage.getItem('delimiters').search("Other") >= 0 ) {
                            if (!sessionStorage.otherDelimiter) {
                                $(".jw-step:eq(" + ui.currentStepIndex + ")").find("label.error").text("You Specified \"Other\" as a delimiter.  Please input that delimiter to continue");
                                return false;
                            }
                        }
                    }   
                    break;

                case 7:  // Specify Station Information
                     // Validation: must have the station name specified 
                     if (ui.type == "next") {
                        if (!sessionStorage.getItem("station_name")) {
                            $(".jw-step:eq(" + ui.currentStepIndex + ")").find("label[for=station_name]").text("You need to provide a station name.");
                            return false;
                        } 
                        // Validation: must have the latitude specified 
                        if (!sessionStorage.getItem("latitude")) {
                            $(".jw-step:eq(" + ui.currentStepIndex + ")").find("label[for=latitude]").text("You need to provide a latitude for the station.");
                            return false;
                        } 
                        // Validation: must have atitude units specified 
                        if (!sessionStorage.getItem("lat_units")) {
                            $(".jw-step:eq(" + ui.currentStepIndex + ")").find("label[for=lat_units]").text("You need to provide latitude units.");
                            return false;
                        } 
                        // Validation: must have the longitude specified 
                        if (!sessionStorage.getItem("longitude")) {
                            $(".jw-step:eq(" + ui.currentStepIndex + ")").find("label[for=longitude]").text("You need to provide a longitude for the station.");
                            return false;
                        }  
                        // Validation: must have longitude units specified 
                        if (!sessionStorage.getItem("lon_units")) {
                            $(".jw-step:eq(" + ui.currentStepIndex + ")").find("label[for=lon_units]").text("You need to provide longitude units.");
                            return false;
                        }
                        // Validation: must have the altitude specified 
                        if (!sessionStorage.getItem("altitude")) {
                            $(".jw-step:eq(" + ui.currentStepIndex + ")").find("label[for=altitude]").text("You need to provide a altitude for the station.");
                            return false;
                        }  
                        // Validation: must have altitude units specified 
                        if (!sessionStorage.getItem("alt_units")) {
                            $(".jw-step:eq(" + ui.currentStepIndex + ")").find("label[for=alt_units]").text("You need to provide altitude units.");
                            return false;
                        }
                    } 
                    break;

                case 8:  // Specify Global Metadata
                     // Validation: must have the title specified 
                     if (ui.type == "next") {
                        if (!sessionStorage.getItem("title")) {
                            $(".jw-step:eq(" + ui.currentStepIndex + ")").find("label[for=title]").text("You need to provide a title for this dataset.");
                            return false;
                        } 
                        // Validation: must have the institution specified 
                        if (!sessionStorage.getItem("institution")) {
                            $(".jw-step:eq(" + ui.currentStepIndex + ")").find("label[for=institution]").text("You need to provide a institution for this dataset.");
                            return false;
                        } 
                        // Validation: must have the description specified 
                        if (!sessionStorage.getItem("description")) {
                            $(".jw-step:eq(" + ui.currentStepIndex + ")").find("label[for=description]").text("You need to provide a description for this dataset.");
                            return false;
                        }  
                    } 
                    break;

            }          
        

        }

        // by using nextStepIndex, we can intercept the user when they are *about to start* on a particular step
        switch (ui.nextStepIndex) {   

            case 0:  // Select CF Type
                // If we land on this page and user has already enter something 
                // (e.g., clicked previous or used the menu to navigate)
                // Don't hide the 'Next' button
                if (ui.type == "previous") {
                    if (sessionStorage.cfType) {
                        $("#faux").remove();
                        $(".jw-button-next").removeClass("hideMe");
                    }
                }
/*
------------------------------------------
*/
sessionStorage.setItem("delimiters", "Comma");
sessionStorage.setItem("description", "description, bla bla bla");
sessionStorage.setItem("institution", "Institution");
sessionStorage.setItem("title", "title");
sessionStorage.setItem("headerLineNumbers", "0,1,2,3,4,5,6,7,8,9,10,11,12,13");
sessionStorage.setItem("variable0", "Do Not Use");
sessionStorage.setItem("variable0Metadata", "Do Not Use");
sessionStorage.setItem("variable0Unit", "Do Not Use");
sessionStorage.setItem("variable1", "time");
sessionStorage.setItem("variable1Metadata", "coordVar:yes,dataType:float,long_name:time from data logger,missing_value:-999,units:days since 1900-01-01,standard_name:time");
sessionStorage.setItem("variable1Unit", "days since 1900-01-01");
sessionStorage.setItem("variable2", "soil_temperature");
sessionStorage.setItem("variable2Metadata", "coordVar:no,dataType:float,long_name:surface temp,missing_value:-999,units:C,source:SN 102,standard_name:soil_temperature");
sessionStorage.setItem("variable2Unit", "C");
sessionStorage.setItem("variable3", "soil_temperature_2");
sessionStorage.setItem("variable3Metadata", "coordVar:no,dataType:float,long_name:surface temp to 25 centimeters,missing_value:-999,units:C,source:SN 103");
sessionStorage.setItem("variable3Unit", "C");
sessionStorage.setItem("variable4", "Do Not Use");
sessionStorage.setItem("variable4Metadata", "Do Not Use");
sessionStorage.setItem("variable4Unit", "Do Not Use");
sessionStorage.setItem("variable5", "Do Not Use");
sessionStorage.setItem("variable5Metadata", "Do Not Use");
sessionStorage.setItem("variable5Unit", "Do Not Use");
sessionStorage.setItem("variable6", "Do Not Use");
sessionStorage.setItem("variable6Metadata", "Do Not Use");
sessionStorage.setItem("variable6Unit", "Do Not Use");
sessionStorage.setItem("variable7", "Do Not Use");
sessionStorage.setItem("variable7Metadata", "Do Not Use");
sessionStorage.setItem("variable7Unit", "Do Not Use");
sessionStorage.setItem("variable8", "Do Not Use");
sessionStorage.setItem("variable8Metadata", "Do Not Use");
sessionStorage.setItem("variable8Unit", "Do Not Use");
sessionStorage.setItem("variable9", "Do Not Use");
sessionStorage.setItem("variable9Metadata", "Do Not Use");
sessionStorage.setItem("variable9Unit", "Do Not Use");
/*
------------------------------------------
*/



                break;


            case 1:  // Upload ASCII file
                // Get the cf-type value selected and display
                displaySelected("a[step=" + ui.currentStepIndex + "]", "cfType", "CF Type");

                // Initially hide the upload button (will appear when user opens file chooser)
                $("#upload").addClass("hideMe");

                // If we land on this page and user has already enter something 
                // (e.g., clicked previous or used the menu to navigate)    
                // Don't hide the 'Next' button           
                if ((ui.type == "previous") || (ui.type == "next")) {
                    if (sessionStorage.uniqueId) {
                        $("#file").addClass("hideMe");      
                        progressBarCallback();
                        $("#faux").remove();
                        $(".jw-button-next").removeClass("hideMe");
                    }
                }
                break;


            case 2:  // Specify Header Lines

                $.post("parse", { uniqueId: sessionStorage.getItem('uniqueId'), fileName: sessionStorage.getItem('fileName') },  
                    function(data) {
                       drawGrid(data, "3")
                    }, 
                "text");

                // If we land on this page and user has already enter something 
                // (e.g., clicked previous or used the menu to navigate)    
                // Don't hide the 'Next' button           
                if ((ui.type == "previous") || (ui.type == "next")) {
                    if (sessionStorage.headerLineNumbers) {
                        $("#faux").remove();
                        $(".jw-button-next").removeClass("hideMe");
                    }
                }
                break;


            case 3:  // Specify Delimiters

                // If we land on this page and user has already enter something 
                // (e.g., clicked previous or used the menu to navigate)    
                // Don't hide the 'Next' button           
                if ((ui.type == "previous") || (ui.type == "next")) {
                    if (sessionStorage.delimiters) {                        
                        $('#step4 #delimiter').each(function(){      
                            if (sessionStorage.getItem('delimiters').search($(this).val()) >= 0 ) {   
                                $(this).attr('checked', true);
                            } else {
                                $(this).attr('checked', false);
                            }
                        });
                        if (sessionStorage.otherDelimiter) {    
                            $('#otherDelimiter').val(sessionStorage.getItem('otherDelimiter'));
                        }
                        $("#faux").remove();
                        $(".jw-button-next").removeClass("hideMe");
                    }
                }

                break;

            case 4:  // Specify Variable Names
                $.post("parse", { uniqueId: sessionStorage.getItem('uniqueId'), fileName: sessionStorage.getItem('fileName'), otherDelimiter:  sessionStorage.getItem('otherDelimiter'), headerLineNumbers:  sessionStorage.getItem('headerLineNumbers'), delimiters:  sessionStorage.getItem('delimiters')},  
                    function(data) {
                       drawGrid(data, "5")
                    }, 
                "text");
                break;

            case 5:  // Specify Variable Units
                $.post("parse", { uniqueId: sessionStorage.getItem('uniqueId'), fileName: sessionStorage.getItem('fileName'), otherDelimiter:  sessionStorage.getItem('otherDelimiter'), headerLineNumbers:  sessionStorage.getItem('headerLineNumbers'), delimiters:  sessionStorage.getItem('delimiters')},  
                    function(data) {
                       drawGrid(data, "6")
                    }, 
                "text");
                break;

            case 6:  // Specify Variable Metadata
                $.post("parse", { uniqueId: sessionStorage.getItem('uniqueId'), fileName: sessionStorage.getItem('fileName'), otherDelimiter:  sessionStorage.getItem('otherDelimiter'), headerLineNumbers:  sessionStorage.getItem('headerLineNumbers'), delimiters:  sessionStorage.getItem('delimiters')},  
                    function(data) {
                       drawGrid(data, "7")
                    }, 
                "text");

                break;       

            case 7:  // Specify Station Information

                // If we land on this page and user has already enter something 
                // (e.g., clicked previous or used the menu to navigate)    
                // Don't hide the 'Next' button           
                if ((ui.type == "previous") || (ui.type == "next")) {
                    if (sessionStorage.getItem("station_name")) {
                        if (sessionStorage.getItem("latitude")) {                            
                           if (sessionStorage.getItem("lat_units")){ 
                               if (sessionStorage.getItem("longitude")) {
                                   if (sessionStorage.getItem("lon_units")) {                            
                                       if (sessionStorage.getItem("altitude")){ 
                                           if (sessionStorage.getItem("alt_units")){ 
                                               $("#faux").remove();
                                               $(".jw-button-next").removeClass("hideMe");
                                           }
                                       }
                                   }
                               }
                           }
                        }
                    }
                  
                    // populate input elements from sessionStorage
                    var inputElements = $("#step8 input");
                    for (var i = 0; i < inputElements.length; i++) {  
                        var name = $(inputElements[i]).attr('name');
                        if (sessionStorage.getItem(name)) {
                            $("input[name=\"" + name + "\"]").val(sessionStorage.getItem(name));
                         } else {
                            $("input[name=\"" + name + "\"]").val("");
                         }
                    }

                }
                break;   



   

            case 8:  // Specify Global Metadata

                // If we land on this page and user has already enter something 
                // (e.g., clicked previous or used the menu to navigate)    
                // Don't hide the 'Next' button           
                if ((ui.type == "previous") || (ui.type == "next")) {
                    if (sessionStorage.getItem("title")) {
                        if (sessionStorage.getItem("institution")) {                            
                           if (sessionStorage.getItem("description")){ 
                               $("#faux").remove();
                               $(".jw-button-next").removeClass("hideMe");
                           }
                        }
                    }
                   
                    // populate input elements from sessionStorage
                    var inputElements = $("#step9 input");
                    for (var i = 0; i < inputElements.length; i++) {  
                        var name = $(inputElements[i]).attr('name');
                        if (sessionStorage.getItem(name)) {
                            $("input[name=\"" + name + "\"]").val(sessionStorage.getItem(name));
                         } else {
                            $("input[name=\"" + name + "\"]").val("");
                         }
                    }
                    // populate textarea elements from sessionStorage
                    var textareaElements = $("#step9 textarea");
                    for (var i = 0; i < textareaElements.length; i++) {  
                        var name = $(textareaElements[i]).attr('name');
                        if (sessionStorage.getItem(name)) {
                            $("textarea[name=\"" + name + "\"]").val(sessionStorage.getItem(name));
                         } else {
                            $("textarea[name=\"" + name + "\"]").val("");
                         }
                    }
                }
                break;   

            case 9:  // Download!!!
                $(".jw-button-finish").addClass("hideMe");
                var data = {};
                var variableNames = "";
                var variableUnits = "";
                var variableMetadata = "";
                for (var i = 0; i < sessionStorage.length; i++) {  
                    var key = sessionStorage.key(i);
                    var value = sessionStorage.getItem(key);
                    if (key.match(/[variable]{1}\d+/)) {
                        if (key.match(/Unit/)) {
                            variableUnits = variableUnits + "," + key + ":" + value;
                        } else if (key.match(/Metadata/)) {                            
                            variableMetadata = variableMetadata + "," + key + "=" + value.replace(/,/g, "+");
                        } else {
                            variableNames = variableNames + "," + key + ":" + value;
                        }
                        
                    } else {
                        data[key] = value;
                    }
                }
                data["variableNames"] = variableNames.replace(/,/, "");
                data["variableUnits"] = variableUnits.replace(/,/, "");
                data["variableMetadata"] = variableMetadata.replace(/,/, "");
                $.post("parse", data,  
                    function(data) {                       
                        finish(data)
                    }, 
                "text");
                break;      
        }
    })


    /* 
     INITIAL DOCUMENT LOAD
     */
    $w.ready(function() {
        $(".jw-button-next").prop('disabled', true).addClass("disabled");
    });

    /* 
     STEP 1 
     */
    // Show 'Next' button after user makes a selection
    $('#step1 input').bind('click', function() {
        $(".jw-button-next").removeAttr("disabled").removeClass("disabled");
    });

    /* 
     STEP 2
     */
    // Show upload button after user launches file chooser
    $('#file').bind('click', function() {
        $("#upload").removeClass("hideMe");        
    });

    // Validate size of file being uploaded
    $("#file").bind('change', function() {
        if (($("#file")[0].files[0].size / 1024) > 1024) {
            $("#step2").find("label.error").text("Error! File size should be less then 1MB");
            $("#upload").addClass("hideMe"); 
            return false;
        } else if (($("#file")[0].files[0].size / 1024) <= 0) {
            $("#step2").find("label.error").text("Error! You are attempting to upload an empty file");
            $("#upload").addClass("hideMe"); 
            return false;
        } else {
            if ($("#file")[0].files[0].type.search("text") < 0) {
                $("#step2").find("label.error").text("Error! Incorrect file type selected for upload");
                $("#upload").addClass("hideMe"); 
                return false;
            } else {
                $("#step2").find("label.error").text("");
                $("#upload").removeClass("hideMe");  
            }
        }
    });

    // Upload file & show 'Next' button after user uploads file
    $('#upload').bind('click', function() {
        up.send(); 
        addToSession("fileName", $("#file").val().replace(/\\/g, '').replace(/C:/g, '').replace(/fakepath/g, ''));
        $("#file").addClass("hideMe"); 
        $("#faux").remove();
        $(".jw-button-next").removeClass("hideMe");
    });

    // Callback function to bring a hidden box back
    function progressBarCallback() {
        setTimeout(function() {
	    $('#progress').html(sessionStorage.getItem('fileName') + " successfully uploaded").removeClass('progress').fadeIn("fast");
            $("#clearFileUpload").removeClass("hideMe");
        }, 1000 );
    };

    // Event Handler to removed uploaded file from session and recreate the upload form.
    $("#clearFileUpload").bind('click', function() {
        sessionStorage.removeItem("uniqueId");
        sessionStorage.removeItem("fileName");
        $("#file").removeClass("hideMe");   
        $("#clearFileUpload").addClass("hideMe");
        $('#progress').css('width','0%').html("0%").addClass("progress");
        $("#faux").remove();
        $(".jw-button-next").addClass("hideMe").after(faux);
    });

    // Show upload button after user launches file chooser
    $('#showHeaders').bind('click', function() {
        $("#upload").removeClass("hideMe");        
    });

    /* 
     STEP 4
     */
    // Show 'Next' button after user makes a selection
    $('#step4 input:checkbox').bind('click', function() {
        var checkedDelimiters = $('input:checkbox').serializeArray();
        var delimiterArray = [];
        $.each(checkedDelimiters, function(index, field){            
            delimiterArray[index] = field.value;    
            if (field.value == "Other") {
                $("#otherDelimiter").removeClass("hideMe");
            }        
        });
        addToSession("delimiters", delimiterArray);
        if (sessionStorage.getItem('otherDelimiter')) {
            if (sessionStorage.getItem('delimiters').search("Other") < 0) {
                sessionStorage.removeItem("otherDelimiter");
                $('#step4 #otherDelimiter').val('');
                $("#otherDelimiter").addClass("hideMe");
            }
        }

        $('#step4 #otherDelimiter').focusout(function() {
            addToSession("otherDelimiter", $(this).val());
        });

        $('#step4 #otherDelimiter').focus(function() {
            $('#step4').find("label.error").text('');
            $("#faux").remove();
            $(".jw-button-next").removeClass("hideMe")
        });

        $("#faux").remove();
        $(".jw-button-next").removeClass("hideMe");
    });

    /* 
     STEP 8
     */
    // Show 'Next' button after user makes a selection
    $('#step8 input').focusout(function() {
        if ($(this).attr('value') != "") {
            addToSession($(this).attr('name'), $(this).attr('value'));
            if (sessionStorage.getItem("station_name")) {
                if (sessionStorage.getItem("latitude")) {  
                    if (sessionStorage.getItem("lat_units")){ 
                        if (sessionStorage.getItem("longitude")) {
                            if (sessionStorage.getItem("lon_units")) {
                                if (sessionStorage.getItem("altitude")){ 
                                    if (sessionStorage.getItem("alt_units")){ 
                                         $("#faux").remove();
                                         $(".jw-button-next").removeClass("hideMe");
                                    }
                                }
                            }
                        }
                    }
                }
            }  
        }
    });



    /* 
     STEP 9
     */
    // Show 'Next' button after user makes a selection
    $('#step9 input').focusout(function() {
        if ($(this).attr('value') != "") {
            addToSession($(this).attr('name'), $(this).attr('value'));
            if (sessionStorage.getItem("title")) {
                if (sessionStorage.getItem("institution")) {                            
                    if (sessionStorage.getItem("description")){ 
                        console.log('sdasasdasd');
                        $("#faux").remove();
                        $(".jw-button-next").removeClass("hideMe");
                    }
                }
            }
        }
    });

    $('#step9 textarea').focusout(function() {
        if ($(this).attr('value') != "") {
            addToSession($(this).attr('name'), $(this).attr('value'));
            if (sessionStorage.getItem("title")) {
                if (sessionStorage.getItem("institution")) {                            
                    if (sessionStorage.getItem("description")){ 
                        $("#faux").remove();
                        $(".jw-button-next").removeClass("hideMe");
                    }
                }
            }
        }
    });
 
});


// General function called to add a value to the session
function addToSession(key, value) {
    if(typeof(Storage)!=="undefined")  {
        sessionStorage.setItem(key, value);
    } else  {
        // add some jQuery method here for non-HTML5
    }
}


function displaySelected (node, key, text) {
    $(node).text(text + ": " + sessionStorage.getItem(key));
}


function finish (data) {
    var urls = data.split(/\r\n|\r|\n/g);
    for (var i = 0; i < urls.length; i++) {           
        var link = "<li><a href=\""  +  urls[i].replace("/opt/tomcat/webapps", "")  +  "\">" + urls[i].replace("/opt/tomcat/webapps/pzhtaDownload/", "")  +  "</a></li>";
        $("#step10 ol").append(link);
    }
}

