var urlPath = window.location.pathname.replace("/repo", "");
var colors = ["#007bff", "#28a745", "#e53935", "#c3e6cb", "#dc3545", "#6c757d"];
var repoinfoapp = angular.module("repoInfoApp", []);
repoinfoapp.controller("repoInfoCtr", function($scope, $http) {
    $http.get("/repodata" + urlPath).then(function(res) {
        /*variables */
        var dateJacoco = [];
        var datePitMutant = [];
        var date = [];
        var jacocoLineData = [];
        var pitmutantLineData = [];

        /*Extract data*/
        if (res.data.length > 0) {
            /*Get most recent total data for pie graph*/
            if (res.data[0].AmpOptions['test-criterion'] == "JacocoCoverageSelector") {
                $scope.mostRecentData = [res.data[0].AmpResult.TotalResult.totalAmpCoverage, res.data[0].AmpResult.TotalResult.totalInitialCoverage];
                $scope.mostRecentSelector = "JacocoCoverageSelector";
                $scope.donutLabel = ["totalAmpCoverage", "totalInitialCoverage"]
            } else {
                $scope.mostRecentData = [res.data[0].AmpResult.TotalResult.totalNewMutantkilled, res.data[0].AmpResult.TotalResult.totalOrignalKilledMutants];
                $scope.mostRecentSelector = "PitmutantScoreSelector";
                $scope.donutLabel = ["totalNewMutantkilled", "totalOrignalKilledMutants"]
            }

            /*Get line data*/
            for (i in res.data) {
                /*JacococCoverage*/
                if (res.data[i].AmpOptions['test-criterion'] == "JacocoCoverageSelector") {
                    var val1 = parseInt(res.data[i].AmpResult.TotalResult.totalAmpCoverage);
                    var val2 = parseInt(res.data[i].AmpResult.TotalResult.totalInitialCoverage);
                    jacocoLineData.unshift(Math.round(val1 / val2) - 1);
                    dateJacoco.unshift(res.data[i].Date);
                } else {
                    /*PitmutantScore*/
                    var val1 = parseInt(res.data[i].AmpResult.TotalResult.totalNewMutantkilled);
                    var val2 = parseInt(res.data[i].AmpResult.TotalResult.totalOrignalKilledMutants);
                    pitmutantLineData.unshift(Math.round(val1 / val2));
                    datePitMutant.unshift(res.data[i].Date);
                }
            }
            delete res.data[0].AmpResult["TotalResult"];
            /*[{
                data: [1, 2],
                label: 'Dataset-1',
                borderColor: "#007bff",
                backgroundColor: "#007bff",
                fill: true
            }]*/
            var datasets = [];
            var barLabels = [];
            /*Get bar labels for bar graph*/
            if (res.data[0].AmpOptions['test-criterion'] == "JacocoCoverageSelector") {
                barLabels = ["initialCoverage","amplifiedCoverage","totalCoverage"];
            } else {
                barLabels = ["originalKilledMutants","NewMutantKilled"];
            }
            /*Get most recent all test data  for bar graph*/
            for( key in res.data[0].AmpResult) {
                var dataset = {};
                var data = [];
                var stats = res.data[0].AmpResult[key];
                var label = key.split("/D/").pop();
                var borderColor = $scope.generateRandomColor();
                var backgroundColor = borderColor;

                if (res.data[0].AmpOptions['test-criterion'] == "JacocoCoverageSelector") {
                    data.push(stats.initialCoverage);
                    data.push(stats.totalAmpCoverage);
                    data.push(stats.totalCoverage);
                } else {
                    data.push(stats.originalKilledMutants);
                    data.push(stats.NewMutantKilled)
                }

                dataset["data"] = data;
                dataset["label"] = label;
                dataset["borderColor"] = borderColor;
                dataset["backgroundColor"] = backgroundColor;
                dataset["fill"] = true;
                datasets.push(dataset);
            }

            /*console.log(datasets);*/
            /*Line data*/
            $scope.pitmutantLineData = pitmutantLineData;
            $scope.jacocoLineData = jacocoLineData;
            $scope.dateJacoco = dateJacoco;
            $scope.datePitMutant = datePitMutant;

            /*Bar data*/
            $scope.datasets = datasets;
            $scope.barLabels = barLabels;
        } else {
            /*Default value so that the page does not look weird*/
            $scope.recentRepoData = res.data;
            $scope.mostRecentData = [];
            $scope.mostRecentSelector = "Unknown";
            $scope.pitmutantLineData = [];
            $scope.jacocoLineData = [];
            $scope.date = "";
            $scope.dataset = [];
        }
    });
    $scope.showPitMutant = true;
    $scope.switchTab = function() {
        $scope.showPitMutant = !$scope.showPitMutant;
    }

    $scope.generateRandomColor = function() {
        return '#'+(Math.random()*0xFFFFFF<<0).toString(16);
    }

    /*Graphs*/
    $scope.addDonutGraph = function(elemId, donutdata, title, label) {
        new Chart(document.getElementById(elemId), {
            type: "doughnut",
            data: {
                datasets: [{
                    data: donutdata,
                    backgroundColor: [
                        "#007bff",
                        "#28a745",
                        "#e53935",
                        "#c3e6cb",
                        "#dc3545",
                        "#6c757d"
                    ]
                }],
                // These labels appear in the legend and in the tooltips when hovering different arcs
                labels: label
            },
            responsive: true,
            maintainAspectRatio: true,
            options: {
                legend: {
                    position: 'bottom'
                },
                title: {
                    display: true,
                    text: title
                }
            }
        });
    };

    $scope.addLineGraph = function(elemId, lineData, label, nameLabel, color) {
        new Chart(document.getElementById(elemId), {
            type: "line",
            data: {
                labels: label,
                datasets: [{
                    data: lineData,
                    label: nameLabel,
                    borderColor: color,
                    backgroundColor: color,
                    fill: true
                }]
            },
            responsive: true,
            maintainAspectRatio: true,
            options: {
                scales: {
                    xAxes: [{
                        scaleLabel: {
                            display: true,
                            labelString: 'Date'
                        },
                        ticks: {
                            autoSkip: true,
                            maxTicksLimit: 10
                        }
                    }]
                },
                title: {
                    display: true
                },
                layout: {
                    padding: 0,
                    margin: 0
                }
            }
        });
    }

    $scope.addBarGraph = function(elemId,datasets,labels) {
        console.log(datasets);
        console.log(labels);
        new Chart(document.getElementById(elemId), {
            type: "bar",
            data: {
                labels: labels,
                datasets: datasets
            },
            responsive: true,
            maintainAspectRatio: true,
            options: {
                scales: {
                    xAxes: [{
                        stacked: true
                    }],
                    yAxes: [{
                        stacked: true
                    }]
                }
            }
        });
    }
});