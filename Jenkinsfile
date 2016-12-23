#!groovy

stage 'Build OpenEdge plugin'
node ('master') {
  if (env.BRANCH_NAME.startsWith('PR')) {
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'GitHub-GQuerret', usernameVariable: 'GH_LOGIN', passwordVariable: 'GH_PASSWORD']]) {
      def resp = httpRequest url: "https://api.github.com/repos/Riverside-Software/sonar-openedge/pulls/${env.BRANCH_NAME.substring(3)}", customHeaders: [[name: 'Authorization', value: "token ${env.GH_PASSWORD}"]]
      // def resp = getPR(env.BRANCH_NAME, env.GH_PASSWORD)
      echo "resp ok ${resp.content}"
      def ttl = getTitle(resp.content)
      echo "title ${ttl}" 
      def itm = getItem(env.BRANCH_NAME)
      echo "itm ok ${itm}"
      currentBuild.displayName = "Current build ${ttl}"
      echo "current build ok"
      itm.setDisplayName("Current item ${ttl}")
    }
  }
  gitClean()
  checkout scm
  withEnv(["PATH+MAVEN=${tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'}/bin"]) {
    if ("master" == env.BRANCH_NAME) {
      sh "mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install -Dmaven.test.failure.ignore=true"
    } else {
      sh "mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent package -Dmaven.test.failure.ignore=true"
    }
  }
  archiveArtifacts artifacts: 'openedge-plugin/target/sonar-openedge-plugin-*.jar', excludes: 'openedge-plugin/target/sonar-openedge-plugin-*-sources.jar'
  step([$class: 'Publisher', reportFilenamePattern: '**/testng-results.xml'])

  if ("master" == env.BRANCH_NAME) {
    withEnv(["PATH+MAVEN=${tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'}/bin"]) {
      sh "mvn -Dsonar.host.url=http://sonar.riverside-software.fr sonar:sonar"
    }
  } else {
    withEnv(["PATH+MAVEN=${tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'}/bin"]) {
      withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'GitHub-GQuerret', usernameVariable: 'GH_LOGIN', passwordVariable: 'GH_PASSWORD']]) {
        sh "mvn -Dsonar.host.url=http://sonar.riverside-software.fr -Dsonar.analysis.mode=issues -Dsonar.github.pullRequest=${env.BRANCH_NAME.substring(3)} -Dsonar.github.repository=Riverside-Software/sonar-openedge -Dsonar.github.oauth=${env.GH_PASSWORD} sonar:sonar"
      }
    }
  }
}

@NonCPS
def getPR(branchName, gitToken) {
  // echo "Before HTTP request..."
  httpRequest url: "https://api.github.com/repos/Riverside-Software/sonar-openedge/pulls/${branchName.substring(3)}", customHeaders: [[name: 'Authorization', value: "token ${gitToken}"]]
  // echo "Response : ${response}"
  // item = Jenkins.instance.getItemByFullName("sonar-openedge/${branchName}")
  // echo "Item : ${item}"
  // item.setDisplayName("My custom description")
  // echo "Done..."
}

@NonCPS
def getItem(branchName) {
  Jenkins.instance.getItemByFullName("sonar-openedge/${branchName}")
}

@NonCPS
def getTitle(json) {
    def slurper = new groovy.json.JsonSlurper()
    def jsonObject = slurper.parseText(json)
    result.title
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
