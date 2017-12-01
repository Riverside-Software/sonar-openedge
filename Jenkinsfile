pipeline {
  agent { label 'master' }
  options {
    buildDiscarder(logRotator(numToKeepStr:'10'))
    timeout(time: 15, unit: 'MINUTES')
    skipDefaultCheckout()
  }

  stages {
    stage ('Build OpenEdge plugin') {
      steps {
        checkout([$class: 'GitSCM', branches: scm.branches, extensions: scm.extensions + [[$class: 'CleanCheckout']], userRemoteConfigs: scm.userRemoteConfigs])
        script {
          withEnv(["PATH+MAVEN=${tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'}/bin"]) {
            if ("master" == env.BRANCH_NAME) {
              sh "mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install -Dmaven.test.failure.ignore=true"
            } else {
              sh "mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent package -Dmaven.test.failure.ignore=true"
            }
          }
         }
      }
    }
    stage ('SonarQube analysis') {
      steps {
        script {
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
      }
    }
  }
}
