pipeline {
  agent { label 'Linux-Office' }
  options {
    buildDiscarder(logRotator(daysToKeepStr:'10'))
    timeout(time: 15, unit: 'MINUTES')
    skipDefaultCheckout()
    disableConcurrentBuilds()
  }

  stages {
    stage ('Build OpenEdge plugin') {
      steps {
        checkout([$class: 'GitSCM', branches: scm.branches, extensions: scm.extensions + [[$class: 'CleanCheckout']], userRemoteConfigs: scm.userRemoteConfigs])
        script {
          withEnv(["MVN_HOME=${tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'}", "JAVA_HOME=${tool name: 'Corretto 8', type: 'jdk'}"]) {
            if ("master" == env.BRANCH_NAME) {
              sh "$MVN_HOME/bin/mvn -P release clean package verify deploy -Dgit.commit=\$(git rev-parse --short HEAD)"
            } else if (("develop" == env.BRANCH_NAME) || env.BRANCH_NAME.startsWith("release") || env.BRANCH_NAME.startsWith("hotfix")) {
              sh "$MVN_HOME/bin/mvn clean javadoc:javadoc install -Dmaven.test.failure.ignore=true -Dgit.commit=\$(git rev-parse --short HEAD)"
            } else {
              sh "$MVN_HOME/bin/mvn clean package -Dmaven.test.failure.ignore=true"
            }
          }
        }
        archiveArtifacts artifacts: 'openedge-plugin/target/sonar-openedge-plugin-*.jar'
        step([$class: 'Publisher', reportFilenamePattern: '**/target/surefire-reports/testng-results.xml'])
      }
    }

    stage ('SonarQube analysis') {
      steps {
        script {
          withEnv(["MVN_HOME=${tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'}", "JAVA_HOME=${tool name: 'Corretto 11', type: 'jdk'}"]) {
            withSonarQubeEnv(installationName: 'SonarCloud') {
              sh "$MVN_HOME/bin/mvn -Dsonar.organization=rssw -Dsonar.branch.name=${env.BRANCH_NAME} sonar:sonar"
            }
          }
        }
      }
    }
  }
}
