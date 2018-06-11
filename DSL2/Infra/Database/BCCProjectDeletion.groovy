def top_folder = '/Infra'
def under_folder = '/Infra/Database'

folder(top_folder)
folder(under_folder)

def workspace = '/deployment/ansible/'
def project_ws = ['is-appsetup','is-iaas','is_inventory']

def job_name= under_folder +'/BCC Project Deletion'

job(job_name) {
    description 'BCC Project Deletion'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }

parameters {
        labelParam('DeploymentSlave')
		stringParam('ENV_PREFIX')
		stringParam('SCRIPTS_DIR','/deployment/ecomm/database')
		stringParam('DB_UNAME')
		password{
                  name('DB_PASSWORD')
                  defaultValue('dn(2t5fcntdnmcna14')
                  description('Default Publishing Password')
                     }
		stringParam('DB_HOST')
		stringParam('DB_SERVICE')
		stringParam('NO_OF_DAYS_OLD_PROJ_TO_BE_DELETED')
		stringParam('TYPE_OF_PROJ_TO_BE_DELETED')
		}
customWorkspace('/deployment/ecomm/database');
steps {

      shell('#!/bin/sh \n'+
			'export PATH=$PATH:${ORACLE_HOME}/bin \n'+
			'export LD_LIBRARY_PATH=:${ORACLE_HOME}/lib \n'+

			'cd ${SCRIPTS_DIR} \n'+
			'echo "${ENV_PREFIX} ${DB_UNAME} ${DB_PASSWORD} ${DB_HOST} ${DB_SERVICE} ${NO_OF_DAYS_OLD_PROJ_TO_BE_DELETED} ${TYPE_OF_PROJ_TO_BE_DELETED}" \n'+
			'. ./BCC_Active_Project_Deletion.sh ${ENV_PREFIX} ${DB_UNAME} ${DB_PASSWORD} ${DB_HOST} ${DB_SERVICE} ${NO_OF_DAYS_OLD_PROJ_TO_BE_DELETED} ${TYPE_OF_PROJ_TO_BE_DELETED}')
}
}	 
