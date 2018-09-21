/**
 * SlickGrid/custom/UnitBuilder.js
 *
 * Module to handle the creation of event handling for the unit builder.
 */
var UnitBuilder = (function () {

    /**
     * This function binds events associated with the unit builder added to the dialog DOM.
     *
     * @param key  The key used to store the data in the variableMetadata value field.
     */
    function bindUnitBuildEvents(key) {
        // unit builder data type selection
        $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitBuilderDataType\"]").on("change", function () {
            if ($(this).val() !== "") {
                addUnitBuilderOptionsToDom($(this).val());
            }
        });

        // unit builder chooser
        $("#dialog #unitBuilder img#unitBuilderChooser").bind("click", function () {

            var unitString;
    
            // get the user selected values of the unit chooser
            var unitSelected = $("#dialog #unitBuilder select[name=\"unitSelected\"]").val();

            var prefixSelected = $("#dialog #unitBuilder select[name=\"unitPrefix\"]").val();
            if (prefixSelected !== null) {
                unitString = prefixSelected + unitSelected;
            } else {
                unitString = unitSelected;
            }

            // get what is in the units from variableMetadata value field
            var unitsInStorage = getItemEntered(key + "Metadata", "units");
            if (unitsInStorage === null) {
                unitsInStorage = "";
            }

            var metadataString;
            if ($(this).attr("alt") === "Add To Units") { // Adding to units
    
                // get rid of any error messages
                $(this).parents("li").find("label[for=\"units\"].error").text("");
    
                unitsInStorage = unitsInStorage + unitString;
    
                // concatenation the entered value to any existing Metadata values pulled from the variableMetadata value field
                metadataString = buildStringForSession(key + "Metadata", "units", unitsInStorage);
    
                // update the data in the variableMetadata value field
                addToSession(key + "Metadata", metadataString);

                // update units display in dialog to show new value
                $("#dialog #requiredMetadataAssignment input[name=\"units\"]").prop("value", unitsInStorage);
    
    
            } else { // Removing from units
                var index = unitsInStorage.lastIndexOf(unitString);
                if (index >= 0) {
                    // lame
                    var pre = unitsInStorage.substring(0, index);
                    var post = unitsInStorage.substring(index + unitString.length);
    
                    // concatenation the entered value to any existing Metadata values pulled from the variableMetadata value field
                    metadataString = buildStringForSession(key + "Metadata", "units", pre + post);

                    // update the data in the variableMetadata value field
                    addToSession(key + "Metadata", metadataString);

                    // update units display in dialog to show new value
                    $("#dialog #requiredMetadataAssignment input[name=\"units\"]").prop("value", pre + post);

                } else {
                    $("#dialog #requiredMetadataAssignment #unitBuilder").find("label.error").text("'" + unitString
                    + "' has NOT been detected in the current units and therefore cannot be removed.");
                }
            }

        });
    }

    function createShowUnitBuilderSelector() {
        return  "    <label class=\"unitBuilder\">\n" +
                "     show unit builder \n" +
                "     <input type=\"checkbox\" name=\"unitBuilder\" value=\"true\"/> \n" +
                "    </label>\n";

    }

    function createUnitBuilderTypeSelector() {
        return  "    <div id=\"unitBuilder\" class=\"hideMe\">\n" +
                "     <label for=\"unitBuilderDataType\" class=\"hideMe whole\">\n" +
                "      What type of data are we building units for?\n" +
                "      <select name=\"unitBuilderDataType\">\n" +
                "      </select>\n" +
                "     </label>\n" +
                "    </div>\n";
    }


    /**
     * Creates the initial HTML input tags for the unit builder.
     *
     * @param key  The key used to store the data in the variableMetadata value field.
     */
    function createUnitBuilder(key) {
        var optionTags = "<option value=\"\">---- select one ----</option>";
        // loop through unitBuilderData
        for (var i = 0; i < unitBuilderData.length; i++) {
            var unitItem = unitBuilderData[i];
            if (unitItem.entry !== "prefix") {
                optionTags = optionTags + "<option value=\"" + unitItem.entry + "\">"
                    + unitItem.entry + "</option>\n";
            }
        }

        // add option tags to selection menu and show to user.
        $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitBuilderDataType\"]").append(optionTags);
        $("#dialog #requiredMetadataAssignment #unitBuilder label[for=\"unitBuilderDataType\"]").removeClass("hideMe");

        var selectionTags = "     <label for=\"unitPrefix\" class=\"hideMe\">\n" +
            "      unit prefix:\n" +
            "      <select name=\"unitPrefix\">\n" +
            "      </select>\n" +
            "     </label>\n" +
            "     <label for=\"unitSelected\" class=\"hideMe\">\n" +
            "      unit:\n" +
            "      <select name=\"unitSelected\">\n" +
            "      </select>\n" +
            "     </label>\n" +
            "     <img src=\"resources/img/add.png\" id=\"unitBuilderChooser\" alt=\"Add To Units\" class=\"hideMe\" /> \n" +
            "     <img src=\"resources/img/remove.png\" id=\"unitBuilderChooser\" alt=\"Remove From Units\" class=\"hideMe\"/> \n" +
            "     <label class=\"error\"></label>";

        // add the additional selection menu tags but hide until activated
        $("#dialog #requiredMetadataAssignment #unitBuilder").append(selectionTags);

        // bind events
        bindUnitBuildEvents(key);
    }

    /**
     * Creates the initial HTML input tags for the unit builder.
     *
     * @param dataTypeSelected  The type of data we are building units for.
     */
    function addUnitBuilderOptionsToDom(dataTypeSelected) {

        var unitTags = "";
        var prefixTags = "";

        var showPrefixList = true;

        // loop through known unitBuilderData
        for (var i = 0; i < unitBuilderData.length; i++) {
            var unitItem = unitBuilderData[i];
            if (unitItem.entry === dataTypeSelected) {
                var units = unitItem.unit;
                for (var x = 0; x < units.length; x++) {
                    unitTags = unitTags + "<option value=\"" + units[x] + "\">" + units[x] + "</option>\n";
                }
                if (unitItem.use_prefix !== "") {
                    showPrefixList = false;
                }
            }

            if (unitItem.entry === "prefix") {
                var prefixes = unitItem.unit;
                for (var y = 0; y < prefixes.length; y++) {
                    prefixTags = prefixTags + "<option value=\"" + prefixes[y] + "\">" + prefixes[y] + "</option>\n";
                }
            }

        }

        // prefixes
        $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitPrefix\"] option").remove();
        if (showPrefixList) {
            $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitPrefix\"]").append(prefixTags);
            $("#dialog #requiredMetadataAssignment #unitBuilder label[for=\"unitPrefix\"]").removeClass("hideMe");
        } else {
            $("#dialog #requiredMetadataAssignment #unitBuilder label[for=\"unitPrefix\"]").addClass("hideMe");
        }

        // units
        $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitSelected\"] option").remove();
        $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitSelected\"]").append(unitTags);
        $("#dialog #requiredMetadataAssignment #unitBuilder label[for=\"unitSelected\"]").removeClass("hideMe");

        // add/remove buttons
        $("#dialog #requiredMetadataAssignment #unitBuilder").find("img").each(function () {
            $(this).removeClass("hideMe");
        });
    }

    /**
     * This function binds general events associated with unit builder entries added to the dialog DOM.
     *
     * @param key  The key used to access the stores data.
     */
    function bindUnitBuilderEvent(key) {
        // Unit builder activation: toggle unit builder box
        $("#dialog #requiredMetadataAssignment input[type=\"checkbox\"]").bind("click", function () {
            $("#dialog #requiredMetadataAssignment #unitBuilder").toggleClass("hideMe");
            createUnitBuilder(key);
        });
    }
    
    
    
    // Expose these functions.
    return {
        createShowUnitBuilderSelector: createShowUnitBuilderSelector,
        createUnitBuilder: createUnitBuilder,
        createUnitBuilderTypeSelector: createUnitBuilderTypeSelector,
        bindUnitBuildEvents: bindUnitBuildEvents,
        bindUnitBuilderEvent: bindUnitBuilderEvent,
        addUnitBuilderOptionsToDom: addUnitBuilderOptionsToDom
    };
    

})();
