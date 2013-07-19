/**
 * uploader/custom.css
 * 
 * Custom functions, etc., associated with the HTML5 AJAX File Uploader.
 */



/** 
 * This function creates the uploader with corresponding options.
 *
 * @param progressBar  The progress bar associated with the file upload.
 * @param errorLabel  The error label associated with the file upload.
 * @param uploadButton  The upload button associated with the file upload.
 * @param fileTag The ID of the file button
 */
function instantiateUploader(progressBarId, errorLabel, uploadButton, fileId, clearId) {
    var up = new uploader($(fileId).get(0), {
        url:'upload',
        progress:function(ev){ 
            $(progressBarId).html(((ev.loaded/ev.total)*100) + "%");
            $(progressBarId).css("width","50%");
        },
        error:function(ev){ 
            $(errorLabel).text("Error!  No file uploaded!"); 
            return false; 
        },
        success:function(data){ 
            $(uploadButton).addClass("hideMe");  
            $(progressBarId).html("100%");
            $(progressBarId).effect("fade", 1000, progressBarCallback(progressBarId, clearId));
            addToSession("uniqueId", data); 
            $("#faux").remove();
            $(".jw-button-next").removeClass("hideMe");
        }
    });
    return up;
}
     
/** 
 * A callback function to bring a display the clearFileUpload button.
 */
function progressBarCallback(progressBarId, clearId) {
    setTimeout(function() {
        $(progressBarId).html(getFromSession("fileName") + " successfully uploaded").removeClass("progress").fadeIn("fast");
        $(clearId).removeClass("hideMe");
    }, 1000 );
}

/** 
 * Cleans the file path/name of browser-added extra goodies.
 *
 * @param filePath  The file path as provided by the file input tag.
 */
function cleanFilePath(filePath) {
    var cleanFilePath = filePath.replace(/\\/g, "").replace(/C:/g, "").replace(/fakepath/g, "").replace(".xlsx", ".csv").replace(".xls", ".csv");
    return cleanFilePath;
}