var timetable = new Timetable();

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

function checkEmail(email) {
  var re = /\S+@\S+\.\S+/;
  console.log(email);
  if(re.test(email)){
    document.getElementById("emailcorrect").innerHTML = "VALID";
  }else{
     document.getElementById("emailcorrect").innerHTML = "INVALID";
  }
  this.checkIfAllValid();
}

function checkFirstName(name, id){
    var re = /^[A-Za-z]+$/;
    console.log(name);
    if(re.test(name)){
        document.getElementById(id).innerHTML = "VALID";
      }else{
         document.getElementById(id).innerHTML = "INVALID";
      }
    this.checkIfAllValid();
}

function checkIfAllValid(){
    if(document.getElementById("passwordcorrect").innerHTML === "VALID" && document.getElementById("emailcorrect").innerHTML === "VALID" &&
    document.getElementById("firstnamecorrect").innerHTML === "VALID" && document.getElementById("lastnamecorrect").innerHTML === "VALID"){
        console.log("valid");
        document.getElementById("registerbutton").disabled = false;
    }else{
        console.log("something is missing");
        document.getElementById("registerbutton").disabled = true;
    }
}

function checkPassword(password) {
    var re = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])[0-9a-zA-Z]{8,}$/;
    console.log(password);
    if(re.test(password)){
        document.getElementById("passwordcorrect").innerHTML = "VALID";
    }else{
        document.getElementById("passwordcorrect").innerHTML = "INVALID";
    }
    this.checkIfAllValid();
}

function fireLogout(){
    document.getElementById("logoutbutton").submit();
}

function clearReservation(){
    reservations = [];
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

/*
$.post("http://localhost:8080/api/search", { filterDay: document.getElementById("filterdatepicker").value},
     function(returnedData){
        console.log(returnedData);
}, 'json');
*/

}


function saveRecord() {

    //console.log("test");

            var data = {}
			data["courtID"] =  document.getElementById("courtID").value;
			data["reason"] = document.getElementById("reason").value;


    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/api/save",
        data: JSON.stringify(data),
        headers: { 'api-key':'myKey' },
        dataType: 'json',
        timeout: 600000,


/*

        type : "POST",
        url : "/saveRecord",
        data: 'courtID=' + document.getElementById("courtID").value + '&reason=' + document.getElementById("reason").value,
  */      success: function(data){
            console.log(data)
        }
});

}