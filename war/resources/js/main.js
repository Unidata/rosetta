$(document).ready(function($) {
    var faux = '<div id="faux" class="ui-corner-all disabled" style="display: inline-block;">Faux Next</div>';

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

                case 7:  // Specify Global Metadata
                     // Validation: must have the title specified 
                     if (ui.type == "next") {
                        if (!sessionStorage.getItem("title")) {
                            $(".jw-step:eq(" + ui.currentStepIndex + ")").find("label[for=title]").text("You need to provide a title for this dataset.");
                            return false;
                        } 
                        // Validation: must have the title specified 
                        if (!sessionStorage.getItem("institution")) {
                            $(".jw-step:eq(" + ui.currentStepIndex + ")").find("label[for=institution]").text("You need to provide a institution for this dataset.");
                            return false;
                        } 
                        // Validation: must have the title specified 
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

            case 7:  // Specify Global Metadata

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
                    if (sessionStorage.getItem("title")) {
                        $('input[name=title]').val(sessionStorage.getItem("title"));
                    }
                    if (sessionStorage.getItem("institution")) {
                        $('input[name=institution]').val(sessionStorage.getItem("institution"));
                    }
                    if (sessionStorage.getItem("processor")) {
                        $('input[name=processor]').val(sessionStorage.getItem("processor"));                       
                    }
                    if (sessionStorage.getItem("version")) {
                        $('input[name=version]').val(sessionStorage.getItem("version"));                       
                    }
                    if (sessionStorage.getItem("source")) {
                        $('input[name=source]').val(sessionStorage.getItem("source"));                       
                    }
                    if (sessionStorage.getItem("description")){ 
                        $('textarea[name=description]').val(sessionStorage.getItem("description"));
                    }
                    if (sessionStorage.getItem("comment")){ 
                        $('textarea[name=comment]').val(sessionStorage.getItem("comment"));
                    }
                    if (sessionStorage.getItem("history")){ 
                        $('textarea[name=history]').val(sessionStorage.getItem("history"));
                    }
                    if (sessionStorage.getItem("references")){ 
                        $('textarea[name=references]').val(sessionStorage.getItem("references"));
                    }
                }
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

    $('#step8 textarea').focusout(function() {
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

