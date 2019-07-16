#!/usr/bin/env python
# encoding=utf-8

# dependencies to pip install
# travispy , gitPython, stomp
import time
import os
import tempfile
import signal
import logging
import ConfigParser
import stomp
import git
import datetime
import requests
import StringIO
from travispy import TravisPy


# Start ActiveMQ listener
host = os.getenv("ACTIVEMQ_HOST") or "activemq"
port = 61613
# Uncomment next line if you do not have Kube-DNS working.
# host = os.getenv("REDIS_SERVICE_HOST")
QUEUE_NAME = os.getenv("ACTIVEMQ_QUEUE") or '/queue/Dscanner'
LISTENER_NAME = 'BuildIdListener'
TIMEOUT = 360  # timeout for executing submissions

# Validating travis buildid , for instance see if it's a java project
def validate_id(buildid):
	t = TravisPy()
	build = t.build(message)
	repoid = build['repository_id']
	repobranch = build.commit.branch
	repo = t.repo(repoid)


# fetch a number of most recent buildids and return the latest successful builds
def fetch_latest_successful_build(reposlug,number_of_buildids):
	t = TravisPy()
	build = t.build(message)
	repoid = build['repository_id']
	repobranch = build.commit.branch
	repo = t.repo(repoid)
	reposlug = repo.slug

# Try cloning the branch
def git_branch_exist(url,repobranch,reposlug):
	try:
		repo = git.Repo.clone_from(url, 'clonedrepo', branch=repobranch)
		return True
	except:
		logging.warning("Branch " + str(repobranch) + " or the repo " + str(reposlug) + " itself does not exist anymore")
		return False

class ScannerIdListener(object):
    def __init__(self, conn_listen):
        self.conn_listen = conn_listen
        self.count = 0
        self.start = time.time()
    def print_output(self, type, data):
        logging.warning(type)
        for line in data:
            logging.warning(line)
    def on_message(self, headers, message):
		logging.warning("New Repo information arrived: %s" % message)
		self.conn_listen.ack(headers.get('message-id'),headers.get('subscription'))
		# Given a repo slug. it will pick out the latest passed builds for each branch.
		t = TravisPy()
		builds = t.builds(slug=message)
		url = 'https://github.com/' + message + '.git'
		#  print(builds)
		dictionary = dict();
		for b in builds:
			os.system("rm -rf clonedrepo")

			# if the top most build of the branch passed and the branch still exist we send
			# it over to dspot pipeline for amplification
			if b.passed and not dictionary.get(b.commit.branch) and git_branch_exist(url,b.commit.branch,message) and b.config['language'] == "java":
				conn_listen.send("/queue/Dpipeline", str(b.id), persistent='true') 
				dictionary[b.commit.branch] = True

conn_listen = stomp.Connection10([(host,port)])
conn_listen.set_listener(LISTENER_NAME, ScannerIdListener(conn_listen))
conn_listen.start()
conn_listen.connect()
conn_listen.subscribe(QUEUE_NAME,ack='client',headers={'activemq.prefetchSize': 1})
logging.warning("Listener connected")

while True:
    time.sleep(3)
conn_listen.disconnect()
