def top_folder = '/ATG'
def under_folder = '/ATG/Deploy'
def under_folder2 = '/ATG/Deploy/Endeca'

folder(top_folder)
folder(under_folder)
folder(under_folder2)

job(under_folder2 +'/ExecuteEndecaScripts') {

    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX')
        labelParam('DeploymentSlave')
        stringParam('scriptToExecute')
        stringParam('scriptDir','/app/endeca/apps/bqendeca/control')
    }  
    
    concurrentBuild(true)
     
    steps {
	   
        shell ('cd /deployment/ansible\n\n'+
    	    'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts  is-appsetup/playbooks/executeEndecaScripts.yml -e "inventory_group=itl  endecaEnv=${ENV_PREFIX} scriptToExecute=${scriptToExecute} scriptDir=${scriptDir}" -vvvv')
    }  
}

