def under_folder = '/ATG/Release/'
def job_name = under_folder +'PrepareReleaseFromNexus'
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
        stringParam('ANSIBLE_DEPLOY_DIR','','Ansible deploy directory')
        labelParam('DeploymentSlave')
       
    }  
    
    deliveryPipelineConfiguration("prepare","PrepareReleaseFromNexus");
    
    steps {
    
      shell ('  rm -rf ${DOWNLOAD_RELEASE_TO} \n\
 				mkdir -p ${DOWNLOAD_RELEASE_TO} \n\
 				cd ${DOWNLOAD_RELEASE_TO} \n\
				if [ ! -d ${ANSIBLE_DEPLOY_DIR} ]; then \n\
					mkdir -p ${ANSIBLE_DEPLOY_DIR} \n\
				fi \n\
				wget -q ${NEW_NEXUS_RELEASE_LOCATION}/kf/atg/${APP_VERSION}/${APP_VERSION}.jar \n\
 				jar -xvf ${APP_VERSION}.jar \n\
				rm -f ${APP_VERSION}.jar \n\
 				echo release_version=${APP_VERSION} > build_version.properties  \n\
				rm -rf ${ANSIBLE_DEPLOY_DIR}/jboss \n\
				mkdir -p ${ANSIBLE_DEPLOY_DIR}/jboss/storefront \n\
				cp storefront.ear ${ANSIBLE_DEPLOY_DIR}/jboss/storefront \n\
				mkdir -p ${ANSIBLE_DEPLOY_DIR}/jboss/staging \n\
				cp staging.ear ${ANSIBLE_DEPLOY_DIR}/jboss/staging \n\
				mkdir -p ${ANSIBLE_DEPLOY_DIR}/jboss/auxstorefront \n\
				cp auxstorefront.ear ${ANSIBLE_DEPLOY_DIR}/jboss/auxstorefront \n\
				mkdir -p ${ANSIBLE_DEPLOY_DIR}/jboss/publishing \n\
				cp publishing.ear ${ANSIBLE_DEPLOY_DIR}/jboss/publishing \n\
				mkdir -p ${ANSIBLE_DEPLOY_DIR}/jboss/fulfillment \n\
				cp fulfillment.ear ${ANSIBLE_DEPLOY_DIR}/jboss/fulfillment \n\
				mkdir -p ${ANSIBLE_DEPLOY_DIR}/jboss/agent \n\
				cp agent.ear ${ANSIBLE_DEPLOY_DIR}/jboss/agent \n\
				mkdir -p ${ANSIBLE_DEPLOY_DIR}/jboss/auxagent \n\
				cp auxagent.ear ${ANSIBLE_DEPLOY_DIR}/jboss/auxagent \n\
				mkdir -p ${ANSIBLE_DEPLOY_DIR}/jboss/lockstorefront \n\
				cp lockstorefront.ear ${ANSIBLE_DEPLOY_DIR}/jboss/lockstorefront \n\
				mkdir -p ${ANSIBLE_DEPLOY_DIR}/jboss/lockagent \n\
				cp lockagent.ear ${ANSIBLE_DEPLOY_DIR}/jboss/lockagent \n\
				mkdir -p ${ANSIBLE_DEPLOY_DIR}/ecomm-apache \n\
				cp storefront-0.21.0.zip ${ANSIBLE_DEPLOY_DIR}/ecomm-apache')
    }  
}
