# Introduction
The design for Dspot on Kubernetes(K8s) is meant for deploying in parallel with repairnator as the initial goal(Check out this [fork](https://github.com/gluckzhang/repairnator/tree/kubernetes/repairnator/docker-images/kubernetes) and [issue](https://github.com/Spirals-Team/repairnator/issues/813) for more detais), but it can also be deployed completely alone without any side effects. The idea is given a Travis build id from ActiveMQ queue server possiblly submitted from a build scanner like repairnator scanner, then dspot will first clone the repo and check the top most level pomfile of the repo and see if the project support dspot. If it does then we run with the provided configuration in the pom file for Dspot, otherwise we will try provide some basic services by autoconfiguring dspot.properties for the repo and run it with some basic amplifications like only generating assertions for the main tests (not the tests in resources). This way we will still be acquiring some data for research or bug detection even though the Git project does not use Dspot. Either cases will be submit output files on Mongodb and also commit to a new branch on Github if enough information are provided for in the dspot-pipeline.yaml file before deploying.

# To deploy Dspot on Kubenetes

## Setup mongodb and ActiveMQ
First set up mongodb on kubernetes with a persistent memory volume(PVC)(This mean that even if you erase the deployment and redeploy nothing will be lost, the database would look the same)

Then create the mongodb deployemnt using mongodb.yaml in the k8s-mongodb folder .

* kubectl create -f k8s-mongodb/mongodb.yaml

Try it out
* kubectl get pods 
Our mongodb pod should be named like "mongo-controller-XXXXX" then we can get into the pod 
* kubectl exec -it mongo-controller-XXXXX bash
* mongo

Now deploy ActiveMQ for effective queue managements of jobs to the pipeline and scanner. First apply the yaml file inside the queue-for-buildids folder
* kubectl create -f /queue-for-buildids/activemq.yaml
To access to the web interface
* kubectl get pods 
It should look like activemq-XXXXXXX-XXXXX. Then 
* kubectl exec -it activemq-XXXXXXX-XXXXX bash
To expose to localhost for webinterface access and also publishing(sending message to queue) outside the cluster. 
* kubectl port-forward activemq-XXXXXXX-XXXXX 8161:8161 61613:61613
Access webinterface with "http://localhost:8161/admin/" in your browser. The default username is "admin" and password is also "admin".

To send message to queue use the publisher.py script in queue-for-buildids folder. Syntax
* python publisher.py -d queue-name message-1 message-2 message3 ... message-N

queue-name is according to the format /queue/name. For instance /queue/scanner will send message to the scanner queue.
You can use this later to manually input build id to the pipeline queue or project to the scanner queue


## Setup Dspotpipeline
Now go to "Dpipeline-dockerimage" folder and directly build and push it to a image registry like docker hub. No change need to be made to the Dockerfile, since we can change or specify enviroment variables in the deployment yaml file later. 

* docker build -t dspot-pipeline:tagname .
* docker tag dspot-pipeline:tagname YOUR_DOCKERHUB_NAME/dspot-pipeline
* docker push YOUR_DOCKERHUB_NAME/dspot-pipeline:tagname

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
* kubectl create -f dspot-pipeline.yaml
* kubectl get pods

We should see a pod called "dspot-pipeline-XXXXXXXX-XXXX" running. By visiting our queue server "http://localhost:8161/admin/" then navigate to queue, we should also see that there are one consummer listening to the queue Dpipeline.

To see some actions, there are 2 build ids related to this [repo](https://github.com/Tailp/travisplay) for trying out listed below

* 551693890 : without dspot plugin in pom file (this is the master branch in the repo mentioned). Travis link: https://travis-ci.org/Tailp/travisplay/builds/551693890
* 551741145 : with dspot plugin (with_dspot branch). Travis link: https://travis-ci.org/Tailp/travisplay/builds/551741145

Now open a new terminal then we need to log the dspot-pipeline in realtime for its output
* kubectl logs -f dspot-pipeline-XXXXXXXX-XXXX

Since we have port forwarded previously for ActiveMQ server we now use the script "publisher.py" in "queue-for-buildids" to send these build ids to the queue all the dspot-pipelines listen to.
* python publisher.py -d /queue/Dpipeline 551693890 551741145

To this point you should see some reaction from the terminal logging the dspot pod announcing like a new build id has arrived. 

To scale dspot, for instance
* kubectl scale --replicas=3 -f dspot-pipeline.yal
This will create 2 more dspot-pipelines. The ActiveMQ Dpipeline queue should now have 3 consumers.
Note that the build id from queue will only be pulled one at a time by any free dspot-pipeline.


