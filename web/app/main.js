// var Spinner = require('spin');
var FileUpload = require('./fileUpload.js');
var Historical = require('./historical.js');

+ function ($) {
    'use strict';

    // MAIN STARTUP FLOW
    // ==================
    $(document).ready(function () {

        FileUpload.init("", function(data) { return "File upload success. Generation of " + data.company + " started."; });
        Historical.init();

    });

}(jQuery);