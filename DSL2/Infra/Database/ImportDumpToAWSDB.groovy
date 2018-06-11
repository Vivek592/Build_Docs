def top_folder = '/Infra'
def under_folder = '/Infra/Database'

folder(top_folder)
folder(under_folder)

def workspace = '/deployment/ansible/'
def project_ws = ['is-appsetup','is-iaas','is_inventory']

def job_name= under_folder +'/ImportDumpToAWSDB'

job(job_name) {
    description 'Import Database Dump'
    logRotator {
      daysToKeep -1
      numToKeep 10
    }

parameters {
        stringParam('SOURCE_ENV')
		stringParam('AWS_DB_USER','oraown')
		stringParam('AWS_DB_CONN_STR','(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${AWS_DB_SERVER})(PORT=1527))(CONNECT_DATA=(SID=${AWS_DB_SID})))')
		labelParam('DeploymentSlave')
		stringParam('SCRIPTS_DIR','/deployment/ecomm/database')
		stringParam('AWS_DB_SID')
		stringParam('AWS_DB_SERVER','${AWS_DB_SID}.czudcasym0md.eu-west-1.rds.amazonaws.com')
        }
customWorkspace('/deployment/ecomm/database');
steps {

      shell('export PATH=$PATH:${ORACLE_HOME}/bin \n'+
			'export LD_LIBRARY_PATH=:${ORACLE_HOME}/lib \n'+

			'cd ${SCRIPTS_DIR} \n'+


			'. ./imp_onpremise_seed_aws.ksh -s ${SOURCE_ENV} -d ${AWS_DB_SID} -T ${AWS_DB_CONN_STR}')
}
}	 
