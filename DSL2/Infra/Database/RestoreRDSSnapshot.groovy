def top_folder = '/Infra'
def under_folder = '/Infra/Database'

folder(top_folder)
folder(under_folder)

def workspace = '/deployment/ansible/'
def project_ws = ['is-appsetup','is-iaas','is_inventory']

job(under_folder +'/RestoreRDSSnapshot') {
    
    parameters {
        stringParam('ENV_PREFIX','','environment identifier')
        labelParam('DeploymentSlave')
        stringParam('INVENTORY_FILE','${ENV_PREFIX}-hosts')
    } 

    wrappers {
        environmentVariables  {
            propertiesFile('/deployment/ecomm/rds/snapshot.properties')
        }
        
    }

    steps {
        shell('if [ "$snapshot_name" == "" ];then \n'+
              'echo "no snapshot_name provided" \n'+
              'exit 1 \n'+
              'fi \n'+
              'cd /deployment/ansible \n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${INVENTORY_FILE} is-iaas/playbooks/restoreRdsSnapshot.yml -e "inventory_group=atg-ora rds_instance_name=${ENV_PREFIX} snapshot_name=${snapshot_name}" -vvvv \n')  
    }
}


