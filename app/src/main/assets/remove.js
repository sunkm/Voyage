var elscript = document.createElement("script");elscript.text = '
document.getElementsByClassName("row")[6].setAttribute("style","display:none");
document.getElementsByClassName("foot-font")[3].setAttribute("style","display:none");
document.getElementsByClassName("credits love")[0].setAttribute("style","display:none");
document.getElementsByClassName("filter filter-black")[0].setAttribute("style","display:none");
document.getElementsByClassName("parallax-background parallax-active activated")[0].setAttribute("style","display:none");
document.getElementById("top-header").setAttribute("style","display:none");
document.getElementsByClassName("navbar-header")[0].setAttribute("style","display:none");
document.getElementsByClassName("navbar filter-bar navbar-fixed-top filled")[0].setAttribute("style","display:none");
';elscript.setAttribute("type","text/javascript");document.body.appendChild(elscript);
