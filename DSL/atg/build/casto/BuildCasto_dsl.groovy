def top_folder = '/ATG'
def sub_folder = '/ATG/Build'
def under_folder = '/ATG/Build/Casto'

folder(top_folder)
folder(sub_folder)
folder(under_folder)

job(under_folder +'/BuildCasto') {
    
  logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        stringParam('branch','develop','Working branch')
        labelParam('ATGSlave')
       
    } 
        scm {
             git {
             remote {
                 url 'https://github.com/KITSGitHubAdmin/KITS-App_ATG-Dev.git'
                 branch '${branch}'
                 credentials 'ca5be30b-657d-4c0d-8191-edc84a8ad31e'
                 }          
             }
    }
    
    steps {
    
       shell('export PATH=${RUBY_HOME}:/$PATH \n'+
			'export JAVA_HOME=${JDK_VER_TO_USE} \n'+
			'export ATG_HOME=${ATG_HOME} \n'+
			'export JBOSS_HOME=/tmp \n'+
			'buildr clean ATG:CASTO:build emma:xml pmd:rule:xml pmd:cpd:xml -e sqa4 \n'+
			'buildr link test=no -e sqa4 \n' +
			'echo BUILD_VERSION=${GIT_COMMIT} > build_version.properties' )  
    }
    
    publishers {
        pmd('**/pmd.xml') {
            healthLimits(3, 20)
            thresholdLimit('high')
            defaultEncoding('UTF-8')
            thresholds(
                    failedTotal: [ high: 2]
                    
            )
        }
        
        emma('**/reports/emma/coverage.xml') {
            minClass(85)
            maxClass(90)
            minMethod(75)
            maxMethod(80)
            minBlock(65)
            maxBlock(82)
            minLine(69)
            maxLine(82)
           
        }
        
        archiveJunit('**/junit/*.xml')
        
        dry('**/cpd.xml', 80, 20) {
            healthLimits(3, 20)
            thresholdLimit('high')
            defaultEncoding('UTF-8')
            canRunOnFailed(false)
            computeNew(false)
            thresholds(
                    failedTotal: [ high: 18],

            )
            }
    }
    }
    
def assemble_folder=under_folder +"/Assembly"

folder(assemble_folder)

buildFlowJob(assemble_folder +'/AssembleEars') {
    description 'This stops or starts a given environment '
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        labelParam('ATGSlave')
       
    }  

   buildFlow("build('AssembleEar',module_to_assemble: 'ATG:KF:lock' , ATGSlave: params['ATGSlave']) \n\
              parallel (\n\
				   { build('AssembleEar',module_to_assemble: 'ATG:CASTO:Web' , ATGSlave: params['ATGSlave']) }, \n\
                   { build('AssembleEar',module_to_assemble: 'ATG:CASTO:Fulfillment' , ATGSlave: params['ATGSlave']) }, \n\
                   { build('AssembleEar',module_to_assemble: 'ATG:CASTO:Fulfillment-Agent' , ATGSlave: params['ATGSlave']) }, \n\
                   { build('AssembleEar',module_to_assemble: 'ATG:CASTO:Staging' , ATGSlave: params['ATGSlave'] )}, \n\
                   { build('AssembleEar',module_to_assemble: 'ATG:CASTO:Publishing' , ATGSlave: params['ATGSlave'] )}, \n\
                   { build('AssembleEar',module_to_assemble: 'ATG:CASTO:Agent' , ATGSlave: params['ATGSlave']) }, \n\
                   { build('AssembleEar',module_to_assemble: 'ATG:CASTO:Auxiliary-Web' , ATGSlave: params['ATGSlave']) } \n\
                   )"
                   )
          
        
    }  


job(assemble_folder +'/AssembleEar') {
    description 'Assemble a given ATG application'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('module_to_assemble','','Casto Ears to assemble')
        labelParam('ATGSlave')   
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell (' cd ../../BuildCasto/ \n' +
             'export PATH=${RUBY_HOME}:/$PATH \n'+
			'export JAVA_HOME=${JDK_VER_TO_USE} \n'+
			'export ATG_HOME=${ATG_HOME} \n'+
			'export JBOSS_HOME=/tmp \n'+
              'buildr ${module_to_assemble}:assemble ${module_to_assemble}:pack_ear ${module_to_assemble}:package test=no -e sqa3')
    }  
}



def db_folder=under_folder+'/Database'
folder(db_folder)
job(db_folder +'/dbIncAndLoadData') {
    description 'Update Database'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX', '')
	    labelParam('ATGSlave')
		stringParam('production_jdbc_password', 'mh*31cmtawhjwg1nc8')
		stringParam('switching_b_jdbc_password', 'vc6430)922ifrmsfd6')
		stringParam('switching_a_jdbc_password', 'dnbcdv*17217dncma')
		stringParam('publishing_jdbc_password', 'dn(2t5fcntdnmcna14')
		stringParam('staging_jdbc_password', 'r59nh*fddsn216fnvcl')
		stringParam('agent_jdbc_password', 'sdsad29*wj1nfdl27a')
		stringParam('db_url','jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${ENV_PREFIX}.cgedocbjqkfs.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=${ENV_PREFIX})))')
		stringParam('staging_URL', '${db_url}')	
		stringParam('production_URL', '${db_url}')		
		stringParam('switching_a_URL', '${db_url}')	
		stringParam('switching_b_URL', '${db_url}')	 	
		stringParam('publishing_URL', '${db_url}')	 	
		stringParam('agent_URL', '${db_url}')	 
    }  
        
    steps {
    
      shell (' cd ../../BuildCasto/ \n' +
             'export PATH=${RUBY_HOME}:/$PATH \n'+
			'export JAVA_HOME=${JDK_VER_TO_USE} \n'+
			'export ATG_HOME=${ATG_HOME} \n'+
			'export JBOSS_HOME=/tmp \n'+
            'buildr ATG:CASTO:env-install:db_inc ATG:CASTO:env-install:data_load_data -e ${ENV_PREFIX} ')
    }  
}

def release_folder = '/ATG/Build/Casto/Release'
folder(release_folder)
job( release_folder +'/CopyEarsToDeployServer') {
    description 'This copys a given ATG apps to a  deployment slave '
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('DOWNLOAD_RELEASE_TO','/deployment/ecomm/release/atg','Location to copy release')
        labelParam('ATGSlave')
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('EAR_SRC_DIR','../../BuildCasto/','Source dir where ears are created')
       
    }  
        
    steps {
    
      shell ('   deploymentSlave="atg-${ENV_PREFIX}-aws-slv01.aws.gha.kfplc.com" \n\
                cd ${EAR_SRC_DIR} \n\
				scp CASTO/Web/dev_ears/casto-web-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/storefront.ear \n\
				scp CASTO/Staging/dev_ears/casto-staging-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/staging.ear \n\
				scp CASTO/Auxiliary-Web/dev_ears/casto-auxiliary-web-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/auxstorefront.ear \n\
				scp CASTO/Publishing/dev_ears/casto-publishing-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/publishing.ear \n\
				scp CASTO/Fulfillment/dev_ears/casto-fulfillment-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/fulfillment.ear \n\
				scp CASTO/Agent/dev_ears/casto-agent-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/agent.ear \n\
				scp CASTO/Fulfillment-Agent/dev_ears/casto-fulfillment_agent-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/auxagent.ear \n\
				scp CASTO/Web/target/ATG-CASTO-Web-1.0.zip ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/storefront-0.21.0.zip \n\
				scp KF/lock/dev_ears/lock-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/lockstorefront.ear \n\
				scp KF/lock/dev_ears/lock-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/lockagent.ear \n\
				scp build_version.properties ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}')
    }  
    
    publishers {
        downstreamParameterized {
            trigger('CopyEarsToAnsibleDir') {
                setCondition('UNSTABLE_OR_BETTER')
                parameters{
                    predefinedProp('AnsibleSlave','atg-${ENV_PREFIX}-aws-slv01')                        
                    currentBuild() 
                }
            }
        }
    }
}


job(release_folder +'/CopyEarsToAnsibleDir') {
    description 'This downloads a given ATG release from nexus into a slave and prepares for deployment '
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('DOWNLOAD_RELEASE_TO','/deployment/ecomm/release/atg','Location to download release')
        stringParam('ANSIBLE_DEPLOY_DIR','/deployment/source/devops-source','Ansible deploy directory')
        labelParam('AnsibleSlave')
       
    }  
        
    steps {
    
      shell ('  cd ${DOWNLOAD_RELEASE_TO} \n\
				if [ ! -d ${ANSIBLE_DEPLOY_DIR} ]; then \n\
					mkdir -p ${ANSIBLE_DEPLOY_DIR} \n\
				fi \n\
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


job(release_folder +'/CreateCIRelease') {
    description 'This downloads a given ATG release from nexus into a slave and prepares for deployment '
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('DOWNLOAD_RELEASE_TO','/deployment/ecomm/release/atg','Location to download release')
        stringParam('APP_VERSION','ATG_CI_CASTO_REL')
        labelParam('AnsibleSlave')
       
    }  
        
    steps {
    
      shell ('cd ${DOWNLOAD_RELEASE_TO} \n'+
             'jar -cvf ${WORKSPACE}/${APP_VERSION}.jar . \n'+
             'curl --upload-file ${WORKSPACE}/${APP_VERSION}.jar ${NEW_NEXUS_RELEASE_LOCATION}/kf/atg/${APP_VERSION}/${APP_VERSION}.jar --user darwin:darwin \n'+
             'rm -f ${WORKSPACE}/${APP_VERSION}.jar' )
      }
}

job(assemble_folder +'/KF-Assemble') {
    description 'Assemble a given ATG application'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        labelParam('ATGSlave')   
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell (' cd ../../BuildCasto/ \n' +
             'export PATH=${RUBY_HOME}:/$PATH \n'+
			'export JAVA_HOME=${JDK_VER_TO_USE} \n'+
			'export ATG_HOME=${ATG_HOME} \n'+
			'export JBOSS_HOME=/tmp \n'+
              'buildr ATG:KF:assemble  test=no -e sqa3')
    }  
}