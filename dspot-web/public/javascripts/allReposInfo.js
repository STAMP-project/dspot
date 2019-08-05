var colors = ["#007bff", "#28a745", "#e53935", "#c3e6cb", "#dc3545", "#6c757d"];
var allReposApp = angular.module("allReposApp", []);
/*Directives*/
allReposApp.directive("addDonutGraphForPitMutant", function() {
    return {
        template: "{{addDonutGraph(x.RepoSlug + x.RepoBranch + 'PitMutantScore' + dateDiff(x.Date),[x.AmpResult.TotalResult.totalOrignalKilledMutants,x.AmpResult.TotalResult.totalNewMutantkilled],['OriginalKills', 'NewKills'],['#007bff', '#28a745'])}}"
    };
});

allReposApp.directive("addDonutGraphForJacocoCov", function() {
    return {
        template: "{{addDonutGraph(x.RepoSlug + x.RepoBranch + 'JacocoCov' + dateDiff(x.Date),[x.AmpResult.TotalResult.totalAmpCoverage],['totalInitialCoverage', 'totalAmpCoverage'],['#6c757d', '#c3e6cb'])}}"
    };
});


/*Controller*/
allReposApp.controller("allReposCtr", function($scope, $http) {
    /*variables */
    var statesToInclude = ['recent', 'pending']; 
    /*Http requests*/
    $http.get("/data/All").then(function(res) {
        $scope.allReposData = res.data;
        $scope.dataToShow = res.data;
        $scope.InputSlug = '';
        $scope.filteredDataToShow = [];

        var recentReposDataVar = [];
        var pendingReposDataVar = [];
        /*Separate data - put this into some kind of function later*/
        for (index in res.data) {
          if (res.data[index].State == "recent") {
            recentReposDataVar.push(res.data[index]);
          } else {
            pendingReposDataVar.push(res.data[index]);
          }
        }
        $scope.recentReposData = recentReposDataVar;
        $scope.pendingReposData = pendingReposDataVar;

        $scope.$watchGroup(["InputSlug","dataToShow"], function() {
          var filteredData = [];
          for ( index in $scope.dataToShow) {
            if($scope.dataToShow[index].RepoSlug.includes($scope.InputSlug)) {
              filteredData.push($scope.dataToShow[index]);
            }
          }
          $scope.filteredDataToShow = filteredData;
        });
    }).then(function() {
      /*Pagination*/
      $scope.curPage = 0;
      $scope.itemsPerPage = 10;
      $scope.maxSize = 5;
      $scope.numOfPages = function() {
        return Math.ceil($scope.filteredDataToShow.length / $scope.itemsPerPage);
      };

      $scope.$watchGroup(['curPage + numPerPage','filteredDataToShow'], function() {
          var begin = $scope.curPage * $scope.itemsPerPage,
              end = begin + $scope.itemsPerPage;
          $scope.slicedData = $scope.filteredDataToShow.slice(begin, end);
      });
    })

$scope.addDonutGraph = function(elemId, donutdata, label, colors) {
    new Chart(document.getElementById(elemId), {
        type: "doughnut",
        data: {
            datasets: [{
                data: donutdata,
                backgroundColor: colors
            }],

            // These labels appear in the legend and in the tooltips when hovering different arcs
            labels: label
        },
        responsive: true,
        maintainAspectRatio: true,
        options: {
            legend: {
                display: false
            },
            tooltips: {
                enabled: false,
                displayColors: false,
                borderWidth: 10
            }
        }
    });
};


/*Helpers*/
$scope.dateDiff = function(givenDate) {
  var newDate = Math.floor((new Date() - new Date(givenDate))/1000);
  if (newDate < 60) {
    return  newDate.toString() + " seconds ago";
  } else if (Math.floor(newDate/60) < 60) {
    return Math.floor(newDate/60).toString() + " minutes ago";
  } else if (Math.floor(newDate/3600) < 24) {
    return Math.floor(newDate/3600).toString() + " hrs ago";
  } else {
    return Math.floor(newDate/86400).toString() + " days ago";
  };
}


$scope.switchRecentState = function() {
  var index = $.inArray("recent", statesToInclude);
  if (index > -1) {
    statesToInclude.splice(index, 1);
    /*Recent state is with the view array, switch to only pending if pending is with include states*/
    if ($.inArray("pending", statesToInclude) > -1) {
      $scope.dataToShow = $scope.pendingReposData;
    } else {
      $scope.dataToShow = [];
    }
  } else {
    /*If it's empty then only include recent data*/
    statesToInclude.push("recent");
    if ($.inArray("pending", statesToInclude) > -1) {
      /*If pending should also be included*/
      $scope.dataToShow = $scope.allReposData;
    } else {
      /*Otherwise only show recent*/
      $scope.dataToShow = $scope.recentReposData;
    }
  }
}

$scope.switchPendingState = function() {
  var index = $.inArray("pending", statesToInclude);
  if (index > -1) {
    /*Pending state is with the view array, after switching only show recent*/
    statesToInclude.splice(index, 1);
    if ($.inArray("recent", statesToInclude) > -1) {
      $scope.dataToShow = $scope.recentReposData;
    } else {
      $scope.dataToShow = [];
    }
  } else {
    /*If it's empty then only include pending data*/
    statesToInclude.push("pending");
    if ($.inArray("recent", statesToInclude) > -1) {
      /*If recent should also be included*/
      $scope.dataToShow = $scope.allReposData;
    } else {
      /*Otherwise only show pending*/
      $scope.dataToShow = $scope.pendingReposData;
    }
  }
}

$scope.resetFilter = function() {
  firstFilteredData = [];
}

});

/*Filters*/
allReposApp.filter('cut', function() {
    return function(value, wordwise, max, tail) {
        if (!value) return '';

        max = parseInt(max, 10);
        if (!max) return value;
        if (value.length <= max) return value;

        value = value.substr(0, max);
        if (wordwise) {
            var lastspace = value.lastIndexOf(' ');
            if (lastspace !== -1) {
                //Also remove . and , so its gives a cleaner result.
                if (value.charAt(lastspace - 1) === '.' || value.charAt(lastspace - 1) === ',') {
                    lastspace = lastspace - 1;
                }
                value = value.substr(0, lastspace);
            }
        }

        return value + (tail || ' …');
    };
});

allReposApp.filter('range', function() {
  return function(input, total) {
    if(total > 0) {
      total = parseInt(total);

      for (var i=0; i<total; i++) {
        input.push(i);
      }

      return input;
    } else {
      return [];
    }
  };
});
