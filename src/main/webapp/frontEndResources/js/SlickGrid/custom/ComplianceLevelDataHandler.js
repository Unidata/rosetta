/**
 * SlickGrid/custom/ComplianceLevelDataHandler.js
 *
 * Module to handle compliance level data.
 */
var ComplianceLevelDataHandler = (function () {

    /**
    * Uses the array of compliance level metadata attributes gleaned from server-side metadata profiles.
    * Creates a list of form tags for these items corresponding to the metadataType (coordinate or non-coordinate).
    *
    * @param key  The key used to access the stored variable data.
    * @param complianceLevel  Whether the attribute is required, recommended or additional.
    */
    function populateComplianceLevelInputTags(key, complianceLevel) {        

        var metadataType = VariableStorageHandler.getVariableData(key, "metadataType");
        // We will stash oru created tags here.
        var metadataTags = [];
        // We will stash our required attribute names here.
        var required = [];
        var used = [];
        for (var i = 0; i < metadataProfileVariableData.length; i++) {
            var metadataProfile = metadataProfileVariableData[i];
            if (metadataProfile.complianceLevel === "optional") {
                metadataProfile.complianceLevel = "additional";
            }
            if (metadataProfile.complianceLevel === complianceLevel) {
                if (metadataProfile.metadataType === "CoordinateVariable") {
                   metadataProfile.metadataType = "coordinate";
                } 
                if (metadataProfile.metadataType === "DataVariable") {
                    metadataProfile.metadataType = "non-coordinate";
                }

                // Kludge
                if (metadataProfile.attributeName.includes("valid_")) {
                    continue;
                }
                
                // Kludge
                if (metadataProfile.attributeName === "calendar") {
                    var metadataTypeStructure = VariableStorageHandler.getVariableData(key, "metadataTypeStructure");
                    if (metadataTypeStructure === "latitude" || metadataTypeStructure === "longitude" || metadataTypeStructure === "vertical") {
                        continue;
                    }
                }

                
                if (!used.includes(metadataProfile.attributeName)) {
                    used.push(metadataProfile.attributeName);
                    var tag = createComplianceLevelTagElement(key, metadataProfile);
                    metadataTags.push(tag);
                    if (complianceLevel === "required" && metadataProfile.complianceLevel === "required") {
                        // Push attribute name onto required array.
                        required.push(metadataProfile.attributeName);
                    }
                }
            } 
            
        }
        
        // If compliance level is required, add list to storage.
        if (complianceLevel === "required") {
            storeData("_v" + key.replace("variableName", ""), required);
        }
        

        // Sort the array.
        metadataTags.sort();
        var metadataTagsAsAString = "";
        for (var x = 0; x < metadataTags.length; x++) {
            metadataTagsAsAString = metadataTagsAsAString + metadataTags[x];
        }
        return metadataTagsAsAString;
    }




    /**
     *
     * @param key
     * @param metadataItem  The metadata attribute item taken from the metadata profile.
     */
    function createComplianceLevelTagElement(key, metadataItem, variableValue) {
        // Pull out the relevant data items for the metadataItem object and assign to variables.
        var tagName = metadataItem.attributeName;
        var displayName = metadataItem.attributeName.replace(/_/g, " ");
        if (metadataItem.displayName) {
            displayName = metadataItem.displayName;
        } 
        var helpTip = "";
        if (metadataItem.description) {
            helpTip = metadataItem.description;
        } 
        if (metadataItem.exampleValues) {
            helpTip = helpTip + "  Examples: " + metadataItem.exampleValues;
        } 
        var complianceLevel = "additional"; // Default
        if (metadataItem.complianceLevel) {
            complianceLevel = metadataItem.complianceLevel;
        }

        // Assign any matching stored compliance level data to the tagValue.
        var tagValue = VariableStorageHandler.getComplianceLevelVariableData(key, tagName, complianceLevel);
        if (tagValue === undefined) {
            tagValue="";
        }
    
        // If we are building the units tag, add in the unit builder.
        var unitBuilderSelector = "";
        var unitBuilder = "";
        if (tagName === "units") {
            unitBuilderSelector = UnitBuilder.createShowUnitBuilderSelector();
            unitBuilder = UnitBuilder.createUnitBuilderTypeSelector();
        }

        // Create help tip element, if defined.
        var helpTipElement = "";
        if (helpTip !== "") {
            helpTipElement = "<img src=\"resources/img/help.png\" alt=\"" + helpTip + "\" />";
        }
    

        //   older additional metadata chooser 
        //   tag = "<option id=\"" + tagName + "__" + metadataTypeStructure + "\" value=\"" + tagName + "\">" + displayName + "</option>\n";


        // This may be a hold-over from a prior version of rosetta when users could possibly see the compliance level data if the metadataType
        // (coordinate or non-coordinate) and metadataTypeValue (string, int, etc.) were not specified by the user yet.  Just in case, I'm
        // keeping this here to disable the tag if those elements aren't available. 
        /*var isDisabled = "disabled";
        if (VariableStorageHandler.getVariableData(key, "metadataType") !== undefined) {
            if (VariableStorageHandler.getVariableData(key, "metadataTypeValue") !== undefined) {
                isDisabled = "";
            }
        }
        */

        // Create the tag!
        var tag = 
            "   <li>\n" +
            "    <label for=\"" + tagName + "\" class=\"error\"></label>" +
            "    <label>\n" +
            "     " + displayName + "\n" +
            "     " + helpTipElement + "\n" +
            "     <input id=\"" + tagName + "\" type=\"text\" name=\"" + tagName + "\" value=\"" + tagValue + "\" /> \n" +
            "    </label>\n" + unitBuilderSelector + unitBuilder +
            "   </li>\n";

        return tag;
    }   

    /**
     * Create the compliance-level input tags from the metadata profile data and add to DOM.
     *
     * @param key  The key used to access the stored variable data.
     */
    function addComplainceLevelDataToDialog(key) {

        // Add required attribute tags to DOM and bind events.
        $("#dialog #requiredMetadataAssignment ul").empty();
        var requiredMetadata = populateComplianceLevelInputTags(key, "required");
        if (requiredMetadata !== "") {
            DialogDomHandler.enableDiv("requiredMetadataAssignment");
            $("#dialog #requiredMetadataAssignment ul").append(requiredMetadata);
            bindRequiredMetadataEvents(key);
        }

        // Add recommended attribute tags to DOM and bind events.
        $("#dialog #recommendedMetadataAssignment ul").empty();
        var recommendedMetadata = populateComplianceLevelInputTags(key, "recommended");
        if (recommendedMetadata !== "") {
            DialogDomHandler.enableDiv("recommendedMetadataAssignment");
            $("#dialog #recommendedMetadataAssignment ul").append(recommendedMetadata);
            bindRecommendedMetadataEvents(key);
        }
        
        // Add addtional attribute tags to DOM and bind events.
        $("#dialog #additionalMetadataAssignment select").empty();
        var additionalMetadata = populateComplianceLevelInputTags(key, "additional");
        $("#dialog #additionalMetadataAssignment select").append(additionalMetadata);
        if (additionalMetadata !== "") {
            DialogDomHandler.enableDiv("additionalMetadataAssignment");
            $("#dialog #additionalMetadataAssignment ul").append(additionalMetadata);
            bindAdditionalMetadataEvents(key);
        }

        // Bind unit builder events.
        UnitBuilder.bindUnitBuilderEvent(key);

    }

    /**
     * This function binds events associated with required compliance-level metadata entries added to the dialog DOM.
     *
     * @param key  The key used to access the stored variable data.
     */
    function bindRequiredMetadataEvents(key) {
        $("#dialog #requiredMetadataAssignment input[type=\"text\"]").on("focusout", function () {

            // Get rid of any prior error messages.
            $(this).parents("li").find("label.error").text("");

            // Assign the attributeName & attributeValue to the user input.
            var attributeName = $(this).attr("name");
            var attributeValue = $(this).val();

            // Update the stored variable data with the required attribute.
            VariableStorageHandler.storeComplianceLevelVariableData(key, attributeName, attributeValue, "required");
    
            // validate user input
            //validateVariableData(key);
        });
    }


    /**
     * This function binds events associated with recommended compliance-level metadata entries added to the dialog DOM.
     *
     * @param key  The key used to access the stored variable data.
     */
    function bindRecommendedMetadataEvents(key) {
        $("#dialog #recommendedMetadataAssignment input[type=\"text\"]").on("focusout", function () {
             // Get rid of any prior error messages.
            $(this).parents("li").find("label.error").text("");

            // Assign the attributeName & attributeValue to the user input.
            var attributeName = $(this).attr("name");
            var attributeValue = $(this).val();

            // Update the stored variable data with the recommended attribute.
            VariableStorageHandler.storeComplianceLevelVariableData(key, attributeName, attributeValue, "recommended");
    
            // validate user input
            //validateVariableData(key);
        });
    }


    /**
     * This function binds events associated with additional compliance-level metadata entries added to the dialog DOM.
     *
     * @param key  The key used to access the stored variable data.
     */
    function bindAdditionalMetadataEvents(key) {
        $("#dialog #additionalMetadataAssignment input[type=\"text\"]").on("focusout", function () {

             // Get rid of any prior error messages.
            $(this).parents("li").find("label.error").text("");

            // Assign the attributeName & attributeValue to the user input.
            var attributeName = $(this).attr("name");
            var attributeValue = $(this).val();

            // Update the stored variable data with the recommended attribute.
            VariableStorageHandler.storeComplianceLevelVariableData(key, attributeName, attributeValue, "additional");
    
            // validate user input
            //validateVariableData(key);
        });
    }

    
    // Expose these functions.
    return {
        addComplainceLevelDataToDialog: addComplainceLevelDataToDialog
    };
    

})();


