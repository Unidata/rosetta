/**
 * GlobalMetadata.js
 *
 * Module to global metadata.
 */
var GlobalMetadata = (function () {

    /**
     * Uses the server-sent metadata profile information to create the global variable tags.
     *
     * @param metadataProfileGeneralData  The array of global variables.
     */
    function populateTags(metadataProfileGeneralData) {

        // We will stash our required variable names here.
        var required = [];

        // We will stash our tags in here by group.
        var metadataGroupsMap = new Map();

        for (var i = 0; i < metadataProfileGeneralData.length; i++) {
            var variable = metadataProfileGeneralData[i];
            var metadataGroup = variable.metadataGroup;
            var complianceLevel = variable.complianceLevel;
            var attributeName = variable.attributeName;
            var displayName = variable.displayName;
            // If there is no display name provided, use the attribute name.
            if (!displayName) {
                displayName = attributeName.replace(/_/g, ' ');
            }
            var description = variable.description;
            var exampleValues = variable.exampleValues;

            // Push required variables onto the required array (used later for validation).
            if (complianceLevel === "required") {
                required.push(attributeName + "__" + metadataGroup);
            }
            // We prefer the term additional to optional.
            if (complianceLevel === "optional") {
                complianceLevel = "additional";
            }

            // Create the tag.
            var tag =     
                "<li>\n" +
                "   <label>\n" +
                "       " + displayName + "\n";
            if (description  || exampleValues) {
                tag = tag + "       <img src=\"resources/img/help.png\" alt=\"" + description + "<br/><br/> Examples:<br/> " + exampleValues + "\"/>\n";
            }
            tag = tag +
                "       <br/>\n" +
                "       <input type=\"text\" name=\"" + attributeName + "\" id=\"" + attributeName + "__" + metadataGroup+ "\" value=\"\"/>\n" +
                "   </label>\n" +
                "   <label for=\"" + attributeName + "\" class=\"error\"></label>\n" +
                "</li>\n";


            // Assign to the correct map entry.
            if (metadataGroupsMap.has(metadataGroup)) {
                // Already exists.
    
                // get the compliance level map.
                var complianceLevelsMap =  metadataGroupsMap.get(metadataGroup);
        
                if (complianceLevelsMap.has(complianceLevel)) {
                    // Already exists.
                
                    // Get array of tags.
                    var tags =  complianceLevelsMap.get(complianceLevel);
                    tags.push(tag);
    
                } else {
                    // Doesn't exist in the compliance level map yet.
                    
                    // Add tag to compliance level map.
                    complianceLevelsMap.set(complianceLevel, [tag]);
        
                    // Add the compliance levels map to the metadata group map.
                    metadataGroupsMap.set(metadataGroup, complianceLevelsMap);
                }

            } else {
                // Doesn't exist in the metadata group map yet.
            
                // Create compliance levels map and add the tag string to the inner array.
                var complianceLevelsMap = new Map();
                complianceLevelsMap.set(complianceLevel, [tag]);
    
                // Add the compliance levels map to the metadata group map.
                metadataGroupsMap.set(metadataGroup, complianceLevelsMap);
            }
        }
        
        // Store required globals.
        storeData("_g", required);

        // Attach tags to the DOM.
        attachToDom(metadataGroupsMap);

        // Bind general metadata events.
        bindGeneralMetadataEvents() 
    }



    /**
     *
     *
     * @param metadataGroupsMap   The map of tags organized by group.
     */
    function attachToDom(metadataGroupsMap) {

        // Sort the map.
        metadataGroupsMap = sortMapByKeys(metadataGroupsMap, "normal");

        // For each map key (group).
        for (var [metadataGroup, complianceLevelsMap] of metadataGroupsMap.entries()) {

            // Reverse sort the map.
            complianceLevelsMap = sortMapByKeys(complianceLevelsMap, "reverse");

            // Create a category title from the metadata group.
            var title = metadataGroup.replace(/_/g, ' ');
            if (title === "root") {
                title = "general";
            }
            // Capitalize first character.
            title = title.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
            $("#generalMetadataAssignment").append(
                "<div class=\"category\" id =\"" + metadataGroup + "\">\n" +
                "   <h5 id=\"" + metadataGroup + "Toggle\" class=\"toggle expand\">" + title + "</h5>\n" +
                "   <div class=\"hideMe\" id=\"" + metadataGroup + "ToggleSection\"></div>\n" +
                "</div>\n"
            );

            for (const [complianceLevel, tags] of complianceLevelsMap.entries()) {
                var complianceLevelTitle = complianceLevel.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
                $("#generalMetadataAssignment #" + metadataGroup + " #"+ metadataGroup + "ToggleSection").append("<p><b>" + complianceLevelTitle + " Metadata:</b></p>");
                $("#generalMetadataAssignment #" + metadataGroup + " #"+ metadataGroup + "ToggleSection").append("<ul id=\"" + complianceLevel + "\"></ul>");
                 for (var i = 0; i < tags.length; i++) {
                    $("#generalMetadataAssignment #" + metadataGroup + " ul#" + complianceLevel).append(tags[i]);
                 }
            }
        }
        // Populate the tags with stored values.
        populateDataFromStorage();
    }

    /**
     * Sorts the provided map by keys according to the given sort order.
     *
     * @param map   The map to sort.
     * @param sortOrder  The order of the sort.
     * @returns {Map}   The sorted map.
     */
    function sortMapByKeys(map, sortOrder) {
        var keys = [];
        for (const [key, value] of map.entries()) {
            keys.push(key);
        }
        keys.sort();
        if (sortOrder === "reverse") {
            keys.reverse();
        }
        var sortedMap = new Map();
        for (var i = 0; i < keys.length; i++) {
            var key = keys[i];
            sortedMap.set(key, map.get(key));
        }
        return sortedMap;
    }

    /**
     * Binds focus out events for the input tags added to the DOM. Stores the entered values.
     */
    function bindGeneralMetadataEvents() {
        $("#generalMetadataAssignment input").focusout(function () {
            if ($(this).val().match(/\w+/)) {
                $(this).removeClass("required");
                storeGlobalMetadataEntry($(this).attr("id"), $(this).val());
                isComplete();
            }
        });
    }

    /**
     * Tests to see if all the required metadata has an entry.
     * Enables the next button if all required global metadata has been provided.
     */
    function isComplete() {
        if (testCompleteness()) {
            // Add stored global metadata info to input tag value.
            $("#globalMetadata").val(JSON.stringify(getStoredGlobalMetadata()));

            // remove disabled status for submit button.
            $("input[type=submit]#Next").removeAttr("disabled");
            // remove disabled class for submit button.
            $("input[type=submit]#Next").removeClass("disabled");
        }
    }

    /**
     * Compares the stored global metadata entries with the list of required entries.
     * Returns a boolean value based on whether all the required entries have been provided.
     *
     * @returns {boolean}   true if all required items are provided; otherwise false.
     */
    function testCompleteness() {
        var required = getStoredData("_g").split(/,/g);

        // Get the stored global data.
        var globalMetadata = getStoredGlobalMetadata();
        var stored = [];
        Object.keys(globalMetadata).forEach(function(key) {
            stored.push(key);
        });
        
        for (var i = 0; i < required.length; i++) {
            if (!stored.includes(required[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Populates the input tags with stored data.
     */
    function populateDataFromStorage() {
        // Get the stored global data.
        var globalMetadata = getStoredGlobalMetadata();
        Object.keys(globalMetadata).forEach(function(key) {
            var inputTag = $("#generalMetadataAssignment input#" + key);
            $(inputTag).val(globalMetadata[key]);
        });
        // Expand/collapse relevant categories.
        toggleCategories();
    }

    /**
     *
     */
    function toggleCategories() {
        // Check each input field.
        $("#generalMetadataAssignment input").each(function () {  
            var inputTag = $(this);
            // Look at only the required tags.
            var ulTags = $(inputTag).parents("ul");
            if($(ulTags).attr("id") === "required") {
                // If the required input tag does not have a provided value.
                if (!$(inputTag).val().match(/\w+/)) {
                    // Find the toggle section div.
                    $(inputTag).parents("div").each(function () {
                        var parent = $(this);
                        if($(parent).attr("id").includes("ToggleSection")) {
                            // Expand the section that contains the required fields that need to be populated.
                            $(parent).removeClass("hideMe");
                            // Change the icon for the category header.
                            var categoryTitle = $(parent).prev("");
                            $(categoryTitle).removeClass("expand");
                            $(categoryTitle).addClass("collapse");
                            // Add required class to input tag.
                            $(inputTag).addClass("required");
                        }
                    });
                }
            }
        });
    }

    /**
     * Stores the provided key/value pair.
     *
     * @param key   The key of the entry to store.
     * @param value The value to store.
     */
    function storeGlobalMetadataEntry(key, value) {
        // Get the stored global data.
        var globalMetadata = getStoredGlobalMetadata();
        
        // Assign the provided object key to the provided value.
        globalMetadata[key] = value;

        // Update the stored global data.
        updateStoredGlobalMetadata(globalMetadata);
    }

    /**
     * Removes the entry corresponding to the provided key from storage.
     *
     * @param key   The key of the entry to remove.
     */
    function removeGlobaMetadataEntry(key) {
        // Get the stored global data.
        var globalMetadata =getStoredGlobalMetadata();

        // Delete the entry using the provided object key.
        delete globalMetadata[key];

        // Update the stored global data.
        updateStoredGlobalMetadata(globalMetadata);
    }

    /**
     * Retrieves the stored entry corresponding to the provided key.
     *
     * @param key    The key of the item to retrieve.
     * @returns {*}  The stored entry.
     */
   function getGlobalDataMetadataEntry(key) {
        // Get the stored global data.
        var globalMetadata = getStoredGlobalMetadata();

        return globalMetadata[key];
    }

    /**
     * Retrieves ALL stored global metadata entries.
     *
     * @returns {any} The global metadata.
     */
    function getStoredGlobalMetadata() {
        // Get the stored global data.
        var globalMetadata = getStoredData("globalMetadata");

        if (globalMetadata === null) {
            // This shouldn't happen; if we are here then something has gone very wrong (this info should be in the stored).
            displayErrorMessage("Unable to access stored variable matadata.");
        } else {
            // Un-stringify.
            return JSON.parse(globalMetadata);
        }
    }

    /**
     * Updated the stored global metadata with the provided object.
     *
     * @param globalMetadata The global metadata object.
     */
    function updateStoredGlobalMetadata(globalMetadata) {
        // Stringify and stored data.
        storeData("globalMetadata", JSON.stringify(globalMetadata));
    }

    // Expose these functions.
    return {
        populateTags: populateTags
    };
    

})();
