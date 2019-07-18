FROM node:8

WORKDIR /root/

COPY package.json /root/
COPY monitor.js /root/

RUN npm install express body-parser stompit mongodb 

# Can be changed in the repo-monitor.yaml file, but assume by default service of mongodb is called mongo.
ENV MONGODB_HOST=mongodb://mongo:27017

# Fixed values, does not need to be changed
ENV QUEUE_NAME=Dscanner
ENV MONGODB_NAME=githook
ENV GITHUB_USERNAME=dspot

# This expose the port of this monitor server 
EXPOSE 30050

CMD ["nodejs","monitor.js"]