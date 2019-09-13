var urlPath = window.location.pathname.replace("/repo", "");
var colors = ["#007bff", "#28a745", "#e53935", "#c3e6cb", "#dc3545", "#6c757d"];
var repoinfoapp = angular.module("repoInfoApp", []);
repoinfoapp.controller("repoInfoCtr", function($scope, $http) {
    $http.get("/repodata" + urlPath).then(function(res) {
        /*variables */
        var dateJacoco = [];
        var date = [];
        var jacocoLineData = [];
        var pitmutantLineData = [];
        /*Extract data*/
        if (res.data.length > 0) {
            /*Get most recent total data for pie graph*/
            if (res.data[0].AmpOptions['test-criterion'] == "JacocoCoverageSelector") {
                $scope.mostRecentData = [res.data[0].AmpResult.TotalResult.totalInitialCoverage,res.data[0].AmpResult.TotalResult.totalAmpCoverage];
                $scope.mostRecentSelector = "JacocoCoverageSelector";
                $scope.donutLabel = ["totalInitialCoverage","totalAmpCoverage"]
                $scope.donutLabelColor = ["#6c757d","#c3e6cb"]
            } else {
                $scope.mostRecentData = [res.data[0].AmpResult.TotalResult.totalOriginalKilledMutants,res.data[0].AmpResult.TotalResult.totalNewMutantKilled];
                $scope.mostRecentSelector = "PitmutantScoreSelector";
                $scope.donutLabel = ["totalOriginalKilledMutants","totalNewMutantKilled"]
                $scope.donutLabelColor = ["#007bff","#28a745"]
            }

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
                var label = key.split("/D/").pop();
                if ( label != "TotalResult") {
                    var dataset = {};
                    var data = [];
                    var stats = res.data[0].AmpResult[key];
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
            }

            /*Bar data*/
            $scope.datasets = datasets;
            $scope.barLabels = barLabels;

            var pitmutantLineData1 = [];
            var pitmutantLineData2 = [];
            var datePitMutant = [];
            /*Get line data*/
            for (var i=0 ; i < res.data.length; i++) {
                /*JacococCoverage*/
                if (res.data[i].AmpOptions['test-criterion'] == "JacocoCoverageSelector") {
                    var val1 = parseInt(res.data[i].AmpResult.TotalResult.totalAmpCoverage);
                    var val2 = parseInt(res.data[i].AmpResult.TotalResult.totalInitialCoverage);
                    jacocoLineData.unshift(Math.round(((val1 / val2) - 1)*100)/100);
                    dateJacoco.unshift(res.data[i].Date);
                } else {
                    /*PitmutantScore*/
                    var val1 = parseInt(res.data[i].AmpResult.TotalResult.totalNewMutantKilled);
                    var val2 = parseInt(res.data[i].AmpResult.TotalResult.totalOriginalKilledMutants);
                    pitmutantLineData1.unshift(val1);
                    pitmutantLineData2.unshift(val2);
                    datePitMutant.unshift(res.data[i].Date);
                }
            }
            pitmutantLineData.unshift(pitmutantLineData1);
            pitmutantLineData.unshift(pitmutantLineData2);

            /*Line data*/
            $scope.pitmutantLineData = pitmutantLineData;
            $scope.jacocoLineData = jacocoLineData;
            $scope.dateJacoco = dateJacoco;
            $scope.datePitMutant = datePitMutant;
            $scope.lineNameLabels = ["totalOriginalKilledMutants","totalNewMutantKilled"];
            $scope.lineColors = ["#007bff","#28a745"]

        } else {
            /*Default value so that the page does not look weird*/
            $scope.recentRepoData = res.data;
            $scope.mostRecentData = [];
            $scope.mostRecentSelector = "Unknown";
            $scope.jacocoLineData = [[],[]];
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
    $scope.addDonutGraph = function(elemId, donutdata, title, label,color) {
        new Chart(document.getElementById(elemId), {
            type: "doughnut",
            data: {
                datasets: [{
                    data: donutdata,
                    backgroundColor: color
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

    $scope.addLineGraphForJacoco = function(elemId, lineData, label, nameLabel, color) {
        if (lineData == undefined) {
            lineData = [[""],[""]];
        }
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
                            maxTicksLimit: 5
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

    $scope.addLineGraphForPitMutant = function(elemId,lineData,lineNameLabels,lineColors,pointLabels) {
        new Chart(document.getElementById(elemId), {
            type: "line",
            data: {
                labels: pointLabels,
                datasets: [{
                    data: lineData[0],
                    label: lineNameLabels[0],
                    borderColor: lineColors[0],
                    backgroundColor: lineColors[0],
                    fill: false},
                    {
                    data: lineData[1],
                    label: lineNameLabels[1],
                    borderColor: lineColors[1],
                    backgroundColor: lineColors[1],
                    fill: false}
                ],
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
                            maxTicksLimit: 5
                        }
                    }],
                    yAxes: [{
                        ticks: {
                            beginAtZero: true
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
    $scope.ngGridFIx = function() {
        window.dispatchEvent(new Event('resize'));
    }
});
