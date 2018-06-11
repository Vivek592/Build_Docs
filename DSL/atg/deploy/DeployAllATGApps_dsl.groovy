def under_folder = '/ATG/Deploy/'


buildFlowJob(under_folder +'DeployAllATGApps') {
    description 'This deployes all ATG applications'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
       
    }  
    
   buildFlow(
         
        "parallel ( { build('DeployATGApp',persona: 'storefront' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave']) }, \n\
                   { build('DeployATGApp',persona: 'agent' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave']) }, \n\
                   { build('DeployATGApp',persona: 'staging' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave']) }, \n\
                   { build('DeployATGApp',persona: 'auxagent',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'] )}, \n\
                   { build('DeployATGApp',persona: 'auxstorefront',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'] )}, \n\
                   { build('DeployATGApp',persona: 'fulfillment' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave']) }, \n\
                   { build('DeployATGApp',persona: 'lockstorefront' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave']) }, \n\
                   { build('DeployATGApp',persona: 'lockagent' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave']) } \n\
                   { build('DeployATGApp',persona: 'publishing' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave']) },\n\
                   { build('DeployATGApp',persona: 'auxindex' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'])},\n\
                   { build('ATG/Deploy/DeployStaticContent' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave']) }\n\
                   )\n\
          build('DownloadDeviceAtlas',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave']) "
         )
            
            
        
    }  

job(under_folder +'DeployATGApp') {
    description 'Deploys a given ATG application'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('persona','','Environment  pre-fix')
        labelParam('DeploymentSlave')
       
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell ('cd /deployment/ansible \n'+
              'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/deployEarToJboss.yml  -e "inventory_group=${persona} svr_create=yes"')
    }  
}



job(under_folder +'DeployStaticContent') {
    description 'Deploys static content'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
       
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell ('cd /deployment/ansible \n'+
              'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/deployStaticContentToApache.yml  -e "inventory_group=web svr_create=yes"')
    }  
}



