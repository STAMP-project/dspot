var formApp = angular.module("formApp", []);
formApp.controller("formCtr", function($scope, $http) {
    $scope.successAlert = false;
    $scope.failedAlert = false;
    $scope.reset = function() {
        $scope.user.repo = { url: "", branch: "" };
        $scope.user.email = "";
        $scope.submitForm.$setPristine();
        $scope.submitForm.$setUntouched()
    }

    $scope.submit = function() {
        /*Default branch name as master - removed in later version*/
        document.getElementById("gitBranch").defaultValue = "master";

        $scope.successAlert = false;
        $scope.failedAlert = false;
        var dataToSubmit = angular.copy($scope.user);
        $http({
            method: "POST",
            url: "/reposubmit",
            data: dataToSubmit
        }).then(function mySuccess(res) {
            $scope.response = res.data;
            $scope.successAlert = true;
            $scope.reset();
        }, function myError(res) {
            $scope.response = res.data;
            $scope.failedAlert = true;
        });
    }
})

var colors = ["#007bff", "#28a745", "#e53935", "#c3e6cb", "#dc3545", "#6c757d"];
var recentRepos = angular.module("recentReposApp", []);
/*Directives*/
recentRepos.directive("addDonutGraphForPitMutant", function() {
  return {
    template : "{{addDonutGraph(x.RepoSlug + '(' + x.RepoBranch + ')',[x.AmpResult.TotalResult.totalOrignalKilledMutants,x.AmpResult.TotalResult.totalNewMutantkilled],['OriginalKills', 'NewKills'],['#007bff', '#28a745'])}}"
  };
});

recentRepos.directive("addDonutGraphForJacocoCov", function() {
  return {
    template : "{{addDonutGraph(x.RepoSlug + '(' + x.RepoBranch + ')',[x.AmpResult.TotalResult.totalAmpCoverage],['totalInitialCoverage', 'totalAmpCoverage'],['#6c757d', '#c3e6cb'])}}"
  };
});

/*Controller*/
recentRepos.controller("recentReposCtr", function($scope, $http) {
    $http.get("/data/recent").then(function(res) {
        $scope.recentReposData = res.data;
    });
    $scope.addDonutGraph = function(elemId, donutdata,label,colors) {
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
});

/*Filter*/
angular.module("recentReposApp").filter('cut', function () {
    return function (value, wordwise, max, tail) {
        if (!value) return '';

        max = parseInt(max, 10);
        if (!max) return value;
        if (value.length <= max) return value;

        value = value.substr(0, max);
        if (wordwise) {
            var lastspace = value.lastIndexOf(' ');
            if (lastspace !== -1) {
              //Also remove . and , so its gives a cleaner result.
              if (value.charAt(lastspace-1) === '.' || value.charAt(lastspace-1) === ',') {
                lastspace = lastspace - 1;
              }
              value = value.substr(0, lastspace);
            }
        }

        return value + (tail || ' …');
    };
});

angular.bootstrap(document.getElementById("App2"),['recentReposApp'])
