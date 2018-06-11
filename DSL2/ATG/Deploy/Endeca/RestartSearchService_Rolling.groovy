def top_folder = '/ATG'
def under_folder = '/ATG/Deploy'
def under_folder2 = '/ATG/Deploy/Endeca'

folder(top_folder)
folder(under_folder)
folder(under_folder2)

job(under_folder2 +'/RestartSearchService_Rolling') {
    description 'Stop/Start ITL services on Tomcat'
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        choiceParam('state', ['started','stopped'])
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
        stringParam('inventory_group')
    }  
    
    concurrentBuild(true)
     
    steps {
	   
        shell ('cd /deployment/ansible\n\n'+
    	    'ansible-playbook -i is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/setTomcatState.yml -e "inventory_group=${inventory_group} tomcatState=${state}" --limit ${inventory_group}[${instanceId}]')
    }  
}
