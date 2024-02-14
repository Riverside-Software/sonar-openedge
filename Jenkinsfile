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
        checkout([$class: 'GitSCM', branches: scm.branches, extensions: scm.extensions + [[$class: 'CleanCheckout']], userRemoteConfigs: [[credentialsId: scm.userRemoteConfigs.credentialsId[0], url: scm.userRemoteConfigs.url[0], refspec: '+refs/heads/main:refs/remotes/origin/main']] ])
        checkout([$class: 'GitSCM', branches: scm.branches, extensions: scm.extensions + [[$class: 'CleanCheckout']], userRemoteConfigs: [[credentialsId: scm.userRemoteConfigs.credentialsId[0], url: scm.userRemoteConfigs.url[0], refspec: '+refs/heads/develop:refs/remotes/origin/develop']] ])
        script {
          withEnv(["MVN_HOME=${tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'}", "JAVA_HOME=${tool name: 'Corretto 11', type: 'jdk'}"]) {
            if ("main" == env.BRANCH_NAME) {
              sh "$MVN_HOME/bin/mvn -P release clean deploy -Dgit.commit=\$(git rev-parse --short HEAD)"
              mail body: "---", to: "g.querret@riverside-software.fr", subject: "Release artifact on Sonatype"
            } else if ("develop" == env.BRANCH_NAME) {
              sh "$MVN_HOME/bin/mvn clean javadoc:javadoc deploy -Dmaven.test.failure.ignore=true -Dgit.commit=\$(git rev-parse --short HEAD)"
            } else if (env.BRANCH_NAME.startsWith("release") || env.BRANCH_NAME.startsWith("hotfix")) {
              sh "$MVN_HOME/bin/mvn clean javadoc:javadoc install -Dmaven.test.failure.ignore=true -Dgit.commit=\$(git rev-parse --short HEAD)"
            } else {
              sh "$MVN_HOME/bin/mvn clean package -Dmaven.test.failure.ignore=true -Dgit.commit=\$(git rev-parse --short HEAD)"
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
          withEnv(["MVN_HOME=${tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'}", "JAVA_HOME=${tool name: 'JDK17', type: 'jdk'}"]) {
            withSonarQubeEnv(installationName: 'SonarCloud') {
              if (("main" == env.BRANCH_NAME) || ("develop" == env.BRANCH_NAME)) {
                sh "$MVN_HOME/bin/mvn -Dsonar.organization=rssw -Dsonar.branch.name=${env.BRANCH_NAME} sonar:sonar"
              } else if (env.BRANCH_NAME.startsWith("hotfix")) {
                sh "$MVN_HOME/bin/mvn -Dsonar.organization=rssw -Dsonar.branch.name=${env.BRANCH_NAME} -Dsonar.branch.target=main sonar:sonar"
              } else {
                sh "$MVN_HOME/bin/mvn -Dsonar.organization=rssw -Dsonar.branch.name=${env.BRANCH_NAME} -Dsonar.branch.target=develop sonar:sonar"
              }
            }
          }
        }
      }
    }
  }

  post {
    failure {
      script {
        mail body: "Check console output at ${BUILD_URL}/console", to: "g.querret@riverside-software.fr", subject: "sonar-openedge build failure in Jenkins - Branch ${BRANCH_NAME}"
      }
    }
    fixed {
      script {
        mail body: "Console output at ${BUILD_URL}/console", to: "g.querret@riverside-software.fr", subject: "sonar-openedge build is back to normal - Branch ${BRANCH_NAME}"
      }
    }
  }
}
