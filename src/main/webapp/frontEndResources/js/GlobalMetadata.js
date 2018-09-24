/**
 * GlobalMetadata.js
 *
 * Module to global metadata.
 */
var GlobalMetadata = (function () {

    function populateTags(metadataProfileGeneralData) {
        var metadataGroupsMap = new Map();
        for (var i = 0; i < metadataProfileGeneralData.length; i++) {
            var attribute = metadataProfileGeneralData[i];
            var metadataGroup = attribute.metadataGroup;
            var complianceLevel = attribute.complianceLevel;
            var attributeName = attribute.attributeName;
            var displayName = attribute.displayName;
            if (!displayName) {
                displayName = attributeName;
            }
            var description = attribute.description;
            var exampleValues = attribute.exampleValues;
    
    
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
                "       <input class=\"" + complianceLevel + "\" type=\"text\" name=\"" + attributeName + "\" id=\"\" value=\"\"/>\n" +
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
        console.log(metadataGroupsMap);
        // Attach tags to the DOM.
        attachToDom(metadataGroupsMap);
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
    }


    // Expose these functions.
    return {
        populateTags: populateTags
    };
    

})();
