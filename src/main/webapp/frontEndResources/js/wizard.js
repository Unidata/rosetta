(function($) {
    $.fn.rosettaWizard = function(options) {
        options = $.extend({  
            submitButton: "" 
        }, options); 
        
        let element = this;

        let steps = $(element).find("fieldset");
        let count = steps.length;
        let submmitButtonName = "#" + options.submitButton;
        $(submmitButtonName).hide();

        // create nav menu
        $(element).before("<nav><ul id='steps'></ul></nav>");
        steps.each(function(i) {
            $(this).wrap("<div id='step" + i + "'></div>");
            $(this).append("<p id='step" + i + "commands'></p>");

            let name = $(this).find("legend").html();
            $("#steps").append("<li id='stepDesc" + i + "'>" + name + "</li>");
            let currentStep =  $("nav p#step"+ i +"commands");
            
            if (i == 0) { // first
                createNextButton(i);
                selectStep(i);
            } else if (i == count - 1) { // last
                $("#step" + i).hide();
                createPrevButton(i);
            } else { // everything else
                $("#step" + i).hide();
                createPrevButton(i);
                createNextButton(i);
            }
        });

        function createPrevButton(i) {
            let stepName = "step" + i;
            $("#" + stepName + "commands").append("<button type='button' id='" + stepName + "Prev' class='previous'>Previous</button>");

            $("#" + stepName + "Prev").bind("click", function(e) {
                $("#" + stepName).hide();
                $("#step" + (i - 1)).show();
                $(submmitButtonName).hide();
                selectStep(i - 1);
            });
        }

        function createNextButton(i) {

            let stepName = "step" + i;
            $("#" + stepName + "commands").append("<button type='button' id='" + stepName + "Next' class='next'>Next</button>");

            $("#" + stepName + "Next").bind("click", function(e) {
                $("#" + stepName).hide();
                $("#step" + (i + 1)).show();
                if (i + 2 == count)
                $(submmitButtonName).show();
                selectStep(i + 1);

                if (i === 1) {
                    $.post("parse", {uniqueId: getFromSession("uniqueId"), fileName: getFromSession("dataFileName")},
                        function (data) {
                            drawGrid(data, "2")
                        }, "text");
                }
                if (i === 2) {
                    
                }

            });
        }

        function selectStep(i) {
            $("#steps li").removeClass("current");
            $("#stepDesc" + i).addClass("current");
        }

    }
})(jQuery); 
