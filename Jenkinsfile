pipeline {
  agent { label 'Linux-Office' }
  options {
    buildDiscarder(logRotator(numToKeepStr:'10'))
    timeout(time: 15, unit: 'MINUTES')
    skipDefaultCheckout()
    disableConcurrentBuilds()
  }

  stages {
    stage ('Build OpenEdge plugin') {
      steps {
        checkout([$class: 'GitSCM', branches: scm.branches, extensions: scm.extensions + [[$class: 'CleanCheckout']], userRemoteConfigs: scm.userRemoteConfigs])
        script {
          withEnv(["PATH+MAVEN=${tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'}/bin"]) {
            if ("master" == env.BRANCH_NAME) {
              sh "mvn -P release clean package verify deploy -Dgit.commit=\$(git rev-parse --short HEAD)"
            } else if (("develop" == env.BRANCH_NAME) || env.BRANCH_NAME.startsWith("release") || env.BRANCH_NAME.startsWith("hotfix")) {
              sh "mvn clean javadoc:javadoc install -Dmaven.test.failure.ignore=true -Dgit.commit=\$(git rev-parse --short HEAD)"
            } else {
              sh "mvn clean package -Dmaven.test.failure.ignore=true"
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
          withEnv(["PATH+MAVEN=${tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'}/bin"]) {
            withSonarQubeEnv(credentialsId: 'SQToken', installationName: 'RSSW') {
              if (("master" == env.BRANCH_NAME) || ("develop" == env.BRANCH_NAME)) {
                sh "mvn -Dsonar.branch.name=${env.BRANCH_NAME} sonar:sonar"
              } else {
                def targetBranch = env.BRANCH_NAME.startsWith("release") ? "master" : "develop"
                sh "mvn -Dsonar.branch.name=${env.BRANCH_NAME} -Dsonar.branch.target=${targetBranch} sonar:sonar"
              }
            }
          }
        }
      }
    }
  }
}
