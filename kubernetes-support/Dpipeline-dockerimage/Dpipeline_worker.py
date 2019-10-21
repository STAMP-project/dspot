#!/usr/bin/env python
# encoding=utf-8

# dependencies to pip install
# travispy , gitPython, stomp, pymongo
from travispy import TravisPy
from pymongo import MongoClient
from xml.etree import ElementTree
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
import smtplib
import time
import os
import tempfile
import subprocess
import signal
import logging
import StringIO
import ConfigParser
import stomp
import git
import datetime
import requests
import StringIO
import smtplib
import time

RUN_OPTIONS_JAR = os.getenv("RUN_OPTIONS_JAR") or ''
SLUG_MODE = True
RESTFUL = True
SMTP_AUTH = True
SMTP_TLS = True
if os.getenv("RESTFUL") != "1":
    RESTFUL = False

if os.getenv("SLUG_MODE") != "1":
    SLUG_MODE = False

if os.getenv("SMTP_AUTH") != "1":
    SMTP_AUTH = False

if os.getenv("SMTP_TLS") != "1":
    SMTP_TLS = False

RUN_TIMEOUT = os.getenv("RUN_TIMEOUT") or 10  # minutes
RECONNECT_TIME = os.getenv("RECONNECT_TIME") or 24 # hours, default 24 hours then reconnect again.
RECONNECT_TIME = int(RECONNECT_TIME)*3600 # Make it to hours.

# Preset will be removed later
if SLUG_MODE:
    logging.warn("RUNNING IN SLUG_MODE FOR DSPOT-WEB")

# Connect to Mongodb, Check if connection is up
MONGO_URL = os.getenv('MONGO_URL') or 'mongodb://localhost:27017/'
MONGO_DBNAME = os.getenv('MONGO_DBNAME') or 'Dspot'
MONGO_COLNAME = os.getenv('MONGO_COLNAME') or 'AmpRecords'
client = MongoClient(MONGO_URL,15)
db = client[MONGO_DBNAME]
coll = db[MONGO_COLNAME]

# Gmail smtp
SMTP_ADDRESS= os.getenv('SMTP_ADDRESS') or "foo@gmail.com"
SMTP_PASSWORD= os.getenv('SMTP_PASSWORD') or "unknown"
SMTP_HOST = os.getenv('SMTP_HOST') or "smtp.gmail.com"
SMTP_PORT = os.getenv('SMTP_PORT') or "587"

RUN_OPTIONS_JAR = RUN_OPTIONS_JAR + " --collector MongodbCollector " + " --mongo-url " + MONGO_URL + " --mongo-dbname " + MONGO_DBNAME + " --mongo-colname " + MONGO_COLNAME + " --smtp-username " + SMTP_ADDRESS + " --smtp-password " + SMTP_PASSWORD
RUN_OPTIONS_JAR = RUN_OPTIONS_JAR + " --smtp-host " + SMTP_HOST + " --smtp-port " + SMTP_PORT

if RESTFUL:
    RUN_OPTIONS_JAR = RUN_OPTIONS_JAR + " --restful "
if SMTP_AUTH:
    RUN_OPTIONS_JAR = RUN_OPTIONS_JAR + " --smtp-auth "
if SMTP_TLS:
    RUN_OPTIONS_JAR = RUN_OPTIONS_JAR + " --smtp-tls "

logging.warn(RUN_OPTIONS_JAR)
# Check connection
mongo_connected = True
try:
    client.server_info()
except:
    mongo_connected = False
    logging.warn("Python: Failed to connect to mongodb")

# Start ActiveMQ listener
host = os.getenv("ACTIVEMQ_HOST") or "localhost"
port = 61613
# Uncomment next line if you do not have Kube-DNS working.
# host = os.getenv("REDIS_SERVICE_HOST")
QUEUE_NAME = os.getenv("ACTIVEMQ_QUEUE") or '/queue/Dpipeline'
LISTENER_NAME =  "Listener"
TIMEOUT = 360  # timeout for executing submissions

# Send result files over to the user email.
def email_result (message,subject,to_email,files=[]): 
    # Construct basic mail content
    msg = MIMEMultipart()
    msg['From'] = os.getenv("GMAIL_ADDRESS") or "foo@gmail.com"
    msg['To'] = to_email
    msg['Subject'] = subject
    msg.attach(MIMEText(message, 'plain'))

    # Attaching files
    if files != []:
        for file_name in files:
            file_attach = MIMEText(file(file_name).read())
            file_attach.add_header('Content-Disposition', 'attachment', filename=file_name)
            msg.attach(file_attach)

    # Connect to Gmail server and send the mail
    server = smtplib.SMTP('smtp.gmail.com: 587')
    server.starttls()
    # Login Credentials for sending the mail
    server.login(msg['From'], os.getenv("GMAIL_PASSWORD") or "unknown")
    # send the message via the server.
    server.sendmail(msg['From'], msg['To'], msg.as_string())
    server.quit()
    logging.warn("successfully sent email to : " + (msg['To']))

# If we can still find a pending document for this repo then we know that  
# it has been errors during runs. 
def error_during_run (query):
    if coll.find_one(query) == None:
        logging.warn("Everything went smoothly")
        return False       
    return True

# Subprocess running returning the output as a string
def exec_get_output(cmd, val=False, wait=True,workdir="./"):
    p = subprocess.Popen("timeout " + str(RUN_TIMEOUT*60) + " " + cmd, stdout=subprocess.PIPE,stderr=subprocess.STDOUT,shell=val,cwd=workdir)
    out = ""
    start = time.time()
    end = time.time()
    killed = False
    while p.poll() == None:
      line = p.stdout.readline().rstrip().replace("WARNING:root:","")
      out = out + line + "\n"
      logging.warn(line)
      end = time.time()
      if (end - start)/60 > RUN_TIMEOUT and not killed:
        logging.warn("TIMEOUT , RUN EXCEEDED " + str(RUN_TIMEOUT) + " minutes")
        killed = True
        p.kill()
        out = out + "TIMEOUT , RUN EXCEEDED " + str(RUN_TIMEOUT) + " minutes \n"
    return [out,killed]


# Check if enough information is provided to push to Git. 
# configure git if that's the case
def check_endprocess_gitcommit(repo):
    if (os.getenv("GITHUB_USEREMAIL")=='' or os.getenv("GITHUB_USERNAME")=='' or os.getenv("PUSH_URL")==''):
        logging.warn('Not enough Git information provided, no git commit will occur at end process')
        logging.warn('Please double check GITHUB_USEREMAIL , GITHUB_USERNAME and PUSH_URL')
        return False   
    else:

        repo.config_writer().set_value("user","name",os.getenv("GITHUB_USERNAME")).release()
        repo.config_writer().set_value("user","email",os.getenv("GITHUB_USEREMAIL")).release()
        return True

# Check if it supports Dspot
def check_Dspot_supported(POM_FILE):
    tree = ElementTree.parse(POM_FILE)
    root = tree.getroot()
    namespace = root.tag.split('}')[0] + '}'  # Acquire namespace of the pom
    for plugin in root.iter(namespace + 'plugin'):
        groupId = plugin.find(namespace + 'groupId').text
        artifactId = plugin.find(namespace + 'artifactId').text
        # Check if project support dspot and has configured for it
        if (groupId == "eu.stamp-project") and (artifactId == "dspot-maven"):
            return True
    return False

# Find and return the JAVA version specified in maven pom.
def find_JAVA_VERSION(POM_FILE):
    version_dictionary = {''}

    tree = ElementTree.parse(POM_FILE)
    root = tree.getroot()
    namespace = root.tag.split('}')[0] + '}' # Acquire namespace of the pom
    properties = root.find(namespace + 'properties')

    JAVA_VERSION_FOUND = False
    if (properties != None):
        str_to_find = [namespace + 'maven.compiler.source',namespace + 'maven.compiler.release',namespace + 'java.version']
        JAVA_VERSION = ''
        # Look for version in properties first

        for find_string in str_to_find:
            property = properties.find(find_string)
            if property is not None:
                JAVA_VERSION = property.text
                JAVA_VERSION_FOUND = True
                break

        # If not found then look for it in plugins
        if not JAVA_VERSION_FOUND:
            # here means that Java version might not be stored in properties but in plugins instead
            for plugin in root.iter(namespace + 'plugin'):
                text = plugin.find(namespace + 'artifactId').text
                # Check if it's the correct plugin
                if text == ('maven-compiler-plugin'):
                    configuration = plugin.find(namespace + 'configuration')
                    str_to_find = [namespace + 'source',namespace + 'release']
                    for find_string in str_to_find:
                        config_object = configuration.find(find_string)
                        if config_object is not None:
                            JAVA_VERSION = config_object.text
                            JAVA_VERSION_FOUND = True
                            break
                if JAVA_VERSION_FOUND :
                    break

    if not JAVA_VERSION_FOUND:
        logging.warn("No java version found, malconfigured pom ?, assume default as Java 1.8 ")
        return '1.8'
    else:
        return JAVA_VERSION

def report_error(reason,error_output_string,reposlug,repobranch):
    # Construct mongodb query for finding in database later
    query = {}
    query["RepoSlug"] = reposlug
    query["RepoBranch"] = repobranch
    query["State"] = "pending"
    if error_during_run(query):
        email = coll.find_one(query)["Email"]

        #Set state as error.
        coll.update_one(query, {"$set": {"State":"error"}})
        logging.warn("Error during amplifications, set document state as error") 

        #Send error via mail in a file
        files = []
        if error_output_string != "":
            text_file = open("ErrorOutput.txt", "w");
            text_file.write(error_output_string)
            files.append("ErrorOutput.txt")
        subject = "Amplification failed !!"
        email_result(reason,subject,email,files)
        exec_get_output("rm -rf ErrorOutput.txt",True)
        return True;
    return False;

# Run Dspot preconfigured, the project support Dspot
# and has configured in the pom file
# NOTE CURRENTLY THIS IS NOT SUPPORTED YET SINCE MONGODB FEATURE IS NOT MERGED YET.
def run_Dspot_preconfig(reposlug,repobranch,selector):
    # If no dspot.properties found in the project root or no dspot plugin then it does
    # not support Dspot
    basic_opts = "--repo-slug " + reposlug + " --repo-branch " + repobranch + " -s " + selector
    if not (os.path.isfile("clonedrepo/dspot.properties")):
        return False
    logging.warn("PROJECT DOES SUPPORT DSPOT")
    res = exec_get_output('java -jar ../dspot.jar -p dspot.properties ' + basic_opts + RUN_OPTIONS_JAR ,True,False,"./clonedrepo/")
    error_message = ""
    if res[1] == True: 
        error_message = "Process was TIMEOUT, RUN EXCEEDED " + str(RUN_TIMEOUT) + " minutes, currently we don't support heavy projects \n \n --STAMP/Dspot"
    else :
        error_message = "Hi, your amplification has failed. Check the attached output files for more details.\n \n --STAMP/Dspot"

    if report_error(error_message,res[0],reposlug,repobranch):
        logging.warn("Amplification failed, error reported")
    # exec_get_output('rm -rf clonedrepo', True)
    return True # Dspot was preconfigured



# This help providing some basic configurations for projects that does not support Dspot
# Configure the dspot.properties file for each root project module.
# Output with a properties file for using.
def configure(module_name, module_path, root_name, project_path, outputdir,java_version):
    config = ConfigParser.RawConfigParser()
    config.optionxform = str

    config.add_section('SPECS')
    config.set('SPECS', 'project', project_path)
    config.set('SPECS', 'targetModule', module_path)
    config.set('SPECS','src','src/main/java')
    config.set('SPECS','testSrc','src/test/java')
    config.set('SPECS','javaVersion',java_version)
    config.set('SPECS', 'outputDirectory', outputdir)
    logging.warn('start saving file')
    with open('project.properties', 'w') as configfile:
        config.write(configfile)


# This will try to autoconfigure Dspot and provide some basic amplifications
# for projects which do not support dspot
def run_Dspot_autoconfig(reposlug,repobranch,selector):
    basic_opts = "--repo-slug " + reposlug + " --repo-branch " + repobranch + " " + " -s " + selector

    # Find project roots
    # Normal project with only one root
    # Currently only aim for normal one with super pom at top most directory
    roots = exec_get_output(
        'find clonedrepo/ -maxdepth 1 -mindepth 0 -type f -name "pom.xml"', True)[0].rstrip().split('\n')
    # Project with multiroots like repairnator
    if isinstance(roots, str):
        roots = [roots]
    if roots == '':
        logging.warn('Unsupported project structure')
        exit(1)

    # find modules related to root.
    # the first path in the list is always in fact the path to the root.
    # loop through roots
    paths = []
    for root in roots:
        # This need to be executed as shell=True otherwise it will not work
        paths  = (sorted(exec_get_output('mvn -q -f ' + root +
                                            ' --also-make exec:exec -Dexec.executable="pwd"', True)[0].rstrip().split('\n')))
    # the first one is always the root at relative path '.'
    del paths[0]
    root_path = "clonedrepo"
    root_name = ""
    project_path = 'clonedrepo'
    # default values of module if there are no modules(a.k.a not a multimodules project)
    module_path = '.'
    module_name = ''
    # ignore the first index 0 since it's the root path
    # this check if it's a multimodules project by looking at the path returned
    # If it's just a simple project then it should only
    # contain the rootpath in the list otherwise we also get the
    # module path , which make the list longer than length 1.
    if len(paths) > 1:
        logging.warn("MULTI PROJECTS FOUND")
        logging.warn("CURRENTLY WE DON't SUPPORT THAT, NEED DSPOT MAVEN PLUGIN AND PRECONFIGURE IT")
        query = {}
        query["RepoSlug"] = reposlug
        query["RepoBranch"] = repobranch
        # query["State"] = "pending"
        # email = coll.find_one(query)["Email"]

        report_error("Hi, your project does not support Dspot, please add a dspot.properties file at root.\n \n --STAMP/Dspot","",reposlug,repobranch)
        # email_result(message,subject,email)

    else:
        # Check if the module has tests otherwise move on to the next module
        logging.warn(root_path + '/src/test/java')
        if os.path.exists(root_path + '/src/test/java'):
            logging.warn("SIMPLE PROJECT found ")
            outputdir = 'clonedrepo/dspot-out/RootProject'
            JAVA_VERSION = find_JAVA_VERSION(project_path + '/pom.xml')

            # Configure a .properties file give the module, root and output dir information
            configure(module_name, module_path,
                      root_name, project_path, outputdir,JAVA_VERSION)
            logging.warn('Running Dspot')
            res = exec_get_output('java -jar dspot.jar -p project.properties ' + basic_opts + RUN_OPTIONS_JAR,True)

            error_message = ""
            if res[1] == True:
                error_message = "Process was TIMEOUT, RUN EXCEEDED " + str(RUN_TIMEOUT) + " minutes, currently we don't support heavy projects \n \n --STAMP/Dspot"
            else: 
                error_message = "Hi, your amplification has failed. Check the attached output files for more details.\n \n --STAMP/Dspot"
            
            report_error(error_message,res[0],reposlug,repobranch)

            # move properties file to outputdir when done .
            exec_get_output('mv -t ' + outputdir +
                            ' project.properties debug.log  2>/dev/null', True)
            # also clean up after each run
            exec_get_output('rm -rf NUL target/', True)
        else:
            logging.warn(root_name + " ignored due to no tests found")
            report_error("Hi, your amplification has failed. Ignored due to no tests found, creating your own dspot.properties file is adviced.\n \n --STAMP/Dspot","",reposlug,repobranch)

def connect_and_subscribe(conn):
    conn.start()
    conn.connect('guest', 'guest', wait=True)
    conn.subscribe(QUEUE_NAME,ack='auto', headers={'activemq.prefetchSize': 1},id=1)
    logging.warn("Listener connected")
    # This is for Kubernetes bug, require to periodically reconnection to pods 
    # in another namespace.
    time.sleep(RECONNECT_TIME) 
    logging.warn("Disconnecting and renew connection")
    conn.disconnect()

class Listener(object):

    def __init__(self, conn):
        self.conn = conn
        self.count = 0
        self.start = time.time()

    def print_output(self, type, data):
        for line in data:
            logging.warn(line)

    def on_disconnected(self):
        logging.warn("DISCONNECTED")
        # Reconnecting
        connect_and_subscribe(self.conn)
        logging.warn("RECONNECTED")

    def on_message(self, headers, message):
        logging.warn("New build id arrived: %s" % message)
        # Remove output file after each run to avoid filling up the container.
        self.conn.ack(headers.get('message-id'), headers.get('subscription'))
        os.system("rm -rf clonedrepo")
        # Fetch slug and branch name from travis given buildid.
        reposlug = ""
        repobranch = ""
        selector = ""
        if not SLUG_MODE:
            t = TravisPy()
            build = t.build(message)
            repoid = build['repository_id']
            repobranch = build.commit.branch
            repo = t.repo(repoid)
            reposlug = repo.slug

        else:
            # Expect message format 'selector,branchname,selector'
            strList = message.split(",");
            logging.warn(message)
            logging.warn(strList)
            reposlug = strList[0]
            repobranch = strList[1]
            selector = strList[2]

        # Clone the repo branch
        token = os.getenv('GITHUB_OAUTH') or ''
        url = 'https://:@github.com/' + reposlug + '.git'
        repo = None
        branch_exist = True
        logging.warn('Cloning url ' + url)

        try:
            repo = git.Repo.clone_from(url, 'clonedrepo', branch=repobranch)
        except:
            logging.warn("Branch " + str(repobranch) + " or the repo " +
                            str(reposlug) + " itself does not exist anymore")
            branch_exist = False
            report_error("Invalid repo or branch, double check if the input repo url is correct \n \n --STAMP/Dspot","",reposlug,repobranch)


        # Check if project support dspot otherwise try autoconfiguring 
        # assuming that dspot configurations is in the top most directory of
        # the project
        timecap = '-{date:%Y-%m-%d-%H-%M-%S}'.format(date=datetime.datetime.now())
        if branch_exist:
            if not run_Dspot_preconfig(reposlug,repobranch,selector):
                logging.warn("PROJECT DOES NOT SUPPORT DSPOT, COMMENCING AUTOCONFIGURING")
                run_Dspot_autoconfig(reposlug,repobranch,selector)

            logging.warn("PIPELINE MESSAGE: DONE , AWAITING FOR NEW BUILD ID")

conn = stomp.Connection10([(host, port)])
conn.set_listener(LISTENER_NAME, Listener(conn))
connect_and_subscribe(conn);

while True:
    time.sleep(3)
