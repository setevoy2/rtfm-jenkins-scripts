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

        // sh -e to prevent printing variables in a build log
        // read https://stackoverflow.com/a/39908900/2720802
        sh "#!/bin/sh -e\n" + "export AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} && export AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}"
        sh "aws cloudformation --region eu-west-1 describe-stacks --stack-name ${cfStackName} &> /dev/null"
    }
}

def cfStackCreateOrUpdate(action='1', cfTemplatesRepoUrl='2', cfBranch='3', cfTemplateFile='4', cfStackName='5', env='6', cfKeyName='7', homeAllowLocation='8', ciAllowLocation='9') {

    docker.image('mesosphere/aws-cli').inside('-v /var/run/docker.sock:/var/run/docker.sock') {

        git branch: "${cfBranch}",  url: "${cfTemplatesRepoUrl}"

        stage("CF Stack ${action}") {

            sh "echo Stack ${action}"

            // sh -e to prevent printing variables in a build log
            // read https://stackoverflow.com/a/39908900/2720802
            sh "#!/bin/sh -e\n" + "export AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} && export AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}"
            sh "aws cloudformation --region eu-west-1 ${action}-stack --stack-name ${cfStackName} --template-body file://${cfTemplateFile} --parameters ParameterKey=ENV,ParameterValue=${env} ParameterKey=KeyName,ParameterValue=${cfKeyName} ParameterKey=HomeAllowLocation,ParameterValue=${homeAllowLocation}/32 ParameterKey=JenkinsIP,ParameterValue=${ciAllowLocation}/32"
            
        }
    }
}

def ansibleRolesInstall() {

    docker.image('williamyeh/ansible:master-ubuntu16.04').inside('-v /var/run/docker.sock:/var/run/docker.sock') {

        git branch: "${ANSIBLE_GITHUB_BRANCH}", url: "${ANSIBLE_GITHUB_REPO_URL}"

        stage('Roles install') {

            sh "ansible-galaxy install --ignore-certs --role-file requirements.yml"
        }
    }
}

def ansiblePlaybookValidate(ansibleHostLimit='1', ansiblePlaybookFile='2') {

    docker.image('williamyeh/ansible:master-ubuntu16.04').inside('-v /var/run/docker.sock:/var/run/docker.sock') {

        git branch: "${ANSIBLE_GITHUB_BRANCH}",  url: "${ANSIBLE_GITHUB_REPO_URL}"

        stage('Ansible playbook validate') {

            sh "ansible-playbook --syntax-check --limit=${ansibleHostLimit} ${ansiblePlaybookFile}"
        }
    }
}

def ansiblePlaybookApply(ansibleHostLimit='1', ansiblePlaybookFile='2', ansiblePemFile='3') {

    docker.image('williamyeh/ansible:master-ubuntu16.04').inside('-v /var/run/docker.sock:/var/run/docker.sock') {

        git branch: "${ANSIBLE_GITHUB_BRANCH}",  url: "${ANSIBLE_GITHUB_REPO_URL}"

        dir('credentials') {
            git branch: "master", credentialsId: 'setevoy_bitbucket_aws', url: "${ANSIBLE_BB_CREDENTIALS_REPO_URL}"
        }
        
        dir('roles/nginx/templates/virtualhosts') {
            git branch: "${ANSIBLE_BB_TEMPLATES_BRANCH}", credentialsId: 'setevoy_bitbucket_aws', url: "${ANSIBLE_BB_TEMPLATES_REPO_URL}"
        }

        stage('Ansible playbook apply') {

            sh "chmod 400 credentials/${ANSIBLE_EC2_PEM_FILE}"
            sh "ansible-playbook --limit=${ansibleHostLimit} --private-key=credentials/${ansiblePemFile} ${ansiblePlaybookFile}"
        }
    }
}

return this
