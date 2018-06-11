def under_folder = 'Infra/Deploy'
folder(under_folder)

def job_name = under_folder +'/CreateATGAppEnv'

job(job_name) {
    description 'Creates a given ATG server'
    logRotator {
      daysToKeep -1
      numToKeep 20
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('group_name','','Persona')
        stringParam('Charge_Code','','Charge code for the environment')
        stringParam('WAIT_FOR','','Waits for given number of seconds before ansible command is executed')
        stringParam('svr_create','','yes: creates server, No: Does not create server')
        labelParam('DeploymentSlave')
       
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell ('cd /deployment/ansible \n\
			  sleep ${WAIT_FOR} \n\
			  ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/configureJbossATG.yml \n\
			 -e "inventory_group=${group_name} svr_create=${svr_create} chargeCode=${Charge_Code}" -vv')
    }  
}