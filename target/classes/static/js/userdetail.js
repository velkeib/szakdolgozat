function onStartMembership(){

    var that = this;

    $.ajax({
        type : "POST",
        url : "http://localhost:8080/startMembership",
        data : {id: authenticatedUser.id},
        success: function(data){
                console.log(data)
                //window.location.href = "http://localhost:8080/home";
        },
        error: function (err) {
            console.log(err)
        }
    });
}