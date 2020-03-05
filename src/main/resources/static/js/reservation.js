this.onInit();


function onInit(){
    console.log(reservation);
    document.getElementById("inputFirstName").value = reservation.customer.split(" ")[0]
    document.getElementById("inputLastName").value = reservation.customer.split(" ")[1]

     var startDate = reservation.startDate.split((" "))[0]
     var endDate = reservation.endDate.split((" "))[0]

    document.getElementById("inputStartDate").value = startDate.split("-")[1] + "/" + startDate.split("-")[2] + "/" + startDate.split("-")[0]
    document.getElementById("inputStartTime").value = reservation.startDate.split(" ")[1]
    document.getElementById("inputEndDate").value = endDate.split("-")[1] + "/" + endDate.split("-")[2] + "/" + endDate.split("-")[0]
    document.getElementById("inputEndTime").value = reservation.endDate.split(" ")[1]

    for(var i = 0; i < tennisCourts.length; i++){
        if(tennisCourts[i].id == reservation.courtID){
            reservation.courtID = reservation.courtID + " - " + tennisCourts[i].name
        }
    }


    document.getElementById("inputCourt").value = reservation.courtID

    document.getElementById("inputReason").value = reservation.reason

}

function onTest(){
    console.log(window.location.href);
}


function onUpdateRecord() {

    //console.log("test");

    var that = this;

    $.ajax({
        type : "POST",
        url : "http://localhost:8080/updateRecord",
        data : {Id: window.location.href.split("/")[window.location.href.split("/").length-1],
        startDate : document.getElementById("inputStartDate").value,
        startTime : document.getElementById("inputStartTime").value,
        endDate : document.getElementById("inputEndDate").value,
        endTime : document.getElementById("inputEndTime").value,
        courtId : document.getElementById("inputCourt").value,
        reason : document.getElementById("inputReason").value},
        success: function(data){
            //console.log(data)
            window.location.href = "http://localhost:8080/home";
        },
        error: function (err) {
            console.log(err)
        }
    });

}


function onDeleteRecord() {

    //console.log("test");

    var that = this;

    $.ajax({
        type : "POST",
        url : "http://localhost:8080/deleteRecord/" + window.location.href.split("/")[window.location.href.split("/").length-1],
        success: function(data){
            //console.log(data)
            window.location.href = "http://localhost:8080/home";
        },
        error: function (err) {
            console.log(err)
        }
    });

}
