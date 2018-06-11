def top_folder = '/ATG'
def sub_folder = '/ATG/Build'
def under_folder = '/ATG/Build/BQ'
def workspace_dir = '/deployment/ecomm/jenkins-slave/workspace/ATG/Build/BQ/Pipelines/${branch}/Build-Flow'

def release_folder = under_folder+'/Release'
def assemble_folder=under_folder +"/Assembly"

folder(top_folder)
folder(sub_folder)
folder(under_folder)

job(under_folder +'/BuildBQ') {
    
  logRotator {
      daysToKeep -1
      numToKeep 5
    }
    
    parameters {
        stringParam('branch','develop','Working branch')
        labelParam('ATGSlave')
       
    } 
   customWorkspace(workspace_dir)
        
    
    steps {
       shell('export PATH=${RUBY_HOME}:/$PATH \n'+
			'export JAVA_HOME=${JDK_VER_TO_USE} \n'+
			'export ATG_HOME=${ATG_HOME} \n'+
			'export JBOSS_HOME=/tmp \n'+
			'buildr clean ATG:BQ:build emma:xml pmd:rule:xml pmd:cpd:xml -e sqa4 \n'+
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
    

folder(assemble_folder)

buildFlowJob(assemble_folder +'/AssembleEars') {
    description 'This stops or starts a given environment '
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        labelParam('ATGSlave')
        stringParam('branch','','Working branch')
       
    }  

   buildFlow("build('AssembleEar',module_to_assemble: 'ATG:KF:lock' , ATGSlave: params['ATGSlave'],branch:params['branch']) \n\
              parallel (\n\
				   { build('AssembleEar',module_to_assemble: 'ATG:BQ:Web' , ATGSlave: params['ATGSlave'], branch:params['branch'] ) }, \n\
                   { build('AssembleEar',module_to_assemble: 'ATG:BQ:Fulfillment' , ATGSlave: params['ATGSlave'], branch:params['branch']) }, \n\
                   { build('AssembleEar',module_to_assemble: 'ATG:BQ:Fulfillment-Agent' , ATGSlave: params['ATGSlave'], branch:params['branch']) }, \n\
                   { build('AssembleEar',module_to_assemble: 'ATG:BQ:Staging' , ATGSlave: params['ATGSlave'], branch:params['branch'] )}, \n\
                   { build('AssembleEar',module_to_assemble: 'ATG:BQ:Publishing' , ATGSlave: params['ATGSlave'], branch:params['branch'] )}, \n\
                   { build('AssembleEar',module_to_assemble: 'ATG:BQ:Agent' , ATGSlave: params['ATGSlave'], branch:params['branch']) }, \n\
                   { build('AssembleEar',module_to_assemble: 'ATG:BQ:Auxiliary-Web' , ATGSlave: params['ATGSlave'], branch:params['branch']) } \n\
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
        stringParam('module_to_assemble','','BQ Ears to assemble')
        labelParam('ATGSlave')   
    }  
    
    concurrentBuild(true)
    customWorkspace(workspace_dir)
    
    steps {
    
      shell ('export PATH=${RUBY_HOME}:/$PATH \n'+
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
	    stringParam('branch','','Working branch')
		 
    }  
       customWorkspace(workspace_dir)
    
    steps {
    
      shell ('export PATH=${RUBY_HOME}:/$PATH \n'+
			'export JAVA_HOME=${JDK_VER_TO_USE} \n'+
			'export ATG_HOME=${ATG_HOME} \n'+
			'export JBOSS_HOME=/tmp \n'+
            'buildr ATG:BQ:env-install:db_inc ATG:BQ:env-install:data_load_data -e ${ENV_PREFIX} ')
    }  
}

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
        stringParam('branch','','Working branch')
        
       
    }  
   
      customWorkspace(workspace_dir)
   
    steps {
    
      shell ('   deploymentSlave="atg-${ENV_PREFIX}-aws-slv01.aws.gha.kfplc.com" \n\n\
 	 	 	 	result=$(ssh  ${deploymentSlave} \'rm -rf \'${DOWNLOAD_RELEASE_TO}\'\') \n\
 	 	 	 	result=$(ssh  ${deploymentSlave} \'mkdir \'${DOWNLOAD_RELEASE_TO}\'\') \n\n\
 	 	 	 	scp BQ/Web/dev_ears/bq-web-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/storefront.ear \n\
 	 	 	 	scp BQ/Staging/dev_ears/bq-staging-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/staging.ear \n\
 	 	 	 	scp BQ/Auxiliary-Web/dev_ears/bq-auxiliary-web-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/auxstorefront.ear \n\
 	 	 	 	scp BQ/Auxiliary-Web/dev_ears/bq-auxiliary-web-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/auxindex.ear \n\
 	 	 	 	scp BQ/Publishing/dev_ears/bq-publishing-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/publishing.ear \n\
 	 	 	 	scp BQ/Fulfillment/dev_ears/bq-fulfillment-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/fulfillment.ear \n\
 	 	 	 	scp BQ/Agent/dev_ears/bq-agent-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/agent.ear \n\
 	 	 	 	scp BQ/Fulfillment-Agent/dev_ears/bq-fulfillment_agent-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/auxagent.ear \n\
 	 	 	 	scp BQ/Web/target/ATG-BQ-Web-1.0.zip ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/storefront-0.21.0.zip \n\
 	 	 	 	scp KF/lock/dev_ears/lock-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/lockstorefront.ear \n\
 	 	 	 	scp KF/lock/dev_ears/lock-packed.ear ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/lockagent.ear \n\
 	 	 	 	scp build_version.properties ${deploymentSlave}:${DOWNLOAD_RELEASE_TO} \n\n\
				### Pack up KF install to be used for db increment &  data load ####### \n\n\
 	 	 	 	export PATH=${RUBY_HOME}:/$PATH  \n\
				export JAVA_HOME=${JDK_VER_TO_USE}  \n\
				export ATG_HOME=${ATG_HOME}  \n\
				export JBOSS_HOME=/tmp  \n\
 	 	 	 	rm -rf BQ/env-install/target \n\
				buildr ATG:BQ:env-install:package -e sqa \n\
				scp BQ/env-install/target/ATG-BQ-env-install-1.0.jar ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}/kf-install-0.2.0.jar \n\n\
				### Pack up ATG modules to be used for load data####### \n\n\
				cd ${ATG_HOME} \n\
 	 	 	 	rm -rf /app/ecomm/tmp \n\
 	 	 	 	mkdir /app/ecomm/tmp \n\
				jar -cf /app/ecomm/tmp/atg_modules.zip KF BQ \n\
 	 	 	 	scp /app/ecomm/tmp/atg_modules.zip ${deploymentSlave}:${DOWNLOAD_RELEASE_TO}')
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
        stringParam('APP_VERSION','ATG_CI_BQ_REL')
        labelParam('AnsibleSlave')
       
    }  
        
    steps {
    
      shell ('cd ${DOWNLOAD_RELEASE_TO} \n'+
             'jar -cvf ${WORKSPACE}/${APP_VERSION}.jar . \n'+
             'curl --upload-file ${WORKSPACE}/${APP_VERSION}.jar ${NEW_NEXUS_RELEASE_LOCATION}/kf/atg/${APP_VERSION}/${APP_VERSION}.jar --user darwin:darwin \n'+
             'rm -f ${WORKSPACE}/${APP_VERSION}.jar' )
      }
}