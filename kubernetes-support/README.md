# Introduction
The design for dspot on Kubernetes(K8s) is meant for deploying in parallel with repairnator as the initial goal(Check out this [fork](https://github.com/gluckzhang/repairnator/tree/kubernetes/repairnator/docker-images/kubernetes) and [issue](https://github.com/Spirals-Team/repairnator/issues/813) for more detais), but it can also be deployed completely alone without any side effects. 

The idea is given a Travis build id from ActiveMQ dspot-pipeline queue server possiblly submitted from a build scanner like repairnator scanner or manually by a developer, then dspot will first clone the repo and check the top most level pom file of the repo and see if the project support dspot. If it does then we run with the provided configuration in the pom file for dspot, otherwise we will try provide some basic services by autoconfiguring dspot.properties for the repo and run it with some basic amplifications like only generating assertions for the main tests (not the tests in resources). This way we will still be acquiring some data for research or bug detection even though the Git project does not use dspot. Either cases will be submit output files on Mongodb and also commit to a new branch on Github if enough information are provided for in the dspot-pipeline.yaml file before deploying. 

Other than submit a Travis build id directly to the dspot pipeline, a repo slug such as "Tailp/travisplay" can also be submitted to the scanner instead. The scanner will scan the branches on the repo and submit the most recent build id only from those branches with "passed" travis status to the dspot-pipeline queue for amplication and ofcource the scanner will first check if the id belong a java project repo otherwise it will be ignored. 

This design also provides git webhook service described in details at the end of this README. Currently, This server which monitor git webhook is configured to be sending reposlug to the dspot-scanner only when the build is success travis badge status for further amplication of the passed build's tests. Then, the scanner will scan as described previously and submit the build ids to the dspot-pipeline for amplifying.
# To deploy dspot on Kubenetes
## Setup mongodb
Note: if you already have a mongodb outside kubernetes then you can map it as an external service described in [this link](ttps://cloud.google.com/blog/products/gcp/kubernetes-best-practices-mapping-external-services) and this part can be skipped , just remember to name the service as mongo for k8s dns look-up then nothing need to be changed.

First set up mongodb on kubernetes with a persistent memory volume(PVC) called mongo-disk(This mean that even if you erase the deployment and redeploy nothing will be lost, the database would look the same)

```
  gcloud compute disks create --size=200GB --zone=$ZONE mongo-disk
```
Then create the mongodb deployment using mongodb.yaml in the k8s-mongodb folder .

```
  kubectl create -f k8s-mongodb/mongodb.yaml
```
Try it out

```
  kubectl get pods 
```

Our mongodb pod should be named like "mongo-controller-XXXXX" then we can get into the pod 

```
  kubectl exec -it mongo-controller-XXXXX bash
  mongo
```

## Setup ActiveMQ

Now deploy ActiveMQ for effective queue managements of jobs to the pipeline and scanner. First apply the yaml file inside the queue-for-buildids folder
```
  kubectl create -f /queue-for-buildids/activemq.yaml
```

To access to the web interface
```
  kubectl get pods 
```

It should look like activemq-XXXXXXX-XXXXX. Then 
```
  kubectl exec -it activemq-XXXXXXX-XXXXX bash
```

To expose to localhost for webinterface access and also publishing(sending message to queue) outside the cluster. 
```
  kubectl port-forward activemq-XXXXXXX-XXXXX 8161:8161 61613:61613
```

Access webinterface with "http://localhost:8161/admin/" in your browser. The default username is "admin" and password is also "admin".

To send message to queue use the publisher.py script in queue-for-buildids folder. Syntax
```
  python publisher.py -d queue-name message-1 message-2 message3 ... message-N
```

queue-name is according to the format /queue/name. For instance /queue/scanner will send message to the scanner queue.
You can use this later to manually input build id to the pipeline queue or project to the scanner queue


## Setup Dspot pipeline

Now go to "Dpipeline-dockerimage" folder and directly build and push it to a image registry like docker hub. No change need to be made to the Dockerfile, since we can change or specify enviroment variables in the deployment yaml file later. 
```
  docker build -t dspot-pipeline:tagname .
  docker tag dspot-pipeline:tagname YOUR_DOCKERHUB_NAME/dspot-pipeline
  docker push YOUR_DOCKERHUB_NAME/dspot-pipeline:tagname
```

Now in the "Dspot-yamlfile" folder, we filled in the name of the pushed image in "dspot-pipeline.yaml" . Look for the part as shown down here
```
...
	containers:
      - name: dspot-pipeline
        image: YOUR_DOCKERHUB_NAME/repairnator-scanner:tagname
...
```

Also, the pipeline can also commit back to some github repo by specifying 

* PUSH_URL : the https github url like for instance "https://github.com/STAMP-project/dspot/"
* GIT_USERNAME: It's just the committer's name so it can be anything but not empty, maybe "Dspot" is a good name.
* GIT_EMAIL: just a regular email address.
* GITHUB_OAUTH: a git token elligible for pushing to the repo specified by the PUSH_URL

Now we can deploy by applying the yaml file.
```
  kubectl create -f dspot-pipeline.yaml
  kubectl get pods
```

We should see a pod called "dspot-pipeline-XXXXXXXX-XXXX" running. By visiting our queue server "http://localhost:8161/admin/" then navigate to queue, we should also see that there are one consummer listening to the queue Dpipeline.

To see some actions, there are 2 build ids related to this [repo](https://github.com/Tailp/travisplay) for trying out listed below
```

```
* 560279603 : without dspot plugin in pom file (this is the master branch in the repo mentioned). [Travis link](https://travis-ci.org/Tailp/travisplay/builds/560279603)
* 560288647 : with dspot plugin (with_dspot branch). [Travis link](https://travis-ci.org/Tailp/travisplay/builds/560288647)

Now open a new terminal then we need to log the dspot-pipeline in realtime for its output
```
  kubectl logs -f dspot-pipeline-XXXXXXXX-XXXX
```

Since we have port forwarded previously for ActiveMQ server we now use the script "publisher.py" in "queue-for-buildids" to send these build ids to the queue all the dspot-pipelines listen to.
```
  python publisher.py -d /queue/Dpipeline 560279603 560288647
```

To this point you should see some reaction from the terminal logging the dspot pod announcing like a new build id has arrived. 

To scale dspot, for instance
```
  kubectl scale --replicas=3 -f dspot-pipeline.yaml
```

This will create 2 more dspot-pipelines. The ActiveMQ Dpipeline queue should now have 3 consumers.
Note that the build id from queue will only be pulled one at a time by any free dspot-pipeline.

## Setup Dspot scanner

Similar to the pipeline but simpler, we just need to go into "Dpipeline-dockerimage" directly build and push the docker image with out any changes 
```
  docker build -t dspot-scanner:tagname .
  docker tag dspot-pipeline:tagname YOUR_DOCKERHUB_NAME/dspot-scanner
  docker push YOUR_DOCKERHUB_NAME/dspot-scanner:tagname
```

Then again in the "Dspot-yamlfile" folder, filled in the "YOUR_DOCKERHUB_NAME/dspot-scanner:tagname" in the "dspot-scanner.yaml" then deploy it
```
  kubectl create -f dspot-pipeline.yaml
  kubectl get pods
```


to monitor this use 
```
  kubectl logs -f dspot-scanner-XXXXXXXX-XXXX
```
  

and scale this with 
```
  kubectl scale --replicas=3 -f dspot-scanner.yaml
```

## Setup repo monitor
The idea with this monitor is that it first check any incoming github webhook if it's authorized to be scanned or not before sending a message to the scanner queue to scan the repo mentioned in the github-payload. To security-check incomming message it will fetch credentials information stored in the database mongodb deployed previously , then it doublechecks it will the accompied hash SHA1 also in the incomming webhook payload. This means that the one sended need to be registered in MongoDB to be able to use this feature. An example to add a register a user is first go to the database by 
```
  kubectl get pods
  kubectl exec -it activemq-XXXXXXX-XXXXX bash
  mongo
```

Then we need to create a database called "githook" (note that it must be named like this according to the script if you don't plan to change it) and insert some information into a collection called "gitSecrets". These are done by these steps below.
```
  use githook
  db.gitSecrets.insert({"slugName" : "Spirals-Team/repairnator", "gitSecret" : "123456789"})
```

Next is deploying the repo monitor itself on K8s. First you need to go to the "repomonitor-dockerimage" to build and push an image to docker just like how we did with the scanner. Then there is also a yaml file in "repairnator-deployment-yamlfiles" folder for deploying and also here we need to replace in the "repo-monitor.yaml" file the name of your image you tagged before when pushing to docker (format DOCKER_USER_NAME/IMAGE_NAME). Then apply the yaml
```
  kubectl create -f repo-monitor.yaml
``` 
  

This will create a pod named "repo-monitor-XXXXXXXX-XXXX" together with a service creating a Loadbalancer with an external address for github. We need to fetch this address by this command 
```
  kubectl get svc 
```

and look for a service named "repo-monitor" then take its external IP (it usually takes a while before the LoadBalancer finish setting up).
Then go to your repo github page -> setting -> webhook -> add new webhook and then add the Payload URL" as "http://LoadBalancer_IP/" shown in the picture below, "Content type" is set as "application/json" then secret is the "gitSecret" previously added to the database "123456789" (Note: this should be a hash of some kind like a MD5 hash to be safer) then also select "Let me select individual events" to scroll down a bit to select "Statuses". The reason we choose only sending status update is because the server is set to only activate the pipeline when the build status is failure.
![repo-monitor-picture](repo-monitor-example.png)

Now back to the terminal. We can check the logs of our repo-monitor deployed earlier by first 
```
  kubectl logs -f repo-monitor-XXXXXXXX-XXXX
```


You should be able to see something like
```
Listening on port 30050!
Authorized
Sending scan request for slug Spirals-Team/repairnator
```
Same logic apply for any repo want to be monitored. First register the slug in database with a secret then add a webhook like mentioned before then we are done. 

## Delete deployment
Provided with every yaml files mentioned in this readme (all of them are in "repairnator-deployment-yamlfile" folder), call 
```
  kubectl delete -f "yamlfile" 
```
To remove each of them. For instance kubectl delete -f repairnator-scanner.yaml to remove the scanners. 