$(document).ready(function ($) {

    /**
     * Hide the CF Type div (will be shown 
     * after user selects a community type).
     */
    $('#cfType').addClass('hideMe');


    /**
     * Community type selection triggers listing of
     * platform types and CF DSG selection options.
     */
    $('input#communityType').click(function() {
        $('#cfType').removeClass('hideMe');
        makeCommunityPlatformAjaxRequest($(this).val().replace(/\s/g, ''));
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
        console.log(data);
    }).fail(function(request) {
        // create error message
        console.log($('platforms'));
      //  <p class="error">Unable to load community list.  <spring:message code="fatal.error.message"/></p>
        
        //console.log('fail');
    });
}
