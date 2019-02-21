$(document).ready(function() {

    var providerUrl = $('#business-event-table-provider').val();

    //create Tabulator on DOM element with id "example-table"
    var table = new Tabulator("#business-event-table", {
        height:205, // set height of table (in CSS or here), this enables the Virtual DOM and improves render speed dramatically (can be any valid css height value)
        layout:"fitColumns",
        ajaxURL: providerUrl, //ajax URL

        pagination:"remote",
        paginationDataSent:{
                "page":"pageNo", //change page request parameter to "pageNo"
                "size":"pageSize", //change page request parameter to "pageSize"
            },
        paginationDataReceived:{
                "last_page":"maxPages", //change last_page parameter name to "maxPages"
                "data":"events",
            } ,
        paginationSize:6,
        paginationSizeSelector:[3, 4, 5, 6, 7, 8, 9, 10, 15, 20],

        ajaxSorting:true, //send sort data to the server instead of processing locally

        movableColumns:true,
        columns:[ //Define Table Columns
            {visible:false, field:"id", sorter:"string"},
            {title:"Title", field:"title", sorter:"string"},
            {title:"Description", field:"description", sorter:"string"},
            {title:"Date", field:"date", sorter:"date"},
            {title:"Place", field:"place", sorter:"string"},
            {title:"Topic", field:"topic", sorter:"string"},
        ],
        rowClick:function(e, row){ //trigger an alert message when the row is clicked
            // Request event data by id

            // Show "Wait message"
            $('#business-event-details-wait').show();
            // Hide event info
            $('#business-event-details-table').hide();

            // Show event view
            $( "#business-event-details" ).dialog({
              modal: true,
              buttons: {
                Ok: function() {
                  $( this ).dialog( "close" );
                }
              }
            });

            // Ask event data by id
            // Use JQuery AJAX request to post data to a Sling Servlet
            $.ajax({
                 type: 'GET',
                 url: providerUrl,
                 data:'id=' + row.getData().id,
                 success: function(msg){

                    var event = jQuery.parseJSON(msg);

                    $('#business-event-title').html(event.title);
                    $('#business-event-description').html(event.description);
                    $('#business-event-date').html(event.date);
                    $('#business-event-place').html(event.place);
                    $('#business-event-topic').html(event.topic);

                    // Hide "Wait message"
                    $('#business-event-details-wait').hide();
                    // Show event info
                    $('#business-event-details-table').show();
                 }
             });
        },
    });

    $('#business-event-table-refresh').click(function() {
        table.setData();
    });

 });

