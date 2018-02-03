#!/usr/bin/env groovy

node {

    /* Variables inherited from Jenkins job's settings

    // String parameters
    CI_BRANCH = "${CI_BRANCH}"
    CI_SCRIPTS_REPO_URL = "${CI_SCRIPTS_REPO_URL}"

    */

    dir('ciscripts') {
        git branch: "${CI_BRANCH}", url: "${CI_SCRIPTS_REPO_URL}"
    }

    def provision = load 'ciscripts/provision.groovy'

    provision.ansibleRolesInstall()

    // ansibleHostLimit='1', ansiblePlaybookFile='2'
    provision.ansiblePlaybookValidate("${ANSIBLE_HOST_LIMIT}", "${ANSIBLE_PLAYBOOK_FILE}")

    // ansibleHostLimit='1', ansiblePlaybookFile='2', ansiblePemFile='3'
    provision.ansiblePlaybookApply("${ANSIBLE_HOST_LIMIT}", "${ANSIBLE_PLAYBOOK_FILE}", "${ANSIBLE_EC2_PEM_FILE}")

}
