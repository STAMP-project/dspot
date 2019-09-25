FROM maven:3.6.1-jdk-8

# Download the jar file from dspot releases
ADD https://github.com/STAMP-project/dspot/releases/download/dspot-2.2.0/dspot-2.2.0-jar-with-dependencies.jar /root/dspot.jar 

# Add customisable Maven settings file.
# It can be used, for instance, to pass corporate proxy settings,
# otherwise download dependencies from Maven repositories will fail
COPY settings.xml /root/.m2/

COPY Dpipeline_worker.py /root/

RUN apt-get update
RUN apt-get install cloc -y
RUN apt-get install python-pip -y
RUN pip install stomp.py gitpython travispy pymongo

# this is the run option for dspot allowing different dspot run setup.
# Down here is a variant when running jar and a variant when running with maven.
# Both should be filled out and functionally be identical.  
# If nothing specify then it will just run as "java -jar JAR -p PROPERTIES_FILE",
# which is the minimal required. If value is "-p JacocoCoverageSelector", then
# the final command is "java -jar JAR -p PROPERTIES_FILE -p JacocoCoverageSelector".
# The identical options for maven plugin is "-Dtest-criterion=JacocoCoverageSelector"
# Otherwise as basic as "mvn eu.stamp-project:dspot-maven:amplify-unit-tests".

ENV RUN_OPTIONS_JAR=
ENV RUN_OPTIONS_MAVEN=
ENV RUN_TIMEOUT=10
# number of hours before connection is renewed (mean for Kubernetes).
ENV RECONNECT_TIME=24 
## If 1 then it will expect receiving repo slug and branch name instead. Otherwise it will be build id.
ENV RESTFUL=1
ENV SLUG_MODE=1

# SMTP , for emailing result to the user when finish.
ENV SMTP_ADDRESS=
ENV SMTP_PASSWORD=
ENV SMTP_HOST=
ENV SMTP_PORT=
ENV SMTP_AUTH=1
ENV SMTP_TLS=1

# RESTFUL should be 1 if runing with dspot.
ENV MONGO_URL=mongodb://localhost:27017
ENV MONGO_DBNAME=Dspot
ENV MONGO_COLNAME=AmpRecords

## Optional for pushing to the repo related to the push url, Also can be specified later.
ENV GITHUB_OAUTH=
ENV GITHUB_USEREMAIL=
ENV PUSH_URL=

## Fixed values no need to change
ENV ACTIVEMQ_HOST=localhost
ENV ACTIVEMQ_QUEUE=/queue/Dpipeline
ENV GITHUB_USERNAME=dspot

WORKDIR /root/

ENTRYPOINT ["python","Dpipeline_worker.py"]
