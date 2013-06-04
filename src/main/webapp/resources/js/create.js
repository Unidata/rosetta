/**
 * template creation script for rosetta.  Creation starts here and is called via this script.
 */

$(document).ready(function($) {

    // automagically make any image alt a tooltip
    $(document).tooltip({ items: "img[alt]",
        content: function() { return $(this).attr("alt") } 
    });

    // our faux next button that is disabled
    var faux = '<div id="faux" class="ui-corner-all disabled">Next</div>';

    // instantiate jWizard!
    $w = $("#FORM");
    $w.validate();
    $w.jWizard({
        menuEnable: true,
        titleHide: false,
        buttons : {
            finishType : "button"
        }
    })

    /** 
     * The bindings below are event handlers, they will all be executed before proceeding to the callback 
     *
     * ui = {
     *       type: "previous|next|first|last|manual",
     *       currentStepIndex: [int],
     *       nextStepIndex: [int]
     * }; 
     */

    /** 
     * Handling custom navigation through the wizard     
     */
    .bind("jwizardchangestep", function(event, ui) {
        // "manual" is always triggered by the user, never jWizard itself
        if (ui.type !== "manual") {          
           $("#faux").remove();
           $(".jw-button-next").addClass("hideMe").after(faux);
            var error;
            // using currentStepIndex, we can intercept the user when they are *done* with a particular step
            switch(ui.currentStepIndex) { 
                case 0: 
                    // Validation
                    error = validateItemExistsInSession(ui.currentStepIndex, "cfType", "You must select a platform to continue.");
                    if (!error) {
                        return false;
                    }
                break;

                case 1:  
                    // Validation
                    if (ui.type == "next") {
                        error = validateItemExistsInSession(ui.currentStepIndex, "uniqueId", "You need to upload a file to continue.");
                        if (!error) {
                            return false;
                        }
                    }  
                break;

                case 2: 
                    // Validation
                    if (ui.type == "next") {
                        error = validateItemExistsInSession(ui.currentStepIndex, "headerLineNumbers", "You need to specify which lines are header lines to continue.");
                        if (!error) {
                            return false;
                        }
                    }   
                break;

                case 3:  
                    // Validation
                    if (ui.type == "next") {
                        error = validateItemExistsInSession(ui.currentStepIndex, "delimiters", "You need to specify at least one delimiter to continue.");
                        if (!error) {
                            return false;
                        }
                        error = validateOtherDelimiter(ui.currentStepIndex);
                        if (!error) {
                            return false;
                        }
                    }   
                break;

                case 5:      
                    // Validation                
                    if (ui.type == "next") {
                        // platformMetadata array loaded via jstl
                        error = validateMetadataEntries(platformMetadata, "platformMetadata", ui.currentStepIndex);
                        if (!error) {
                            return false;
                        }                        
                    } 
                break;

                case 6:  
                    // Validation                    
                    if (ui.type == "next") {
                        // generalMetadata array loaded via jstl 
                        error = validateMetadataEntries(generalMetadata, "generalMetadata", ui.currentStepIndex);
                        if (!error) {
                            return false;
                        }
                    } 
                break;
            } 
        }

        // by using nextStepIndex, we can intercept the user when they are *about to start* on a particular step
        switch(ui.nextStepIndex) {   
            case 0:  
                // If we land on this page and user has already enter something 
                // (e.g., clicked previous or used the menu to navigate)
                // Don't hide the 'Next' button
                if (ui.type == "previous") {
                    if (getFromSession("cfType")) {
                        $("#faux").remove();
                        $(".jw-button-next").removeClass("hideMe");
                    }
                }
            break;

            case 1:  
                // Initially hide the upload button (will appear when user opens file chooser)
                $("#upload").addClass("hideMe");

                // If we land on this page and user has already enter something 
                // (e.g., clicked previous or used the menu to navigate)    
                // Don't hide the 'Next' button           
                if ((ui.type == "previous") || (ui.type == "next")) {
                    if (getFromSession("uniqueId")) {
                        $("#file").addClass("hideMe");      
                        progressBarCallback();
                        $("#faux").remove();
                        $(".jw-button-next").removeClass("hideMe");
                    }
                }
            break;

            case 2:  
                $.post("parse", { uniqueId: getFromSession("uniqueId"), fileName: getFromSession("fileName") },  
                    function(data) {
                       drawGrid(data, "2")
                    }, 
                "text");

                // If we land on this page and user has already enter something 
                // (e.g., clicked previous or used the menu to navigate)    
                // Don't hide the 'Next' button           
                if ((ui.type == "previous") || (ui.type == "next")) {
                    if (getFromSession("headerLineNumbers")) {
                        $("#faux").remove();
                        $(".jw-button-next").removeClass("hideMe");
                    }
                }
            break;

            case 3:  
                // If we land on this page and user has already enter something 
                // (e.g., clicked previous or used the menu to navigate)    
                // Don't hide the 'Next' button           
                if ((ui.type == "previous") || (ui.type == "next")) {
                    if (getFromSession("delimiters")) {                        
                        $("#step3 #delimiter").each(function(){      
                            if (getFromSession("delimiters").search($(this).val()) >= 0 ) {  
                                if ($(this).val() == "Other") {
                                    if (!getFromSession("otherDelimiter")) { 
                                        removeItemFromSessionString("delimiters", "Other")
                                        return true;
                                    } 
                                }
                                $(this).attr("checked", true);
                            } else {
                                $(this).attr("checked", false);
                            }
                        });
                        if (getFromSession("otherDelimiter")) {    
                            $("#otherDelimiter").val(getFromSession("otherDelimiter"));
                            $("#otherDelimiter").removeClass("hideMe");
                        }
                        $("#faux").remove();
                        $(".jw-button-next").removeClass("hideMe");
                    }
                }
            break;

            case 4: 
                $.post("parse", { uniqueId: getFromSession("uniqueId"), fileName: getFromSession("fileName"), otherDelimiter:  getFromSession("otherDelimiter"), headerLineNumbers:  getFromSession("headerLineNumbers"), delimiters:  getFromSession("delimiters")},  
                    function(data) {
                       drawGrid(data, "4")
                    }, 
                "text");
                break;

            case 5:  
                // If we land on this page and user has already enter something 
                // (e.g., clicked previous or used the menu to navigate)    
                // Don't hide the 'Next' button           
                if ((ui.type == "previous") || (ui.type == "next")) {
                    if (getItemEntered("platformMetadata", "platformName") != null ) {
                        if (getItemEntered("platformMetadata", "latitude") != null ) { 
                            if (getItemEntered("platformMetadata", "longitude") != null ) {
                                if (getItemEntered("platformMetadata", "altitude") != null ) {                                    
                                    $("#faux").remove();
                                    $(".jw-button-next").removeClass("hideMe");                                   
                                }                  
                            }        
                        } 
                    }
                }

                // populate input elements from sessionStorage
                var inputElements = $("#step5 input");
                for (var i = 0; i < inputElements.length; i++) {  
                    var name = $(inputElements[i]).attr("name");
                    var itemInSession = getItemEntered("platformMetadata", name);
                    if (itemInSession != null) {
                        $("input[name=\"" + name + "\"]").val(itemInSession);
                    } else {
                        $("input[name=\"" + name + "\"]").val("");
                    }
                }
            break;

            case 6:  
                // If we land on this page and user has already enter something 
                // (e.g., clicked previous or used the menu to navigate)    
                // Don't hide the 'Next' button           
                if ((ui.type == "previous") || (ui.type == "next")) {
                    if (getItemEntered("generalMetadata", "title") != null ) {
                        if (getItemEntered("generalMetadata", "institution") != null ) { 
                            if (getItemEntered("generalMetadata", "description") != null ) {                                
                                $("#faux").remove();
                                $(".jw-button-next").removeClass("hideMe"); 
                            }        
                        } 
                    }
                }

                // populate input elements from sessionStorage
                var inputElements = $("#step6 input"); 
                for (var i = 0; i < inputElements.length; i++) {
                    var name = $(inputElements[i]).attr("name");
                    var itemInSession = getItemEntered("generalMetadata", name);
                    if (itemInSession != null) {
                        $("input[name=\"" + name + "\"]").val(itemInSession);
                    } else {
                        $("input[name=\"" + name + "\"]").val("");
                    }
                }
            break;   

            case 7:
                var data = getAllDataInSession();
                $.post("parse", data,
                    function(data) {
                        var urls = data.split(/\r\n|\r|\n/g);
                        var download = $("ul#download");
                        console.warn("here 1");
                        var zipPattern = /^\.zip$/i;
                        var ncPattern = /^\.nc$/i;
                        console.warn("here 2");
                        $(download).empty();
                        for (var i = 0; i < urls.length; i++) {
                            var fileExt = urls[i].match(/\.[a-zA-Z]{3,4}$/);
                            if (zipPattern.test(fileExt)) {
                                var linkName = "Rosetta transaction receipt"
                            } else if (ncPattern.test(fileExt)) {
                                var linkName = "netCDF Data File"
                            } else {
                                var linkName = urls[i];
                            }

                            var link = "<li><a href=\""  +  "fileDownload/" + getFromSession("uniqueId") + "/" + urls[i]  +  "\">" + linkName  +  "</a></li>";
                            console.warn(link);
                            $(download).append(link);
                        }
                    }, 
                "text");
                $(".jw-button-next").removeClass("hideMe")
                $(".jw-button-finish").addClass("hideMe");
                $("#faux").remove();
            break;
        }
    })


    /**
     * CUSTOM EVENT HANDLERS BY STEP
     */


    /**
     * INITIAL DOCUMENT LOAD
     */
    $w.ready(function() {
        $(".jw-button-next").prop("disabled", true).addClass("disabled");
    });

    /** 
     * STEP 0 
     */    
    $("#step0 input").bind("click", function() {
        // add to session
        addToSession("cfType", $(this).val());
        // Show 'Next' button after user makes a selection
        $(".jw-button-next").removeAttr("disabled").removeClass("disabled");
    });

    /** 
     * STEP 1
     */        
    $("#file").bind("change", function() {
        // Validate file being uploaded
        var error = validateUploadedFile($("#file")[0].files[0], 1);
        if (!error) {
            return false;
        } else {
            // Show upload button after user launches file chooser (if upload successful)
            $(".jw-step:eq(1)").find("label.error").text("");
            $("#upload").removeClass("hideMe");  
        }
    });
    
    $("#upload").bind("click", function() {
        // Upload file and add to session
        var up = instantiateUploader($("#progress"), $(".jw-step:eq(1)").find("label.error"), $("#upload"));
        up.send();         
        addToSession("fileName", cleanFilePath($("#file").val()));
        // show 'Next' button after user uploads file
        $("#file").addClass("hideMe"); 
    });
   
    $("#clearFileUpload").bind("click", function() {
        //removed uploaded file from session and recreate the upload form.
        removeFromSession("uniqueId");
        removeFromSession("fileName");
        $("#file").removeClass("hideMe");   
        $("#clearFileUpload").addClass("hideMe");
        // clear progress bar
        $("#progress").attr("style","").addClass("progress");
        $("#progress").html("0%");
        // clear any notices about file types
        $("#notice").empty();
        // hide the 'Next' button
        $("#faux").remove();
        $(".jw-button-next").addClass("hideMe").after(faux);
    });

    /** 
     * STEP 2 handled in SlickGrid/custom/headerLineSelection.js
     */   

    /** 
     * STEP 3
     */   
    $("#step3 input:checkbox").bind("click", function() {
        $(".jw-step:eq(3)").find("label.error").text("");
        // create array from selected values
        var checkedDelimiters = $("input:checkbox").serializeArray();
        var delimiterArray = [];
        $.each(checkedDelimiters, function(index, field){            
            delimiterArray[index] = field.value;         
        });

        // add to session
        addToSession("delimiters", delimiterArray);
        if (delimiterArray.length <= 0) {
            removeFromSession("delimiters");
        } else {
            // Show 'Next' button after user makes a selection
            $("#faux").remove();
            $(".jw-button-next").removeClass("hideMe");
        }

        // if Other is selected 
        if ($(this).val() == "Other") {
            if (jQuery.inArray("Other", delimiterArray) < 0) {
                // toggled off
                $("#otherDelimiter").addClass("hideMe");
                $("#otherDelimiter").val("");
                removeFromSession("otherDelimiter");
            } else {
                // toggled on 
                $("#otherDelimiter").removeClass("hideMe");
            }
        }
    });

    $("#otherDelimiter").on("focusin", function() {
        $(".jw-step:eq(3)").find("label.error").text("");
    });

    $("#otherDelimiter").on("focusout", function() {
        addToSession("otherDelimiter", $(this).val());
        if (getFromSession("delimiters")) {
            // Show 'Next' button after user makes a selection
            $("#faux").remove();
            $(".jw-button-next").removeClass("hideMe");
        }
    });

    /** 
     * STEP 4 handled in SlickGrid/custom/variableSpecification.js
     */   

    /** 
     * STEP 5 
     */
    // Show 'Next' button after user makes a selection
    $("#step5 input").on("focusout", function() {
        if ($(this).attr("value") != "") {
            // add to the session
            var metadataString = buildStringForSession("platformMetadata", $(this).attr("name"), $(this).attr("value"));
            addToSession("platformMetadata", metadataString);  
        } else {
            // entered a blank value so get rid of it in the session
            removeItemFromSessionString("platformMetadata", $(this).attr("name"));
        }

        // see if we can expose the next button
        if (getItemEntered("platformMetadata", "platformName") != null ) {
            if (getItemEntered("platformMetadata", "latitude") != null ) { 
                if (getItemEntered("platformMetadata", "longitude") != null ) {
                    if (getItemEntered("platformMetadata", "altitude") != null ) {                                    
                        $("#faux").remove();
                        $(".jw-button-next").removeClass("hideMe");                                   
                    }                  
                }        
            } 
        } 
    });

    /** 
     * STEP 6 
     */
    // Show 'Next' button after user makes a selection
    $("#step6 input").on("focusout", function() {
        if ($(this).attr("value") != "") {
            // add to the session
            var metadataString = buildStringForSession("generalMetadata", $(this).attr("name"), $(this).attr("value"));
            addToSession("generalMetadata", metadataString);  
        } else {
            // entered a blank value so get rid of it in the session
            removeItemFromSessionString("generalMetadata", $(this).attr("name"));
        }

        // see if we can expose the next button
        if (getItemEntered("generalMetadata", "title") != null ) {
            if (getItemEntered("generalMetadata", "institution") != null ) { 
                if (getItemEntered("generalMetadata", "description") != null ) {                               
                    $("#faux").remove();
                    $(".jw-button-next").removeClass("hideMe"); 
                }        
            } 
        } 
    });
    /**
     * STEP 7
     */
    // publish to portal

    $("#publish").bind("click", function() {
        var pubName = publisherName.value;
        addToSession("pubName", pubName);
        addToSession("userName", $(userName).val());
        var data = getAllDataInSession();
        data["auth"] = $(userPassword).val();

        $.post("publish", data,
            function(returnData) {
                var pubMessage = $("ul#pubMessage");
                $(pubMessage).empty();
                if (returnData.indexOf("Incorrect") !== -1) {
                    pubMessage.append("<br><label class=\"error\">" + returnData + "</label>");
                } else {
                    $("#publish").remove()
                    var linkName = "View published data!";
                    pubMessage.append("<br><li><a href=\""  +  "http://motherlode.ucar.edu/repository/entry/show?entryid=" + returnData  +  "\">" + linkName  +  "</a></li>");
                }
            },
            "text");
       });
});
