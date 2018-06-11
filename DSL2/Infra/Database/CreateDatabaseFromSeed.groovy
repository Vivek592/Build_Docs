def top_folder = '/Infra'
def under_folder = '/Infra/Database'

folder(top_folder)
folder(under_folder)

def workspace = '/deployment/ansible/'
def project_ws = ['is-appsetup','is-iaas','is_inventory']

def job_name= under_folder +'/CreateDatabaseFromSeed'

job(job_name) {
    description 'Create Database from Seed'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }

parameters {
    	stringParam('SOURCE_DB_USER','oraown')
		password{
                  name('SOURCE_DB_PASSWORD')
                  defaultValue('ChangeMeNow')
                  description('ChangeMeNow')
                     }
		stringParam('LIBRARY_FILE_SOURCE','(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=atgdev02.czudcasym0md.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=ATGDEV02)))')
		stringParam('TARGET_DB_USER','oraown')
		password{
                  name('TARGET_DB_PASSWORD')
                  defaultValue('ChangeMeNow')
                  description('ChangeMeNow')
                     }
		stringParam('TARGET_DB_CONN_STR','(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${ENV_PREFIX}.${DB_SERVER}.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=${ENV_PREFIX})))')
		labelParam('DeploymentSlave')
		stringParam('DB_SEED_ENV')
		stringParam('DB_SEED_VERSION','${DB_SEED_ENV}')
		stringParam('SCRIPTS_DIR','/deployment/ecomm/database')
		stringParam('SID_WHEN_SEED_WAS_CREATED','${DB_SEED_ENV}','Unique Source Environment Identifier / Schema prefix in source dump file')
		stringParam('ENV_PREFIX')
		stringParam('DB_SERVER','czudcasym0md','cgedocbjqkfs for sandpit,czudcasym0md for dev')
		}
steps {

      shell('export PATH=$PATH:${ORACLE_HOME}/bin \n'+
			'export LD_LIBRARY_PATH=:${ORACLE_HOME}/lib \n'+
			'export n_param="$(echo ${ENV_PREFIX} | tr "a-z" "A-Z")" \n'+
			'export m_param="$(echo ${SID_WHEN_SEED_WAS_CREATED} | tr "a-z" "A-Z")" \n'+

			'cd ${SCRIPTS_DIR} \n'+
			'. ./create_database_from_seed.ksh -u ${SOURCE_DB_USER} -p ${SOURCE_DB_PASSWORD} -U ${TARGET_DB_USER} -P ${TARGET_DB_PASSWORD} -t "${LIBRARY_FILE_SOURCE}" -T "${TARGET_DB_CONN_STR}" -f "${DB_SEED_VERSION}" -n "${n_param}" -m "${m_param}"')
	  downstreamParameterized {
			trigger("ResetDBPasswords")
			{
			block {
			buildStepFailure('FAILURE')
			failure('FAILURE')
			unstable('FAILURE')
			}
			parameters{
			currentBuild()    
			}                  
			}
			}
}
}	 
