def top_folder = '/Infra'
def under_folder = '/Infra/Database'

folder(top_folder)
folder(under_folder)

def workspace = '/deployment/ansible/'
def project_ws = ['is-appsetup','is-iaas','is_inventory']

def job_name= under_folder +'/ExportDBToSeedLibrary'

job(job_name) {
    description 'Export database and move seed to Library'
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
        stringParam('SOURCE_DB_SID')
		stringParam('SOURCE_DB_CONN_STR','(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${SOURCE_DB_SID}.${DB_SERVER}.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=${SOURCE_DB_SID})))')
		stringParam('TARGET_DB_USER','oraown')
                password{
                  name('TARGET_DB_PASSWORD')
                  defaultValue('ChangeMeNow')
                  description('ChangeMeNow')
                     }
        stringParam('TARGET_DB_CONN_STR','(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=atgdev02.czudcasym0md.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=ATGDEV02)))')
		labelParam('DeploymentSlave')
		stringParam('DB_SEED_VERSION')
		stringParam('SCRIPTS_DIR','/deployment/ecomm/database')
		stringParam('ENV_PREFIX')
		stringParam('DB_SERVER','czudcasym0md','cgedocbjqkfs for sandpit czudcasym0md for dev')
                }
customWorkspace('/deployment/ecomm/database');
steps {

      shell('export PATH=$PATH:${ORACLE_HOME}/bin \n'+
			'export LD_LIBRARY_PATH=:${ORACLE_HOME}/lib \n'+

			'cd ${SCRIPTS_DIR} \n'+
			'./export_database_to_seed_library.ksh -u ${SOURCE_DB_USER} -p ${SOURCE_DB_PASSWORD} -U ${TARGET_DB_USER} -P ${TARGET_DB_PASSWORD} -t "${SOURCE_DB_CONN_STR}" -T "${TARGET_DB_CONN_STR}" -f "${DB_SEED_VERSION}" -s "${SOURCE_DB_SID}"')
}
}

