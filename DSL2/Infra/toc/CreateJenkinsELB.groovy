
def under_folder = 'Infra/toc'
folder(under_folder)

def job_name = under_folder +'/CreateJenkinsELB'

job(job_name) {
    description 'Creates ELB for Jenkins'
    logRotator {
      daysToKeep -1
      numToKeep 20
    }
    parameters {
        stringParam('Charge_Code','','Charge code for the environment')
        stringParam('svr_create','yes','yes: creates server, No: Does not create server')
        stringParam('team','','Team name')
        labelParam('SharedSlave')
        {
         defaultValue('atg-toc-aws-slv01')
        }
       
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell ('cd /deployment/ansible \n\
			  ansible-playbook -i is-inventory/group_hosts/${team}-hosts is-iaas/playbooks/createJenkinsELB.yml -e "inventory_group=elb svr_create=${svr_create} chargeCode=${Charge_Code} instName=${team}" -vv')
    }  
}
