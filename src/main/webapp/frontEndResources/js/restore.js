/**
 * Restore script for rosetta.  Restoration starts here and is called via this script.
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
    $("#quickSaveButton").addClass("hideMe")

    // instantiate jWizard!
    $w = $("#FORM");
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
                var error;
                // using currentStepIndex, we can intercept the user when they are *done* with a
                // particular step
                switch (ui.currentStepIndex) {
                    case 0:
                        // upload template
                        uploadRosettaTemplate("stepValidation", ui);
                        removeFromSession("uniqueId");
                        removeFromSession("fileName");
                        break;

                    case 1:
                        // upload data file
                        uploadDataFile("stepValidation", ui);
                        break;

                    case 2:
                        specifyHeaderLines("stepValidation", ui);
                        break;

                    case 3:
                        specifyDelimiters("stepValidation", ui);
                        break;

                    case 5:
                        // Validation
                        specifyPlatformMetadata("stepValidation", ui)
                        break;

                    case 6:
                        // Validation
                        specifyGeneralMetadata("stepValidation", ui);
                        break;
                }
            }

            // by using nextStepIndex, we can intercept the user when they are *about to start* on
            // a particular step
            switch (ui.nextStepIndex) {
                case 0:
                    uploadRosettaTemplate("repopulateStep", ui);
                    break;

                case 1:
                    uploadDataFile("repopulateStep", ui);
                    break;

                case 2:
                    specifyHeaderLines("repopulateStep", ui);
                    break;

                case 3:
                    specifyDelimiters("repopulateStep", ui);
                    break;

                case 4:
                    specifyVariableMetadata("repopulateStep", ui);
                    break;

                case 5:
                    specifyPlatformMetadata("repopulateStep", ui);
                    break;

                case 6:
                    specifyGeneralMetadata("repopulateStep", ui);
                    break;

                case 7:
                    convertAndDownload("repopulateStep", getAllDataInSession());
                    break;
            }
        })

    /**
     * CUSTOM EVENT HANDLERS BY STEP
     */

    /**
     * INITIAL DOCUMENT LOAD
     */
    $w.ready(function () {
        $(".jw-button-next").prop("disabled", true).addClass("disabled");
    });

    /**
     * STEP 0
     */
    uploadRosettaTemplate("stepFunctions");
    /**
     * STEP 1
     */
    uploadDataFile("stepFunctions");

    /**
     * STEP 2 handled in SlickGrid/custom/headerLineSelection.js
     */

    /**
     * STEP 3
     */
    specifyDelimiters("stepFunctions", 3);

    /**
     * STEP 4 handled in SlickGrid/custom/variableSpecification.js
     */

    /**
     * STEP 5
     */
    specifyPlatformMetadata("stepFunctions", 5)

    /**
     * STEP 6
     */
    specifyGeneralMetadata("stepFunctions", 6);

    /**
     * STEP 7
     */
    publish("stepFunctions", null);
});