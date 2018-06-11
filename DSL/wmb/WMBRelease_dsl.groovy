def top_folder = '/WMB'
def under_folder = '/WMB/Release'

folder(top_folder)
folder(under_folder)

job(under_folder +'/PrepareWMBReleaseFromNexus') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('APP_VERSION')
        stringParam('DOWNLOAD_RELEASE_TO','/deployment/ecomm/wmbdeployment')
        labelParam('DeploymentSlave')
    }  
    
    concurrentBuild(true)
     
    steps {
	   
        shell ('rm -rf ${DOWNLOAD_RELEASE_TO}\n'+
            'mkdir -p ${DOWNLOAD_RELEASE_TO}\n'+
            'cd ${DOWNLOAD_RELEASE_TO}\n'+
            'wget -q ${NEW_NEXUS_RELEASE_LOCATION}/kf/wmb/devops/${APP_VERSION}/${APP_VERSION}.zip\n'+
            'unzip -xvf ${APP_VERSION}.zip\n'+
            'rm -f ${APP_VERSION}.zip'
    	)
    }  
}

