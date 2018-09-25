/**
 * GlobalMetadata.js
 *
 * Module to global metadata.
 */
var GlobalMetadata = (function () {

    function populateTags(metadataProfileGeneralData) {

        // We will stash our required attribute names here.
        var required = [];

        var metadataGroupsMap = new Map();
        for (var i = 0; i < metadataProfileGeneralData.length; i++) {
            var attribute = metadataProfileGeneralData[i];
            var metadataGroup = attribute.metadataGroup;
            var complianceLevel = attribute.complianceLevel;
            var attributeName = attribute.attributeName;
            var displayName = attribute.displayName;
            var metadataValueType = attribute.metadataValueType;
            if (!displayName) {
                displayName = attributeName;
            }
            var description = attribute.description;
            var exampleValues = attribute.exampleValues;
            
            if (complianceLevel === "required") {
                required.push(attributeName + "__" + metadataGroup);
            }

            if (complianceLevel === "optional") {
                complianceLevel = "additional";
            }
    
            var tag =     
                "<li>\n" +
                "   <label>\n" +
                "       " + displayName + "\n" +
                "       <small>(" + complianceLevel + ")</small>\n";
            if (description  || exampleValues) {
                tag = tag + "       <img src=\"resources/img/help.png\" alt=\"" + description + " Examples: " + exampleValues + "\"/>\n";
            }
            tag = tag +    
                "       <br/>\n" +
                "       <input class=\"" + complianceLevel + "\" type=\"text\" name=\"" + attributeName + "\" id=\"" + attributeName + "__" + metadataGroup+ "\" value=\"\"/>\n" +
                "   </label>\n" +
                "   <label for=\"" + attributeName + "\" class=\"error\"></label>\n" +
                "</li>\n";

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

    function attachToDom(metadataGroupsMap) {
        for (const [metadataGroup, complianceLevelsMap] of metadataGroupsMap.entries()) {   
            var title = metadataGroup.replace(/_/g, ' ');
            if (title === "root") {
                title = "general";
            }            
            title = title.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
            $("#generalMetadataAssignment").append(
                "<div class=\"category\" id =\"" + metadataGroup + "\">\n" +
                "   <h5 id=\"" + metadataGroup + "Toggle\" class=\"toggle expand\">" + title + "</h5>\n" +
                "   <ul class=\"hideMe\" id=\"" + metadataGroup + "ToggleSection\"></ul>\n" +
                "</div>\n"
            );
            for (const [complianceLevel, tags] of complianceLevelsMap.entries()) {
                for (var i = 0; i < tags.length; i++) {
                    $("#generalMetadataAssignment #" + metadataGroup + " ul").append(tags[i]);
                }
            }
        }
        populateDataFromStorage();
    }

    function bindGeneralMetadataEvents() {
        $("#generalMetadataAssignment input").focusout(function () {
            storeGlobalMetadataEntry($(this).attr("id"), $(this).val());
            isComplete();      
        });
    }    

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

    function populateDataFromStorage() {
        // Get the stored global data.
        var globalMetadata = getStoredGlobalMetadata();
        Object.keys(globalMetadata).forEach(function(key) {
            var inputTag = $("#generalMetadataAssignment input#" + key);
            $(inputTag).val(globalMetadata[key]);
        });
        toggleCategories();
    }

    function toggleCategories() {
        $("#generalMetadataAssignment input").each(function () {  
            var inputTag = $(this);
            $(inputTag).prevAll().each(function () { 
                if($(this).prop("tagName") === "SMALL") {
                    var complianceLevel = $(this).text();
                    if (complianceLevel === "(required)") {
                        if (!$(inputTag).val().match(/\w+/)) {
                            $(inputTag).parents().each(function () { 
                                var parent = (this);
                                if($(parent).prop("tagName") === "UL") {
                                    $(parent).removeClass("hideMe");
                                    var categoryTitle = $(parent).prev();
                                    $(categoryTitle).removeClass("expand");
                                    $(categoryTitle).addClass("collapse");
                                }
                            });                   
                        }
                    }
                }
            });
        });
    }



    function storeGlobalMetadataEntry(key, value) {
        // Get the stored global data.
        var globalMetadata = getStoredGlobalMetadata();
        
        // Assign the provided object key to the provided value.
        globalMetadata[key] = value;

        // Update the stored global data.
        updateStoredGlobalMetadata(globalMetadata);
    }


    function removeGlobaMetadataEntry(key) {
        // Get the stored global data.
        var globalMetadata =getStoredGlobalMetadata();

        // Delete the entry using the provided object key.
        delete globalMetadata[key];

        // Update the stored global data.
        updateStoredGlobalMetadata(globalMetadata);
    }

   function getGlobalDataMetadataEntry(key) {
        // Get the stored global data.
        var globalMetadata = getStoredGlobalMetadata();

        return globalMetadata[key];
    }

 
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

    function updateStoredGlobalMetadata(globalMetadata) {
        // Stringify and stored data.
        storeData("globalMetadata", JSON.stringify(globalMetadata));
    }

    function displayErrorMessage(message) {
        $("#dialog #variableNameTypeAssignment").find("label.error").text(message + "  Please contact the Rosetta site administrator.");
    }





    // Expose these functions.
    return {
        populateTags: populateTags
    };
    

})();
