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