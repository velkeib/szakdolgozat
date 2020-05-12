this.onInit();

document.getElementsByClassName("stripe-button-el")[0].disabled = true;
//document.getElementById("startMembership").disabled = true;




function onStartMembership(){

    var that = this;

    $.ajax({
        type : "POST",
        url : "http://localhost:8080/startMembership",
        data : {id: authenticatedUser.id},
        success: function(data){
                console.log(data)
                window.location.href = "http://localhost:8080/userdetail" + data;
        },
        error: function (err) {
            console.log(err)
        }
    });
}

console.log(memberships);


function fireLogout(){
    document.getElementById("logoutbutton").submit();
}

function onInit(){



    if(isMember){
        document.getElementById("startMembership").disabled = true;
    }else{
        document.getElementById("startMembership").disabled = true;
    }

    var innerHTML = "";
    if(memberships !== undefined){
    for(var i = 0; i < memberships.length; i++){

        var isCheckbox = "";
        var paid = "";
        innerHTML = innerHTML + "<tr>";

        if(memberships[i].paymentId != null){
            paid = "Paid"
        }else{
            paid = "Not paid"
            isCheckbox = "<input type='checkbox' onclick='onCheckboxClicked(this)' class='checkboxes' id='checkbox" + i + "'>"
        }

        var startDate = memberships[i].startOfMembership.split("T")
        var endDate = memberships[i].endOfMembership.split("T")

        innerHTML = innerHTML + "<td>"+ memberships[i].id +"</td>" +
                            "<td>" + startDate[0] + "</td>" +
                            "<td>" + endDate[0] + "</td>" +
                            "<td>" + paid + "</td>" +
                            "<td>" + isCheckbox + "</td>";

        innerHTML = innerHTML + "</tr>"

    }
    }
    document.getElementById("tableBody").innerHTML = innerHTML;

    var innerHTML = "";

    for(var i = 0; i < myFutureReservations.length; i++){

            innerHTML = innerHTML + "<tr>";

            var startDate = myFutureReservations[i].startDate.split("T")
            var endDate = myFutureReservations[i].endDate.split("T")
            var courtName = "";

            for(var j = 0; j < tennisCourts.length; j++){
                if(myFutureReservations[i].courtID == tennisCourts[j].id){
                    courtName = tennisCourts[j].id + "-" + tennisCourts[j].name;
                }
            }
            innerHTML = innerHTML + "<td>" + startDate[0] + " " + startDate[1].substring(0,5) + "</td>" +
                                "<td>" + endDate[0] + " " + endDate[1].substring(0,5) + "</td>" +
                                "<td>" + courtName + "</td>" +
                                "<td>" + myFutureReservations[i].reason + "</td>"

            innerHTML = innerHTML + "</tr>"

    }
    document.getElementById("tableBodyReservations").innerHTML = innerHTML;

}



function onCheckboxClicked(oEvent){

    var checkboxes = document.getElementsByClassName("checkboxes");

     if(oEvent.checked === true){
           document.getElementsByClassName("stripe-button-el")[0].disabled = false;
           document.getElementById("id").value = oEvent.parentElement.parentElement.firstElementChild.innerHTML
     }else{
         document.getElementsByClassName("stripe-button-el")[0].disabled = true;
     }

    for(var i = 0; i < checkboxes.length; i++){
        if(checkboxes[i].id !== oEvent.id){
            if(checkboxes[i].disabled === false){
             checkboxes[i].disabled = true;
            }else{
                checkboxes[i].disabled = false;
            }
        }
    }

}

function checkIfCanUpdate(){

    var inputs = document.getElementsByClassName("forminput");

    var i = 0;
    var foundEmpty = true

    while(i < inputs.length && foundEmpty){

        if(inputs[i].value === ""){
            foundEmpty = false
        }

        i++;
    }

    if(!foundEmpty){
        document.getElementById("updateButton").disabled = true;
    }else{
        document.getElementById("updateButton").disabled = false;
    }
}

function onMembershipCheckboxClicked(oEvent){
    if(oEvent.checked){
        if(!isMember){
            document.getElementById("startMembership").disabled = false;
        }
    }else{
        document.getElementById("startMembership").disabled = true;
    }
}

(function() {
  'use strict';
  window.addEventListener('load', function() {
    // Fetch all the forms we want to apply custom Bootstrap validation styles to
    var forms = document.getElementsByClassName('needs-validation');
    // Loop over them and prevent submission
    var validation = Array.prototype.filter.call(forms, function(form) {
      form.addEventListener('submit', function(event) {
        if (form.checkValidity() === false) {
          event.preventDefault();
          event.stopPropagation();
        }
        form.classList.add('was-validated');
      }, false);
    });
  }, false);
})();
