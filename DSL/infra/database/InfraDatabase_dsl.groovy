def top_folder = '/Infra'
def under_folder = '/Infra/Database'

folder(top_folder)
folder(under_folder)

def workspace = '/deployment/ansible/'
def project_ws = ['is-appsetup','is-iaas','is_inventory']

job(under_folder+'/Checkout-DB-Seed-Scripts') {
    
    parameters {
        labelParam('DeploymentSlave')
    } 
    
    customWorkspace('/deployment/ecomm/database');
    scm {
         git {
         	remote {
             	url 'http://git.aws.gha.kfplc.com/KITS/database.git'
            	branch '*/master'
             	credentials 'a598502-4513-475a-9bbc-33bd216f73b6'
             	}
         }          
    }
}

job(under_folder +'/CreateDatabaseFromSeed') {
    
    parameters {
        stringParam('SOURCE_DB_USER','oraown')
        stringParam('SOURCE_DB_PASSWORD','***************') 
        stringParam('LIBRARY_FILE_SOURCE','(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=atgdev02.cgedocbjqkfs.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=ATGDEV02)))')
        stringParam('TARGET_DB_USER','oraown')
        stringParam('TARGET_DB_PASSWORD','***************')
        stringParam('TARGET_DB_CONN_STR','(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${ENV_PREFIX}.cgedocbjqkfs.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=${ENV_PREFIX})))')
        labelParam('DeploymentSlave')
        stringParam('DB_SEED_ENV')
        stringParam('DB_SEED_VERSION','${DB_SEED_ENV}')
        stringParam('SCRIPTS_DIR','/deployment/ecomm/database')
        stringParam('SID_WHEN_SEED_WAS_CREATED','${DB_SEED_ENV}','Unique Source Environment Identifier / Schema prefix in source dump file')
        stringParam('ENV_PREFIX','','environment identifier')
    } 
    
    concurrentBuild(true)

    steps {
	shell('export PATH=$PATH:${ORACLE_HOME}/bin \n'+
	  'export LD_LIBRARY_PATH=:${ORACLE_HOME}/lib \n'+
	  'export n_param="$(echo ${ENV_PREFIX} | tr "a-z" "A-Z")" \n'+
	  'export m_param="$(echo ${SID_WHEN_SEED_WAS_CREATED} | tr "a-z" "A-Z")" \n'+
	  ' \n'+
	  'cd ${SCRIPTS_DIR} \n'+
	  '. ./create_database_from_seed.ksh -u ${SOURCE_DB_USER} -p ${SOURCE_DB_PASSWORD} -U ${TARGET_DB_USER} -P ${TARGET_DB_PASSWORD} -t "${LIBRARY_FILE_SOURCE}" -T "${TARGET_DB_CONN_STR}" -f "${DB_SEED_VERSION}" -n "${n_param}" -m "${m_param}" \n')
	  
	downstreamParameterized {
            trigger('ResetDBPasswords') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('FAILURE')
                }
            }
        }
    }
}

job(under_folder +'/CreateRDSBase') {
    
    parameters {
        stringParam('ENV_PREFIX','','environment identifier')
        labelParam('DeploymentSlave')
        stringParam('Charge_Code')
    } 
    
    steps {
        shell('cd /deployment/ansible \n'+
	      'sleep ${WAIT_FOR} \n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-iaas/playbooks/createOracleDatabase.yml -e "inventory_group=atg-ora svr_create=yes chargeCode=${Charge_Code}" -vvvv \n')  
    }
}

job(under_folder +'/ExportDBToSeedLibrary') {

    logRotator {
      daysToKeep -1
      numToKeep 10
    } 
    
    parameters {
        stringParam('SOURCE_DB_USER','oraown')
        stringParam('SOURCE_DB_PASSWORD','***************') 
        stringParam('SOURCE_DB_SID') 
        stringParam('SOURCE_DB_CONN_STR','(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${SOURCE_DB_SID}.cgedocbjqkfs.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=${SOURCE_DB_SID})))')
        stringParam('TARGET_DB_USER','oraown')
        stringParam('TARGET_DB_PASSWORD','***************')
        stringParam('TARGET_DB_CONN_STR','DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=atgdev02.cgedocbjqkfs.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=ATGDEV02)))')
        labelParam('DeploymentSlave')
        stringParam('DB_SEED_ENV')
        stringParam('SCRIPTS_DIR','/deployment/ecomm/database')
        stringParam('ENV_PREFIX','','environment identifier')
    } 
    
    customWorkspace('/deployment/ecomm/database');
    
    steps {
        shell('export PATH=$PATH:${ORACLE_HOME}/bin \n'+
	      'export LD_LIBRARY_PATH=:${ORACLE_HOME}/lib \n'+
	      '\n'+
	      'cd ${SCRIPTS_DIR} \n'+
	      './export_database_to_seed_library.ksh -u ${SOURCE_DB_USER} -p ${SOURCE_DB_PASSWORD} -U ${TARGET_DB_USER} -P ${TARGET_DB_PASSWORD} -t "${SOURCE_DB_CONN_STR}" -T "${TARGET_DB_CONN_STR}" -f "${DB_SEED_VERSION}" -s "${SOURCE_DB_SID}" \n')  
    }
    publishers {
        downstreamParameterized {
            trigger('/Infra/Release/TagSearchIndexingFiles') {        
                condition('SUCCESS')
            }
        }
    }
}

job(under_folder +'/ResetDBPasswords') {

    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX','','environment identifier')
        stringParam('SCRIPTS_DIR','/deployment/ecomm/database')
    } 
    
    steps {
        shell('#!/bin/sh \n'+
	      'export PATH=$PATH:${ORACLE_HOME}/bin \n'+
	      'export LD_LIBRARY_PATH=:${ORACLE_HOME}/lib \n'+
	      '\n'+
	      'cd ${SCRIPTS_DIR} \n'+
	      '. ./reset_passwords.ksh ${ENV_PREFIX} \n')  
    }
}

