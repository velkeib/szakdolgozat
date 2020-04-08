(function() {
'use strict';
window.addEventListener('load', function() {
// Fetch all the forms we want to apply custom Bootstrap validation styles to
var forms = document.getElementsByClassName('needs-validation');
// Loop over them and prevent submission
var validation = Array.prototype.filter.call(forms, function(form) {
form.addEventListener('submit', function(event) {
checkPassword();
if (form.checkValidity() === false) {
event.preventDefault();
event.stopPropagation();
}
form.classList.add('was-validated');
}, false);
});
}, false);
})();


function checkPassword(){
    document.getElementById("passwordinput").value.length < 5 ? document.getElementById("passwordinput").setCustomValidity("Password has to be at least 5 characters") : document.getElementById("passwordinput").setCustomValidity("");
}