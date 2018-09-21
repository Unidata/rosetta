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
        var metadataTypeStructure = VariableStorageHandler.getVariableData(key, "metadataTypeStructure");

        var metadataTags = [];
        // The metadataProfileVariableData comes from the server-side.
        for (var i = 0; i < metadataProfileVariableData.length; i++) {
            var metadataItem = metadataProfileVariableData[i];
            
            // Make server-side metadata profile values look like stored client-side values for the metadataType & complianceLevel.
            var type;
            if (metadataItem.metadataType === "CoordinateVariable") {
                type = "coordinate";
            }    
            if (metadataItem.metadataType === "DataVariable") {
                type = "non-coordinate";
            }  
            if (metadataItem.complianceLevel === "optional") {
                // We prefer the term additional to optional.
                metadataItem.complianceLevel = "additional";
            }

            // Make client-side metadataTypeStructure value look like server-side metadata profile values.
            if (metadataTypeStructure === "latitude") {
                metadataTypeStructure = "lat";
            } else if (metadataTypeStructure === "longitude") {
                metadataTypeStructure = "lon";
            } else if (metadataTypeStructure === "vertical") {
                metadataTypeStructure = "depth";
            } else {
                metadataTypeStructure = "time";
            }


            var tag;
            // Create a form tag from the metadata entries that match to:
            //    1) metadataType (coordinate or non-coordinate);
            //    2) compliance level (required, recommended, or additional); and
            //    3) metadataTypeStructure (lat, lon, etc.)
            if (type === metadataType) {
                if (metadataItem.complianceLevel === complianceLevel) {
                    if (metadataItem.metadataTypeStructure === metadataTypeStructure) {
                        tag = createComplianceLevelTagElement(key, metadataItem);
                        metadataTags.push(tag);
                    }
                }
            }

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
        var metadataTypeStructure;
        if (metadataItem.metadataTypeStructure) {
            metadataTypeStructure = metadataItem.metadataTypeStructure;
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
            unitBuilderSelector = UnitBuilderHandler.createShowUnitBuilderSelector();
            unitBuilder = UnitBuilderHandler.createUnitBuilderTypeSelector();
        }

        // Create help tip element, if defined.
        var helpTipElement = "";
        if (helpTip !== "") {
            helpTipElement = "<img src=\"resources/img/help.png\" alt=\"" + helpTip + "\" />";
        }
    
        // Create the tag!
        var tag;
        
        if (complianceLevel === "additional") {
            tag = "<option id=\"" + tagName + "__" + metadataTypeStructure + "\" value=\"" + tagName + "\">" + displayName + "</option>\n";
        } else {
            // This may be a hold-over from a prior version of rosetta when users could possibly see the compliance level data if the metadataType
            // (coordinate or non-coordinate) and metadataTypeValue (string, int, etc.) were not specified by the user yet.  Just in case, I'm
            // keeping this here to disable the tag if those elements aren't available. 
            var isDisabled = "disabled";
            if (VariableStorageHandler.getVariableData(key, "metadataType") !== undefined) {
                if (VariableStorageHandler.getVariableData(key, "metadataTypeValue") !== undefined) {
                    isDisabled = "";
                }
            }
    
            tag = 
                "   <li>\n" +
                "    <label for=\"" + tagName + "\" class=\"error\"></label>" +
                "    <label>\n" +
                "     " + displayName + "\n" +
                "     " + helpTipElement + "\n" +
                "     <input id=\"" + tagName + "__" + metadataTypeStructure + "\" type=\"text\" name=\"" + tagName + "\" value=\"" + tagValue + "\"" + isDisabled + "/> \n" +
                "    </label>\n" + unitBuilderSelector + unitBuilder +
                "   </li>\n";
        }
        return tag;
    }   

    /**
     * Create the empty metadata input tags from the metadata profile data passed from the server-side and add to DOM.
     *
     * @param key  The key used to store the data in the variableMetadata value field.
     */
    function addComplainceLevelDataToDialog(key) {
        $("#dialog #requiredMetadataAssignment ul").empty();
        var requiredMetadata = populateComplianceLevelInputTags(key, "required");
        $("#dialog #requiredMetadataAssignment ul").append(requiredMetadata);

        $("#dialog #recommendedMetadataAssignment ul").empty();
        var recommendedMetadata = populateComplianceLevelInputTags(key, "recommended");
        $("#dialog #recommendedMetadataAssignment ul").append(recommendedMetadata);

        $("#dialog #additionalMetadataAssignment select").empty();
        var additionalMetadata = populateComplianceLevelInputTags(key, "additional");
        $("#dialog #additionalMetadataAssignment select").append(additionalMetadata);

        // remove any additional metadata items added to the DOM since the additional metadata choices gets redrawn
        //$("#dialog #additionalMetadataAssignment ul").remove("ul");

        bindGeneralMetadataEvents(key);
    }
    
    // Expose these functions.
    return {
        addComplainceLevelDataToDialog: addComplainceLevelDataToDialog
    };
    

})();


