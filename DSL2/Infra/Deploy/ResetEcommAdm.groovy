def top_folder = '/Infra'
def under_folder = '/Infra/Deploy'

folder(top_folder)
folder(under_folder)

job(under_folder +'/ResetEcommAdm') {
    
    parameters {
        stringParam('ENV_PREFIX','','environment identifier')
        labelParam('DeploymentSlave')
        stringParam('group_name')
    } 
 
    concurrentBuild(true)

    steps {
        shell('cd /deployment/ansible \n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/updateEcommadm.yml -e "inventory_group=${group_name}" -vvvv \n')  
    }
}


