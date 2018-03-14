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
            
            $('nav').append("<p id='step" + i + "commands' class='wizardNav'></p>");

            // 2
            let name = $(this).find("legend").html();
            $("#steps").append("<li id='stepDesc" + i + "'>Step " + (i + 1) + " <span>" + name + "</span></li>");
            let currentStep =  $("nav p#step"+ i +"commands");
            
            if (i == 0) {
                createNextButton(i);
                selectStep(i);
            }
            else if (i == count - 1) {
                $("#step" + i).hide();
               // console.log($("#step" + i));
                createPrevButton(i);
            }
            else {
                 console.log(i);
                 console.log($("#step" + i));
                $("#step" + i).hide();
                createPrevButton(i);
                createNextButton(i);
            }
        });

        function createPrevButton(i) {
            let stepName = "step" + i;
            $("#" + stepName + "commands").append("<a href='#' id='" + stepName + "Prev' class='prev'>< Back</a>");

            $("#" + stepName + "Prev").bind("click", function(e) {
                $("#" + stepName).hide();
                $("#step" + (i - 1)).show();
                $(submmitButtonName).hide();
                selectStep(i - 1);
            });
        }

        function createNextButton(i) {
            let stepName = "step" + i;
            $("#" + stepName + "commands").append("<a href='#' id='" + stepName + "Next' class='next'>Next ></a>");

            $("#" + stepName + "Next").bind("click", function(e) {
                $("#" + stepName).hide();
                $("#step" + (i + 1)).show();
                if (i + 2 == count)
                    $(submmitButtonName).show();
                selectStep(i + 1);
            });
        }

        function selectStep(i) {
            $("#steps li").removeClass("current");
            $("#stepDesc" + i).addClass("current");
        }

    }
})(jQuery); 
