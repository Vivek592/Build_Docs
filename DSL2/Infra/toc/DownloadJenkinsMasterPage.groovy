
def under_folder = 'Infra/toc'
folder(under_folder)

def job_name = under_folder +'/DownloadJenkinsMasterPage'

job(job_name) {
    description 'Checks if URL is available'
    logRotator {
      daysToKeep -1
      numToKeep 20
    }
    parameters {
        stringParam('team','',' Jenkins Instance')
        stringParam('URL_TO_TEST','',' Jenkins Url')
        
        labelParam('SharedSlave')
        {
         defaultValue('master')
        }
       
    }  
    
    concurrentBuild(true)
    
    steps {
	    shell ('cd ${JENKINS_HOME}/userContent/jenkins ;  rm -rf ${team} ; mkdir ${team}; cd ${team}; wget ${URL_TO_TEST}')
        }  
}
