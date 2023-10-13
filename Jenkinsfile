pipeline {
  agent {
    node {
      label 'maven'
    }
  }

  parameters {
      string(name:'TAG_NAME',defaultValue: '',description:'')
      string(name:'APP_NAME',defaultValue: '',description:'')
      string(name:'UNIT_TEST',defaultValue: 'NO',description:'')

  }

  environment {
      DOCKER_CREDENTIAL_ID = 'dockerhub-id'
      GITHUB_CREDENTIAL_ID = 'github-id'
      KUBECONFIG_CREDENTIAL_ID = 'kubeconfig-unbong'
      REGISTRY = 'docker.io'
      DOCKERHUB_NAMESPACE = 'unbong'
      GITHUB_ACCOUNT = 'unbong'

  }

  stages {
    stage('拉取代码') {
      steps {
        git(url: 'https://github.com/unbong/glmall.git', credentialsId: 'github-id', branch: 'master', changelog: true, poll: false)
      }
    }

    stage ('unit test') {
        when{
        expression{
            return params.UNIT_TEST == 'YES'
            }
        }
        steps {
            container ('maven') {
                sh 'mvn clean  -gs `pwd`/maven-setting.xml test'
            }
        }
    }

    stage ('构建并推送') {

        steps {
            container ('maven') {
                sh 'mvn  -Dmaven.test.skip=true -gs `pwd`/maven-setting.xml  clean package '
                sh 'cd  $APP_NAME && docker build -f Dockerfile -t $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER .'
                withCredentials([usernamePassword(passwordVariable : 'DOCKER_PASSWORD' ,usernameVariable : 'DOCKER_USERNAME' ,credentialsId : "$DOCKER_CREDENTIAL_ID" ,)]) {
                        sh 'echo "$DOCKER_PASSWORD" | docker login $REGISTRY -u "$DOCKER_USERNAME" --password-stdin'
                        sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER'
                    }
            }
        }
    }

    stage('push latest'){

           steps{
                container ('maven') {
                  sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:latest '
                  sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:latest '
                }
           }
    }

    stage('deploy to dev') {


     steps {
       input(id: 'deploy-to-dev', message: 'deploy to dev?')
       kubernetesDeploy(configs: "$APP_NAME/deploy/**", enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
      }
    }

    stage('push with tag'){
    when{
      expression{
        return params.TAG_NAME =~ /v.*/
      }
    }
    steps {
        container ('maven') {
          input(id: 'release-image-with-tag', message: 'release image with tag?')
            withCredentials([usernamePassword(credentialsId: "$GITHUB_CREDENTIAL_ID", passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
              sh 'git config --global user.email "ecunbong@gmail.com" '
              sh 'git config --global user.name "unbong" '
              sh 'git tag -a $TAG_NAME -m "$TAG_NAME" '
              sh 'git push http://$GIT_USERNAME:$GIT_PASSWORD@github.com/$GITHUB_ACCOUNT/glmall.git --tags --ipv4'
            }
          sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:$TAG_NAME '
          sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:$TAG_NAME '
    }
    }
  }
  }
}