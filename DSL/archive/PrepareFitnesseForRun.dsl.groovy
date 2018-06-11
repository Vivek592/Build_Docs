def under_folder = '/ATG/Test/Fitnesse'
folder(under_folder)
def job_name = under_folder +'/PrepareFitnesseForRun'
job(job_name) {
    description 'This prepares Fitnesse code to run '
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
       
    }  
    
    
    steps {
    
      shell ('  export suite_file_name=cr4_fitnesse_reg_suite.tar.gz \n\
				rm -rf /app/ecomm/fitnesse \n\
				mkdir /app/ecomm/fitnesse \n\
				cd /app/ecomm/fitnesse \n\
				rm -rf BuildFitnesseCodeCR4 \n\
				rm -f ${suite_file_name} \n\
				wget ${NEW_NEXUS_RELEASE_LOCATION}/kf/fitnesse/${suite_file_name} \n\
				tar -xvf ${suite_file_name} \n\
				cd BuildFitnesseCodeCR4/src/main/resources/configurations \n\
				rm -f teconf-${ENV_PREFIX}.properties \n\
				wget ${NEW_NEXUS_RELEASE_LOCATION}/kf/devops/fitnesse/teconf-${ENV_PREFIX}.properties \n\
				cd /app/ecomm/fitnesse/BuildFitnesseCodeCR4/firefoxRC/ \n\
				rm -f  prefs.js \n\
				rm -f cert8.db \n\
				wget ${NEW_NEXUS_RELEASE_LOCATION}/kf/devops/fitnesse/firefoxRC/prefs.js \n\
				wget ${NEW_NEXUS_RELEASE_LOCATION}/kf/devops/fitnesse/firefoxRC/cert8.db \n\
				mkdir /app/ecomm/fitnesse/tools \n\
				cd /app/ecomm/fitnesse/tools \n\
				wget ${NEW_NEXUS_RELEASE_LOCATION}/kf/fitnesse/tools/transformer/fitnesse-html.xsl \n\
				wget ${NEW_NEXUS_RELEASE_LOCATION}/kf/fitnesse/tools/transformer/XmlToHTmlTransformer.jar \n\
				chmod 777 XmlToHTmlTransformer.jar fitnesse-html.xsl')
    }  
}
