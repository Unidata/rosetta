/**
 * sessionFunctions.css
 *
 * Custom functions that add, edit, and remove items from the session.
 */

/**
 * General function called to add a key/value pair to the session.
 *
 * @param key  The key used to store the data in the session.
 * @param value  The data to be stored in the session.
 */
function addToSession(key, value) {
  if (typeof(Storage) !== "undefined") {
    sessionStorage.setItem(key, value);
  } 
}

// 
function storeData(key, value) {
  if (typeof(Storage) !== "undefined") {
    sessionStorage.setItem(key, value);
  } 
}




/*
function addToVariableString(key, value) {
  var currentString = $("input#variableMetadata").val();
  var newString = "";
  // Is there anything stored yet?
  if (!currentString) { // nothing stored
    newString = key + "<>" + value;
  } else { // data is stored
    // does the key already exist?
    if (currentString.search(key) === -1) { // not found so add
      newString = currentString + "<=>" + key + "<>" + value;
    } else { // found so replace
      var keyValPairs = currentString.split("<=>");
      for (i = 0; i < keyValPairs.length; i++) {
        var variablekeyValuePair = keyValPairs[i].split("<>");
        if (variablekeyValuePair[0] === key) {
          newString += key + "<>" + value;
        } else {
          newString += keyValPairs[i];
        }
        // at the end
        if (i !== (keyValPairs.length - 1)) {
          newString = newString + "<=>";
        }
      }
    }
  }
  // update hidden form variable
  $("input#variableMetadata").prop("value", newString);
}
*/

/**
 * General function called to check if a key exists in the session.
 *
 * @param key  The key used to store the data in the session (to be checked).
 */
// not sure if this is used.
function isInSession(key) {
  if (typeof(Storage) !== "undefined") {
    sessionStorage.hasOwnProperty(key);
  }
}

/*
function isInVariableString(key) {
  var currentString = $("input#variableMetadata").val();
  return currentString.search(key) !== -1;
}
*/

/**
 * General function called to retrieve a value from the session.
 *
 * @param key  The key used to retrieve the data in the session.
 */
function getFromSession(key) {
  if (typeof(Storage) !== "undefined") {
    return sessionStorage.getItem(key);
  }
}

function getStoredData(key) {
  if (typeof(Storage) !== "undefined") {
    return sessionStorage.getItem(key);
  }
}

/*
function getFromVariableString(key) {
  var valueToReturn;
  var currentString = $("input#variableMetadata").val();
  var keyValPairs = currentString.split("<=>");
  for (i = 0; i < keyValPairs.length; i++) {
    var variablekeyValuePair = keyValPairs[i].split("<>");
    if (variablekeyValuePair[0] === key) {
      valueToReturn = variablekeyValuePair[1];
    }
  }
  return valueToReturn;
}
*/

/**
 * General function called to remove a key/value pair to the session.
 *
 * @param key  The key to remove the data in the session.
 */
function removeFromSession(key) {
  if (typeof(Storage) !== "undefined") {
    sessionStorage.removeItem(key);
  } else {
    // add some jQuery method here for non-HTML5
  }
}

function removeFromStorage(key) {
    if (typeof(Storage) !== "undefined") {
        sessionStorage.removeItem(key);
    }
}

function removeAllFromStorage() {
    if (typeof(Storage) !== "undefined") {
        sessionStorage.clear();
    }
}

/*
function removeFromVariableString(key) {
  var newString = "";
  var currentString = $("input#variableMetadata").val();
  var keyValPairs = currentString.split("<=>");
  for (i = 0; i < keyValPairs.length; i++) {
    var variablekeyValuePair = keyValPairs[i].split("<>");
    if (variablekeyValuePair[0] !== key) {
      newString = newString + keyValPairs[i];
      if (i !== (keyValPairs.length - 1)) {
        newString = newString + "<=>";
      }
    }
  }
  if (newString.endsWith("<=>")) {
    newString = newString.substr(0, newString.lastIndexOf("<=>"));
  }

  // update hidden form variable
  $("input#variableMetadata").prop("value", newString);
}
*/

/**
 * General function called to retrieve the size (number of items) in the session.
 */
// not sure if this being used.
function getSessionLength() {
  if (typeof(Storage) !== "undefined") {
    return sessionStorage.length;
  } else {
    // add some jQuery method here for non-HTML5
  }
}

/*
function getVariableStringLength() {
  var currentString = $("input#variableMetadata").val();
  return (currentString.match(/<=>/g) || []).length;
}
*/

/**
 * General function called to retrieve the session key.
 *
 * @param index  The index of the key to retrieve from the session.
 */
function getSessionKey(index) {
  if (typeof(Storage) !== "undefined") {
    return sessionStorage.key(index);
  }
}

/**
 * Removes all key/value pairs from the session except for whatever is defined in the keep array.
 *
 * @param keep  The array of items we wish to keep in the metadata string in the session.

function removeAllButTheseFromSession(keep) {
  var remove = [];
  for (var i = 0; i < getSessionLength(); i++) {
    var key = getSessionKey(i);
    if ($.inArray(key, keep) < 0) {
      remove.push(key);
    }
  }
  for (var i = 0; i < remove.length; i++) {
    removeFromSession(remove[i]);
  }
}
*/
/*?????
function removeAllButTheseFromVariableString(keep) {
  var newString = "";
  var currentString = $("input#variableMetadata").val();
  var remove = [];
  var keyValPairs = currentString.split("<=>");
  for (i = 0; i < keyValPairs.length; i++) {
    var variablekeyValuePair = keyValPairs[i].split("<>");
    if ($.inArray(variablekeyValuePair[0], keep) < 0) {
      remove.push(key);
    }
  }
  for (var i = 0; i < remove.length; i++) {
    removeFromVariableString(remove[i]);
  }
}
*/
/**
 * Looks to see if the user has already provided a value for something by checking the
 * session. This function is different than the getFromSession() function (above) in
 * that it will look for and retrieve data stored in the session as a part of a string
 * of concatenated key/value pairs (E.g. metadata). If the value exists, return that value.
 *
 * @param key  The key used to store the data in the session.
 * @param sought  The data or "key" to seek out in the session metadata.
 */
function getItemEntered(sessionKey, dataSought) {
  var dataInSession = getFromSession(sessionKey);
  if (dataInSession) {
    var pairs = dataInSession.match(/(\\.|[^,])+/g);
    for (var i = 0; i < pairs.length; i++) {
      var keyValuePair = pairs[i].match(/(\\.|[^:])+/g);
      if (keyValuePair[0] == dataSought && typeof keyValuePair[1] == "string") {
        return unescapeCharacters(keyValuePair[1]);
      }
    }
  }
  return null;
}

/*
function getItemEnteredFromVariableString(key, sought) {
  var data = getFromVariableString(key);
  if (data) {
    var pairs = data.match(/(\\.|[^,])+/g);
    for (var i = 0; i < pairs.length; i++) {
      var keyValuePair = pairs[i].match(/(\\.|[^:])+/g);
      if (keyValuePair[0] === sought && typeof keyValuePair[1] === "string") {
        return unescapeCharacters(keyValuePair[1]);
      }
    }
  }
  return null;
}
*/


// don't think this is used.
/*
function searchForValue(sessionKey, valueSought, exceptFrom) {
  var escapedValueSought = escapeCharacters(valueSought);
  var dataInSession = getFromSession(sessionKey);
  if (dataInSession) {
    var pairs = dataInSession.match(/(\\.|[^,])+/g);
    for (var i = 0; i < pairs.length; i++) {
      var keyValuePair = pairs[i].match(/(\\.|[^:])+/g);
      if (keyValuePair[1] === escapedValueSought &&
          keyValuePair[0] !== exceptFrom) {
        return keyValuePair[0];
      }
    }
  }
  return false;
}
*/

function escapeCharacters(value) {
  value = value.replace(/:/g, "\\:");
  value = value.replace(/,/g, "\\,");
  return value;
}

function unescapeCharacters(value) {
  value = value.replace(/\\:/g, ":");
  value = value.replace(/\\,/g, ",");
  return value;
}

/**
 * Builds the string held in the session by concatenating data key/value pairs.
 * If this key aleady exists in the session string, its value is replaced.
 * If they key is a string containing , or : it is escaped with a \
 *
 * @param key  The key used to store the data in the session.
 * @param subKey  The key for the metadata entry.
 * @param subValue  The value for the metadata entry.
 */
function buildStringForSession(sessionKey, key, value) {
  if (typeof value == "string") {
    value = escapeCharacters(value);
  }
  var sessionString = "";
  var dataInSession = getFromSession(sessionKey);
  var valueInSession = getItemEntered(sessionKey, key);
  if (valueInSession != null) { // exists so we need to replace old value with the new
    var pairs = dataInSession.match(/(\\.|[^,])+/g);
    for (var i = 0; i < pairs.length; i++) {
      var keyValuePair = pairs[i].match(/(\\.|[^:])+/g);
      if (keyValuePair[0] == key) {
        var appendString = createSessionAppendString(keyValuePair[0], value);
        if (sessionString == "") {
          sessionString = appendString;
        } else {
          sessionString = sessionString + "," + appendString;
        }
        continue;
      } else {
        var appendString = createSessionAppendString(keyValuePair[0],
            keyValuePair[1]);
        if (sessionString == "") {
          sessionString = appendString;
        } else {
          sessionString = sessionString + "," + appendString;
        }
      }
    }
  } else { // hasn't been added yet, so just concatenate the data
    if (dataInSession == null) { // no metadata in session to start with: add the first entry
      sessionString = key + ":" + value;
    } else { // concatenate the data to existing entries
      sessionString = dataInSession + "," + key + ":" + value;
    }
  }
  return sessionString;
}

/*
function buildStringForVariableString(key, subKey, subValue) {
  if (typeof subValue === "string") {
    subValue = escapeCharacters(subValue);
  }
  var variableSubString = "";
  var data = getFromVariableString(key);
  var value = getItemEnteredFromVariableString(key, subKey);
  if (value != null) { // exists so we need to replace old subValue with the new
    var pairs = data.match(/(\\.|[^,])+/g);
    for (var i = 0; i < pairs.length; i++) {
      var keyValuePair = pairs[i].match(/(\\.|[^:])+/g);
      var appendString;
      if (keyValuePair[0] === subKey) {
        appendString = createSessionAppendString(keyValuePair[0],
            subValue);
        if (variableSubString === "") {
          variableSubString = appendString;
        } else {
          variableSubString = variableSubString + "," + appendString;
        }
      } else {
        appendString = createSessionAppendString(keyValuePair[0],
            keyValuePair[1]);
        if (variableSubString === "") {
          variableSubString = appendString;
        } else {
          variableSubString = variableSubString + "," + appendString;
        }
      }
    }
  } else { // hasn't been added yet, so just concatenate the data
    if (data == null) { // no metadata in session to start with: add the first entry
      variableSubString = subKey + ":" + subValue;
    } else { // concatenate the data to existing entries
      variableSubString = data + "," + subKey + ":" + subValue;
    }
  }
  return variableSubString;
}
*/

/**
 * Utility function that just loops through an array of
 * key/value pairs in the session and returns an array of the keys.
 *
 * @param data  The array of key/value pairs in the session.
 */
function getKeysFromSessionData(sessionData) {
  var sessionKeys = [];
  // make sure chars are correct and no blank entries
  for (var i = 0; i < sessionData.length; i++) {
    var keyValuePair = sessionData[i].match(/(\\.|[^:])+/g);
    sessionKeys.push(keyValuePair[0]);
  }
  return sessionKeys;
}
/*
function getKeysFromVariableStringData(data) {
  var keys = [];
  // make sure chars are correct and no blank entries
  for (var i = 0; i < data.length; i++) {
    var keyValuePair = data[i].match(/(\\.|[^:])+/g);
    keys.push(keyValuePair[0]);
  }
  return keys;
}
*/

/**
 * Utility function that just loops through a string of concatenated
 * key/value pairs in the session and returns an array of the values.
 *
 * @param sessionString  The string of concatenated key/value pairs in the session.
 */
function getValuesFromSessionString(sessionString) {
  var sessionValues = [];
  sessionString = sessionString.match(/(\\.|[^,])+/g);
  // make sure chars are correct and no blank entries
  for (var i = 0; i < sessionString.length; i++) {
    var keyValuePair = sessionString[i].match(/(\\.|[^:])+/g);
    sessionValues.push(unescapeCharacters(keyValuePair[1]));
  }
  return sessionValues;
}





/**
 * Used in the variable metadata collection step.
 * Removes a specific item from the string of concatenated key/value pairs stored in the session.
 *
 * @param key  The key used to store the data in the session.
 * @param itemToRemove  The key in the to look for and remove from the string in the session.
 */
function removeItemFromSessionString(sessionKey, itemToRemove) {
  var sessionString = "";
  var dataInSession = getFromSession(sessionKey);
  if (dataInSession != null) {
    var pairs = dataInSession.match(/(\\.|[^,])+/g);
    for (var i = 0; i < pairs.length; i++) {
      var keyValuePair = pairs[i].match(/(\\.|[^:])+/g);
      if (keyValuePair[0] != itemToRemove) {
        var appendString = createSessionAppendString(keyValuePair[0],
            keyValuePair[1]);
        if (sessionString == "") {
          sessionString = appendString;
        } else {
          sessionString = sessionString + "," + appendString;
        }
      }
      if (sessionString != "") {
        addToSession(sessionKey, sessionString);
      } else {
        removeFromSession(sessionKey);
      }
    }
  }
}
/*
function removeItemFromVariableString(key, itemToRemove) {
  var variableSubString = "";
  var data = getFromVariableString(key);
  if (data != null) {
    var pairs = data.match(/(\\.|[^,])+/g);
    for (var i = 0; i < pairs.length; i++) {
      var keyValuePair = pairs[i].match(/(\\.|[^:])+/g);
      if (keyValuePair[0] !== itemToRemove) {
        var appendString = createSessionAppendString(keyValuePair[0],
            keyValuePair[1]);
        if (variableSubString === "") {
          variableSubString = appendString;
        } else {
          variableSubString = variableSubString + "," + appendString;
        }
      }
      if (variableSubString !== "") {
        addToVariableString(key, variableSubString);
      } else {
        removeFromVariableString(key);
      }
    }
  }
}
*/


/**
 * Removes key/value pairs from A SINGLE ITEM stored in the session except for
 * whatever is defined in the keep array. E.g., Looks up the one item in the session
 * and loops through the value of this item, removing the key/value pairs it contains.
 *
 * The difference between this function and removeAllButTheseFromSession is that this
 * function only modifies ONE ITEM in the session by removing entries from the value.
 * This function gets called when the user opts to change the variable coordinate type designation.
 *
 * @param key  The key used to find the stored data in the session.
 * @param keep  The array of items we wish to keep in the metadata string in the session.
 */
function removeAllButTheseFromSessionString(sessionKey, keep) {
  var sessionString = "";
  var dataInSession = getFromSession(sessionKey);
  var pairs = dataInSession.match(/(\\.|[^,])+/g);
  for (var i = 0; i < pairs.length; i++) {
    var keyValuePair = pairs[i].match(/(\\.|[^:])+/g);
    if (keep.indexOf(keyValuePair[0]) >= 0) {
      var appendString = createSessionAppendString(keyValuePair[0],
          keyValuePair[1]);
      if (sessionString == "") {
        sessionString = appendString;
      } else {
        sessionString = sessionString + "," + appendString;
      }
    }
    addToSession(sessionKey, sessionString);
  }
}

/*
function removeAllButTheseFromVariableString(key, keep) {
  var variableSubString = "";
  var data = getFromVariableString(key);
  var pairs = data.match(/(\\.|[^,])+/g);
  for (var i = 0; i < pairs.length; i++) {
    var keyValuePair = pairs[i].match(/(\\.|[^:])+/g);
    if (keep.indexOf(keyValuePair[0]) >= 0) {
      var appendString = createSessionAppendString(keyValuePair[0],
          keyValuePair[1]);
      if (variableSubString === "") {
        variableSubString = appendString;
      } else {
        variableSubString = variableSubString + "," + appendString;
      }
    }
    addToVariableString(key, variableSubString);
  }
}
*/


/**
 * Returns the entered metadata values from the session except whatever is in the
 * exclusionList array. This function gets called during input validation.
 *
 * @param key  The key used to store the data in the session.
 * @param exclusionList  The array of items we wish to ignore.
 */
function getAllButTheseFromSessionString(sessionKey, exclusionList) {
  var sessionString = [];
  var dataInSession = getFromSession(sessionKey);
  var pairs = dataInSession.match(/(\\.|[^,])+/g);
  for (var i = 0; i < pairs.length; i++) {
    var keyValuePair = pairs[i].match(/(\\.|[^:])+/g);
    if (exclusionList.indexOf(keyValuePair[0]) < 0) {
      var value = "";
      if (keyValuePair.length > 1) {
        value = keyValuePair[1];
      }
      sessionString.push(keyValuePair[0] + ":" + value);
    }
  }
  return sessionString;
}
/*
function getAllButTheseFromVariableString(key, exclusionList) {
  var variableSubString = [];
  var data = getFromVariableString(key);
  var pairs = data.match(/(\\.|[^,])+/g);
  for (var i = 0; i < pairs.length; i++) {
    var keyValuePair = pairs[i].match(/(\\.|[^:])+/g);
    if (exclusionList.indexOf(keyValuePair[0]) < 0) {
      var value = "";
      if (keyValuePair.length > 1) {
        value = keyValuePair[1];
      }
      variableSubString.push(keyValuePair[0] + ":" + value);
    }
  }
  return variableSubString;
}
*/


/**
 * Utility function used by session-related functions when they are manipulating
 * the string of concatenated key/value pairs. In some instances (delimiters,
 * headerLineNumbers) the string of concatenated values is not composed of
 * key/value pairs, but rather justkeys.  This function returns either the key/value
 * pair or just the key depending on whether the value is undefinded or not.
 *
 * @param key  The key of the pair.
 * @param value  The value of the pair (may be undefined).
 */
function createSessionAppendString(key, value) {
  var appendString = key + ":" + value;
  if (value == undefined) {
    appendString = key;
  }
  return appendString;
}

/*
function createVariableStringAppendString(key, value) {
  var appendString = key + ":" + value;
  if (value === undefined) {
    appendString = key;
  }
  return appendString;
}
*/

/**
 * Retrieves the session keys of only those variable containing metadata.
 * Note, those variables specified "Do Not Use" are ignored and not included.
 */
function getVariablesWithMetadata() {
   var sessionKeys = [];
   var sessionLength = getSessionLength();
   for (var i = 0; i < sessionLength; i++) {
       var key = getSessionKey(i);
       var value = getFromSession(key);
       if (key.match(/[variableName]{1}\d+/)) {
           if (key.match(/Metadata/)) {
               sessionKeys.push(key.replace("Metadata", ""));
           }
       }
   }
   return sessionKeys;
}
/*
function getVariablesWithMetadata() {
  var keys = [];
  var currentString = $("input#variableMetadata").val();
  var keyValPairs = currentString.split("<=>");
  for (i = 0; i < keyValPairs.length; i++) {
    var variablekeyValuePair = keyValPairs[i].split("<>");
    if (variablekeyValuePair[0].match(/[variableName]{1}\d+/)) {
      if (variablekeyValuePair[0].match(/Metadata/)) {
        keys.push(variablekeyValuePair[0].replace("Metadata", ""));
      }
    }
  }

  for (var i = 0; i < length; i++) {
    var key = getSessionKey(i);
    var value = getFromSession(key);
    if (key.match(/[variableName]{1}\d+/)) {
      if (key.match(/Metadata/)) {
        keys.push(key.replace("Metadata", ""));
      }
    }
  }
  return keys;
}

/**
 * This function retrieves all the session data and stashes it into an object.
 */
function getAllDataInSession() {
  var data = {};
  var variableNames = "";
  var variableMetadata = "";
  var sessionLength = getSessionLength();
  for (var i = 0; i < sessionLength; i++) {
    var key = getSessionKey(i);
    var value = getFromSession(key);
    if (key.match(/[variableName]{1}\d+/)) {
      if (key.match(/Metadata/)) {
        variableMetadata = variableMetadata + "," + key + "=" + value.replace(
            /,/g, "+");
      } else {
        variableNames = variableNames + "," + key + ":" + value;
      }
    } else {
      data[key] = value;
    }
  }
  data["variableNames"] = variableNames.replace(/,/, "");
  data["variableMetadata"] = variableMetadata.replace(/,/, "");
  data["jsonStrSessionStorage"] = JSON.stringify(sessionStorage);
  return data;
}
