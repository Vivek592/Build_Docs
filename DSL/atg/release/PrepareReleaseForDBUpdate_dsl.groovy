def under_folder = '/ATG/Release/'
def job_name = under_folder +'PrepareReleaseForDBUpdate'
job(job_name) {
    description 'This downloads a given ATG release from nexus into a slave and prepares for deployment '
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('APP_VERSION','','ATG release number')
        stringParam('DOWNLOAD_RELEASE_TO','/deployment/ecomm/release/atg','Location to download release')
        labelParam('DeploymentSlave')
       
    }  
    
    
    steps {
	shell ('  rm -rf ${DOWNLOAD_RELEASE_TO} \n'+
		' mkdir -p ${DOWNLOAD_RELEASE_TO} \n'+
		' cd ${DOWNLOAD_RELEASE_TO} \n'+
		' \n'+
		' \n'+
		' \n'+
		'scp  ecommadm@${AnsibleSlave}:${DOWNLOAD_RELEASE_TO}/atg_modules.zip . \n'+
		'scp  ecommadm@${AnsibleSlave}:${DOWNLOAD_RELEASE_TO}/kf-install-0.2.0.jar . \n'+
		' \n'+
		'rm -f ${ATG_HOME}/atg_modules.zip  \n'+
		' \n'+
		'cp atg_modules.zip ${ATG_HOME} \n'+
		' \n'+
		'cd ${ATG_HOME} \n'+
		' \n'+
		'rm -rf KF \n'+
		'  \n'+
		'jar xf atg_modules.zip \n'+
		' \n'+
		'## END  PREPARE ATG MODULES ## \n'+
		' \n'+
		' \n'+
		'## PREPARE ENV INSTALL ## \n'+
		' \n'+
		'rm -rf ${DB_UPDATE_FOLDER}/${ENV_PREFIX} \n'+
		'mkdir -p ${DB_UPDATE_FOLDER}/${ENV_PREFIX} \n'+
		'cp ${DOWNLOAD_RELEASE_TO}/kf-install-0.2.0.jar ${DB_UPDATE_FOLDER}/${ENV_PREFIX} \n'+
		'cd ${DB_UPDATE_FOLDER}/${ENV_PREFIX} \n'+
		' \n'+
		'jar xf kf-install-0.2.0.jar \n'+
		' \n'+
		' \n'+
		'## END PREPARE ENV INSTALL ##')
    }  
}
