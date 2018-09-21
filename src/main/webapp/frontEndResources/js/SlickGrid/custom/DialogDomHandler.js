/**
 * SlickGrid/custom/DialogDOMHandler.js
 *
 * Module to handle DOM manipulation for dialog, meaning any function
 * that attaches, dettaches, or directly changes the dialog DOM .
 *
 * Compliance level in ComplianceLevelHandler
 * UnitBuilder in UnitBuilderHandler 

 */
var DialogDomHandler = (function () {

    function disableDiv(dialogDomSection) {
        // disable this section of the dialog content.
        $("#dialog #" + dialogDomSection).addClass("inactive");
        var inputTags = $("#dialog #" + dialogDomSection).find("input");
        if (inputTags.length > 0) {
            $(inputTags).each(function () {                
                $(this).prop("disabled", true);
                if ($(this).attr("type") === "text") {
                    $(this).prop("value", "");
                } else { // radio or checkbox
                    $(this).prop("checked", false);
                }
            });
        }
        var selectTags = $("#dialog #" + dialogDomSection).find("select");
        $(selectTags).val("");

    }

    
    /**
     * Enables the input tags and emphasizes the text of the #variableAttributes assignment section of the dialog form.
     *
     * @param dialogDomSection  The section of the dialog DOM to enable.
     */
    function enableDiv(dialogDomSection) {
        // enable this section of the dialog content
        $("#dialog #" + dialogDomSection).removeClass("inactive");
        $("#dialog #" + dialogDomSection).find("input").each(function () {
            $(this).prop("disabled", false);
        });
    }

    /**
     * Disables ALL of the input tags, de-emphasizes the text in the #variableAttributes 
     * section of the dialog form, and removes any associated stored data.
     */
    function disableVariableAttributes(key) {
        // Disable all parts of the dialog content except the first part.
        $("#dialog #variableAttributes").find("div").each(function () {  
            var divTag = $(this);          
            $(divTag).addClass("inactive");
            $(divTag).find("input").each(function () {
                var inputTag = $(this);
                $(inputTag).prop("disabled", true);
                if ($(inputTag).attr("type") === "text") {
                    $(inputTag).prop("value", "");
                } else { // radio or checkbox
                    $(inputTag).prop("checked", false);
                }

                // Remove any stored data associated with these sections.
                var divTagId = $(divTag).attr("id").replace("MetadataAssignment", "");

                // See if the metadata to delete is in the stored object's nested entities.
                if (divTagId === "required" || divTagId === "recommended" || divTagId === "additional") {
                    // Is a nested entity.
                    VariableStorageHandler.removeComplianceLevelVariableData(key, $(inputTag).attr("name"), divTagId) 
                } else {
                    // Not nested.
                    VariableStorageHandler.removeVariableData(key, $(inputTag).attr("name")); 
                }
            });
        });
    }


    /**
     * This function adds HTML input tags with which the user will provide the
     * data associated with the column. This HTML is added to the dialog DOM
     * and the event handlers for the inserted HTML are bound.
     *
     * @param key  The key used to store the data in the variableMetadata value field.
     */
    function addContentToDialog(key) {
        $("#dialog").empty(); // start off with a dialog box free of content

        var dialogContent = "<div id=\"variableNameTypeAssignment\">\n" +
            " <h3>What would you like to do with this column of data?</h3>\n" +
            " <label class=\"error\"></label>" +
            " <ul class=\"half\">\n" +
            "  <li>\n" +
            "   <label>\n" +
            "    <input type=\"radio\" name=\"variableNameType\" value=\"assign\"/> Assign a variable name\n" +
                "   </label>\n" +
            "   <label id=\"variableNameAssignment\">\n" +
            "    <input type=\"text\" name=\"variableName\" value=\"\"/>\n" +
            "   </label>\n" +
            "   <label class=\"existingMetadataImporter hideMe\">\n" +
            "     use metadata from another column?  \n" +
            "    <input type=\"checkbox\" name=\"existingMetadataImporter\" value=\"true\"/> \n" +
            "   </label>\n" +
            "   <div id=\"existingMetadataImporter\" class=\"hideMe\">\n" +
            "    <label for=\"existingMetadataChoice\" class=\"whole\">\n" +
            "     Import metadata from: \n" +
            "     <select name=\"existingMetadataChoice\">\n" +
            "     </select>\n" +
            "    </label>\n" +
            "   </div>\n" +
            "  </li>\n" +
            "  <li>\n" +
            "   <label>\n" +
            "    <input type=\"radio\" name=\"variableNameType\" value=\"do_not_use\"/> Do not use this column of data\n" +
            "   </label>\n" +
            "  </li>\n" +
            " </ul>\n" +
            "</div>\n" +
            "<div id=\"variableAttributes\">\n" +
            " <div id=\"metadataTypeAssignment\">\n" +
            "  <h3>Is this variable a coordinate variable? (examples: latitude, longitude, time)</h3>\n" +
            "  <ul class=\"third\">\n" +
            "   <li>\n" +
            "    <label>\n" +
            "     <input type=\"radio\" name=\"metadataType\" value=\"coordinate\"/> Yes\n" +
            "    </label>\n" +
            "   </li>\n" +
            "   <li>\n" +
            "    <label>\n" +
            "     <input type=\"radio\" name=\"metadataType\" value=\"non-coordinate\"/> No\n" +
            "    </label>\n" +
            "   </li>\n" +
            "  </ul>\n" +
            "  <label for=\"metadataTypeAssignment\" class=\"error\"></label>" +
            " </div>\n" +
            " <div id=\"metadataTypeStructureAssignment\">\n" +
            "  <h3>What type of coordinate variable?</h3>\n" +
            "  <label for=\"metadataTypeStructureAssignment\" class=\"error\"></label>\n" +
            "  <select name=\"metadataTypeStructure\" autocomplete=\"nope\">\n" +
            "    <option value=\"\">---- select one ----</option>\n" +
            "    <option value=\"latitude\">latitude</option>\n" +
            "    <option value=\"longitude\">longitude</option>\n" +
            "    <option value=\"vertical\">vertical</option>\n" +
            "    <option value=\"relativeTime\">Relative time (e.g. hours since 1970-01-01)</option>\n" +
            "    <option value=\"fullDateTime\">Full date and time string</option>\n" +
            "    <option value=\"dateOnly\">Date only (year, month, and/or day)</option>\n" +
            "    <option value=\"timeOnly\">Time only (hour, minute, second, and/or millisecond)</option>\n" +
            "  </select>\n" +
            " </div>\n" +
            " <div id=\"verticalDirectionAssignment\">\n" +
            "  <h3>Which direction do the vertical values increase?</h3>\n" +
            "  <ul class=\"third\">\n" +
            "   <li>\n" +
            "    <label>\n" +
            "     <input type=\"radio\" name=\"verticalDirection\" value=\"up\"/> Up\n" +
            "    </label>\n" +
            "   </li>\n" +
            "   <li>\n" +
            "    <label>\n" +
            "     <input type=\"radio\" name=\"verticalDirection\" value=\"down\"/> Down\n" +
            "    </label>\n" +
            "   </li>\n" +
            "  </ul>\n" +
            "  <label class=\"error\"></label>" +
            " </div>\n" +
            " <div id=\"metadataValueTypeAssignment\">\n" +
            "  <h3>Specify variable data type:</h3>\n" +
            "  <ul class=\"third\">\n" +
            "   <li>\n" +
            "    <label>\n" +
            "     <input type=\"radio\" name=\"metadataValueType\" value=\"Integer\"/> Integer\n" +
            "    </label>\n" +
            "   </li>\n" +
            "   <li>\n" +
            "    <label>\n" +
            "     <input type=\"radio\" name=\"metadataValueType\" value=\"Float\"/> Float (decimal)\n" +
            "    </label>\n" +
            "   </li>\n" +
            "   <li>\n" +
            "    <label>\n" +
            "     <input type=\"radio\" name=\"metadataValueType\" value=\"Text\"/> Text\n" +
            "    </label>\n" +
            "   </li>\n" +
            "  </ul>\n" +
            "  <label class=\"error\"></label>" +
            " </div>\n" +
            " <div id=\"requiredMetadataAssignment\">\n" +
            "  <h3>Required Metadata:</h3>\n" +
            "  <ul>\n" +
            "  </ul>\n" +
            " </div>\n" +
            " <div id=\"recommendedMetadataAssignment\">\n" +
            "  <h3>Recommended Metadata:</h3>\n" +
            "  <ul>\n" +
            "  </ul>\n" +
            " </div>\n" +
            " <div id=\"additionalMetadataAssignment\">\n" +
            "  <h3>Additional Metadata:</h3>\n" +
            "  <label for=\"additionalMetadataAssignment\" class=\"error\"></label> \n" +
            "  <img src=\"resources/img/add.png\" id=\"additionalMetadataChooser\" alt=\"Add Metadata\" /> \n" +
            "  <img src=\"resources/img/remove.png\" id=\"additionalMetadataChooser\" alt=\"Remove Metadata\" /> \n" +
            "  <select name=\"additionalMetadata\">\n" +
            "  </select>\n" +
            " </div>\n" +
            "</div>\n";

        $("#dialog").append(dialogContent);

        // Start off with only the first part of the dialog visible (everything in the variableAttributes div is hidden).
        disableVariableAttributes(key);

        // Use any stored data to auto-populate the dialog web form elements.
        populateDataFromStorage(key);
        bindDialogEvents(key);
    }





    // Expose these functions.
    return {
        disableDiv: disableDiv,
        enableDiv: enableDiv,
        addContentToDialog: addContentToDialog,
        disableVariableAttributes: disableVariableAttributes

    };
})();
