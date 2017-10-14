#!/usr/bin/env groovy

node {

    /* Variables inherited from Jenkins job's settings

    // Password Parameters
    AWS_ACCESS_KEY_ID = "${AWS_ACCESS_KEY_ID}"
    AWS_SECRET_ACCESS_KEY = "${AWS_SECRET_ACCESS_KEY}"

    // String parameters
    // Environment type - rtfm-dev or rtfm-production. 
    // To be used during stack-create.
    ENV = "${ENV}"

    CI_BRANCH = "${CI_BRANCH}"
    CI_SCRIPTS_REPO_URL = "${CI_SCRIPTS_REPO_URL}"

    CF_BRANCH = "${CF_BRANCH}"
    CF_TEMPLATES_REPO_URL = "${CF_TEMPLATES_REPO_URL}"
    CF_STACK_TEMPLATE_FILE = "${CF_STACK_TEMPLATE_FILE}"

    */

    dir('ciscripts') {
        git branch: "${CI_BRANCH}", url: "${CI_SCRIPTS_REPO_URL}"
    }

    def provision = load 'ciscripts/provision.groovy'

    // cfTemplatesRepoUrl='1', cfBranch='2', cfTemplateFile='3'
    provision.cfTemplateValidate("${CF_TEMPLATES_REPO_URL}", "${CF_BRANCH}", "${CF_STACK_TEMPLATE_FILE}")
}
