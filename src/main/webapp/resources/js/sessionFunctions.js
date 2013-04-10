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
    if (typeof(Storage)!=="undefined")  {      
        sessionStorage.setItem(key, value);
    } else  {
        // add some jQuery method here for non-HTML5
    }
}

/**  
 * General function called to retrieve a value from the session.
 * 
 * @param key  The key used to retrieve the data in the session.
 */
function getFromSession(key) {
    if (typeof(Storage)!=="undefined")  {
        return sessionStorage.getItem(key);
    } else  {
        // add some jQuery method here for non-HTML5
    }
}

/**  
 * General function called to remove a key/value pair to the session.
 * 
 * @param key  The key to remove the data in the session.
 */
function removeFromSession(key) {
    if (typeof(Storage)!=="undefined")  {
        sessionStorage.removeItem(key);
    } else  {
        // add some jQuery method here for non-HTML5
    }
}

/**  
 * General function called to retrieve the size (number of items) in the session. 
 */
function getSessionLength() {
    if (typeof(Storage)!=="undefined")  {
        return sessionStorage.length; 
    } else  {
        // add some jQuery method here for non-HTML5
    }
}

/**  
 * General function called to retrieve the session key. 
 *
 * @param index  The index of the key to retrieve from the session.
 */
function getSessionKey(index) {
    if (typeof(Storage)!=="undefined")  {
        return sessionStorage.key(index); 
    } else  {
        // add some jQuery method here for non-HTML5
    }
}

/**  
 * Looks to see if the user has already provided a value for something by checking the 
 * session. This function is different than the getFromSession() function (above) in 
 * that it will look for and retrieve data stored in the session as a part of a string 
 * of concatenated key/value pairs (E.g. metadata). If the value exists, return that value.
 * 
 * @param sessionKey  The key used to store the data in the session.
 * @param dataSought  The data or "key" to seek out in the session metadata.
 */
function getItemEntered(sessionKey, dataSought) {
    var dataInSession = getFromSession(sessionKey);
    if (dataInSession) {
        var pairs = dataInSession.split(/,/g);
        for (var i = 0; i < pairs.length; i++) {  
            var keyValuePair = pairs[i].split(/:/);
            if (keyValuePair[0] == dataSought) {
                return keyValuePair[1];
            } else {
                if (i == (pairs.length - 1 )) {
                    return null;
                } else {
                    continue;
                }
            }
        }
    } else { 
        return null;
    }  
}

/**  
 * Builds the string held in the session by concatenating data key/value pairs.
 * If this key aleady exists in the session string, its value is replaced.
 * 
 * @param sessionKey  The key used to store the data in the session.
 * @param key  The key for the metadata entry.
 * @param value  The value for the metadata entry.
 */
function buildStringForSession(sessionKey, key, value) {
    var sessionString = "";
    var dataInSession = getFromSession(sessionKey);
    var valueInSession = getItemEntered(sessionKey, key);
    if (valueInSession != null) { // exists so we need to replace old value with the new
        var pairs = dataInSession.split(/,/g);
        for (var i = 0; i < pairs.length; i++) {             
            var keyValuePair = pairs[i].split(/:/);
            if (keyValuePair[0] == key) {
                var appendString = createSessionAppendString(keyValuePair[0], value);
                if (sessionString == "") {
                    sessionString = appendString;
                } else {
                    sessionString = sessionString + "," + appendString;
                }   
                continue;
            } else {   
                var appendString = createSessionAppendString(keyValuePair[0], keyValuePair[1]);
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

/** 
 * Utility function that just loops through the a string of concatenated 
 * key/value pairs in the session and returns an array of the keys.
 *
 * @param sessionData  The string of concatenated key/value pairs in the session.
 */
function getKeysFromSessionData(sessionData) {
    var sessionKeys = [];
    // make sure chars are correct and no blank entries
    for (var i = 0; i < sessionData.length; i++) {
        var keyValuePair = sessionData[i].split(/:/);
        sessionKeys.push(keyValuePair[0]);
    }    
    return sessionKeys;
}

/** 
 * Removes a specific item from the string of concatenated key/value pairs stored in the session. 
 *
 * @param sessionKey  The key used to store the data in the session.
 * @param itemToRemove  The key in the to look for and remove from the string in the session.
 */
function removeItemFromSessionString(sessionKey, itemToRemove) {
    var sessionString = "";
    var dataInSession = getFromSession(sessionKey);
    if (dataInSession != null) {
        var pairs = dataInSession.split(/,/g);
        for (var i = 0; i < pairs.length; i++) {  
            var keyValuePair = pairs[i].split(/:/);
            if (keyValuePair[0] != itemToRemove) {
                var appendString = createSessionAppendString(keyValuePair[0], keyValuePair[1]);
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

/** 
 * Removes all key/value pairs from the concatenated string stored in the session 
 * except for whatever is defined in the keep array. This function gets called when 
 * the user opts to change the variable coordinate type designation.  
 *
 * @param sessionKey  The key used to store the data in the session.
 * @param keep  The array of items we wish to keep in the metadata string in the session.
 */
function removeAllButTheseFromSessionString(sessionKey, keep) {
    var sessionString = "";
    var dataInSession = getFromSession(sessionKey);
    var pairs = dataInSession.split(/,/g);
    for (var i = 0; i < pairs.length; i++) {  
        var keyValuePair = pairs[i].split(/:/);
        if (keep.indexOf(keyValuePair[0]) >= 0) {
            var appendString = createSessionAppendString(keyValuePair[0], keyValuePair[1]);
            if (sessionString == "") {
                sessionString = appendString;
            } else {
                sessionString = sessionString + "," + appendString;
            } 
        } 
        addToSession(sessionKey, sessionString);
    } 
}

/** 
 * Returns the entered metadata values from the session except whatever is in the 
 * exclusionList array. This function gets called during input validation.  
 *
 * @param sessionKey  The key used to store the data in the session.
 * @param exclusionList  The array of items we wish to ignore.
 */
function getAllButTheseFromSessionString(sessionKey, exclusionList) {
    var sessionString = [];
    var dataInSession = getFromSession(sessionKey);
    var pairs = dataInSession.split(/,/g);
    for (var i = 0; i < pairs.length; i++) {  
        var keyValuePair = pairs[i].split(/:/);
        if (exclusionList.indexOf(keyValuePair[0]) < 0) {
            sessionString.push(keyValuePair[0] + ":" + keyValuePair[1]);
        } 
    } 
    return sessionString;
}

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
                  variableMetadata = variableMetadata + "," + key + "=" + value.replace(/,/g, "+");
              } else {
                  variableNames = variableNames + "," + key + ":" + value;
              }
          } else {
              data[key] = value;
          }
    }
    data["variableNames"] = variableNames.replace(/,/, "");
    data["variableMetadata"] = variableMetadata.replace(/,/, "");
    return data;
}
