FROM python:2

COPY Dscanner_worker.py /root/

RUN apt-get update
RUN apt-get install cloc -y
RUN apt-get install python-pip -y
RUN apt-get install curl
RUN pip install stomp.py gitpython travispy

# Fixed values no need to change
ENV ACTIVEMQ_QUEUE=/queue/Dscanner


WORKDIR /root/

ENTRYPOINT ["python","Dscanner_worker.py"]