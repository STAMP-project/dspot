FROM node:8

WORKDIR /usr/src/app

COPY package*.json ./

RUN npm install --no-optional

COPY . .

env MONGODB_HOST=
env MONGODB_NAME=
env MONGODB_COLNAME=
env ACTIVEMQ_QUEUENAME=
env ACTIVEMQ_HOSTNAME=
env ACTIVEMQ_PORT=

EXPOSE 3000

ENTRYPOINT npm run-script start
