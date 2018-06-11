def top_folder = '/WMB'
def under_folder = '/WMB/Deploy'

folder(top_folder)
folder(under_folder)

job(under_folder +'/StopStartTomcat') {
    description 'Stop/Start WMB mock services on Tomcat'
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        choiceParam('state', ['started','stopped'])
        stringParam('ENV_PREFIX')
        labelParam('DeploymentSlave')
    }
    
    concurrentBuild(true)
     
    steps {
	   
        shell ('cd /deployment/ansible\n'+
    	    'ansible-playbook -i is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/setTomcatState.yml -e "inventory_group=tomcat tomcatState=${state}"')
    }  
} 