/**
 * WebStorage.js
 *
 * Module for storing data in web storage.  Currently using session storage.
 */
var WebStorage = (function () {
   /**
     * Adds a key/value pair to session storage.
     *
     * @param key  The key used to store the data in session storage.
     * @param value  The data to be stored in session storage.
     */
    function storeData(key, value) {
        if (typeof(Storage) !== "undefined") {
            sessionStorage.setItem(key, value);
        }
    }

    /**
     * Retrieves a value from to session storage using the provided key.
     *
     * @param key  The key used to retrieve the data in session storage.
     * @return  The value found in session storage that corresponds to the key.
     */
    function getStoredData(key) {
        if (typeof(Storage) !== "undefined") {
            return sessionStorage.getItem(key);
        }
     }

    /**
     * Removes a key/value pair from session storage.
     *
     * @param key  The key to remove the data in session storage.
     */
    function removeFromStorage(key) {
        if (typeof(Storage) !== "undefined") {
            sessionStorage.removeItem(key);
        }
    }

    /**
     * Removes all data from session storage.
     */
    function removeAllFromStorage() {
        if (typeof(Storage) !== "undefined") {
            sessionStorage.clear();
        }
    }

    // Expose these functions.
    return {
        storeData: storeData,
        getStoredData: getStoredData,
        removeFromStorage: removeAllFromStorage,
        removeAllFromStorage: removeAllFromStorage
    };

})();