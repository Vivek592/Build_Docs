def under_folder = '/ATG/Release'
def sub_folder = '/ATG/Release/Endeca'

folder(under_folder)
folder(sub_folder)

job(under_folder +'/CopyDBArtefactToATGSlave') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('DOWNLOAD_RELEASE_TO','/deployment/ecomm/release/atg','ATG release number')
        labelParam('AnsibleSlave')
        stringParam('ATGSlave')
       
    }  
     
    steps {
	shell ('## PREPARE ATG MODULES ## \n'+
	    'cd ${DOWNLOAD_RELEASE_TO} \n'+
	    'eval `ssh-agent -s` \n'+
	    'ssh-add /home/ecommadm/.ssh/id_rsa \n'+
	    'scp  atg_modules.zip kf-install-0.2.0.jar  ecommadm@${ATGSlave}:${DOWNLOAD_RELEASE_TO}')
    }
}

job(under_folder +'/PrepareForDB') {
    description 'This downloads a given ATG release from nexus into a slave and prepares for deployment '
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX')
        stringParam('APP_VERSION','ATG release number')
        stringParam('DOWNLOAD_RELEASE_TO','/deployment/ecomm/release/atg')
        labelParam('ATGSlave')
        stringParam('ANSIBLE_DEPLOY_DIR','/deployment/source/devops-source/')
       
    }  
    
    configure { project -> project / 'properties' / 'se.diabol.jenkins.pipeline.PipelineProperty'  << { stageName 'prepare' }} 
    configure { project -> project / 'properties' / 'se.diabol.jenkins.pipeline.PipelineProperty'  << { taskName 'PrepareReleaseFromNexus' }} 
     
    steps {
	shell ('rm -rf ${DOWNLOAD_RELEASE_TO} \n'+
	    ' mkdir -p ${DOWNLOAD_RELEASE_TO} \n'+
    	    ' cd ${DOWNLOAD_RELEASE_TO} \n'+
    	    ' \n'+
   	    'REL_IN_NEW_NEXUS=${NEW_NEXUS_RELEASE_LOCATION}/kf/atg/${APP_VERSION}/${APP_VERSION}.jar \n'+
    	    'REL_IN_OLD_NEXUS=${OLD_NEXUS_RELEASE_LOCATION}/kf/atg/${APP_VERSION}/${APP_VERSION}.jar \n'+
    	    ' \n'+
    	    'if curl --output /dev/null --silent --head --fail "$REL_IN_NEW_NEXUS"; then \n'+
    	    '  echo "URL exists:in  $url" \n'+
    	    '   wget  $REL_IN_NEW_NEXUS \n'+
    	    '  \n'+
    	    'else \n'+
    	    '  wget $REL_IN_OLD_NEXUS \n'+
    	    'fi \n'+
    	    ' \n'+
    	    ' \n'+
    	    'unzip -d . ${APP_VERSION}.jar atg_modules.zip kf-install-0.2.0.jar \n'+
    	    ' \n'+
    	    ' \n'+
    	    ' \n'+
    	    'cp atg_modules.zip ${ATG_HOME} \n'+
    	    ' \n'+
    	    'cd ${ATG_HOME} \n'+
    	    ' \n'+
   	    'rm -rf KF \n'+
    	    'rm -rf BQ \n'+
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
    	    'jar xf kf-install-0.2.0.jar')
    }
}

job(sub_folder +'/CreateATGRelease') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('APP_VERSION')
        stringParam('ENV_PREFIX')
    }  
    
    steps {
	   
	downstreamParameterized {
            trigger('ATG/Build/BQ/Release/CreateCIRelease') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
                parameters{
                    predefinedProp('AnsibleSlave','atg-${ENV_PREFIX}-aws-slv01')                        
                    currentBuild() 
                }
            }
        }
    }  
}

job(sub_folder +'/CreateEACRelease') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('RELEASE_NO','${BUILD_NUMBER}')
        labelParam('AnsibleSlave')
    }  
    
    steps {
	shell ('wget ${OLD_NEXUS_RELEASE_LOCATION}/com/endeca/eac-project/1.0.0/eac-project-1.0.0.tar \n'+
    	    ' \n'+
   	    'mv eac-project-1.0.0.tar eac-project-${RELEASE_NO}.tar \n'+
    	    ' \n'+
    	    'tar -xvf  eac-project-${RELEASE_NO}.tar eac-project/build_version.properties \n'+
    	    'cat eac-project/build_version.properties \n'+
    	    '  \n'+
    	    'curl --upload-file eac-project-${RELEASE_NO}.tar ${OLD_NEXUS_RELEASE_LOCATION}/com/endeca/eac-project/${RELEASE_NO}/eac-project-${RELEASE_NO}.tar --user darwin:darwin ')
    }
}

buildFlowJob(sub_folder+"/CreateEndecaReleases") {
    displayName("CreateEndecaReleases")
    parameters {
        stringParam('ATG_RELEASE','ATG_ENDECA_REL_')
        stringParam('EAC_RELEASE_NO')
        stringParam('SEARCH_SERVICE_RELEASE_NO') 
        stringParam('ENV_PREFIX')  
    }  
   
   buildFlow(
          "parallel (\n    { build('CreateATGRelease',ENV_PREFIX: params['ENV_PREFIX'] , APP_VERSION: params['ATG_RELEASE'] ) },\n    { build('CreateEACRelease', ENV_PREFIX: params['ENV_PREFIX'] , RELEASE_NO: params['EAC_RELEASE_NO'], AnsibleSlave: 'atg-'+params['ENV_PREFIX']+'-aws-slv01') },\n    { build('CreateSearchServiceRelease',ENV_PREFIX: params['ENV_PREFIX'] , RELEASE_NO: params['SEARCH_SERVICE_RELEASE_NO'] , AnsibleSlave: 'atg-'+params['ENV_PREFIX']+'-aws-slv01') }\n    )" 
          )
   configure { project -> project / 'buildNeedsWorkspace'('true') }            

} 

job(sub_folder +'/CreateSearchServiceRelease') {
 
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('RELEASE_NO','${BUILD_NUMBER}')
         labelParam('AnsibleSlave')
    }  
    
    steps {
	shell ('wget ${OLD_NEXUS_RELEASE_LOCATION}/com/endeca/search-service/1.0.0/search-service-1.0.0.war \n'+
	    'wget ${OLD_NEXUS_RELEASE_LOCATION}/com/endeca/search-service/1.0.0/env-install-1.0.0.zip \n'+
    	    ' \n'+
   	    'mv search-service-1.0.0.war search-service-${RELEASE_NO}.war \n'+
    	    'mv env-install-1.0.0.zip env-install-${RELEASE_NO}.zip \n'+
    	    ' \n'+
    	    'jar -xvf  search-service-${RELEASE_NO}.war build_version.properties \n'+
    	    'cat build_version.properties \n'+
    	    '  \n'+
     	    'curl --upload-file search-service-${RELEASE_NO}.war ${OLD_NEXUS_RELEASE_LOCATION}/com/endeca/search-service/${RELEASE_NO}/search-service-${RELEASE_NO}.war --user darwin:darwin  \n'+
    	    'curl --upload-file env-install-${RELEASE_NO}.zip ${OLD_NEXUS_RELEASE_LOCATION}/com/endeca/search-service/${RELEASE_NO}/env-install-${RELEASE_NO}.zip --user darwin:darwin')
    }
}

job(sub_folder +'/TagEACRelease') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('BUILD_VERSION')
    }
    
    scm {
        git {
            remote {
                 url 'https://github.com/KITSGitHubAdmin/KITS-Endeca.git'
                 branch '${BUILD_VERSION}'
                 credentials '943c3395-077b-4aab-9028-a173acc8155f'
                 }          
             }
    }  
    
    steps {
	shell ('# this is to force a workspace to be created (otherwise, there\'s no retention within Jenkins \n'+
   	    'echo BUILD_VERSION:$BUILD_VERSION \n'+
    	    'echo RELEASE_NAME:$RELEASE_NAME \n'+
    	    '# output the header of the latest commit \n'+
    	    'git show -s')
    }
    publishers { 
        git { 
            tag(' ', ' ') {
                create()
            } 
        } 
    } 
}
