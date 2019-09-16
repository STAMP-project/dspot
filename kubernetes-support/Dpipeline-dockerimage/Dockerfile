FROM maven:3.6.1-jdk-8

# Download the jar file from dspot releases
ADD https://github.com/STAMP-project/dspot/releases/download/dspot-2.1.0/dspot-2.1.0-jar-with-dependencies.jar /root/

# Add customisable Maven settings file.
# It can be used, for instance, to pass corporate proxy settings,
# otherwise download dependencies from Maven repositories will fail
COPY settings.xml /root/.m2/


COPY Dpipeline_worker.py /root/

# Can be specified or changed later in the dspot-pipeline.yaml file
ENV MONGODB_HOST=mongodb://mongo:27017
ENV ACTIVEMQ_HOST=

# Optional for pushing to the repo related to the push url
# Also can be specified later.
ENV GITHUB_OAUTH=
ENV GITHUB_USEREMAIL=
ENV PUSH_URL=

# Fixed values no need to change
ENV ACTIVEMQ_QUEUE=/queue/Dpipeline
ENV GITHUB_USERNAME=dspot

RUN apt-get update
RUN apt-get install cloc -y
RUN apt-get install python-pip -y
RUN pip install stomp.py gitpython travispy pymongo



WORKDIR /root/

ENTRYPOINT ["python","Dpipeline_worker.py"]