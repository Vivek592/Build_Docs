def top_folder = '/Infra'
def under_folder = '/Infra/Database'

folder(top_folder)
folder(under_folder)

def workspace = '/deployment/ansible/'
def project_ws = ['is-appsetup','is-iaas','is_inventory']

def job_name= under_folder +'/FTPDBFileToAWSDB'

job(job_name) {
    description 'FTP on premise database dump'
    logRotator {
      daysToKeep -1
      numToKeep 10
    }

parameters {
        stringParam('SOURCE_DB_USER','atgseed')
		password{
		  name('SOURCE_DB_PASSWORD')
		  defaultValue('Password!1234567')
		  description('Source Database Password')
		     }
		stringParam('SOURCE_DB_SERVER')
		stringParam('SOURCE_DB_SID')
		stringParam('SOURCE_DB_CONN_STR','(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${SOURCE_DB_SERVER})(PORT=1527))(CONNECT_DATA=(SID=${SOURCE_DB_SID})))')
		labelParam('DeploymentSlave')
		stringParam('SCRIPTS_DIR','/deployment/ecomm/database')
		stringParam('AWS_DB_USER','oraown')
		password{
		  name('AWS_DB_PASSWORD')
		  defaultValue('ChangeMeNow')
		  description('ChangeMeNow')
		     }
	    stringParam('AWS_DB_SID')
		stringParam('AWS_DB_SERVER','${AWS_DB_SID}.czudcasym0md.eu-west-1.rds.amazonaws.com')
		stringParam('AWS_DB_CONN_STR','(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${AWS_DB_SERVER})(PORT=1527))(CONNECT_DATA=(SID=${AWS_DB_SID})))')
                stringParam('SEED_VERSION','${SOURCE_ENV}')
          }
customWorkspace('/deployment/ecomm/database');
steps {

      shell('export PATH=$PATH:${ORACLE_HOME}/bin \n'+
			'export LD_LIBRARY_PATH=:${ORACLE_HOME}/lib \n'+

			'cd ${SCRIPTS_DIR} \n'+


			'. ./ftp_seed_file.ksh  -u ${SOURCE_DB_USER} -p ${SOURCE_DB_PASSWORD} -t ${SOURCE_DB_CONN_STR} -s ${SOURCE_ENV} -T ${AWS_DB_CONN_STR}')
}
}	 
