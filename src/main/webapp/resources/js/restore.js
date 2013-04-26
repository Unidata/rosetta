/**
 * Restore script for pzhta.  Restoration starts here and is called via this script.
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
                    if (ui.type == "next") {
                        error = validateItemExistsInSession(ui.currentStepIndex, "uniqueId", "You need to upload a file to continue.");
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
                // Initially hide the upload button (will appear when user opens file chooser)
                $("#upload").addClass("hideMe");
                // If we land on this page and user has already enter something
                // (e.g., clicked previous or used the menu to navigate)    
                // Don't hide the 'Next' button           
                if (ui.type == "previous") {
                    if (getFromSession("uniqueId")) {
                        $("#file").addClass("hideMe");
                        progressBarCallback();
                        $("#faux").remove();
                        $(".jw-button-next").removeClass("hideMe");
                    }
                }

            break;

            case 1:
                $.post("restoreFromZip",
                      { uniqueId: getFromSession("uniqueId"), fileName: getFromSession("fileName") },
                      function(data) {
                          var restoredSessionStorage = JSON.parse(data);
                          for(var item in restoredSessionStorage) {
                              addToSession(item, restoredSessionStorage[item]);
                          }
                      },
                      "text");
                removeFromSession("uniqueId");
                removeFromSession("fileName");
                // If we land on this page and user has already enter something
                // (e.g., clicked previous or used the menu to navigate)
                // Don't hide the 'Next' button
                if ((ui.type == "previous") || (ui.type == "next")) {
                    if (getFromSession("headerLineNumbers")) {
                        $("#faux").remove();
                        $(".jw-button-next").removeClass("hideMe");
                    }
                }


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
        $(".jw-button-next").removeAttr("disabled").removeClass("disabled");
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
});
