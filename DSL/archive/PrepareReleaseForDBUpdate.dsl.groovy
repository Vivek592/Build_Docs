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
    
      shell ('  cd ${DOWNLOAD_RELEASE_TO} \n\
 				rm -f ${ATG_HOME}/atg_modules.zip \n\
				cp atg_modules.zip ${ATG_HOME} \n\
				cd ${ATG_HOME} \n\
				rm -rf KF \n\
				jar xf atg_modules.zip \n\
				rm -rf ${DB_UPDATE_FOLDER}/${ENV_PREFIX} \n\
				mkdir -p ${DB_UPDATE_FOLDER}/${ENV_PREFIX} \n\
				cp ${DOWNLOAD_RELEASE_TO}/kf-install-0.2.0.jar ${DB_UPDATE_FOLDER}/${ENV_PREFIX} \n\
				cd ${DB_UPDATE_FOLDER}/${ENV_PREFIX} \n\
				jar xf kf-install-0.2.0.jar')
    }  
}
