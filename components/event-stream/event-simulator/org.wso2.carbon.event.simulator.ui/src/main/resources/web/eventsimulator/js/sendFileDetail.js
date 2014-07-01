function sendFileDetails(fileName){

    jQuery.ajax({
        type: "POST",
        url: "../eventsimulator/sendFileDetail_ajaxprocessor.jsp?fileName=" + fileName + "",
        async: true,


        success:function(msg){
            CARBON.showInfoDialog("Events sending using file is started .......");


        }


    });
}

function deleteFile(fileName){

    jQuery.ajax({
        type: "POST",
        url: "../eventsimulator/deleteFile_ajaxprocessor.jsp?fileName=" + fileName + "",
        async: false,


        success:function(msg){
            if(msg!=null &&msg.trim()=="deleted"){
                CARBON.showInfoDialog("File is successfully deleted, please refresh the page to see the changes");
            }else{
                CARBON.showErrorDialog(msg);
            }


        }


    });
}