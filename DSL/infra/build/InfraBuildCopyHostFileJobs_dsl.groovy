def top_folder = '/Infra'
def under_folder = '/Infra/Build'

folder(top_folder)
folder(under_folder)

job(under_folder +'/CopyHostFileToAnsibleSlave') {
    
    logRotator {
      daysToKeep -2
      numToKeep 30
    }
    
    parameters {
    	  labelParam('DeploymentSlave') { 
	  defaultValue('atg-shared-aws-slv01') 
	  description('Deployment Slave label') 
	  stringParam('ENV_PREFIX')
	} 
        
    } 
    
    steps {
        shell('scp /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts atg-${ENV_PREFIX}-aws-slv01.aws.gha.kfplc.com:/deployment/ansible/is-inventory/group_hosts/')
    }
}

job(under_folder +'/CreateHostFileFromTemplate') {
    
    logRotator {
      daysToKeep -2
      numToKeep 30
    }
    
    parameters {
        stringParam('ENV_PREFIX')
        labelParam('DeploymentSlave')
    } 
    
    steps {
        shell('rm -f ${ENV_PREFIX}-hosts \n'+
         'export env_no=${ENV_PREFIX#*devops} \n'+
         'echo ${env_no} \n'+
         'cp devops00-hosts-template ${ENV_PREFIX}-hosts \n'+
         'ls -l \n'+
         'sed -i -e \'s/envId="00"/envId="\'${env_no}\'"/g\' ${ENV_PREFIX}-hosts \n'+
         'ls -l \n'
         )
    }
}

