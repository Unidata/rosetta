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
        
        // Get the compliance-level data in map form to quickly eliminate duplicates and create the correct input tag.
        var complianceLevelData = findDuplicateAttributes();

        // Get the metadata profile data that corresponds to the provided complianceLevel.
        var metadataTypesMap = complianceLevelData.get(complianceLevel);

        // Get the attribute names data that corresponds to the stored metadataType.
        var attributeNamesMap = metadataTypesMap.get(metadataType);
        
        if (metadataTypeStructure !== undefined) {
            if (metadataTypeStructure === "latitude") {
                metadataTypeStructure = "lat";
            } else if (metadataTypeStructure === "longitude") {
                metadataTypeStructure = "lon";
            } else if (metadataTypeStructure === "vertical") {
                metadataTypeStructure = "depth";
            } else  {
                metadataTypeStructure = "time";
            }
        }

        
        for (const [key, metadataTypeStructures] of attributeNamesMap) {
            var tag;

            if (metadataTypeStructure !== undefined) {
                if (metadataTypeStructures.includes(metadataTypeStructure)) {
                    tag = createComplianceLevelTagElement(key, metadataItem);
                }
            } else {
                tag = createComplianceLevelTagElement(key, metadataItem);
            }
            console.log(key, value);
            console.log(metadataTypeStructure);
            
            // var tag = createComplianceLevelTagElement(key, metadataItem);
            // metadataTags.push(tag);
            //console.log(tag);
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
     * This function loops through the server-side metadata profile data and creates the following map based on compliance level:
     *
     * map name              key                  value
     *-----------------------------------------------------------------------------
     * complianceLevelMap    compliance level     metadataTypesMap
     * metadataTypesMap      metadata type        attributeNamesMap
     * attributeNamesMap     attribute name       array of metadata type structures
     * 
     * @return  The aforementioned compliance level map.
     */           
    function findDuplicateAttributes() {
        var complianceLevelsMap = new Map();
        // The metadataProfileVariableData comes from the server-side.
        for (var i = 0; i < metadataProfileVariableData.length; i++) {
            var attributeName = metadataProfileVariableData[i].attributeName;
            var metadataType = metadataProfileVariableData[i].metadataType;
            // Make the data from metadata profile look like the client-side data.
            if (metadataType === "CoordinateVariable") {
                metadataType = "coordinate";
            } 
            if (metadataType === "DataVariable") {
                metadataType = "non-coordinate";
            }  
            var metadataTypeStructure = metadataProfileVariableData[i].metadataTypeStructure;
            var complianceLevel = metadataProfileVariableData[i].complianceLevel;
            // Make the data from metadata profile look like the client-side data.
            if (complianceLevel === "optional") {
                complianceLevel = "additional";
            }

            if (complianceLevelsMap.has(complianceLevel)) {
                // Already exists.

                // Get the existing metadata type map.
                var metadataTypesMap = complianceLevelsMap.get(complianceLevel);
    
                if (metadataTypesMap.has(metadataType)) {
                    // Already exists.

                    // Get the existing metadata type map.
                    var attributeNamesMap = metadataTypesMap.get(metadataType);

                    if (attributeNamesMap.has(attributeName)) {
                        // Already exists.

                        // Get the existing metadata type structures array.
                        var metadataTypeStructures = attributeNamesMap.get(attributeName);
                        metadataTypeStructures.push(metadataTypeStructure);
                                    
                     } else {
                        // Doesn't exist in the attribute names map yet.
                    
                        // Add attribute name what holds an array of corresponding the metadata type structures.
                        attributeNamesMap.set(attributeName, [metadataTypeStructure]);
                     }

                } else {
                    // Doesn't exist in the metadata types map yet.

                    // Create a map for the attribute names where the attribute name holds an array of corresponding the metadata type structures.
                    var attributeNamesMap = new Map();
                    attributeNamesMap.set(attributeName, [metadataTypeStructure]);

                    // Add the attribute names map to the metadata types map.
                    metadataTypesMap.set(metadataType, attributeNamesMap);
                }
                
            } else {
                // Doesn't exist in the compliance levels map yet.

                // Create a map for the attribute names where the attribute name holds an array of corresponding the metadata type structures.
                var attributeNamesMap = new Map();
                attributeNamesMap.set(attributeName, [metadataTypeStructure]);

                // Create a map for the metadata types and add the attribute names map.
                var metadataTypesMap = new Map();
                metadataTypesMap.set(metadataType, attributeNamesMap);

                // Add the metadata types map to the compliance level map.
                complianceLevelsMap.set(complianceLevel, metadataTypesMap);
            }
        }

        return complianceLevelsMap;
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
        var isDisabled = "disabled";
        if (VariableStorageHandler.getVariableData(key, "metadataType") !== undefined) {
            if (VariableStorageHandler.getVariableData(key, "metadataTypeValue") !== undefined) {
                isDisabled = "";
            }
        }

        // Create the tag!
        var tag = 
            "   <li>\n" +
            "    <label for=\"" + tagName + "\" class=\"error\"></label>" +
            "    <label>\n" +
            "     " + displayName + "\n" +
            "     " + helpTipElement + "\n" +
            "     <input id=\"" + tagName + "__" + metadataTypeStructure + "\" type=\"text\" name=\"" + tagName + "\" value=\"" + tagValue + "\"" + isDisabled + "/> \n" +
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
        console.log(key);
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

        // Bind events associated with thr 
        bindRequiredMetadataEvents(key);
        bindRecommendedMetadataEvents(key);
        bindAdditionalMetadataEvents(key);
        UnitBuilder.bindUnitBuilderEvent(key);

    }

    /**
     * Creates the HTML input tag for collecting additional metadata.  This function is called
     * when the user selections a value from the additional metadata chooser, or when
     * pre-populating the dialog form with pre-existing values.
     *
     * @param tagName  The item that goes in the name attribute of the input tag.
     * @param displayName  The display or "pretty" name for the input tag.
     * @param tagValue  The pre-existing value gleaned from the variableMetadata value field (if it exists).
     */
    function createAdditionalMetadataTag(tagName, displayName, tagValue) {
        // Create a form tag containing metadata information
        // The additional metadata sectional has a general error tag associated with it for general
        // errors, and we are creating error tags associated with the input tags for displaying
        // specific errors.
        return "   <li>\n" +
            "    <label for=\"" + tagName + "\" class=\"error\"></label> \n" +
            "    <label>\n" +
            "     " + displayName + "\n" +
                "     <input type=\"text\" name=\"" + tagName + "\" value=\"" + tagValue + "\"/> \n" +
            "    </label>\n" +
            "   </li>\n";
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
            storeComplianceLevelVariableData(key, attributeName, attributevalue, "required");
    
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
        $("#dialog #recommendedMetadataAssignment input").on("focusout", function () {
             // Get rid of any prior error messages.
            $(this).parents("li").find("label.error").text("");

            // Assign the attributeName & attributeValue to the user input.
            var attributeName = $(this).attr("name");
            var attributeValue = $(this).val();

            // Update the stored variable data with the recommended attribute.
            storeComplianceLevelVariableData(key, attributeName, attributevalue, "recommended");
    
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
        $("#dialog #additionalMetadataAssignment input").on("focusout", function () {
             // Get rid of any prior error messages.
            $(this).parents("li").find("label.error").text("");

            // Assign the attributeName & attributeValue to the user input.
            var attributeName = $(this).attr("name");
            var attributeValue = $(this).val();

            // Update the stored variable data with the recommended attribute.
            storeComplianceLevelVariableData(key, attributeName, attributevalue, "additional");
    
            // validate user input
            //validateVariableData(key);
        });
    }


    /************************************ older additional metadata chooser *************************/
    /**
     * This function binds general events associated with the metadata entries added to the dialog DOM.
     *
     * @param key  The key used to store the data in the variableMetadata value field.
     */
    /*
    function bindAdditionalMetadataEventBuilder(key) {
       
        // additional metadata chooser
        $("#dialog #additionalMetadataAssignment img#additionalMetadataChooser").unbind("click").bind("click", function () {
    
            var additionalMetadataSelected = $("#dialog #additionalMetadataAssignment select[name=\"additionalMetadata\"]").val();
    
            // get the display name
            var displayName = getMetadataDisplayName(additionalMetadataSelected);
    
            // see if the user has already provided the value to some of these metadata items.
            var tagValue = getItemEntered(key + "Metadata", additionalMetadataSelected);
            if (tagValue === null) {
                tagValue = "";
            }
    
            var tag = ComplianceLevelDataHandler.createAdditionalMetadataTag(additionalMetadataSelected, displayName, tagValue);
    
            var additionalMetadataInputTags = $("#dialog #additionalMetadataAssignment ul");
    
    
            if ($(this).attr("alt") === "Add Metadata") { // Adding an additional metadata item
    
                // get rid of any global error messages
                $("#additionalMetadataAssignment").find("label[for=\"additionalMetadataAssignment\"].error").text("");
    
                if ($(additionalMetadataInputTags).length === 0) {
                    // no metadata has been added yet, so create bulleted list and add tag.
                    $("#dialog #additionalMetadataAssignment").append("<ul>" + tag + "</ul>");
                } else {
                    // metadata has already been added and bulleted list exists.
    
                    // trying to add an already existing metadata item: show error message
                    if ($(additionalMetadataInputTags).find("li input[name=\"" + additionalMetadataSelected + "\"]").length > 0) {
                        $("#additionalMetadataAssignment").find("label[for=\"additionalMetadataAssignment\"].error")
                                .text("'" + getMetadataDisplayName(additionalMetadataSelected)
                                + "' has already been selected.");
                    } else {
                        // append new tag
                        $(additionalMetadataInputTags).append(tag);
                    }
                }
    
            } else { // Removing an additional metadata item
                // get rid of any global error messages
                $("#additionalMetadataAssignment").find("label[for=\"additionalMetadataAssignment\"].error").text("");
    
                if ($(additionalMetadataInputTags).find("li input[name=\"" + additionalMetadataSelected + "'\"]").length === 0) {
                    // trying to add an remove a metadata item that doesn't exist: show error message
                    $("#additionalMetadataAssignment").find("label[for=\"additionalMetadataAssignment\"].error")
                            .text("'" + getMetadataDisplayName(additionalMetadataSelected)
                            + "' has NOT been selected and therefore cannot be removed.");
                } else {
                    // remove existing metadata item
                    $(additionalMetadataInputTags).find("li input[name=\"" + additionalMetadataSelected + "\"]").parents("li").remove();
                    var listChildren = $(additionalMetadataInputTags).find("li");
                    if ($(listChildren).length <= 0) {
                        $(additionalMetadataInputTags).remove("ul");
                    }
                    // remove from variableMetadata value field as well
                    removeItemFromSessionString(key + "Metadata", additionalMetadataSelected);
                }
            }
            // bind the events for the newly created additional metadata input tags
            bindAdditionalMetadataEvents(key);
        });
    }
    */
    /********************* event binder for older additional metadata chooser *************************/
    /**
     * This function binds events associated with the additional metadata entries added to the dialog DOM.
     *
     * @param key  The key used to store the data in the variableMetadata value field.
     */
    /*
    function bindAdditionalMetadataEvents(key) {
        // Additional metadata entries
        $("#dialog #additionalMetadataAssignment ul li input").on("focusout", function () {
            var tagName = $(this).attr("name");
            var tagValue = $(this).val();
    
            // get rid of any error messages
            $(this).parents("li").find("label[for=\"" + tagName + "\"].error").text("");
    
            // concatenation the entered value to any existing Metadata values pulled from the variableMetadata value field
            var metadataString = buildStringForSession(key + "Metadata", tagName, tagValue);
    
            // update the data in the variableMetadata value field
            addToSession(key + "Metadata", metadataString);
    
            // validate user input
            //validateVariableData(key);
        });
    }
    */

    
    // Expose these functions.
    return {
        addComplainceLevelDataToDialog: addComplainceLevelDataToDialog,
        createAdditionalMetadataTag: createAdditionalMetadataTag
    };
    

})();


