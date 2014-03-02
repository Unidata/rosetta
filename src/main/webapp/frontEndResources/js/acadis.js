
$( document ).ready(function() {
    console.log( "ready!" );
    var dataJsonStr = JSON.stringify(data);
    addToSession("acadisInventory", dataJsonStr);
    var dataFiles = [];
    for(var k in data) dataFiles.push(k);
    var numDataFiles = dataFiles.length;
    var selectElement = $("<select name='fileToConvert' id='acadisFileSelector'></select>");
    for (var i = 0; i < numDataFiles; i++){
        var name = dataFiles[i];
        var dlLink = data[name];
        var inv =  name + " " + dlLink;
        var optionElement=$("<option></option>")
        optionElement.append(name);
        selectElement.append(optionElement);
    }
    $("#inventory").append(selectElement);

    // add function to do post to createAcadis, set appropriate session storage stuff, and
    // move along with the main createAcadis wizzard
    $("#getAcadisFile").bind("click", function() {
        var inventory = JSON.parse(getFromSession("acadisInventory"));
        var fileName = $("#acadisFileSelector").val();
        addToSession("fileName", fileName);
        var remoteAccessUrl = inventory[fileName];
        var postData = {"fileName" : fileName,
            "remoteAccessUrl" : remoteAccessUrl}

        $.post("createAcadis", postData,
            function(returnData) {
                // do stuff with returnData
                var uniqueId = returnData;
                addToSession("uniqueId", uniqueId);
                if (fileName.contains(".xlsx")) {
                    fileName = fileName.replace(".xlsx", ".csv");
                } else if (fileName.contains(".xls")) {
                    fileName = fileName.replace(".xls", ".csv");
                };
                addToSession("fileName", fileName);
                var newUrl = $(location).attr("origin") + "/rosetta/createAcadis"
                $(location).attr('href',newUrl);
            },
            "text");
    });
 });