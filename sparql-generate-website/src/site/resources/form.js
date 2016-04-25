var varQueryUri = "http://w3id.org/sparql-generate/query/example1.rqg";
//var varQueryUri = "http://localhost:8080/sparql-generate/query/example1.rqg";
var varQueryText = "PREFIX rqg-fn: <http://w3id.org/sparql-generate/fn/>\nPREFIX rqg-ite: <http://w3id.org/sparql-generate/ite/>\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n\nGENERATE {\n <s> ?key ?valuedec .\n} ITERATOR rqg-ite:JSONListKeys( ?message ) AS ?key \nWHERE {\n BIND( rqg-fn:JSONPath( ?message , CONCAT( '$.' , ?key )) AS ?value ) \n BIND( xsd:decimal( ?value ) AS ?valuedec )\n FILTER( ?valuedec > 3 )\n}";
var varMessage = "{\n \"a\" : \"1\",\n \"b\" : \"2\",\n \"c\" : \"3\",\n \"d\" : \"4\",\n \"e\" : \"5\"\n}";

    function submit() {
    var query = document.getElementById('queryform');
    query.value = encodeURIComponent(query.value);
    console.log(query.value);
    var message = document.getElementById('message');
    message.value = encodeURIComponent(message.value);
    var queryuri = document.getElementById('queryuri');
    queryuri.value = encodeURIComponent(queryuri.value);
    var variable = document.getElementById('variable');
    variable.value = encodeURIComponent(variable.value);
    return true;
};

function change() {
    if (document.getElementById('form').elements['source'].value === 'text') {
        var forurl = document.getElementsByClassName('forurl');
        for (var i = 0; i < forurl.length; i++) {
            forurl.item(i).style.display = 'none';
        }
        var fortext = document.getElementsByClassName('fortext');
        for (var i = 0; i < forurl.length; i++) {
            fortext.item(i).style.display = '';
        }
        document.getElementById('queryform').value = '';
    } else {
        var forurl = document.getElementsByClassName('forurl');
        for (var i = 0; i < forurl.length; i++) {
            forurl.item(i).style.display = '';
        }
        var fortext = document.getElementsByClassName('fortext');
        for (var i = 0; i < forurl.length; i++) {
            fortext.item(i).style.display = 'none';
        }
        document.getElementById('queryuri').value = '';
    }
};

window.onload = (function(varQueryUri, varMessage) {
    return function() {
        document.getElementById('queryuri').value = varQueryUri;
        document.getElementById('message').value = varMessage;
        change();
    };
})(varQueryUri, varMessage);