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
   * STEP 3: Custom file attribute step.
   * delimiter selected, so show next button.
   * header line js handled in js/SlickGrid/custom/headerLineSelection.js
   */
  $("input#delimiter").change(function () {
    manageCustomFileButtonNav(); // This method lives in js/SlickGrid/custom/headerLineSelection.js
  });

  /**
   * STEP 5: OIIP general metadata.
   * toggle metadata sections
   */
  $("h5.toggle").click(function () {
    if ($(this).hasClass("expand")) {
      $(this).removeClass("expand");
      $(this).addClass("collapse");
    } else {
      $(this).removeClass("collapse");
      $(this).addClass("expand");
    }

    var corresponding = $(this).attr("id") + "Section";
    if ($("#" + corresponding).hasClass("hideMe")) {
      $("#" + corresponding).removeClass("hideMe");
    } else {
      $("#" + corresponding).addClass("hideMe");
    }
  });

  // If any of the required animal metadata is missing:
  if (
      !$("input#species_capture").val() ||
      !$("input#speciesTSN_capture").val() ||
      !$("input#length_type_capture").val() ||
      !$("input#length_method_capture").val() ||
      !$("input#condition_capture").val() ||
      !$("input#length_recapture").val() ||
      !$("input#length_unit_recapture").val() ||
      !$("input#length_type_recapture").val() ||
      !$("input#length_method_recapture").val()
  ) {
    $("#animalToggle").removeClass("expand");
    $("#animalToggle").removeClass("collapse");
    $("#animalToggleSection").removeClass("hideMe");
  }

  // If any of the required attachement metadata is missing:
  if (!$("input#attachment_method").val()) {
    $("#attachmentToggle").removeClass("expand");
    $("#attachmentToggle").removeClass("collapse");
    $("#attachmentToggleSection").removeClass("hideMe");
  }

  // If any of the required deployment metadata is missing:
  if (
      !$("input#lon_release").val() ||
      !$("input#lat_release").val() ||
      !$("input#person_tagger_capture").val() ||
      !$("input#datetime_release").val()
  ) {
    $("#deploymentToggle").removeClass("expand");
    $("#deploymentToggle").removeClass("collapse");
    $("#deploymentToggleSection").removeClass("hideMe");
  }

  // If any of the required device metadata is missing:
  if (
      !$("input#device_type").val() ||
      !$("input#manufacturer").val() ||
      !$("input#model").val() ||
      !$("input#serial_number").val() ||
      !$("input#device_name").val() ||
      !$("input#person_owner").val() ||
      !$("input#owner_contact").val() ||
      !$("input#firmware").val()
  ) {
    $("#deviceToggle").removeClass("expand");
    $("#deviceToggle").removeClass("collapse");
    $("#deviceToggleSection").removeClass("hideMe");
  }

  // If any of the required end of mission metadata is missing:
  if (
      !$("input#end_details").val() ||
      !$("input#datetime_end").val() ||
      !$("input#lon_end").val() ||
      !$("input#lat_end").val() ||
      !$("input#end_type").val()
  ) {
    $("#end_of_missionToggle").removeClass("expand");
    $("#end_of_missionToggle").removeClass("collapse");
    $("#end_of_missionToggleSection").removeClass("hideMe");
  }

  // If any of the required programming metadata is missing:
  if (!$("input#programming_software").val() || !$(
          "input#programming_report").val()) {
    $("#programmingToggle").removeClass("expand");
    $("#programmingToggle").removeClass("collapse");
    $("#programmingToggleSection").removeClass("hideMe");
  }

  // If any of the required quality metadata is missing:
  if (!$("input#found_problem").val() || !$("input#person_qc").val()) {
    $("#qualityToggle").removeClass("expand");
    $("#qualityToggle").removeClass("collapse");
    $("#qualityToggleSection").removeClass("hideMe");
  }

  // If any of the required waypoints metadata is missing:
  if (!$("input#waypoints_source").val()) {
    $("#waypointsToggle").removeClass("expand");
    $("#waypointsToggle").removeClass("collapse");
    $("#waypointsToggleSection").removeClass("hideMe");
  }

  /**
   * STEP 5: OIIP general metadata.
   * all required fields have data, so show next button.
   */
  $("input.required").change(function () {
    if (metadataType === "general") {
      if ($("input#title").val() && $("input#description").val() && $(
              "input#institution").val()) {
        // remove disabled status for submit button.
        $("input[type=submit]#Next").removeAttr("disabled");
        // remove disabled class for submit button.
        $("input[type=submit]#Next").removeClass("disabled");
      }
    }

    // kludgy!
    if ($("input#species_capture").val()) {
      if ($("input#speciesTSN_capture").val()) {
        if ($("input#length_type_capture").val()) {
          if ($("input#length_method_capture").val()) {
            if ($("input#condition_capture").val()) {
              if ($("input#length_recapture").val()) {
                if ($("input#length_unit_recapture").val()) {
                  if ($("input#length_type_recapture").val()) {
                    if ($("input#length_method_recapture").val()) {
                      if ($("input#attachment_method").val()) {
                        if ($("input#lon_release").val()) {
                          if ($("input#lat_release").val()) {
                            if ($("input#person_tagger_capture").val()) {
                              if ($("input#datetime_release").val()) {
                                if ($("input#device_type").val()) {
                                  if ($("input#manufacturer").val()) {
                                    if ($("input#model").val()) {
                                      if ($("input#serial_number").val()) {
                                        if ($("input#device_name").val()) {
                                          if ($("input#person_owner").val()) {
                                            if ($("input#owner_contact").val()) {
                                              if ($("input#firmware").val()) {
                                                if ($("input#end_details").val()) {
                                                  if ($("input#datetime_end").val()) {
                                                    if ($("input#lon_end").val()) {
                                                      if ($("input#lat_end").val()) {
                                                        if ($("input#end_type").val()) {
                                                          if ($("input#programming_software").val()) {
                                                            if ($("input#programming_report").val()) {
                                                              if ($("input#found_problem").val()) {
                                                                if ($("input#person_qc").val()) {
                                                                  if ($("input#waypoints_source").val()) {
                                                                    // remove disabled status for submit button.
                                                                    $("input[type=submit]#Next").removeAttr(
                                                                        "disabled");
                                                                    // remove disabled class for submit button.
                                                                    $("input[type=submit]#Next").removeClass(
                                                                        "disabled");
                                                                  }
                                                                }
                                                              }
                                                            }
                                                          }
                                                        }
                                                      }
                                                    }
                                                  }
                                                }
                                              }
                                            }
                                          }
                                        }
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

  });

});





