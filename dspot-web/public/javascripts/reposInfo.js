var colors = ["#007bff", "#28a745", "#e53935", "#c3e6cb", "#dc3545", "#6c757d"];
var reposinfoapp = angular.module("reposInfoApp", []);
reposinfoapp.controller("reposInfoCtr", function($scope, $http) {
  $http.get("reposInfo.data").then(function(res) {
    console.log(res.data)
    $scope.reposInfoData = res.data;
  });
  $scope.addDonutGraph = function(elemId, donutdata) {
    new Chart(document.getElementById(elemId), {
      type: "doughnut",
      data: {
        datasets: [
          {
            data: donutdata,
            backgroundColor: [
              "#007bff",
              "#28a745",
              "#e53935",
              "#c3e6cb",
              "#dc3545",
              "#6c757d"
            ]
          }
        ],

        // These labels appear in the legend and in the tooltips when hovering different arcs
        labels: ["Red", "Yellow", "Blue"]
      },
      responsive: true,
      maintainAspectRatio: true,
      options: {
        legend: {
          display: false
        },
        tooltips: {
          enabled: false
        }
      }
    });
  };
});
