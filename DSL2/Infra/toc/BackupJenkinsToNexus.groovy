
def under_folder = 'Infra/toc'
folder(under_folder)

def job_name = under_folder +'/BackupJenkinsToNexus'

job(job_name) {
    description 'Backups a Jenkins Master'
    logRotator {
      daysToKeep -1
      numToKeep 20
    }
    parameters {
        stringParam('JENKINS_NEXUS_URL','${NEW_NEXUS_RELEASE_LOCATION}/kf/devops/jenkins/${team}/${team}-jenkins.tar','Nexus url of jenkins backupp')
        stringParam('team','','Name of Jenkins Instance')
        stringParam('NEXUS_USR','Srv-jenkins')
        stringParam('NEXUS_PWD','UF549kN6BcykCY2d')
         labelParam('SharedSlave')
        {
         defaultValue('atg-toc-aws-slv01')
        }
       
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell ('cd /deployment/ansible \n\
			  ansible-playbook -i is-inventory/group_hosts/${team}-hosts is-appsetup/playbooks/backupJenkinsToNexus.yml -e "inventory_group=jenkins instName=${team} JENKINS_NEXUS_URL=${JENKINS_NEXUS_URL} NEXUS_USR=${NEXUS_USR} NEXUS_PWD=${NEXUS_PWD}" -vv')
    }  
}
