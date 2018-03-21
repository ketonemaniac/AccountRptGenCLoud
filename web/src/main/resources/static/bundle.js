(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
"use strict";

// UPLOAD CLASS DEFINITION
// ======================
var dropZone = document.getElementById('drop-zone');
var uploadForm = document.getElementById('js-upload-form');


var startUpload = function (files) {

    // https://coligo.io/building-ajax-file-uploader-with-node/

    if (files.length > 0) {
        // One or more files selected, process the file upload

        // create a FormData object which will be sent as the data payload in the
        // AJAX request
        var formData = new FormData();

        // loop through all the selected files
        for (var i = 0; i < files.length; i++) {
            var file = files[i];

            // add the files to formData object for the data payload
            formData.append('file', file, file.name);
            var spinHandle = loadingOverlay().activate();
            $.ajax({
                url: '/uploadFile',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (data) {
                    console.log('done');
                    loadingOverlay().cancel(spinHandle);
                    // histTable.row.add( [
                    //     data.company,    
                    //     DateFormat(new Date(data.generationTime), "yyyy-mm-dd HH:MM:ss"),
                    //     data.status,
                    // ]).draw(false);
                    $("#modalTitle").text("Generation Started");
                    $("#modalText").html("When complete. please supply password <b>" + data.password + "</b> to the file <b>" + data.filename + "</b>");
                    $("#myModal").modal("show");
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    console.log('error');
                    loadingOverlay().cancel(spinHandle);
                    $("#modalTitle").text("Upload Error");
                    $("#modalText").text("See Server logs for details. textStatus=" + textStatus + " errorThrown=" + errorThrown);
                    $("#myModal").modal("show");
                }
                /*xhr: function() {
                    // create an XMLHttpRequest
                    var xhr = new XMLHttpRequest();
 
                    // listen to the 'progress' event
                    xhr.upload.addEventListener('progress', function(evt) {
 
                    if (evt.lengthComputable) {
                        // calculate the percentage of upload completed
                        var percentComplete = evt.loaded / evt.total;
                        percentComplete = parseInt(percentComplete * 100);
 
                        // update the Bootstrap progress bar with the new percentage
                        $('.progress-bar').text(percentComplete + '%');
                        $('.progress-bar').width(percentComplete + '%');
 
                        // once the upload reaches 100%, set the progress bar text to done
                        if (percentComplete === 100) {
                            $('.progress-bar').html('Done');
                            // spinner.spin();
                            $(".overlay").fadeIn().append(spinner.el);
                            // new Spinner({color:'#999', lines: 12}).spin($(".overlay"));
 
                        }
 
                    }
 
                    }, false);
 
                    return xhr;
                }*/
            });
        }


    }


}


var startDownload = function (filename) {
    // ajax doesn't handle file downloads elegantly
    var req = new XMLHttpRequest();
    req.open("POST", "/downloadFile", true);
    req.setRequestHeader("Content-Type", "application/json");
    req.responseType = "blob";
    req.onreadystatechange = function () {
        if (req.readyState === 4 && req.status === 200) {
            // test for IE
            if (typeof window.navigator.msSaveBlob === 'function') {
                window.navigator.msSaveBlob(req.response, "PdfName-" + new Date().getTime() + ".pdf");
            } else {
                var blob = req.response;
                var link = document.createElement('a');
                link.href = window.URL.createObjectURL(blob);
                link.download = filename;
                // append the link to the document body
                document.body.appendChild(link);
                link.click();
            }
        }
    };
    req.send(JSON.stringify({ "filename": filename }));
}

var init = function () {

    // EVENT LISTENERS
    // ===============
    uploadForm.addEventListener('submit', function (f) {
        var uploadFiles = document.getElementById('js-upload-files').files;
        f.preventDefault()
        startUpload(uploadFiles)
    })

    dropZone.ondrop = function (f) {
        // prevent brower from really opening the file
        f.preventDefault();
        this.className = 'upload-drop-zone';
        startUpload(f.dataTransfer.files)
    }

    dropZone.ondragover = function () {
        this.className = 'upload-drop-zone drop';   // change css
        return false;
    }

    dropZone.ondragleave = function () {
        this.className = 'upload-drop-zone';
        return false;
    }

    // We can attach the `fileselect` event to all file inputs on the page
    $(document).on('change', ':file', function () {
        var input = $(this),
            numFiles = input.get(0).files ? input.get(0).files.length : 1,
            label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
        input.trigger('fileselect', [numFiles, label]);
    });


    $(document).on('click', '#genHistoryTable .dlbtn', function () {
        startDownload($(this).closest("tr").children('td:nth-child(3)').text());
    });
}

module.exports.init = init;

},{}],2:[function(require,module,exports){
var DateFormat = require('dateformat');

'use strict';

// UPLOAD CLASS DEFINITION
// ======================
var histTable = $('#genHistoryTable').DataTable({
    "paging": false,
    "ordering": false,
    "info": false,
    "searching": false
});


// AJAX FUNCTIONS
// ===============

var listFiles = function () {
    $.ajax({
        url: '/listFiles',
        type: 'GET',
        processData: false,
        contentType: false,
        success: function (dataArr) {
            histTable.clear();
            $.each(dataArr, function (idx, data) {
                console.log("file is " + data.filename);
                var statusText = "<span class=\"glyphicon glyphicon-envelope\" style=\"color:green\"></span>&nbsp;" + data.status;
                histTable.row.add([
                    data.company,
                    DateFormat(new Date(data.generationTime), "yyyy-mm-dd HH:MM:ss"),
                    statusText,
                ]);
            });
            histTable.draw(false);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            $("#modalTitle").text("List Error");
            $("#modalText").text("See Server logs for details. textStatus=" + textStatus + " errorThrown=" + errorThrown);
            $("#myModal").modal("show");
        }
    });
}

var init = function () {
    listFiles();
    setInterval(listFiles, 10000);

    // initialize historical chart
    var ctx = document.getElementById("myChart");
    var labels = ['January', 'February', 'March', 'April', 'May', 'June'];
    var data = [20, 10, 30, 40, 50];
    var myChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                borderColor: "#3e95cd",
                fill: false
            }]
        },
        options: {
            legend: {
                display: false
            },
            title: {
                display: true,
                text: 'Reports Generated'
            }
        }
    });
}

module.exports.init = init;
},{"dateformat":4}],3:[function(require,module,exports){
// var Spinner = require('spin');
var FileUpload = require('./fileUpload.js');
var Historical = require('./historical.js');

+ function ($) {
    'use strict';

    // MAIN STARTUP FLOW
    // ==================
    $(document).ready(function () {

        FileUpload.init();
        Historical.init();

    });

}(jQuery);
},{"./fileUpload.js":1,"./historical.js":2}],4:[function(require,module,exports){
/*
 * Date Format 1.2.3
 * (c) 2007-2009 Steven Levithan <stevenlevithan.com>
 * MIT license
 *
 * Includes enhancements by Scott Trenda <scott.trenda.net>
 * and Kris Kowal <cixar.com/~kris.kowal/>
 *
 * Accepts a date, a mask, or a date and a mask.
 * Returns a formatted version of the given date.
 * The date defaults to the current date/time.
 * The mask defaults to dateFormat.masks.default.
 */

(function(global) {
  'use strict';

  var dateFormat = (function() {
      var token = /d{1,4}|m{1,4}|yy(?:yy)?|([HhMsTt])\1?|[LloSZWN]|'[^']*'|'[^']*'/g;
      var timezone = /\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g;
      var timezoneClip = /[^-+\dA-Z]/g;
  
      // Regexes and supporting functions are cached through closure
      return function (date, mask, utc, gmt) {
  
        // You can't provide utc if you skip other args (use the 'UTC:' mask prefix)
        if (arguments.length === 1 && kindOf(date) === 'string' && !/\d/.test(date)) {
          mask = date;
          date = undefined;
        }
  
        date = date || new Date;
  
        if(!(date instanceof Date)) {
          date = new Date(date);
        }
  
        if (isNaN(date)) {
          throw TypeError('Invalid date');
        }
  
        mask = String(dateFormat.masks[mask] || mask || dateFormat.masks['default']);
  
        // Allow setting the utc/gmt argument via the mask
        var maskSlice = mask.slice(0, 4);
        if (maskSlice === 'UTC:' || maskSlice === 'GMT:') {
          mask = mask.slice(4);
          utc = true;
          if (maskSlice === 'GMT:') {
            gmt = true;
          }
        }
  
        var _ = utc ? 'getUTC' : 'get';
        var d = date[_ + 'Date']();
        var D = date[_ + 'Day']();
        var m = date[_ + 'Month']();
        var y = date[_ + 'FullYear']();
        var H = date[_ + 'Hours']();
        var M = date[_ + 'Minutes']();
        var s = date[_ + 'Seconds']();
        var L = date[_ + 'Milliseconds']();
        var o = utc ? 0 : date.getTimezoneOffset();
        var W = getWeek(date);
        var N = getDayOfWeek(date);
        var flags = {
          d:    d,
          dd:   pad(d),
          ddd:  dateFormat.i18n.dayNames[D],
          dddd: dateFormat.i18n.dayNames[D + 7],
          m:    m + 1,
          mm:   pad(m + 1),
          mmm:  dateFormat.i18n.monthNames[m],
          mmmm: dateFormat.i18n.monthNames[m + 12],
          yy:   String(y).slice(2),
          yyyy: y,
          h:    H % 12 || 12,
          hh:   pad(H % 12 || 12),
          H:    H,
          HH:   pad(H),
          M:    M,
          MM:   pad(M),
          s:    s,
          ss:   pad(s),
          l:    pad(L, 3),
          L:    pad(Math.round(L / 10)),
          t:    H < 12 ? dateFormat.i18n.timeNames[0] : dateFormat.i18n.timeNames[1],
          tt:   H < 12 ? dateFormat.i18n.timeNames[2] : dateFormat.i18n.timeNames[3],
          T:    H < 12 ? dateFormat.i18n.timeNames[4] : dateFormat.i18n.timeNames[5],
          TT:   H < 12 ? dateFormat.i18n.timeNames[6] : dateFormat.i18n.timeNames[7],
          Z:    gmt ? 'GMT' : utc ? 'UTC' : (String(date).match(timezone) || ['']).pop().replace(timezoneClip, ''),
          o:    (o > 0 ? '-' : '+') + pad(Math.floor(Math.abs(o) / 60) * 100 + Math.abs(o) % 60, 4),
          S:    ['th', 'st', 'nd', 'rd'][d % 10 > 3 ? 0 : (d % 100 - d % 10 != 10) * d % 10],
          W:    W,
          N:    N
        };
  
        return mask.replace(token, function (match) {
          if (match in flags) {
            return flags[match];
          }
          return match.slice(1, match.length - 1);
        });
      };
    })();

  dateFormat.masks = {
    'default':               'ddd mmm dd yyyy HH:MM:ss',
    'shortDate':             'm/d/yy',
    'mediumDate':            'mmm d, yyyy',
    'longDate':              'mmmm d, yyyy',
    'fullDate':              'dddd, mmmm d, yyyy',
    'shortTime':             'h:MM TT',
    'mediumTime':            'h:MM:ss TT',
    'longTime':              'h:MM:ss TT Z',
    'isoDate':               'yyyy-mm-dd',
    'isoTime':               'HH:MM:ss',
    'isoDateTime':           'yyyy-mm-dd\'T\'HH:MM:sso',
    'isoUtcDateTime':        'UTC:yyyy-mm-dd\'T\'HH:MM:ss\'Z\'',
    'expiresHeaderFormat':   'ddd, dd mmm yyyy HH:MM:ss Z'
  };

  // Internationalization strings
  dateFormat.i18n = {
    dayNames: [
      'Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat',
      'Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'
    ],
    monthNames: [
      'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec',
      'January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'
    ],
    timeNames: [
      'a', 'p', 'am', 'pm', 'A', 'P', 'AM', 'PM'
    ]
  };

function pad(val, len) {
  val = String(val);
  len = len || 2;
  while (val.length < len) {
    val = '0' + val;
  }
  return val;
}

/**
 * Get the ISO 8601 week number
 * Based on comments from
 * http://techblog.procurios.nl/k/n618/news/view/33796/14863/Calculate-ISO-8601-week-and-year-in-javascript.html
 *
 * @param  {Object} `date`
 * @return {Number}
 */
function getWeek(date) {
  // Remove time components of date
  var targetThursday = new Date(date.getFullYear(), date.getMonth(), date.getDate());

  // Change date to Thursday same week
  targetThursday.setDate(targetThursday.getDate() - ((targetThursday.getDay() + 6) % 7) + 3);

  // Take January 4th as it is always in week 1 (see ISO 8601)
  var firstThursday = new Date(targetThursday.getFullYear(), 0, 4);

  // Change date to Thursday same week
  firstThursday.setDate(firstThursday.getDate() - ((firstThursday.getDay() + 6) % 7) + 3);

  // Check if daylight-saving-time-switch occurred and correct for it
  var ds = targetThursday.getTimezoneOffset() - firstThursday.getTimezoneOffset();
  targetThursday.setHours(targetThursday.getHours() - ds);

  // Number of weeks between target Thursday and first Thursday
  var weekDiff = (targetThursday - firstThursday) / (86400000*7);
  return 1 + Math.floor(weekDiff);
}

/**
 * Get ISO-8601 numeric representation of the day of the week
 * 1 (for Monday) through 7 (for Sunday)
 * 
 * @param  {Object} `date`
 * @return {Number}
 */
function getDayOfWeek(date) {
  var dow = date.getDay();
  if(dow === 0) {
    dow = 7;
  }
  return dow;
}

/**
 * kind-of shortcut
 * @param  {*} val
 * @return {String}
 */
function kindOf(val) {
  if (val === null) {
    return 'null';
  }

  if (val === undefined) {
    return 'undefined';
  }

  if (typeof val !== 'object') {
    return typeof val;
  }

  if (Array.isArray(val)) {
    return 'array';
  }

  return {}.toString.call(val)
    .slice(8, -1).toLowerCase();
};



  if (typeof define === 'function' && define.amd) {
    define(function () {
      return dateFormat;
    });
  } else if (typeof exports === 'object') {
    module.exports = dateFormat;
  } else {
    global.dateFormat = dateFormat;
  }
})(this);

},{}]},{},[3])
//# sourceMappingURL=bundle.js.map
