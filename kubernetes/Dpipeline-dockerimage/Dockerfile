FROM maven:3.6.1-jdk-8

# Download the jar file from dspot releases
ADD https://github.com/STAMP-project/dspot/releases/download/dspot-2.1.0/dspot-2.1.0-jar-with-dependencies.jar /root/

COPY Dpipeline_worker.py /root/
COPY dspot.properties /root/

# Can be specified or changed later in the dspot-pipeline.yaml file
ENV MONGODB_HOST=mongodb://mongo:27017
ENV ACTIVEMQ_HOST=

# Optional for pushing to the repo related to the push url
# Also can be specified later.
ENV GITHUB_OAUTH=
ENV GITHUB_USERNAME=
ENV GITHUB_USEREMAIL=
ENV PUSH_URL=

# Fixed values no need to change
ENV ACTIVEMQ_QUEUE=/queue/Dpipeline


RUN apt-get update
RUN apt-get install cloc -y
RUN apt-get install python-pip -y
RUN pip install stomp.py gitpython travispy pymongo



WORKDIR /root/

ENTRYPOINT ["python","Dpipeline_worker.py"]