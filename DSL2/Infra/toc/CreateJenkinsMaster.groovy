
def under_folder = 'Infra/toc'
folder(under_folder)

def job_name = under_folder +'/CreateJenkinsMaster'

job(job_name) {
    description 'Creates a Jenkins Master'
    logRotator {
      daysToKeep -1
      numToKeep 20
    }
    parameters {
        stringParam('Charge_Code','','Charge code for the environment')
        stringParam('team','','Name of Jenkins Instance')
        stringParam('svr_create','yes','yes: creates server, No: Does not create server')
        labelParam('SharedSlave')
        {
         defaultValue('atg-toc-aws-slv01')
        }
       
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell ('cd /deployment/ansible \n\
			  ansible-playbook -i is-inventory/group_hosts/${team}-hosts is-appsetup/playbooks/deployJenkinsMaster.yml -e "inventory_group=jenkins svr_create=${svr_create} chargeCode=${Charge_Code} instName=${team}" -vv')
    }  
}
