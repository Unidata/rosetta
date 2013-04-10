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
 */
function instantiateUploader(progressBar, errorLabel, uploadButton) {
    var up = new uploader($("#file").get(0), {
        url:'upload',
        progress:function(ev){ 
            $(progressBar).html(((ev.loaded/ev.total)*100) + "%"); 
            $(progressBar).css("width","50%"); 
        },
        error:function(ev){ 
            $(errorLabel).text("Error!  No file uploaded!"); 
            return false; 
        },
        success:function(data){ 
            $(uploadButton).addClass("hideMe");  
            $(progressBar).html("100%"); 
            $(progressBar).effect("fade", 1000, progressBarCallback()); 
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
function progressBarCallback() {
    setTimeout(function() {
        $("#progress").html(getFromSession("fileName") + " successfully uploaded").removeClass("progress").fadeIn("fast");
        $("#clearFileUpload").removeClass("hideMe");
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
