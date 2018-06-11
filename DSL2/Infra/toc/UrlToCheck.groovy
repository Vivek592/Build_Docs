
def under_folder = 'Infra/toc'
folder(under_folder)

def job_name = under_folder +'/UrlToCheck'

job(job_name) {
    description 'Checks if URL is available'
    logRotator {
      daysToKeep -1
      numToKeep 20
    }
    parameters {
        stringParam('URL_TO_TEST','','Name of Jenkins Instance')
        labelParam('SharedSlave')
        {
         defaultValue('atg-toc-aws-slv01')
        }
       
    }  
    
    concurrentBuild(true)
    
    steps {
	    httpRequest('${URL_TO_TEST}') {
	            httpMode('GET')
	            returnCodeBuildRelevant()
	        }
        }  
}
