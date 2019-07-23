exports.get_page = function(req,res,next) {
    var path = ''
    if (req.originalUrl == '/') {
        path = 'pages/index.html'
    }else{
        path = 'pages' + req.originalUrl + '.html'
    }
    res.sendFile(path,{root: __dirname });
}

exports.get_reposTemplatePage = function(req,res,next) {
    console.log("Getting repos template page");
    res.sendFile('pages/reposTemplate.html',{root: __dirname});
}

exports.get_repoInfoData = function(req,res,next) {
    console.log("getting repo " + req.params.RepoName + " data: " + req.params.DataName);
    var query = {};
    query["repoName"] = req.params.RepoName;
    console.log(query)
    if (req.params.DataName == "recent") {
        console.log("getting recent data for the repo");
        fetchData('uniqueRecords',query,undefined,res);
    } 
    else if (req.params.DataName == "all") {
        console.log("getting all data for the repo");
        fetchData('allRecords',query,undefined,res);
    }
}

exports.get_recentReposData = function(req,res,next) {
    console.log("getting most 3 recent repos data");
    /* res.sendFile( "." + req.originalUrl,{root: __dirname }); */
    fetchData('uniqueRecords',{},3,res);
}

exports.get_reposInfoData = function(req,res,next) {
    console.log("getting all unique data");
    fetchData('uniqueRecords',{},undefined,res);
}

function fetchData(colName,query,limit,res) {
    const MongoClient = require('mongodb').MongoClient;
    const assert = require('assert');
    const MONGODB_HOST = "mongodb://localhost:27017"//process.env.MONGODB_HOST;
    // Database Name
    /* query = {};
    query["repoName"] = reqslug; */
    if (query == undefined) {
        query = {};
    }
    const dbName = "Dspot" //process.env.MONGODB_NAME;
    MongoClient.connect(MONGODB_HOST,{ useNewUrlParser: true }, function(err, client) {
        assert.equal(null, err);
        const db = client.db(dbName);
        console.log("Connected to mongo");

        // This fetch the secret associated with the slug and check it
        if (limit != undefined) {
            db.collection(colName).find(query).sort({date:-1}).limit(limit).toArray(function(err, result) {
                if (err) {
                    console.log(err);
                    res.json(err);
                } else {
                    console.log(result);
                    res.json(result);
                }
            });
        } else{
            db.collection(colName).find(query).sort({date:-1}).toArray(function(err, result) {
                if (err) {
                    console.log(err);
                    res.json(err);
                }else {
                    console.log(result);
                    res.json(result);
                }
            });
        }
        client.close();
    });
}