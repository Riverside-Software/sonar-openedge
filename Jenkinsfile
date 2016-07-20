stage 'Build OpenEdge plugin'
node ('master') {
  gitClean()
  checkout scm
  def mvnHome = tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'
  sh "${mvnHome}\\bin\\mvn install"
}

// see https://issues.jenkins-ci.org/browse/JENKINS-31924
def gitClean() {
    timeout(time: 60, unit: 'SECONDS') {
        if (fileExists('.git')) {
            echo 'Found Git repository: using Git to clean the tree.'
            // The sequence of reset --hard and clean -fdx first
            // in the root and then using submodule foreach
            // is based on how the Jenkins Git SCM clean before checkout
            // feature works.
            if (isUnix()) {
              sh 'git reset --hard'
            } else {
              bat 'git reset --hard'
            }
            // Note: -e is necessary to exclude the temp directory
            // .jenkins-XXXXX in the workspace where Pipeline puts the
            // batch file for the 'bat' command.
            if (isUnix()) {
              sh 'git clean -ffdx -e ".jenkins-*/"'
              sh 'git submodule foreach --recursive git reset --hard'
              sh 'git submodule foreach --recursive git clean -ffdx'
            } else {
              bat 'git clean -ffdx -e ".jenkins-*/"'
              bat 'git submodule foreach --recursive git reset --hard'
              bat 'git submodule foreach --recursive git clean -ffdx'
            }
        }
        else
        {
            echo 'No Git repository found: using deleteDir() to wipe clean'
            deleteDir()
        }
    }
}
