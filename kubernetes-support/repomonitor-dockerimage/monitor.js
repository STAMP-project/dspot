
var express = require('express');
var bodyParser = require('body-parser')
var app = express();

const crypto = require('crypto');
const stompit = require('stompit');
const port = 30050;   // Default 30050

app.use(bodyParser.json({limit: '0.1mb', type: 'application/json',parameterLimit: 500}));
app.use(bodyParser.urlencoded({extended: true}));


/**
 * This function is used to generate the requester's signature for comparing. 
 * It takes the body of the request and the associated secret stored in our database
 * To generate a sha1 signature. This will be used to compare with the sha1 accompanied in 
 * the request body. 
 *
 * @param body the body of github webhook request.
 * @return sha1 signature 
 */
const createComparisonSignature = (body,gitSecret) => {
  const hmac = crypto.createHmac('sha1', gitSecret);
  const self_signature = hmac.update(JSON.stringify(body)).digest('hex');
  return `sha1=${self_signature}`; // shape in GitHub header
}

/**
 * This function is to fast compare signatures instead of === to prevent a timing side-channel attack
 * @param signature our signature stored in the database 
 * @param comparison_signature the requester signature to compair with our. 
                               this is calculated by function createComparisonSignature.
 */
const compareSignatures = (signature, comparison_signature) => {
  const source = Buffer.from(signature);
  const comparison = Buffer.from(comparison_signature);
  return crypto.timingSafeEqual(source, comparison); // constant time comparison
}

/**
 * Verify the github request by compareing SHA1 signature. If signature unmatched then
 * send back a comment with status code 401.
 *
 * @param req request payload 
 * @param res response payload
 * @return the response echoed back to the requester
 */
const verifyGithubPayload = (req, res,gitSecret) => {
  const { headers, body } = req;
  console.log("Verifying")
  const signature = headers['x-hub-signature'];
  const comparison_signature = createComparisonSignature(body,gitSecret);
  if (signature==undefined) {
    return res.status(401).send('No secret provided on github')
  }
  if (!compareSignatures(signature, comparison_signature)) {
    console.log("Mismatched signatures")
    return res.status(401).send('Mismatched signatures');
  }
  console.log("Authorized")
  return res.send("Authorized\n\n" + req.body);
}

/**
 * This handle requests and response of github webhook POST. This will check if the
 * requester is authorized or not. If authorized the slug will be forward to
 * the Activemq queue of the scanner to initialize repairnator's processes. 
 */
app.post('/', function(request, response){
    console.log("AN HTTP POST has arrived")
    var reqslug = ""
    var status = ""
    var authorName = ""

    try {
      reqslug = request.body.repository["full_name"];
      authorName = request.body.commit.commit.author.name;
      status = request.body.state;
    }catch(error) {
    }
    // Connect to mongodb
    // Will only activate if the build status is failure reported by travis.
    if (status == "success" && authorName != process.env.GITHUB_USERNAME) {
      const MongoClient = require('mongodb').MongoClient;
      const assert = require('assert');
      const MONGODB_HOST = process.env.MONGODB_HOST;
      // Database Name
      const dbName = process.env.MONGODB_NAME;
      MongoClient.connect(MONGODB_HOST,{ useNewUrlParser: true }, function(err, client) {
        assert.equal(null, err);
        const db = client.db(dbName);
        // This fetch the secret associated with the slug and check it
        query = {}
        query["slugName"] = reqslug;
        db.collection('gitSecrets').find(query).toArray(function(err, result) {
          if(result.length==0){
            console.log("Invalid request: Unregistered user for slug " + reqslug)
            return response.status(400).send('Invalid request: Unregistered user for slug ' + reqslug);
          }else{
            verifyGithubPayload(request,response,result[0]['gitSecret'])

            // The request is authorized. The slug of the requester will be sent.
            console.log("Sending scan request for slug " + reqslug)
            stompit.connect({ host: 'activemq', port: 61613 }, (err, client) => {
              const frame = client.send({ destination: process.env.QUEUE_NAME });
              frame.write(reqslug);
              frame.end();
              client.disconnect();
            });
          }
        });
        client.close();
      });
    }
    else{
      response.status(200).send('Webhook ignored, only activate upon success travis build and the committer is not Dspot');
    }
    //console.log(createComparisonSignature(request.body));
  });

app.listen(port, () => console.log(`Listening on port ${port}!`))   
