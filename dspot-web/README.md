# Introduction

This is a prototype web interface of Dspot, inspired from [CommitGuru](http://commit.guru/).

Author: Henry Luong

## Screenshots

TODO add screenshots and explanation.

## Running

```
cd dspot-web
npm install --no-optional
npm run-script start
```

## Implementation

It is built by using MEAN (Mongodb,Express,AngularJS and Nodejs) stack, therefore it will be fetching data from database Mongodb and dynamically represent the data on the templated webpages with AngularJS for the Front-End part. Routing on the Back-End will be done by Nodejs and Expressjs to handle requests from the client and communication with the database.
MongoDB will be saving the state ("error","pending","old","success") of the request for REST. Mail will be sent using gmail when done running.

