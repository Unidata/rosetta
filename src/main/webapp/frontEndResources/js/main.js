$(document).ready(function ($) {

    /**
     * STEP 0
     * Hide the CF Type div (will be shown 
     * after user selects a community type).
     */
    $('#cfType').addClass('hideMe');

    /**
     * STEP 0
     * Remove any prior platform display (or error) if page is reloaded
     */
   // $('#platforms ul').remove();
   // $('#platforms p').remove();

    /**
     * STEP 0
     * Community type selection triggers listing of
     * platform types and CF DSG selection options.
     */
    $('#communityType li').click(function(evt) {
        $(this).find('input').prop('checked', true);
        var community = $(this).find('input').val().replace(/\s/g, '');
        
        // unselect the other input options
        $(this).siblings().each(function(){
            $(this).find('input').prop('checked', false);
        });
        // remove any prior platform display (or error) if it exists
        $('#platforms ul').remove();
        $('#platforms p').remove();
        $('#cfType').removeClass('hideMe');

        makeCommunityPlatformAjaxRequest(community);

        // need the following to prevent from being fired in duplicate 
        evt.stopPropagation();
        evt.preventDefault();
    });

    /**
     * STEP 1
     */
    $('#dataFile').change(function(evt) {
        $('#uploadDataFile').removeClass('hideMe');
    });
    $('#uploadDataFile').click(function(evt) {
        var up = instantiateUploader($('#dataFileProgress'), $("label #dataFile"),
                                         $('#uploadDataFile'), $('#dataFile'), $('#clearDataFileUpload'));
        up.send();
        addToSession("dataFileName", cleanFilePath($('#dataFile').val()));
    });

    $('#clearDataFileUpload').bind("click", function () {
            //removed uploaded file from session and recreate the upload form.
            removeAllButTheseFromSession(["platformMetadata", "cfType"]);
            $('#dataFile').removeClass("hideMe");
            $('#clearDataFileUpload').addClass("hideMe");
            // clear progress bar
            $('#dataFileProgress').attr("style", "").addClass("progress");
            $('#dataFileProgress').html("0%");
            // clear any notices about file types
            $("#dataFileNotice").empty();
    });


    $('select#fileType').change(function(evt) {
        var fileType = $(this).find('option:selected').val();
        addToSession("fileType", fileType); // add to session
    });




    $('#positionalFile').change(function(evt) {
        $('#uploadPositionalFile').removeClass('hideMe');
    });
    $('#uploadPositionalFile').click(function(evt) {
        var up = instantiateUploader($('#positionalFileProgress'), $("label #positionalFile"),
                                         $('#uploadPositionalFile'), $('#positionalFile'), $('#clearPositionalFileUpload'));
        up.send();
        addToSession("positionalFileName", cleanFilePath($('#positionalFile').val()));
    });

    $('#clearPositionalFileUpload').bind("click", function () {
            //removed uploaded file from session and recreate the upload form.
            //removeAllButTheseFromSession(["platformMetadata", "cfType"]);
            $('#positionalFile').removeClass("hideMe");
            $('#clearPositionalFileUpload').addClass("hideMe");
            // clear progress bar
            $('#positionalFileProgress').attr("style", "").addClass("progress");
            $('#positionalFileProgress').html("0%");
            // clear any notices about file types
            $("#positionalFileNotice").empty();
    });

    $('#templateFile').change(function(evt) {
        $('#uploadTemplateFile').removeClass('hideMe');
    });
    $('#uploadTemplateFile').click(function(evt) {
        var up = instantiateUploader($('#templateFileProgress'), $("label #templateFile"),
                                         $('#uploadTemplateFile'), $('#templateFile'), $('#clearTemplateFileUpload'));
        up.send();
        addToSession("templateFileName", cleanFilePath($('#templateFile').val()));
    });

    $('#clearTemplateFileUpload').bind("click", function () {
            //removed uploaded file from session and recreate the upload form.
            removeAllButTheseFromSession(["platformMetadata", "cfType"]);
            $('#templateFile').removeClass("hideMe");
            $('#clearTemplateFileUpload').addClass("hideMe");
            // clear progress bar
            $('#templateFileProgress').attr("style", "").addClass("progress");
            $('#templateFileProgress').html("0%");
            // clear any notices about file types
            $("#templateFileNotice").empty();
    });

$('#delimiterList input:checkbox').bind("click", function () {

            // create array from selected values
            var checkedDelimiters = $("input:checkbox").serializeArray();
            var delimiterArray = [];
            $.each(checkedDelimiters, function (index, field) {
                delimiterArray[index] = field.value;
            });

            // add to session
            addToSession("delimiters", delimiterArray);
            if (delimiterArray.length <= 0) {
                removeFromSession("delimiters");
            } else {
                // Show 'Next' button after user makes a selection
                $("#faux").remove();
                $(".jw-button-next").removeClass("hideMe");
            }

            // if Other is selected
            if ($(this).val() == "Other") {
                if (jQuery.inArray("Other", delimiterArray) < 0) {
                    // toggled off
                    $("#otherDelimiter").addClass("hideMe");
                    $("#otherDelimiter").val("");
                    removeFromSession("otherDelimiter");
                } else {
                    // toggled on
                    $("#otherDelimiter").removeClass("hideMe");
                }
            }
        });



        $("#otherDelimiter").on("focusout", function () {
            addToSession("otherDelimiter", $(this).val());
            if (getFromSession("delimiters")) {
            }
        });



      function quickSave() {
        $.post("QuickSave", getAllDataInSession(),
            function (data) {
                var info = JSON.parse(data);
                var link = "fileDownload/" + info["uniqueId"] + "/" + info["fileName"];
                // hidden iFrame method based on
                // http://stackoverflow.com/questions/3749231/download-file-using-javascript-jquery/3749395#3749395
                var hiddenIFrame = 'hiddenDownloadFrame',
                       iframe = document.getElementById(hiddenIFrame);
                if (iframe === null) {
                    iframe = document.createElement('iframe');
                    iframe.id = hiddenIFrame;
                    iframe.style.display = 'none';
                    document.body.appendChild(iframe);
                 }
                 iframe.src = link;
               },
           "text");
        ;
      }
});



/**
 * Sends and AJAX request to get platforms
 * corresponding to the selected community.
 *
 * @param community  The community selected.
 */
function makeCommunityPlatformAjaxRequest(community) {
    let url = baseUrl + '/getPlatforms/' + community;
    $.ajax({
        url: url
    }).done(function(data) {
        var platforms = "";
        $.each(data, function( index, platform ) {
            platforms += '<li>\n' +
                         ' <label>\n' +
                         '  <img src="' + platform.img + '" alt="' + platform.name + '" /> ' +
                         '  <input type="radio" name="cfType" value="' + platform.type + '" validate="required:true">' + platform.name + '</input>' +
                         ' </label>\n' +
                         '</li>\n';
        });
        $('#step0 #cfType #platforms').append('<ul>' + platforms + '</ul>');


        // list of platforms with pictures
        $('#step0 #cfType #platforms ul li').bind("click", function (evt) {  
            $(this).find('input').prop('checked', true); 
            var cfType = $(this).find('input').val();
            addToSession("cfType", cfType); // add to session

            // unselect any cftypes selected in the dropdown
            $('#step0 #cfType #cfdsg option:selected').prop('selected', false);
         //   console.log(isInSession('cfType'));
          //  selectPlatform("stepFunctions", 0);
            // need the following to prevent from being fired in duplicate 
            evt.preventDefault();

        });

        // dropdown menu with cf types
        $('#step0 #cfType #cfdsg select').bind("change", function (evt) {  
            var cfType = $(this).find('option:selected').val();
            addToSession("cfType", cfType); // add to session


            // uncheck any of selected images platform
            $('#step0 #cfType #platforms ul li').each(function(){
                $(this).find('input').prop('checked', false);
            });

        });


    }).fail(function(request) {
        $('#step0 #cfType #platforms').append('<p class="error">Unable to load platform list. Please contact the site administrator.</p>');
    });
}


/**
 * Sends and AJAX request to get platforms
 * corresponding to the selected community.
 *
 * @param community  The community selected.
 */
function uploadDataFile() {
    let url = baseUrl + '/upload';
    $.ajax({
        url: url,
        type: post
    }).done(function(data) {
        var platforms = "";
        $.each(data, function( index, platform ) {
            platforms += '<li>\n' +
                         ' <label>\n' +
                         '  <img src="' + platform.img + '" alt="' + platform.name + '" /> ' +
                         '  <input type="radio" name="cfType" value="' + platform.type + '" validate="required:true">' + platform.name + '</input>' +
                         ' </label>\n' +
                         '</li>\n';
        });
        $('#step0 #cfType #platforms').append('<ul>' + platforms + '</ul>');


        // list of platforms with pictures
        $('#step0 #cfType #platforms ul li').bind("click", function (evt) {  
            $(this).find('input').prop('checked', true); 
            var cfType = $(this).find('input').val();
            addToSession("cfType", cfType); // add to session

            // unselect any cftypes selected in the dropdown
            $('#step0 #cfType #cfdsg option:selected').prop('selected', false);
         //   console.log(isInSession('cfType'));
          //  selectPlatform("stepFunctions", 0);
            // need the following to prevent from being fired in duplicate 
            evt.preventDefault();

        });

        // dropdown menu with cf types
        $('#step0 #cfType #cfdsg select').bind("change", function (evt) {  
            var cfType = $(this).find('option:selected').val();
            addToSession("cfType", cfType); // add to session


            // uncheck any of selected images platform
            $('#step0 #cfType #platforms ul li').each(function(){
                $(this).find('input').prop('checked', false);
            });

        });


    }).fail(function(request) {
        $('#step0 #cfType #platforms').append('<p class="error">Unable to load platform list. Please contact the site administrator.</p>');
    });
}




