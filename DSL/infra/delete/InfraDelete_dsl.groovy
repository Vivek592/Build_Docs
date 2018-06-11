def top_folder = '/Infra'
def under_folder = '/Infra/Delete'

folder(top_folder)
folder(under_folder)

job(under_folder +'/DeleteAnsibleSlave') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        labelParam('DeploymentSlave')
        { 
            defaultValue('atg-shared-aws-slv01') 
            allNodes('allCases', 'IgnoreOfflineNodeEligibility')
        } 
        stringParam('ENV_PREFIX','','environment identifier')
    } 
    
    customWorkspace('/deployment/ansible/');
    
    steps {
        shell('cd /deployment/ansible \n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts /deployment/ansible/is-iaas/playbooks/deleteATGEnv.yml -e \'delete_db=yes\' \n')  
    }
}

job(under_folder +'/DeleteATGEnv') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        labelParam('DeploymentSlave')
        { 
            defaultValue('atg-shared-aws-slv01') 
        } 
        stringParam('ENV_PREFIX','','environment identifier')
    } 
    
    customWorkspace('/deployment/ansible/');
    
    steps {
        downstreamParameterized {
            trigger('Infra/Release/PrepareATGInventoryFromNexus') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
            }
        }
        shell('cd /deployment/ansible \n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts /deployment/ansible/is-iaas/playbooks/deleteATGEnv.yml -e \'delete_db=yes\' \n')  
    }
}