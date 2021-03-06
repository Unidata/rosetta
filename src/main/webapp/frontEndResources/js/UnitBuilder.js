/**
 * UnitBuilder.js
 *
 *Unit Builder used in variable metadata collection step.
 */
var UnitBuilder = (function () {

    var unitBuilderData = [];

    /**
     * Populates the unitBuilderData array with data from the unitBuilderData.xml file.
     */
    function loadUnitBuilderData() {
        $.get("resources/unitBuilderData.xml",
              function (data) {
                  var d = [];
                  $(data).find("entry").each(function () {
                      var e = {};
                      var u = [];
                      e["entry"] = $(this).attr("id");
                      e["use_prefix"] = $(this).find("use_prefix").text();
                      $(this).find("unit").each(function () {
                          u.push($(this).text());
                      });
                      e["unit"] = u;
                      d.push(e);
                  });
                  unitBuilderData = d;
              },
              "xml");
    }


    /**
     * Private, utility function (not exported).
     * This function binds events associated with the unit builder added to the dialog DOM.
     *
     * @param key  The key used to store the data in the variableMetadata value field.
     */
    function bindUnitBuildEvents(key) {
        // Unit builder data type selection.
        $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitBuilderDataType\"]").on("change", function () {
            if ($(this).val() !== "") {
                addUnitBuilderOptionsToDom($(this).val());
            }
        });

        // Unit builder chooser.
        $("#dialog #unitBuilder img#unitBuilderChooser").bind("click", function () {
            var unitString;
    
            // get the user selected values of the unit chooser
            var unitSelected = $("#dialog #unitBuilder select[name=\"unitSelected\"]").val();

            var prefixSelected = $("#dialog #unitBuilder select[name=\"unitPrefix\"]").val();
            if (prefixSelected === null) {
                prefixSelected = "";
            }

            var units = prefixSelected + unitSelected;

            if ($(this).attr("alt") === "Add To Units") { // Adding to units.
    
                // Get rid of any error messages.
                $(this).parents("li").find("label[for=\"units\"].error").text("");

                VariableStorageHandler.storeComplianceLevelVariableData(key, "units", units,"required");

                // Update units display in dialog to show new value.
                $("#dialog #requiredMetadataAssignment input[name=\"units\"]").prop("value", units);
    
    
            } else { // Removing from units
                var index = units.lastIndexOf(units);
                if (index >= 0) {
                    // lame
                    var pre = units.substring(0, index);
                    var post = units.substring(index + units.length);

                    VariableStorageHandler.storeComplianceLevelVariableData(key, "units", pre + post,"required");

                    // update units display in dialog to show new value
                    $("#dialog #requiredMetadataAssignment input[name=\"units\"]").prop("value", pre + post);

                } else {
                    $("#dialog #requiredMetadataAssignment #unitBuilder").find("label.error").text("'" + units
                    + "' has NOT been detected in the current units and therefore cannot be removed.");
                }
            }
        });
    }

    /**
     * Creates and returns the checkbox option to show the Unit Builder.
     *
     * @returns  The Unit Builder checkbox option to attach to the DOM.
     */
    function createShowUnitBuilderSelector() {
        return  "    <label class=\"unitBuilder\">\n" +
                "     Show unit builder \n" +
                "     <input type=\"checkbox\" name=\"unitBuilder\" value=\"true\"/> \n" +
                "    </label>\n";

    }

    /**
     * Creates and returns the dropdown selection to specify the type of units in the Unit Builder.
     *
     * @returns The dropdown selection to specify the type of units in the Unit Builder.
     */
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
     * Private, utility function (not exported).
     * Creates the initial HTML input tags for the unit builder.
     *
     * @param key  The key used to store the data in the variableMetadata value field.
     */
    function createUnitBuilder(key) {
        var optionTags = "<option value=\"\">---- select one ----</option>";
        // Loop through unitBuilderData array.
        for (var i = 0; i < unitBuilderData.length; i++) {
            var unitItem = unitBuilderData[i];
            if (unitItem.entry !== "prefix") {
                optionTags = optionTags + "<option value=\"" + unitItem.entry + "\">"
                    + unitItem.entry + "</option>\n";
            }
        }

        // Add option tags to selection menu and show to user.
        $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitBuilderDataType\"]").empty();
        $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitBuilderDataType\"]").append(optionTags);
        $("#dialog #requiredMetadataAssignment #unitBuilder label[for=\"unitBuilderDataType\"]").removeClass("hideMe");

        var selectionTags = "     <label id=\"unitPrefix\" for=\"unitPrefix\" class=\"hideMe\">\n" +
            "      unit prefix:\n" +
            "      <select name=\"unitPrefix\">\n" +
            "      </select>\n" +
            "     </label>\n" +
            "     <label id=\"unitSelected\" for=\"unitSelected\" class=\"hideMe\">\n" +
            "      unit:\n" +
            "      <select name=\"unitSelected\">\n" +
            "      </select>\n" +
            "     </label>\n" +
            "     <span>" +
            "      <img src=\"resources/img/add.png\" id=\"unitBuilderChooser\" alt=\"Add To Units\" class=\"hideMe\" /> \n" +
            "      <img src=\"resources/img/remove.png\" id=\"unitBuilderChooser\" alt=\"Remove From Units\" class=\"hideMe\"/> \n" +
            "      <label class=\"error\"></label>" +
            "     </span>";

        // Add the additional selection menu tags but hide until activated
        $("#dialog #requiredMetadataAssignment #unitBuilder .error").remove();
        $("#dialog #requiredMetadataAssignment #unitBuilder #unitPrefix").remove();
        $("#dialog #requiredMetadataAssignment #unitBuilder #unitSelected").remove();
        $("#dialog #requiredMetadataAssignment #unitBuilder #unitBuilderChooser").remove();
        $("#dialog #requiredMetadataAssignment #unitBuilder #unitBuilderChooser").remove();
        $("#dialog #requiredMetadataAssignment #unitBuilder").append(selectionTags);

        // Bind Unit Builder events.
        bindUnitBuildEvents(key);
    }

    /**
     * Private, utility function (not exported).
     * Creates the initial HTML input tags for the unit builder.
     *
     * @param dataTypeSelected  The type of data we are building units for.
     */
    function addUnitBuilderOptionsToDom(dataTypeSelected) {

        var unitTags = "";
        var prefixTags = "";

        var showPrefixList = true;

        // Loop through known unitBuilderData.
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

        // Prefixes.
        $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitPrefix\"] option").remove();
        if (showPrefixList) {
            $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitPrefix\"]").append(prefixTags);
            $("#dialog #requiredMetadataAssignment #unitBuilder label[for=\"unitPrefix\"]").removeClass("hideMe");
        } else {
            $("#dialog #requiredMetadataAssignment #unitBuilder label[for=\"unitPrefix\"]").addClass("hideMe");
        }

        // Units.
        $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitSelected\"] option").remove();
        $("#dialog #requiredMetadataAssignment #unitBuilder select[name=\"unitSelected\"]").append(unitTags);
        $("#dialog #requiredMetadataAssignment #unitBuilder label[for=\"unitSelected\"]").removeClass("hideMe");

        // Add/remove buttons.
        $("#dialog #requiredMetadataAssignment #unitBuilder").find("img").each(function () {
            $(this).removeClass("hideMe");
        });
    }

    /**
     * This function binds global events associated with unit builder entries added to the dialog DOM.
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
        loadUnitBuilderData: loadUnitBuilderData,
        createShowUnitBuilderSelector: createShowUnitBuilderSelector,
        createUnitBuilderTypeSelector: createUnitBuilderTypeSelector,
        bindUnitBuilderEvent: bindUnitBuilderEvent
    };

})();
