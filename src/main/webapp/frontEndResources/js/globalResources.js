/**
 * globalResources.css
 *
 * Custom functions and variables that load resources from the server via AJAX.
 */

/**
 * Global vars needed in various places
 */
var cfStandards = [];
var cfStandardUnits = {};
var metadata = [];
var unitBuilderData = [];

/**
 * Populates the cfStandards array with data from the cf-standard-name-table.xml file.
 */
function loadCFStandards() {
    $.get("resources/cf-standard-name-table.xml",
          function (data) {
              var s = [];
              $(data).find("entry").each(function () {
                  s.push($(this).attr("id"));
              });
              cfStandards = s;
          },
          "xml");
}

/**
 * Populates the cfStandardsUnits object with data from the cf-standard-name-table.xml file.
 */
function loadCFStandardUnits() {
    $.get("resources/cf-standard-name-table.xml",
          function (data) {
              var u = {};
              $(data).find("entry").each(function () {
                  u[$(this).attr("id")] = $(this).find("canonical_units").text();
              });
              cfStandardUnits = u;
          },
          "xml");
}

/**
 * Populates the metadata array with data from the metadata.xml file.
 */
function loadMetadata() {
    $.get("resources/metadata.xml",
          function (data) {
              var m = [];
              $(data).find("entry").each(function () {
                  var e = {};
                  e["entry"] = $(this).attr("id");
                  e["displayName"] = $(this).attr("displayName");
                  e["type"] = $(this).find("type").text();
                  e["necessity"] = $(this).find("necessity").text();
                  e["helptip"] = $(this).find("helptip").text();
                  m.push(e);
              });
              metadata = m;
          },
          "xml");
}

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
 * This function accepts a metadata id or entry and looks up the
 * corresponding metadata display name from the list of known metadata
 * items.  If one is found either the display name is returned (if found)
 * or the entry/id name is returned (if not found).
 *
 * @param metadataId  The metadata item entry or id as it appears in the reference XML file.
 */
function getMetadataDisplayName(metadataId) {
    var displayName = metadataId;
    // loop through known metadata 
    for (var i = 0; i < metadata.length; i++) {
        var metadataItem = metadata[i];
        if (metadataItem.entry == metadataId) {
            if (metadataItem.displayName != undefined) {
                displayName = metadataItem.displayName;
            }
        }
    }
    return displayName;
}

/**
 * Returns an array of the required metadata for the specified variable type.
 *
 * @param variableType  The variable "type" (coordinate variable or not).
 */
function getKnownRequiredMetadataList(variableType) {
    var requiredMetadata = [];

    // loop through known metadata 
    for (var i = 0; i < metadata.length; i++) {
        var metadataItem = metadata[i];

        // Grab the metadata entries that correspond to the variable type (coordinate or
        // non-coordinate),  grab the required metadata entries 
        if (metadataItem.type == variableType) {
            if (metadataItem.necessity == "required") {
                requiredMetadata.push(metadataItem.entry);
            }
        }

        // Grab the required metadata entries that correspond to the both coordinate or
        // non-coordinate variables
        if (metadataItem.type == "both") {
            if (metadataItem.necessity == "required") {
                requiredMetadata.push(metadataItem.entry);
            }
        }
    }
    return requiredMetadata;
}

/**
 * Returns an array of the recommended metadata for the specified variable type.
 *
 * @param variableType  The variable "type" (coordinate variable or not).
 */
function getKnownRecommendedMetadataList(variableType) {
    var recommendedMetadata = [];

    // loop through known metadata 
    for (var i = 0; i < metadata.length; i++) {
        var metadataItem = metadata[i];

        // Grab the metadata entries that correspond to the variable type (coordinate or
        // non-coordinate),  grab the recommended metadata entries 
        if (metadataItem.type == variableType) {
            if (metadataItem.necessity == "recommended") {
                recommendedMetadata.push(metadataItem.entry);
            }
        }

        // Grab the recommended metadata entries that correspond to the both coordinate or
        // non-coordinate variables
        if (metadataItem.type == "both") {
            if (metadataItem.necessity == "recommended") {
                recommendedMetadata.push(metadataItem.entry);
            }
        }
    }
    return recommendedMetadata;
}

/**
 * Returns an array of the additional metadata for the specified variable type.
 *
 * @param variableType  The variable "type" (coordinate variable or not).
 */
function getKnownAdditionalMetadataList(variableType) {
    var additionalMetadata = [];

    // loop through known metadata 
    for (var i = 0; i < metadata.length; i++) {
        var metadataItem = metadata[i];

        // Grab the metadata entries that correspond to the variable type (coordinate or
        // non-coordinate),  grab the additional metadata entries 
        if (metadataItem.type == variableType) {
            if (metadataItem.necessity == "additional") {
                additionalMetadata.push(metadataItem.entry);
            }
        }

        // Grab the additional metadata entries that correspond to the both coordinate or
        // non-coordinate variables
        if (metadataItem.type == "both") {
            if (metadataItem.necessity == "additional") {
                additionalMetadata.push(metadataItem.entry);
            }
        }
    }
    return additionalMetadata;
}

/**
 * This function checks to see if a particular metadata item (the tag name, aka the metadata
 * entry or id as it appears in the reference XML file) is deemed required.  Returns true if
 * it is and false if it not.
 *
 * @param variableType  The variable "type" (coordinate variable or not).
 * @param metadataItemToTest  The metadata values entered by the user and stored in the session
 */
function isRequiredMetadata(variableType, metadataItemToTest) {
    var requiredMetadata = getKnownRequiredMetadataList(variableType);
    if (requiredMetadata.indexOf(metadataItemToTest) >= 0) {
        return true;
    } else {
        return false;
    }
}

/**
 * This function checks to see if a particular metadata item (the tag name, aka the metadata
 * entry or id as it appears in the reference XML file) is deemed recommended.  Returns true if
 * it is and false if it not.
 *
 * @param variableType  The variable "type" (coordinate variable or not).
 * @param metadataItemToTest  The metadata values entered by the user and stored in the session
 */
function isRecommendedMetadata(variableType, metadataItemToTest) {
    var recommendedMetadata = getKnownRecommendedMetadataList(variableType);
    if (recommendedMetadata.indexOf(metadataItemToTest) >= 0) {
        return true;
    } else {
        return false;
    }
}

/**
 * This function checks to see if a particular metadata item (the tag name, aka the metadata
 * entry or id as it appears in the reference XML file) is deemed additional.  Returns true if
 * it is and false if it not.
 *
 * @param variableType  The variable "type" (coordinate variable or not).
 * @param metadataItemToTest  The metadata values entered by the user and stored in the session
 */
function isAdditionalMetadata(variableType, metadataItemToTest) {
    var additionalMetadata = getKnownAdditionalMetadataList(variableType);
    if (additionalMetadata.indexOf(metadataItemToTest) >= 0) {
        return true;
    } else {
        return false;
    }
}

/**
 * Uses the array of cfStandard names gleaned from the cf-standard-name-table.xml file.
 * Loop through the array and if we have a match, return true.  If no match, return false.
 *
 * @param variableName  The name of the variable we are testing to see if it is a standard_name.
 */
function isCFStandardName(variableName) {
    for (var i = 0; i < cfStandards.length; i++) {
        if (variableName == cfStandards[i]) {
            return true;
        } else {
            if (i == (cfStandards.length - 1)) {
                return false;
            }
        }
    }
}

