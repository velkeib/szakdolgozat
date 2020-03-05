
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

