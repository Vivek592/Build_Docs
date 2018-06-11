def top_folder = '/WMB'
def under_folder = '/WMB/Deploy'

folder(top_folder)
folder(under_folder)

job(under_folder +'/StatusCheckWMB') {
    description 'Restart a given WMB application'
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        choiceParam('action', ['status'],'use restart to stop & start the app')
        stringParam('ENV_PREFIX')
        labelParam('DeploymentSlave')
        booleanParam('failIfDown')
    }
    
    concurrentBuild(true)
     
    steps {
	   
        shell ('cd /deployment/ansible\n'+
    	    'ansible-playbook -i is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/serviceIntegration.yml -e "action=${action} includeMB=yes failIfDown=${failIfDown}"')
    }  
} 
