#!groovy

stage 'Build OpenEdge plugin'
node ('master') {
  checkout (scm, extensions: [[$class: 'CleanCheckout']])
//  [$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false,, submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'GitHub-GQuerret', url: 'https://github.com/Riverside-Software/sonar-openedge']]])
//  gitClean()
//  checkout scm
  echo " Branch: ${env.BRANCH_NAME}"
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
