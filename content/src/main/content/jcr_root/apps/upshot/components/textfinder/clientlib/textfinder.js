// Creates a GUID value using JavaScript - used for the
// unique value for the generated claim
 function createUUID() {

    var s = [];
    var hexDigits = "0123456789abcdef";
    for (var i = 0; i < 36; i++) {
        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
    }
    s[14] = "4";  // bits 12-15 of the time_hi_and_version field to 0010
    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);  // bits 6-7 of the clock_seq_hi_and_reserved to 01
    s[8] = s[13] = s[18] = s[23] = "-";

    var uuid = s.join("");
    return uuid;
}

$(document).ready(function() {

    $('#textfinder-submit').click(function() {

        var failure = function(err) {
                 alert("Unable to retrive data "+err);
       };

        //Get the user-defined values
        var searchText= $('#searchText').val();
        var searchPath= $('#searchPath').val();
        var queryEngine= $('#queryEngine').val();
        var claimId = createUUID();

        //Use JQuery AJAX request to post data to a Sling Servlet
        $.ajax({
             type: 'GET',
             url:'/bin/upshot/servlets/TextFinderServlet',
             data:'id=' + claimId + '&searchText=' + searchText + '&searchPath=' + searchPath + '&queryEngine=' + queryEngine,
             success: function(msg){

                var json = jQuery.parseJSON(msg);
                var msgId=   json.id;
                var links = json.links;

                // $('#claimNum').val(msgId);
                var list = "";
                for(var i = 0; i < links.length; i++) {
                    list += "<li><strong>" + links[i].title + "</strong><br><a href='" + links[i].url + "'>" + links[i].url + "</a></li>";
                }
                if(list.length > 0) {
                    list = "<ul>" + list + "</ul>";
                }
                $('#json').html($('#queryEngine').val() + ":<br>" + list);
             }
         });
      });

}); // end ready
