def top_folder = '/Infra'
def under_folder = '/Infra/Control'
def under_folder2 = '/Infra/Control/ssh'
def under_folder3 = '/Infra/Control/VPN'

folder(top_folder)
folder(under_folder)
folder(under_folder2)
folder(under_folder3)

job(under_folder +'/ReTagAwsEnvironment') {
    
    parameters {
        stringParam('DeploymentSlave')
        stringParam('Charge_Code')
        stringParam('ENV_PREFIX')
   } 
 
    concurrentBuild(true)

    steps {
        shell('ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-iaas/playbooks/tag_environment.yml -e "tag_name=Environment tag_value=${ENV_PREFIX} chargeCode=${Charge_Code}"')  
    }
}



