pipeline {
  agent { label 'Linux-Office03' }
  options {
    buildDiscarder(logRotator(daysToKeepStr:'10'))
    timeout(time: 15, unit: 'MINUTES')
    skipDefaultCheckout()
    disableConcurrentBuilds()
  }

  stages {
    stage ('👷 Build') {
      environment {
        OP_CLI_PATH = '/usr/local/bin/'
        MAVEN_GPG_PASSPHRASE = 'op://Jenkins/GPG/password'
        SONATYPE_USERNAME = 'op://Jenkins/Sonatype/username'
        SONATYPE_PASSWORD = 'op://Jenkins/Sonatype/password'
      }
      steps {
        checkout([$class: 'GitSCM', branches: scm.branches, extensions: scm.extensions + [[$class: 'CleanCheckout']], userRemoteConfigs: scm.userRemoteConfigs])
        checkout([$class: 'GitSCM', branches: scm.branches, extensions: scm.extensions + [[$class: 'CleanCheckout']], userRemoteConfigs: [[credentialsId: scm.userRemoteConfigs.credentialsId[0], url: scm.userRemoteConfigs.url[0], refspec: '+refs/heads/main:refs/remotes/origin/main']] ])
        checkout([$class: 'GitSCM', branches: scm.branches, extensions: scm.extensions + [[$class: 'CleanCheckout']], userRemoteConfigs: [[credentialsId: scm.userRemoteConfigs.credentialsId[0], url: scm.userRemoteConfigs.url[0], refspec: '+refs/heads/develop:refs/remotes/origin/develop']] ])
        script {
          withEnv(["PATH+MVN=${tool name: 'Maven 3', type: 'maven'}/bin", "JAVA_HOME=${tool name: 'JDK17', type: 'jdk'}"]) {
            if ("main" == env.BRANCH_NAME) {
              withSecrets() {
                configFileProvider([configFile(fileId: 'MvnSettingsRSSW', variable: 'MAVEN_SETTINGS')]) {
                  sh 'mvn -s ${MAVEN_SETTINGS} -P release clean deploy -Dgit.commit=\$(git rev-parse --short HEAD)'
                }
              }
              mail body: "https://central.sonatype.com/publishing/deployments", to: "jenkins-reports@riverside-software.fr", subject: "sonar-openedge - Publish artifact on Central"
            } else if ("develop" == env.BRANCH_NAME) {
              sh "mvn clean javadoc:javadoc deploy -Dmaven.test.failure.ignore=true -Dgit.commit=\$(git rev-parse --short HEAD)"
            } else if (env.BRANCH_NAME.startsWith("release") || env.BRANCH_NAME.startsWith("hotfix")) {
              sh "mvn clean javadoc:javadoc install -Dmaven.test.failure.ignore=true -Dgit.commit=\$(git rev-parse --short HEAD)"
            } else {
              sh "mvn clean verify -Dmaven.test.failure.ignore=true -Dgit.commit=\$(git rev-parse --short HEAD)"
            }
          }
        }
        step([$class: 'Publisher', reportFilenamePattern: '**/target/surefire-reports/testng-results.xml'])
        archiveArtifacts artifacts: '**/openedge-plugin/target/sonar-openedge-plugin-*.jar'
      }
    }

    stage ('🔍 SonarQube analysis') {
      steps {
        script {
          withEnv(["PATH+MVN=${tool name: 'Maven 3', type: 'maven'}/bin", "JAVA_HOME=${tool name: 'JDK17', type: 'jdk'}"]) {
            withSonarQubeEnv(credentialsId: 'SQToken', installationName: 'RSSW') {
              if (("main" == env.BRANCH_NAME) || ("develop" == env.BRANCH_NAME)) {
                sh "mvn -Dsonar.organization=rssw -Dsonar.branch.name=${env.BRANCH_NAME} sonar:sonar"
              } else if (env.BRANCH_NAME.startsWith("hotfix")) {
                sh "mvn -Dsonar.organization=rssw -Dsonar.branch.name=${env.BRANCH_NAME} -Dsonar.branch.target=main sonar:sonar"
              } else {
                sh "mvn -Dsonar.organization=rssw -Dsonar.branch.name=${env.BRANCH_NAME} -Dsonar.branch.target=develop sonar:sonar"
              }
            }
          }
        }
      }
    }
  }

  post {
    unstable {
      script {
        mail body: "Check console output at ${BUILD_URL}/console", to: "jenkins-reports@riverside-software.fr", subject: "sonar-openedge ${BRANCH_NAME} build is unstable"
      }
    }
    failure {
      script {
        mail body: "Check console output at ${BUILD_URL}/console", to: "jenkins-reports@riverside-software.fr", subject: "sonar-openedge ${BRANCH_NAME} build failure"
      }
    }
    fixed {
      script {
        mail body: "Console output at ${BUILD_URL}/console", to: "jenkins-reports@riverside-software.fr", subject: "sonar-openedge ${BRANCH_NAME} build is back to normal"
      }
    }
  }
}
