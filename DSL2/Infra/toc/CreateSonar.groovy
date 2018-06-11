
def under_folder = 'Infra/toc'
folder(under_folder)

def job_name = under_folder +'/CreateSonar'

job(job_name) {
    description 'Creates a Sonar'
    logRotator {
      daysToKeep -1
      numToKeep 20
    }
    parameters {
        stringParam('Charge_Code','','Charge code for the environment')
        stringParam('svr_create','yes','yes: creates server, No: Does not create server')
        stringParam('instId','01','Intance Id of Sonar i.e 01,02...')
        stringParam('team','','Team name..')
        labelParam('SharedSlave')
        {
         defaultValue('atg-toc-aws-slv01')
        }
       
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell ('cd /deployment/ansible \n\
			  ansible-playbook -i is-inventory/group_hosts/${team}-hosts is-appsetup/playbooks/deploySonar.yml -e "inventory_group=sonar svr_create=${svr_create} chargeCode=${Charge_Code} instId=${instId}" -vv')
    }  
}
