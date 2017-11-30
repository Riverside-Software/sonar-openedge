#!groovy

stage 'Build OpenEdge plugin'
node ('master') {
  // Set job description with PR title
  if (env.BRANCH_NAME.startsWith('PR')) {
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'GitHub-GQuerret', usernameVariable: 'GH_LOGIN', passwordVariable: 'GH_PASSWORD']]) {
      def resp = httpRequest url: "https://api.github.com/repos/Riverside-Software/sonar-openedge/pulls/${env.BRANCH_NAME.substring(3)}", customHeaders: [[name: 'Authorization', value: "token ${env.GH_PASSWORD}"]]
      def ttl = getTitle(resp)
      def itm = getItem(env.BRANCH_NAME)
      itm.setDisplayName("PR-${env.BRANCH_NAME.substring(3)} '${ttl}'")
    }
  }
  gitClean()
  checkout scm
  timeout(5) {
    withEnv(["PATH+MAVEN=${tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'}/bin"]) {
      if ("master" == env.BRANCH_NAME) {
        sh "mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install -Dmaven.test.failure.ignore=true"
      } else {
        sh "mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent package -Dmaven.test.failure.ignore=true"
      }
    }
  }
  archiveArtifacts artifacts: 'openedge-plugin/target/sonar-openedge-plugin-*.jar', excludes: 'openedge-plugin/target/sonar-openedge-plugin-*-sources.jar'
  step([$class: 'Publisher', reportFilenamePattern: '**/testng-results.xml'])

  withEnv(["PATH+MAVEN=${tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'}/bin"]) {
    withCredentials([string(credentialsId: 'SonarCloudToken', variable: 'SONARCLOUD_TOKEN')]) {
      if ("master" == env.BRANCH_NAME) {
        sh "mvn -Dsonar.host.url=https://sonarcloud.io -Dsonar.organization=rssw -Dsonar.login=${env.SONARCLOUD_TOKEN} -Dsonar.branch.name=${env.BRANCH_NAME} sonar:sonar"
      } else {
        sh "mvn -Dsonar.host.url=https://sonarcloud.io -Dsonar.organization=rssw -Dsonar.login=${env.SONARCLOUD_TOKEN} -Dsonar.branch.name=${env.BRANCH_NAME} -Dsonar.branch.target=master sonar:sonar"
      }
    }
  }
}

@NonCPS
def getItem(branchName) {
  Jenkins.instance.getItemByFullName("sonar-openedge/${branchName}")
}

@NonCPS
def getTitle(json) {
    def slurper = new groovy.json.JsonSlurper()
    def jsonObject = slurper.parseText(json.content)
    jsonObject.title
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
