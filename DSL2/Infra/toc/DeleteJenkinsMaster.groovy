def under_folder = 'Infra/toc'
folder(under_folder)

def job_name = under_folder +'/DeleteJenkinsMaster'

job(job_name) {
    description 'Delete a Jenkins Master'
    logRotator {
      daysToKeep -1
      numToKeep 20
    }
    parameters {
        stringParam('team','','Name of Jenkins Instance')
         labelParam('SharedSlave')
        {
         defaultValue('atg-toc-aws-slv01')
        }
       
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell ('cd /deployment/ansible \n\
			  ansible-playbook -i is-inventory/group_hosts/${team}-hosts is-iaas/playbooks/deleteEc2Instance.yml -e "ec2_host_name=jenkins dnsUser=automation" -vv')
    }  
}