$(document).ready(function ($) {

  // automagically make any image alt a tooltip
  $(document).tooltip({
    items: "img[alt]",
    content: function () {
      return $(this).attr("alt");
    }
  });

  /* horizontal nav drop down menu */
  $("ul.nav").dropit({action: "click", triggerEl: "b"});

  /**
   * STEP 1: CF Type selection step.
   * cfType selection via clicking on platform images, so show next button.
   */
  $(".platforms").on("selectableselected", function (event, ui) {

    // get li corresponding to selected platform (for use below).
    var selectedLi = $(this).find(".ui-selected");
    // get the radio button corresponding to the selected platform.
    var selectedInput = $(this).find(".ui-selected input");

    // uncheck all other platform radio buttons.
    $(".platforms li input").prop("checked", false);
    // make sure all other platform li are not highlighted (workaround for jQuery quirk).
    $(".platforms li").removeClass("ui-selected");

    // unselect any selected cfTypes in the dropdown menu.
    $("#cfTypeSpecified select option:selected").prop("selected", false);
    // uncheck any selected metadata profiles (except CF).
    $.each($("#metadataProfile li input"), function(index, checkbox) {
        if($(checkbox).val() !== "CF") {
            $(checkbox).prop("checked", false);
        }
    });
    // Hide metadata profile section
    $("#metadataProfile").addClass("hideMe");

    // check the selected radio button.
    $(selectedInput).prop("checked", true);
    // highlight the selected li
    $(selectedLi).addClass("ui-selected");

    // remove disabled status for submit button.
    $("input[type=submit]").removeAttr("disabled");
    // remove disabled class for submit button.
    $("input[type=submit]").removeClass("disabled");
  });

  /**
   * STEP 1: CF Type selection step.
   * cfType selection via dropdown menu,
   * metadata profile type & next button.
   */
  $("#cfType select").change(function (event, ui) {
    // uncheck all other platform radio buttons.
    $(".platforms li input").prop("checked", false);
    // make sure all other platform li are not highlighted (workaround for jQuery quirk).
    $(".platforms li").removeClass("ui-selected");
    // remove disabled status for submit button.
    $("input[type=submit]").removeAttr("disabled");
    // remove disabled class for submit button.
    $("input[type=submit]").removeClass("disabled");
    // toggle metadata profile div
    if ($(this).val() !== "") {
      if ($("#metadataProfile").hasClass("hideMe")) {
        $("#metadataProfile").removeClass("hideMe");
      }
    } else {
      $("#metadataProfile").addClass("hideMe");
    }
  });

  /**
   * STEP 2: File upload step.
   * fileType selection via dropdown menu.
   */
  $("select#dataFileType").change(function (event, ui) {
    var fileType = $(this).find(":selected").text();
    if (fileType === "--") {
      // user de-selected the file type.
      if (!$("#upload").hasClass("hideMe")) {
        // add hideMe class for file upload section.
        $("#upload").addClass("hideMe");
      }
    } else if (fileType === "Custom File Type") {
      // user selected custom file type.

      // remove hideMe class for file upload section.
      $("#upload").removeClass("hideMe");
      // remove hideMe class for external positional file upload.
      $("#upload #custom").removeClass("hideMe");
    } else {
      // user selected known file type.

      // remove hideMe class for file upload section.
      $("#upload").removeClass("hideMe");
      // add hideMe class for external positional file upload.
      $("#upload #custom").addClass("hideMe");
    }
  });

  /**
   * STEP 2: File upload step.
   * clear file upload selected.
   */
  $(".clearFileUpload").click(function () {
    // order of element matter in this case.
    $(this).addClass("hideMe");
    $(this).prev("i").empty();
    var file = $(this).next("input");
    $(file).removeClass("hideMe");

    $(file).next("input:hidden").prop("value", "");

    if ($(file).attr("id") === "dataFile") {
      // remove disabled status for submit button.
      $("input[type=submit]#Next").prop("disabled", "disabled");
      // remove disabled class for submit button.
      $("input[type=submit]#Next").addClass("disabled");
    }

  });

  /**
   * STEP 2: File upload step.
   * if a file is uploaded show the clear file upload button.
   */
  $("input:file").change(function () {
    // Get uploaded file name.
    var id = $(this).attr('id');
    var fileName = document.getElementById(id).files[0].name;
    // find clear file upload button and show it
    var button = $(this).prev(".clearFileUpload");
    $(button).removeClass("hideMe");
    // add file name to <i></i>
    $(button).prev().text(fileName);
    $(this).next().attr("value", fileName);
    // hide the input button
    $(this).addClass("hideMe");
  });

  
/**
   * STEP 2: File upload step.
   * dataFile to upload selected, so show next button.
   */
  $("input:file#data").change(function () {
    // remove disabled status for submit button.
    $("input[type=submit]#Next").removeAttr("disabled");
    // remove disabled class for submit button.
    $("input[type=submit]#Next").removeClass("disabled");
  });


  /**
   * STEP 5: global metadata.
   * toggle metadata sections
   */
  $(".toggle").click(function () {
    if ($(this).hasClass("expand")) {
      $(this).removeClass("expand");
      $(this).addClass("collapse");
    } else {
      $(this).removeClass("collapse");
      $(this).addClass("expand");
    }

    var corresponding = $(this).attr("id") + "Section";
    console.log(corresponding);
    if ($("#" + corresponding).hasClass("hideMe")) {
      $("#" + corresponding).removeClass("hideMe");
    } else {
      $("#" + corresponding).addClass("hideMe");
    }
  });


    /**
     * STEP 6: convert and download
     * remove web storage and cookie
     */
  $("#convertAndDownload input").click(function () {
      var name = "rosetta="; //Create the cookie name variable with cookie name concatenate with = sign
      // Invalidate the cookie.
      var cookie = window.document.cookie;
      if (cookie.includes("rosetta")) {
          document.cookie = 'rosetta=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
      }
      // Remove everything from storage.
      WebStorage.removeAllFromStorage();
  });


});





