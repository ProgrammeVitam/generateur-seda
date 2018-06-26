// https://jenkins.io/doc/book/pipeline/syntax/
// https://jenkins.io/doc/pipeline/steps/
// https://www.cloudbees.com/sites/default/files/declarative-pipeline-refcard.pdf

// https://vetlugin.wordpress.com/2017/01/31/guide-jenkins-pipeline-merge-requests/

// KWA TOOD :
// - estimate deviation from base branch (if relevant)
// - separate stage for the javadoc:aggregate-jar build (in order to -T 1C the packaging)
// - fix the partial build

pipeline {
    agent {
        label 'slaves'
    }

    environment {
        MVN_BASE = "/usr/local/maven/bin/mvn --settings ${pwd()}/.ci/settings.xml"
        MVN_COMMAND = "${MVN_BASE} --show-version --batch-mode --errors --fail-at-end -DinstallAtEnd=true -DdeployAtEnd=true"
        DEPLOY_GOAL = " " // Deploy goal used by maven ; typically "deploy" for master* branches & "" (nothing) for everything else (we don't deploy) ; keep a space so can work in other branches than develop
        CI = credentials("app-jenkins")
        SERVICE_SONAR_URL = credentials("service-sonar-url")
        SERVICE_NEXUS_URL = credentials("service-nexus-url")
        // SERVICE_CHECKMARX_URL = credentials("service-checkmarx-url")
        // SERVICE_REPO_SSHURL = credentials("repository-connection-string")
        // SERVICE_GIT_URL = credentials("service-gitlab-url")
        SERVICE_PROXY_HOST = credentials("http-proxy-host")
        // SERVICE_PROXY_PORT = credentials("http-proxy-port")
    }

   stages {

       stage("Tools configuration") {
           steps {
               echo "Workspace location : ${env.WORKSPACE}"
               echo "Branch : ${env.GIT_BRANCH}"
           }
       }

        // Override the default maven deploy target when on master (publish on nexus)
        stage("Computing maven target") {
            when {
                anyOf {
                    branch "develop*"
                    branch "master_*"
                    branch "master"
                }
            }
            environment {
                DEPLOY_GOAL = "deploy"
                MASTER_BRANCH = "true"
            }
            steps {
                script {
                    // overwrite file content with one more goal
                    writeFile file: 'deploy_goal.txt', text: "${env.DEPLOY_GOAL}"
                    writeFile file: 'master_branch.txt', text: "${env.MASTER_BRANCH}"
                 }
                echo "We are on master branch (${env.GIT_BRANCH}) ; deploy goal is \"${env.DEPLOY_GOAL}\""
            }
        }
        
        // stage ("Compile") {
        //     steps {
        //         sh '$MVN_COMMAND -f pom.xml clean compile -P vitam,doc'
        //     }
        // }
        // stage ("Execute unit tests") {
        //     steps {
        //         sh '$MVN_COMMAND -f pom.xml clean test -P vitam'
        //     }
        //     post {
        //         always {
        //             junit '**/target/surefire-reports/*.xml'
        //         }
        //     }
        // }

        stage("Build packages") {
            environment {
                DEPLOY_GOAL = readFile("deploy_goal.txt")
            }
            steps {
                sh '$MVN_COMMAND -P vitam,doc -f pom.xml -Dmaven.test.skip=true -DskipTests=true clean package $DEPLOY_GOAL'
                // javadoc:aggregate-jar rpm:attached-rpm jdeb:jdeb
            }        
        }
    }
}
