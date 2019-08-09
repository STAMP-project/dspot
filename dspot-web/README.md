# Introduction

This is a prototype web interface of Dspot, inspired from [CommitGuru](http://commit.guru/).

Prototype demo link: <http://dspot.kth-assert.net/>

Author: Henry Luong

## Running

```
cd dspot-web
npm install --no-optional
npm run-script start
```

Then page can be accessed at localhost:3000.


## Screenshots

### Front page

The front page enables to submit a new repo.

![Pic 1](https://github.com/Tailp/dspot/blob/web/dspot-web/screenshots/pic1.png)

### All amplified repos page

One can browse the list of successfully amplified projects.

![Pic 3](https://github.com/Tailp/dspot/blob/web/dspot-web/screenshots/pic2.png)

### Demo submission

A repo has been submitted

![Pic 4](https://github.com/Tailp/dspot/blob/web/dspot-web/screenshots/pic3.png)

When done, there is one page per repo to display details

![Pic 6](https://github.com/Tailp/dspot/blob/web/dspot-web/screenshots/pic4.png)

## Implementation

It is built by using MEAN (Mongodb,Express,AngularJS and Nodejs) stack, therefore it will be fetching data from database Mongodb and dynamically represent the data on the templated webpages with AngularJS for the Front-End part. Routing on the Back-End will be done by Nodejs and Expressjs to handle requests from the client and communication with the database.
MongoDB will be saving the state ("error","pending","old","success") of the request for REST. Mail will be sent using gmail when done running.

