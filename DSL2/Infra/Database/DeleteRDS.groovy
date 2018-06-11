def top_folder = '/Infra'
def under_folder = '/Infra/Database'

folder(top_folder)
folder(under_folder)

def workspace = '/deployment/ansible/'
def project_ws = ['is-appsetup','is-iaas','is_inventory']

def job_name= under_folder +'/DeleteRDS'

job(job_name) {
    description 'delete RDS'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }

parameters {
        labelParam('DeploymentSlave')
		stringParam('ENV_PREFIX')
		stringParam('INVENTORY_FILE','${ENV_PREFIX}-hosts')
		}
steps {
      shell('envId=`echo ${ENV_PREFIX} | sed \'s/[^0-9]*//g\'` \n'+
            'envName=`echo ${ENV_PREFIX} | sed \'s/[0-9]*//g\'` \n'+
            '[ -z ${envName} ] && ( echo "FAILED: envName is null" ;exit 1 ) \n'+
            'cd /deployment/ansible \n'+
            'ansible-playbook --limit ${ENV_PREFIX}:local:dns  is-iaas/playbooks/deleteRds.yml -e "envId=${envId} envName=${envName} inventory_group=atg-ora instance_name=${ENV_PREFIX} delete_db=yes" -vvvv')
}
}
