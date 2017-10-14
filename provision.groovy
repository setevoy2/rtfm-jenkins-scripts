#!/usr/bin/env groovy

def cfTemplateValidate(cfTemplatesRepoUrl='1', cfBranch='2', cfTemplateFile='3') {

    docker.image('mesosphere/aws-cli').inside('-v /var/run/docker.sock:/var/run/docker.sock') {

        git branch: "${cfBranch}",  url: "${cfTemplatesRepoUrl}"

        stage('CF Temlate validate') {

			// sh -e to prevent printing variables in a build log
			// read https://stackoverflow.com/a/39908900/2720802
			sh "#!/bin/sh -e\n" + "export AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} && export AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}"
            sh "aws cloudformation --region eu-west-1 validate-template --template-body file://${cfTemplateFile}"
        }
    }
}

def cfCheckStackPresent(cfStackName='1') {

    docker.image('mesosphere/aws-cli').inside('-v /var/run/docker.sock:/var/run/docker.sock') {

        stage('CF check if stack present') {

            // sh -e to prevent printing variables in a build log
            // read https://stackoverflow.com/a/39908900/2720802
            sh "#!/bin/sh -e\n" + "export AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} && export AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}"
            sh "aws cloudformation --region eu-west-1 describe-stacks --stack-name ${cfStackName} &> /dev/null"
        }
    }
}

def cfStackCreateOrUpdate(action='1', cfTemplatesRepoUrl='2', cfBranch='3', cfTemplateFile='4', cfStackName='5', env='6', cfKeyName='7', allowLocation='8') {

    docker.image('mesosphere/aws-cli').inside('-v /var/run/docker.sock:/var/run/docker.sock') {

        git branch: "${cfBranch}",  url: "${cfTemplatesRepoUrl}"

        stage("CF Stack ${action}") {

            sh "echo Stack ${action}"

            // sh -e to prevent printing variables in a build log
            // read https://stackoverflow.com/a/39908900/2720802
            //sh "#!/bin/sh -e\n" + "export AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} && export AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}"
            sh "aws cloudformation --region eu-west-1 ${action}-stack --stack-name ${cfStackName} --template-body file://${cfTemplateFile} --parameters ParameterKey=ENV,ParameterValue=${env} ParameterKey=KeyName,ParameterValue=${cfKeyName} ParameterKey=HomeAllowLocation,ParameterValue=${allowLocation}/32"
            
        }
    }
}

return this
