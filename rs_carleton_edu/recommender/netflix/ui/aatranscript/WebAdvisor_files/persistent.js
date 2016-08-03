/* Javascript for the xhtml-compliant persistent navigation */

function jump(object) { if(object.options[object.selectedIndex].value!=""){window.location.href = object.options[object.selectedIndex].value;} }
function persistentQuickNavSubmit()
{
submit_form = document.getElementById('persistentQuickNav');
submit_form.submit();
}

function searchSite()
{
submit_form = document.getElementById('persistentSearch');
submit_form.submit();
}

function searchDirectory()
{
search_target = document.getElementById('persistentSearchTarget');
search_target.setAttribute('value', 'directory');
submit_form = document.getElementById('persistentSearch');
submit_form.submit();
}
function doPersistentSubmitReplacement()
{
search_quicknav = document.getElementById('persistentQuickNavSubmit');
new_search_quicknav = document.createElement('a');
new_search_quicknav.setAttribute('href','javascript:persistentQuickNavSubmit()');
new_search_quicknav.setAttribute('id', 'persistentQuickNavSubmit');
new_search_quicknav.setAttribute('title', 'Go to selected page');
new_search_quicknav.appendChild(document.createTextNode('go'));
search_quicknav.parentNode.insertBefore(new_search_quicknav,search_quicknav);
search_quicknav.parentNode.removeChild(search_quicknav);

search_site = document.getElementById('persistentSearchSubmitSite');
new_search_site = document.createElement('a');
new_search_site.setAttribute('href', 'javascript:searchSite()');
new_search_site.setAttribute('id', 'persistentSearchSubmitSite');
new_search_site.setAttribute('title', 'Search Web pages');
new_search_site.appendChild(document.createTextNode('site'));
search_site.parentNode.insertBefore(new_search_site, search_site);
search_site.parentNode.removeChild(search_site);

search_directory = document.getElementById('persistentSearchSubmitDirectory');
new_search_directory = document.createElement('a');
new_search_directory.setAttribute('href', 'javascript:searchDirectory()');
new_search_directory.setAttribute('id', 'persistentSearchSubmitDirectory');
new_search_directory.setAttribute('title', 'Search the Carleton directory');
new_search_directory.appendChild(document.createTextNode('people'));
search_directory.parentNode.insertBefore(new_search_directory,search_directory);
search_directory.parentNode.removeChild(search_directory);

search_input = document.getElementById('persistentSearchInput');
search_input.setAttribute('onfocus', "if(this.value==' Search') {this.value='';}");
search_input.setAttribute('onblur', "if(this.value=='') {this.value=' Search';}");

}

if (window.addEventListener)
  window.addEventListener("load", doPersistentSubmitReplacement, true);
else if (window.attachEvent)
  window.attachEvent("onload", doPersistentSubmitReplacement);
