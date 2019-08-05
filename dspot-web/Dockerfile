FROM node:10

WORKDIR /usr/src/app

COPY package*.json ./

RUN npm install

# If left empty this will run with default values
ENV NODE_VERSION=8.16.0
# Host url to mongodb, default: "mongodb://localhost:27017"
ENV MONGODB_HOST=
# Database name, default: Dspot
ENV MONGODB_NAME=
# Collection name, default: AmpRecords            
ENV MONGODB_COLNAME=
# Hostname to activemq server, default: localhost
ENV ACTIVEMQ_HOSTNAME=
# Port for activemq server, default: 61613
ENV ACTIVEMQ_PORT=
# Port for web app, default: 3000, also need change EXPOSE.
ENV PORT=

COPY . .

EXPOSE 3000

CMD npm run-script start
