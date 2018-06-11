
def under_folder = 'Infra/toc'
folder(under_folder)

def job_name = under_folder +'/AttachEnvironmentToJenkinsMaster'

job(job_name) {
    description 'Add existing slave to this master'
    logRotator {
      daysToKeep -1
      numToKeep 20
    }
    parameters {
        stringParam('team','','Name of Jenkins Instance')
        stringParam('ENV_PREFIX','','Environment Name')
        labelParam('SharedSlave')
        {
         defaultValue('atg-toc-aws-slv01')
        }
       
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell ('cd /deployment/ansible \n\
			  ansible-playbook is-appsetup/playbooks/startJenkinsSwarm.yml -i is-inventory/group_hosts/${ENV_PREFIX}-hosts -e "inventory_group=slaveATG jenkins_master=jenkins-${team}-master.aws.ghanp.kfplc.com"  -vv')
    }  
}
