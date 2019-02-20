$(document).ready(function() {

    var providerUrl = $('#business-event-table-provider').val();

    //create Tabulator on DOM element with id "example-table"
    var table = new Tabulator("#business-event-table", {
        height:205, // set height of table (in CSS or here), this enables the Virtual DOM and improves render speed dramatically (can be any valid css height value)
        ajaxURL: providerUrl, //ajax URL
        ajaxParams:{pageSize:"-1", sortField:"title", sortOrder:"desc"}, //ajax parameters
        layout:"fitColumns",
        pagination:"local",
        paginationSize:3,
        paginationSizeSelector:[3, 4, 5, 6, 7, 8, 9, 10, 15, 20],
        movableColumns:true,
        columns:[ //Define Table Columns
            {title:"Title", field:"title", sorter:"string"},
            {title:"Description", field:"description", sorter:"string"},
            {title:"Date", field:"date", sorter:"date"},
            {title:"Place", field:"place", sorter:"string"},
            {title:"Topic", field:"topic", sorter:"string"},
        ],
    });

//    table.setData("http://www.getmydata.com/now", {key1:"value1", key2:"value2"});


//    //Use JQuery AJAX request to post data to a Sling Servlet
//    $.ajax({
//         type: 'GET',
//         url: providerUrl,
//         data:'pageSize=-1&sortField=title&sortOrder=asc',
//         success: function(msg){
//
//            var events = jQuery.parseJSON(msg);
//
//            //create Tabulator on DOM element with id "example-table"
//            var table = new Tabulator("#business-event-table", {
//                height:205, // set height of table (in CSS or here), this enables the Virtual DOM and improves render speed dramatically (can be any valid css height value)
//                data:events, //assign data to table
//                layout:"fitColumns", //fit columns to width of table (optional)
//                columns:[ //Define Table Columns
//                    {title:"Title", field:"title", sorter:"string"},
//                    {title:"Description", field:"description", sorter:"string"},
//                    {title:"Date", field:"date", sorter:"date"},
//                    {title:"Place", field:"place", sorter:"string"},
//                    {title:"Topic", field:"topic", sorter:"string"},
//                ],
//            });
//
//         }
//    });
 });

