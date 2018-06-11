def top_folder = '/Infra'
def under_folder = '/Infra/Database'

folder(top_folder)
folder(under_folder)

def workspace = '/deployment/ansible/'
def project_ws = ['is-appsetup','is-iaas','is_inventory']

def job_name= under_folder +'/CreateRDSBase'

job(job_name) {
    description 'Create Database from snapshot'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }

parameters {
		labelParam('DeploymentSlave')
		stringParam('ENV_PREFIX')
		stringParam('Charge_Code')
		stringParam('INVENTORY_FILE','${ENV_PREFIX}-hosts')
		stringParam('rds_instance_type','db.m3.large')
		stringParam('rds_db_size_GB','100')
		}
steps {

      shell('envId=`echo ${ENV_PREFIX} | sed \'s/[^0-9]*//g\'` \n'+
			'envName=`echo ${ENV_PREFIX} | sed \'s/[0-9]*//g\'` \n'+
			'[ -z ${envName} ] && ( echo "FAILED: envName is null" ;exit 1 ) \n'+
			'cd /deployment/ansible \n'+
			'ansible-playbook --limit ${ENV_PREFIX}:local:dns  is-iaas/playbooks/createOracleDatabase.yml -e "envId=${envId} envName=${envName} inventory_group=atg-ora svr_create=yes chargeCode=${Charge_Code} rds_instance_type=${rds_instance_type} rds_db_size_GB=${rds_db_size_GB}" -vvvv')

}
}	 
