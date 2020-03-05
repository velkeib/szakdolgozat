var timetable = new Timetable();

var selectedID;

timetable.setScope(8,22)

timetable.addLocations(this.setLocations());

this.setReservations();

this.setLocations();

this.setTimeTable();

function setTimeTable(){
    timetable.events = [];


  for(var i = 0; i < reservations.length; i++){
    console.log(authenticatedUser.firstName + " " + authenticatedUser.lastName +"   "+ reservations[i].customer);

     timetable.addEvent(reservations[i].reason + "\n " + reservations[i].customer, reservations[i].courtName, new Date(reservations[i].startDate.split(/[\s-:]+/)[0], reservations[i].startDate.split(/[\s-:]+/)[1],
                                                                                        reservations[i].startDate.split(/[\s-:]+/)[2], reservations[i].startDate.split(/[\s-:]+/)[3],
                                                                                            reservations[i].startDate.split(/[\s-:]+/)[4]), new Date(reservations[i].endDate.split(/[\s-:]+/)[0], reservations[i].endDate.split(/[\s-:]+/)[1],
                                                                                                                                                      reservations[i].endDate.split(/[\s-:]+/)[2], reservations[i].endDate.split(/[\s-:]+/)[3],
                                                                                                                                                      reservations[i].endDate.split(/[\s-:]+/)[4]),
                                                                           { onClick: function(event) {
                                                                                 //window.alert('You clicked on the ' + event.name + ' event in ' + event.location + '. This is an example of a click handler');
                                                                                 if(authenticatedUser.firstName + " " + authenticatedUser.lastName === event.name.split("\n ")[1]){
                                                                                    window.location.href = "http://localhost:8080/reservation/" + event.options.data.id;
                                                                                }
                                                                             }, class: authenticatedUser.firstName + " " + authenticatedUser.lastName === reservations[i].customer ? 'vip-only' : '', data: { id : reservations[i].id} } );
  }
  var renderer = new Timetable.Renderer(timetable);
  renderer.draw('.timetable');
}

function setLocations(){
    console.log(tennisCourts);

    var locations = [];

    for(var i = 0; i < tennisCourts.length; i++){
        locations.push(tennisCourts[i].name);
    }

    return locations;
}


function setReservations(){

    for(var i = 0; i < reservations.length; i++){
        for(var j = 0; j < tennisCourts.length; j++){
            if(reservations[i].courtID == tennisCourts[j].id){
                reservations[i].courtName = tennisCourts[j].name;
            }
        }
    }

    console.log(reservations);
}

function dayChanged() {

    //console.log("test");

    var that = this;

    $.ajax({
        type : "POST",
        url : "http://localhost:8080/api/search",
        data: { filterDay: document.getElementById("filterdatepicker").value},
        success: function(data){
            //console.log(data)
            that.clearReservation();

            reservations = data;

            that.setReservations();
            that.setTimeTable();

        },
        error: function (err) {
            console.log(err)
        }
});


function onRoleChange(){
    $.ajax({
            type : "POST",
            url : "http://localhost:8080/changeAuthorization",
            data: { userID: document.getElementById("userRoleChange").value,
                    role: document.getElementById("userRole").value},
            success: function(data){
                console.log(data)
                everyUser = data;

                document.getElementById("userRoleChange").innerHTML = "";
                for(var i = 0; i < everyUser.length; i++){
                    var option = document.createElement("option");
                    option.text = everyUser[i].firstName + " " + everyUser[i].lastName + " - " + everyUser[i].role;
                    option.value = everyUser[i].id;

                    document.getElementById("userRoleChange").appendChild(option);
                }
            },
            error: function (err) {
                console.log(err)
            }
    });
}


function onUpdateRecord() {

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
