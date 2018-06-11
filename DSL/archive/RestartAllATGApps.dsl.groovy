def under_folder = '/ATG/Deploy/'


buildFlowJob(under_folder +'RestartAllATGApps') {
    description 'This stops or starts a given environment '
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('action','','start or stop')
        labelParam('DeploymentSlave')
       
    }  
        deliveryPipelineConfiguration("Stop or Start","Restart");

   buildFlow("parallel(\n\
                   { build('RestartATGApp',persona: 'lockstorefront' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'],action: params['action']) }, \n\
                   { build('RestartATGApp',persona: 'lockagent' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'],action: params['action']) }\n\
 				 )\n\
                parallel (\n\
				   { build('RestartATGApp',persona: 'storefront' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'],action: params['action']) }, \n\
                   { build('RestartATGApp',persona: 'agent' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'],action: params['action']) }, \n\
                   { build('RestartATGApp',persona: 'staging' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'],action: params['action']) }, \n\
                   { build('RestartATGApp',persona: 'auxagent',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'] ,action: params['action'])}, \n\
                   { build('RestartATGApp',persona: 'auxstorefront',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'] ,action: params['action'])}, \n\
                   { build('RestartATGApp',persona: 'fulfillment' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'],action: params['action']) } \n\
         		)\n\
 				build('RestartATGApp',persona: 'publishing' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'],action: params['action'])\n\
 				build('RestartATGApp',persona: 'search' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'],action: params['action'])"   
       
      		 )
            
            
        
    }  


job(under_folder +'RestartATGApp') {
    description 'Restart a given ATG application'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('persona','','Environment  pre-fix')
        labelParam('DeploymentSlave')
        choiceParam('action', ['start','stop','restart'],'use restart to stop & start the app')
       
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell (' cd /deployment/ansible \n' +
              'ansible-playbook -i is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/ATGctrl.yml  -e "inventory_group=${persona} request=${action}"')
    }  
}

