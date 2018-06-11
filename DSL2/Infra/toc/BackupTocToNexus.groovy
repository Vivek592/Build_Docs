
def under_folder = 'Infra/toc'
folder(under_folder)

def job_name = under_folder +'/BackupTocToNexus'

job(job_name) {
    description 'Backups a TOC Master'
    logRotator {
      daysToKeep -1
      numToKeep 20
    }
    parameters {
        stringParam('JENKINS_NEXUS_URL','${NEW_NEXUS_RELEASE_LOCATION}/kf/devops/jenkins/${team}/${team}-jenkins.tar','Nexus url of jenkins backupp')
        stringParam('team','toc','Name of Jenkins Instance')
        stringParam('NEXUS_USR','Srv-jenkins')
        stringParam('NEXUS_PWD','UF549kN6BcykCY2d')
       
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell ('cd /deployment/ \n\
             tar -cf ${team}-jenkins.tar \n\
             jenkins/ --exclude="jenkins/logs" --exclude="jenkins/jobs/**/builds" --exclude=".git" --exclude="jenkins/**/.git/" \n\
              curl --upload-file ${team}-jenkins.tar ${JENKINS_NEXUS_URL} --user ${NEXUS_USR}:${NEXUS_PWD} \n\
              rm -f ${team}-jenkins.tar ')
              
      
    }  
}
