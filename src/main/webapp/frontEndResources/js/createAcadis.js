/**
 * template creation script for rosetta for the ACADIS project.
 * Creation starts here and is called via this script.
 */

$(document).ready(function($) {

    // automagically make any image alt a tooltip
    $(document).tooltip({ items: "img[alt]",
        content: function() { return $(this).attr("alt") }
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
                        $(".jw-button-next").removeClass("hideMe")
                        $(".jw-button-finish").addClass("hideMe");
                        $("#faux").remove();
                        break
                    case 1:
                        selectPlatform("stepValidation", ui);
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

            // by using nextStepIndex, we can intercept the user when they are *about to start* on a particular step
            switch(ui.nextStepIndex) {
                case 1:
                    selectPlatform("repopulateStep", ui);
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
                    var stepDataCase5 = new Array()
                    stepDataCase5[0] = ui;
                    stepDataCase5[1] = 5;
                    specifyPlatformMetadata("repopulateStep", stepDataCase5);
                    break;

                case 6:
                    var stepDataCase6 = new Array()
                    stepDataCase6[0] = ui;
                    stepDataCase6[1] = 6;
                    specifyGeneralMetadata("repopulateStep", stepDataCase6);
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
    $w.ready(function() {
        $(".jw-button-next").prop("disabled", true).addClass("disabled");
    });

    /**
     * Step 0
     */

    var dataJsonStr = JSON.stringify(data);
    addToSession("acadisInventory", dataJsonStr);
    var dataFiles = [];
    for(var k in data) dataFiles.push(k);
    var numDataFiles = dataFiles.length;
    var selectElement = $("<select name='fileToConvert' id='acadisFileSelector'></select>");
    for (var i = 0; i < numDataFiles; i++){
        var name = dataFiles[i];
        var dlLink = data[name];
        var inv =  name + " " + dlLink;
        var optionElement=$("<option></option>")
        optionElement.append(name);
        selectElement.append(optionElement);
    }
    $("#inventory").append(selectElement);

    // add function to do post to createAcadis, set appropriate session storage stuff, and
    // move along with the main createAcadis wizzard
    $("#getAcadisFile").bind("click", function() {
        var inventory = JSON.parse(getFromSession("acadisInventory"));
        var fileName = $("#acadisFileSelector").val();
        addToSession("fileName", fileName);
        var remoteAccessUrl = inventory[fileName];
        var postData = {"fileName" : fileName,
            "remoteAccessUrl" : remoteAccessUrl}

        $.post("createAcadis", postData,
            function(returnData) {
                // do stuff with returnData
                var uniqueId = returnData;
                addToSession("uniqueId", uniqueId);
                if (fileName.contains(".xlsx")) {
                    fileName = fileName.replace(".xlsx", ".csv");
                } else if (fileName.contains(".xls")) {
                    fileName = fileName.replace(".xls", ".csv");
                };
                addToSession("fileName", fileName);
                //var newUrl = $(location).attr("origin") + "/rosetta/createAcadis"
                //$(location).attr('href',newUrl);
                $(".jw-button-next").prop("disabled", false).removeClass("disabled");
            },
            "text");
    });

    /**
     * STEP 1
     */
    selectPlatform("stepFunctions", 0);

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