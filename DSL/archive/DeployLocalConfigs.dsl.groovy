def under_folder = '/ATG/Deploy/'


buildFlowJob(under_folder +'DeployAllLocalConfigs') {
    description 'This deployes all ATG configurations '
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
       
    }  
    
   buildFlow("parallel(\n\
                   { build('DeployLocalConfigs',persona: 'storefront' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave']) },\n\
                   { build('DeployLocalConfigs',persona: 'agent' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave']) },\n\
                   { build('DeployLocalConfigs',persona: 'staging' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave']) },\n\
                   { build('DeployLocalConfigs',persona: 'auxagent',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'] )},\n\
                   { build('DeployLocalConfigs',persona: 'auxstorefront',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'] )},\n\
                   { build('DeployLocalConfigs',persona: 'fulfillment' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave']) },\n\
                   { build('DeployLocalConfigs',persona: 'lockstorefront' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave']) },\n\
                   { build('DeployLocalConfigs',persona: 'lockagent' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave']) },\n\
                   { build('DeployLocalConfigs',persona: 'publishing' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave']) }\n\
                ) "
         )
    }
    
job(under_folder +'DeployLocalConfigs') {
    description 'Deploys  ATG configurations'
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
              'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/deployLocalconfigToJboss.yml  -e "inventory_group=${persona} svr_create=yes"')
    }  
}  


