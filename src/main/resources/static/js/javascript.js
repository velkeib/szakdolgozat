function checkEmail(email) {
  var re = /\S+@\S+\.\S+/;
  console.log(email);
  if(re.test(email)){
    document.getElementById("emailcorrect").innerHTML = "VALID";
  }else{
     document.getElementById("emailcorrect").innerHTML = "INVALID";
  }
}

function checkFirstName(name, id){
    var re = /^[A-Za-z]+$/;
    console.log(name);
    if(re.test(name)){
        document.getElementById(id).innerHTML = "VALID";
      }else{
         document.getElementById(id).innerHTML = "INVALID";
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
}