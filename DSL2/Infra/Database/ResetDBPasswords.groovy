def top_folder = '/Infra'
def under_folder = '/Infra/Database'

folder(top_folder)
folder(under_folder)

def workspace = '/deployment/ansible/'
def project_ws = ['is-appsetup','is-iaas','is_inventory']

def job_name= under_folder +'/ResetDBPasswords'

job(job_name) {
    description 'Reset database passwords'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }

parameters {
		labelParam('DeploymentSlave')
		stringParam('ENV_PREFIX')
		stringParam('SCRIPTS_DIR','/deployment/ecomm/database')
		stringParam('TARGET_DB_CONN_STR')
        }
steps {
          shell('#!/bin/sh \n'+
				'export PATH=$PATH:${ORACLE_HOME}/bin \n'+
				'export LD_LIBRARY_PATH=:${ORACLE_HOME}/lib \n'+

				'cd ${SCRIPTS_DIR} \n'+
				'. ./reset_passwords.ksh ${ENV_PREFIX} "${TARGET_DB_CONN_STR}"')
				
}
}
