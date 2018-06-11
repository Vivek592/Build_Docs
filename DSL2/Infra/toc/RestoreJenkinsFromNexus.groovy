
def under_folder = 'Infra/toc'
folder(under_folder)

def job_name = under_folder +'/RestoreJenkinsFromNexus'

job(job_name) {
    description 'Restores a Jenkins Master from nexus'
    logRotator {
      daysToKeep -1
      numToKeep 20
    }
    parameters {
        stringParam('JENKINS_NEXUS_URL','${NEW_NEXUS_RELEASE_LOCATION}/kf/devops/jenkins/${team}/${team}-jenkins.tar','Nexus url of jenkins backupp')
        stringParam('team','','Name of Jenkins Instance')
        booleanParam('clean_jenkins_home', false)
         labelParam('SharedSlave')
        {
         defaultValue('atg-toc-aws-slv01')
        }
       
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell ('cd /deployment/ansible \n\
			  ansible-playbook -i is-inventory/group_hosts/${team}-hosts is-appsetup/playbooks/restoreJenkinsFromNexus.yml -e "inventory_group=jenkins instName=${team} JENKINS_NEXUS_URL=${JENKINS_NEXUS_URL} clean_jenkins_home=${clean_jenkins_home}" -vv')
    }  
}
