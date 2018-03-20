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
            // unselect any cftypes selected in the dropdown
            $('#step0 #cfType #cfdsg option:selected').prop('selected', false);

            // need the following to prevent from being fired in duplicate 
            evt.preventDefault();

        });

        // dropdown menu with cf types
        $('#step0 #cfType #cfdsg select').bind("change", function (evt) {  
            // uncheck any of selected images platform
            $('#step0 #cfType #platforms ul li').each(function(){
                $(this).find('input').prop('checked', false);
            });
        });


    }).fail(function(request) {
        $('#step0 #cfType #platforms').append('<p class="error">Unable to load platform list. Please contact the site administrator.</p>');
    });
}
