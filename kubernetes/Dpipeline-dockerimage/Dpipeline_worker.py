#!/usr/bin/env python
# encoding=utf-8

# dependencies to pip install
# travispy , gitPython, stomp, pymongo
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
from travispy import TravisPy
from pymongo import MongoClient
from xml.etree import ElementTree

# login into mongodb
client = MongoClient(os.getenv('MONGODB_HOST') or 'mongodb://localhost:27017/')
# Start ActiveMQ listener
host = os.getenv("ACTIVEMQ_HOST") or "activemq"
port = 61613
# Uncomment next line if you do not have Kube-DNS working.
# host = os.getenv("REDIS_SERVICE_HOST")
QUEUE_NAME = os.getenv("ACTIVEMQ_QUEUE") or '/queue/Dpipeline'
LISTENER_NAME = 'BuildIdListener'
TIMEOUT = 360  # timeout for executing submissions



# Subprocess running returning the output as a string
def exec_get_output(cmd, val=False, wait=True):
    out = subprocess.Popen(cmd, stdout=subprocess.PIPE,
                           shell=val, close_fds=True)
    stdout, stderr = out.communicate()
    if wait:
        out.wait()
    return stdout


# Check if enough information is provided to push to Git. 
# configure git if that's the case
def check_endprocess_gitcommit():
    if (os.getenv("GITHUB_USEREMAIL")=='' or os.getenv("GITHUB_USERNAME")=='' or os.getenv("PUSH_URL")==''):
        print('Not enough Git information provided, no git commit will occur at end process')
        print('Please double check GITHUB_USEREMAIL , GITHUB_USERNAME and PUSH_URL')
        return False   
    else:
        exec_get_output('git config --global user.email ' +
                        os.getenv("GITHUB_USEREMAIL"), True)
        exec_get_output('git config --global user.name ' +
                        os.getenv("GITHUB_USERNAME"), True)
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
        print("No java version found, malconfigured pom ?")
        return ''
    else:
        return JAVA_VERSION



# Run Dspot preconfigured, the project support Dspot
# and has configured in the pom file
def run_Dspot_preconfig(POM_FILE,reposlug,timecap):
    # This is fixed if running maven Dspot plugin
    outputdir = "target/dspot/output"
    # If no pomfile found in the project root or no dspot plugin then it does
    # not support Dspot
    if not (os.path.isfile(POM_FILE) and check_Dspot_supported(POM_FILE)):
        return False
    logging.warning("PROJECT DOES SUPPORT DSPOT")
    logging.warning(exec_get_output(['mvn','-f','clonedrepo', 'dspot:amplify-unit-tests',
                                     '-Dtest-criterion=TakeAllSelector', '-Diteration=1', '-Ddescartes', '-Dgregor']))

    # move files and cleanup after running
    exec_get_output('mv -t ' + outputdir +
                                ' project.properties debug.log  2>/dev/null', True)
    
    # push to Mongodb when done
    # get database
    db = client['Dspot']
    colname = reposlug.split('/')[1] + 'RootProject' + '-' + timecap
    col = db[colname]
    # get all output files but the binaries .class files
    files = exec_get_output(
            'find ' + outputdir + ' -type f | grep -v .class', True).rstrip().split('\n')
    for file in files:
        f = open(file)  # open a file
        text = f.read()    # read the entire contents, should be UTF-8 text
        file_name = file.split('/')[-1]
        logging.warning('File to Mongodb: ' + file_name)
        text_file_doc = {
            "file_name": file_name, "contents": text}
        col.insert(text_file_doc)
    exec_get_output('mv ' + outputdir + ' clonedrepo',True)
    exec_get_output('rm -rf NUL target/ clonedrepo/target', True)
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
    print('start saving file')
    with open('project.properties', 'w') as configfile:
        config.write(configfile)


# This will try to autoconfigure Dspot and provide some basic amplifications
# for projects which do not support dspot
def run_Dspot_autoconfig(reposlug,timecap):
    # Find project roots
    # Normal project with only one root
    roots = exec_get_output(
        'find clonedrepo/ -maxdepth 2 -mindepth 0 -type f -name "pom.xml"', True).rstrip().split('\n')
    # Project with multiroots like repairnator
    if isinstance(roots, str):
        roots = [roots]
    if roots == '':
        logging.warning('Unsupported project structure')
        exit(1)

    # find modules related to root.
    # the first path in the list is always in fact the path to the root.
    # loop through roots
    paths = []
    for root in roots:
        # This need to be executed as shell=True otherwise it will not work
        paths.append(sorted(exec_get_output('mvn -q -f ' + root +
                                            ' --also-make exec:exec -Dexec.executable="pwd"', True).rstrip().split('\n')))
    # Goal is to auto config dspot.config
    # dspot.properties is the standard form for dspot.
    # We use this to configure new config files.
    for listln in paths:
        root_path = listln[0]
        root_name = root_path.split('/')[-1]
        project_path = exec_get_output(
            'realpath --relative-to=. ' + root_path, True).strip()
        # default values of module if there are no modules(a.k.a not a
        # multimodules project)
        module_path = '.'
        module_name = ''
        logging.warning("rootname: " + root_name + " rootpath: " + root_path)
        # ignore the first index 0 since it's the root path
        # this check if it's a multimodules projectx
        if len(listln) > 1:
            logging.warning("MULTI PROJECTS FOUND")
            for i in range(1, len(listln)):
                # Check if the module has tests otherwise move on to the next
                # module (nothing to amplify)
                module_name = (listln[i]).split('/')[-1]
                module_path = exec_get_output(
                    'realpath --relative-to=' + root_path + ' ' + listln[i], True)
                logging.warning("Running Dspot on rootname: " +
                                root_name + " rootpath: " + module_name)
                if os.path.exists(listln[i] + '/src/test/java'):
                    outputdir = 'dspot-out/' + root_name + '_' + module_name + '/'
                    print('project_path: ' + project_path)
                    print('module_path: ' + module_path)
                    JAVA_VERSION = find_JAVA_VERSION(project_path + '/pom.xml')
                    configure(module_name, module_path,
                              root_name, project_path, outputdir,JAVA_VERSION)
                    logging.warning('Running Dspot')
                    logging.warning(exec_get_output(['java', '-jar', 'dspot-2.1.0-jar-with-dependencies.jar', '--path-to-properties',
                                                     'project.properties', '--test-criterion', 'TakeAllSelector', '--iteration', '1', '--descartes', '--gregor']))
                    # move properties file to outputdir when done .
                    exec_get_output(
                        'mv -t ' + outputdir + ' project.properties debug.log  2>/dev/null', True)
                    # also clean up after each run
                    exec_get_output('rm -rf NUL target/', True)
                else:
                    logging.warning(root_name + " module: " + module_name +
                                    " ignored since no tests were found")
        else:
            # Check if the module has tests otherwise move on to the next module
            # (nothing to amplify)
            if os.path.exists(root_path + '/src/test/java'):
                logging.warning("Running Dpot on rootname: " + root_name)
                outputdir = 'clonedrepo/dspot-out/RootProject'
                JAVA_VERSION = find_JAVA_VERSION(project_path + '/pom.xml')
                configure(module_name, module_path,
                          root_name, project_path, outputdir,JAVA_VERSION)
                logging.warning('Running Dspot')
                logging.warning(exec_get_output(['java', '-jar', 'dspot-2.1.0-jar-with-dependencies.jar', '--path-to-properties',
                                                 'project.properties', '--test-criterion', 'TakeAllSelector', '--iteration', '1', '--descartes', '--gregor']))
                # move properties file to outputdir when done .
                exec_get_output('mv -t ' + outputdir +
                                ' project.properties debug.log  2>/dev/null', True)
                # also clean up after each run
                exec_get_output('rm -rf NUL target/', True)
            else:
                logging.warning(root_name + " ignored due to no tests found")
    # Save files in mongodb
    # get database
    db = client['Dspot']
    # insert all docs in a directory into database
    # List all directories in clonedrepo/dspot-out/ folder
    dirs = exec_get_output(
        'find clonedrepo/dspot-out/ -maxdepth 1 -mindepth 1 -type d', True).rstrip().split('\n')
    if isinstance(dirs, str):
        dirs = [dirs]

    for dir in dirs:
        # extract directory name and use it as the colectioname for
        dirname = dir.split('/')[-1]
        colname = reposlug.split('/')[1] + dirname + '-' + timecap
        logging.warning(dir)
        col = db[colname]
        # get all files path in dir, but not the binaries .class files
        files = exec_get_output(
            'find ' + dir + ' -type f | grep -v .class', True).rstrip().split('\n')
        for file in files:
            f = open(file)  # open a file
            text = f.read()    # read the entire contents, should be UTF-8 text
            file_name = file.split('/')[-1]
            logging.warning('File to Mongodb: ' + file_name)
            text_file_doc = {
                "file_name": file_name, "contents": text}
            col.insert(text_file_doc)


class BuildIdListener(object):

    def __init__(self, conn):
        self.conn = conn
        self.count = 0
        self.start = time.time()

    def print_output(self, type, data):
        for line in data:
            logging.warning(line)

    def on_message(self, headers, message):
        logging.warning("New build id arrived: %s" % message)
        # Remove output file after each run to avoid filling up the container.
        os.system("rm -rf clonedrepo")
        # Fetch slug and branch name from travis given buildid.
        t = TravisPy()
        build = t.build(message)
        repoid = build['repository_id']
        repobranch = build.commit.branch
        repo = t.repo(repoid)
        reposlug = repo.slug
        # Clone the repo branch
        token = os.getenv('GITHUB_OAUTH') or ''
        url = 'https://github.com/' + reposlug + '.git'
        repo = None
        logging.warning('Cloning url ' + url)
        try:
            repo = git.Repo.clone_from(url, 'clonedrepo', branch=repobranch)
        except:
            logging.warning("Branch " + str(repobranch) + " or the repo " +
                            str(reposlug) + " itself does not exist anymore")
        self.conn.ack(headers.get('message-id'), headers.get('subscription'))
        # Check if project support dspot otherwise try autoconfiguring 
        # assuming that dspot configurations is in the top most directory of
        # the project
        POM_FILE = "clonedrepo/pom.xml"
        timecap = '-{date:%Y-%m-%d-%H-%M-%S}'.format(
            date=datetime.datetime.now())
        if not run_Dspot_preconfig(POM_FILE,reposlug,timecap):
            logging.warning("PROJECT DOES NOT SUPPORT DSPOT")
            run_Dspot_autoconfig(reposlug,timecap)

        # Commit build to github
        if check_endprocess_gitcommit():
            templist = os.getenv("PUSH_URL").split('//')
            pushurl = templist[0] + '//' + token + '@' + templist[1] 
            branch_name = reposlug.replace('/', "-") + timecap

            logging.warning('Commit to git as new branch with name ' + branch_name)

            current = repo.git.checkout('-b', branch_name)
            # current.checkout()
            time.sleep(10)
            repo.git.add(A=True)
            repo.git.commit(m='update')
            repo.git.push(pushurl, branch_name)
        logging.warning("PIPELINE MESSAGE: DONE , AWAITING FOR NEW BUILD ID")

conn = stomp.Connection10([(host, port)])
conn.set_listener(LISTENER_NAME, BuildIdListener(conn))
conn.start()
conn.connect()
logging.warning("Listener connected")
conn.subscribe(QUEUE_NAME, ack='client', headers={'activemq.prefetchSize': 1})
while True:
    time.sleep(3)
conn.disconnect()
