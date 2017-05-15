/*
 * Copyright (c) 2012-2017 University Corporation for Atmospheric Research/Unidata
 */

/**
 * template creation script for rosetta.  Creation starts here and is called via this script.
 */

$(document).ready(function ($) {

    // automagically make any image alt a tooltip
    $(document).tooltip({
                            items: "img[alt]",
                            content: function () {
                                return $(this).attr("alt")
                            }
                        });

    // our faux next button that is disabled
    var faux = '<div id="faux" class="ui-corner-all disabled">Next</div>';

    // this hides the quick save button in the footer...is enabled once a file is uploaded.
    $("#quickSaveButton").addClass("hideMe");

    // hide the upload button
    $("#upload").addClass("hideMe");

    // instantiate jWizard!
    var $w = $("#FORM");
    $w.validate();
    $w.jWizard({
                   menuEnable: true,
                   titleHide: false,
                   buttons: {
                       finishType: "button"
                   }
               })

    /**
     * The bindings below are event handlers, they will all be executed before proceeding to the
     * callback
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
        .bind("jwizardchangestep", function (event, ui) {
            // "manual" is always triggered by the user, never jWizard itself
            if (ui.type !== "manual") {
                $("#faux").remove();
                $(".jw-button-next").addClass("hideMe").after(faux);
                //var error;
                // using currentStepIndex, we can intercept the user when they are *done* with a
                // particular step
                switch (ui.currentStepIndex) {
                    case 0:
                        uploadDataFile("stepValidation", ui);
                        break;
                    case 3:
                        // Validation
                        specifyGeneralMetadata("stepValidation", ui);
                        break;
                }
            }

            // by using nextStepIndex, we can intercept the user when they are *about to start* on
            // a particular step
            switch (ui.nextStepIndex) {

                case 0:
                    uploadDataFile("repopulateStep", ui);
                    break;
                case 1:
                    selectKnownType("repopulateStep", ui);
                    break;
                case 2:
                    getMetadataFromKnownFile();
                case 3:
                    specifyGeneralMetadata("repopulateStep", ui);
                    break;
                case 4:
                    autoconvertAndDownload("repopulateStep", getAllDataInSession());
                    break;
            }
        });

    /**
     * CUSTOM EVENT HANDLERS BY STEP
     */

    /**
     * INITIAL DOCUMENT LOAD
     */
    $w.ready(function () {
        // OK, there is a chance we can arrive at this step by reloading the page 
        // while maintaining the same session (hence data has been entered). Ergo, 
        // If we land on this page and user has already enter something
        // (e.g., clicked previous or used the menu to navigate)
        // Don't hide the 'Next' button
        if (getFromSession("uniqueId")) {
            $("#faux").remove();
            $(".jw-button-next").removeClass("hideMe");
            // make sure if uniqueId is in session, that the object is selected in the select box
        } else {
            $(".jw-button-next").prop("disabled", true).addClass("disabled");
        }
    });

    /**
     * STEP 0
     */
    uploadDataFile("stepFunctions");

    /**
     * STEP 1
     */
    selectKnownType("stepFunctions");

    /**
     * STEP2
     */
    specifyGeneralMetadata("stepFunctions", 3);

});
