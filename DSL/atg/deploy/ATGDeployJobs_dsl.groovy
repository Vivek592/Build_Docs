def under_folder = '/ATG/Deploy/'
def sub_folder = '/ATG/Deploy/Env'

folder(sub_folder)

job('/ATG/Deploy/Endeca/DeployEAC') {
    
  logRotator {
      daysToKeep 2
    }
    
    parameters {
        stringParam('ENV_PREFIX','','AWS environment')
        labelParam('DeploymentSlave')
        stringParam('APP_VERSION','1.0.0','Release no')
        choiceParam('deploy_mode', ['update', 'init'], 'init : will run first-time-deployment-or-rebuild-app.sh')
        choiceParam('domain', ['aws', 'ndc', 'fdc'])       
    } 
    
    steps {
	shell('cd /deployment/source/devops-source \n' +
		'rm -rf endeca \n' +
		'mkdir endeca \n' +
		'cd endeca \n' +
		'wget ${OLD_NEXUS_RELEASE_LOCATION}/com/endeca/eac-project/${APP_VERSION}/eac-project-${APP_VERSION}.tar \n' +
		'mv eac-project-${APP_VERSION}.tar endeca_eac.tar \n' +
		'tar -zxvf endeca_eac.tar eac-project/kfendeca/config/env/bqendeca/cloud/environment.properties \n' +
		' \n' +
		'cd /deployment/ansible \n' +
		' \n' +
		'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts  is-appsetup/playbooks/deployEndeca.yml -e "inventory_group=itl deploy_mode=${deploy_mode} endecaEnv=${ENV_PREFIX} domain=${domain}" -vvvv')
    }
}

job(sub_folder +'/DeploySearchService') {
    
    logRotator {
        daysToKeep 2
    }
    
    parameters {
        stringParam('ENV_PREFIX','','AWS environment')
        labelParam('DeploymentSlave')
        stringParam('APP_VERSION','','Release no')
       
    } 
    
    steps {
    
       shell( 
			  'cd /deployment/ansible \n' + 
              'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/deployWarToTomcatEndeca.yml -e "inventory_group=${inventory_group} nexus_file_path=${OLD_NEXUS_RELEASE_LOCATION}/com/endeca/search-service/${APP_VERSION}  warFile=search-service-${APP_VERSION}.war configFile=env-install-${APP_VERSION}.zip post_deployment_task=true" -vvvv')
    }
}

job(under_folder +'ConfigureBccAgents') {
    description 'Deploys a given ATG application'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
       
    }  
     
    steps {
	shell ('export PATH=/app/ecomm/tools/jruby/bin:/$PATH \n'+
	    'export JAVA_HOME=/usr/java/jdk1.6.0_45/jre \n'+
	    'cd /app/ecomm/fitnesse/ConditionalBuildTest \n'+
	    'Xvfb :99 -screen 0 1024x768x24 2>&1 >/dev/null & \n'+
	    'export DISPLAY=localhost:99.0 \n'+
	    'buildr fitnesse:test suite=DarwinAcceptanceTests.CloudConfiguration.BccAgentConfiguration.BccTargetAgent \\ \n'+
	    'browser=*firefox site=DIY config=/app/ecomm/fitnesse/ConditionalBuildTest/src/main/resources/configurations/teconf-${ENV_PREFIX}.properties \\ \n'+
	    'excludeFilter=dev,smoke,slow,integration,preload xmlFormat=yes')
	    
	shell ('export PATH=/app/ecomm/tools/jruby/bin:/$PATH \n'+
	    'export JAVA_HOME=/usr/java/jdk1.6.0_45/jre \n'+
	    'cd /app/ecomm/fitnesse/ConditionalBuildTest \n'+
	    'Xvfb :99 -screen 0 1024x768x24 2>&1 >/dev/null & \n'+
	    'export DISPLAY=localhost:99.0 \n'+
	    'buildr fitnesse:test suite=DarwinAcceptanceTests.CloudConfiguration.BccDeploymentAgents.BccDeploymentAgent \\ \n'+
	    'browser=*firefox site=DIY config=/app/ecomm/fitnesse/ConditionalBuildTest/src/main/resources/configurations/teconf-${ENV_PREFIX}.properties \\ \n'+
	    'excludeFilter=dev,smoke,slow,integration,preload xmlFormat=yes')
    }  
}

job(under_folder +'ConfigureSearchAndTriggerIndexing') {
    description 'Deploys a given ATG application'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
       
    }  
    
    steps {
	shell ('export PATH=/app/ecomm/tools/jruby/bin:/$PATH \n'+
	    'export JAVA_HOME=/usr/java/jdk1.6.0_45/jre \n'+
	    'cd /app/ecomm/fitnesse/ConditionalBuildTest \n'+
	    'Xvfb :99 -screen 0 1024x768x24 2>&1 >/dev/null & \n'+
	    'export DISPLAY=localhost:99.0 \n'+
	    'buildr fitnesse:test suite=DarwinAcceptanceTests.CloudConfiguration.BccSearchConfiguration.BccSearchConfiguration \\ \n'+
	    'browser=*firefox site=DIY config=/app/ecomm/fitnesse/ConditionalBuildTest/src/main/resources/configurations/teconf-${ENV_PREFIX}.properties \\ \n'+
	    'excludeFilter=dev,smoke,slow,integration,preload xmlFormat=yes')
	   
	shell ('export PATH=/app/ecomm/tools/jruby/bin:/$PATH \n'+
	    'export JAVA_HOME=/usr/java/jdk1.6.0_45/jre \n'+
	    'cd /app/ecomm/fitnesse/ConditionalBuildTest \n'+
	    'Xvfb :99 -screen 0 1024x768x24 2>&1 >/dev/null & \n'+
	    'export DISPLAY=localhost:99.0 \n'+
	    'buildr fitnesse:test suite=DarwinAcceptanceTests.CloudConfiguration.BccSearchAdmin.BccSearchAdmin \\ \n'+
	    'browser=*firefox site=DIY config=/app/ecomm/fitnesse/ConditionalBuildTest/src/main/resources/configurations/teconf-${ENV_PREFIX}.properties \\ \n'+
	    'excludeFilter=dev,smoke,slow,integration,preload xmlFormat=yes')
	    
	shell ('export PATH=/app/ecomm/tools/jruby/bin:/$PATH \n'+
	    'export JAVA_HOME=/usr/java/jdk1.6.0_45/jre \n'+
	    'cd /app/ecomm/fitnesse/ConditionalBuildTest \n'+
	    'Xvfb :99 -screen 0 1024x768x24 2>&1 >/dev/null & \n'+
	    'export DISPLAY=localhost:99.0 \n'+
	    'buildr fitnesse:test suite=DarwinAcceptanceTests.CloudConfiguration.BccIndexing.SearchIndexing \\ \n'+
	    'browser=*firefox site=DIY config=/app/ecomm/fitnesse/ConditionalBuildTest/src/main/resources/configurations/teconf-${ENV_PREFIX}.properties \\ \n'+
	    'excludeFilter=dev,smoke,slow,integration,preload xmlFormat=yes')	    

    }  
}

job(under_folder +'CreateATGLocalConfigs') {
    description 'Creates ATG localconfigs'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
       
    }  
    
    concurrentBuild(true)
    
    steps {
	   
	downstreamParameterized {
            trigger('ATG/Build/Checkout-ATG-LocalConfigs') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
            }
        }
        
        shell ('cd /deployment/ansible \n'+
	    'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/modifyAtgLocalconfig.yml')
    }  
}

job(under_folder +'DeployAuxIndex') {

    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('persona','auxindex')
        labelParam('DeploymentSlave')
       
    }  
    
    concurrentBuild(true)
    
    steps {
	downstreamParameterized {
            trigger('DeployATGApp') {
                block {
                    buildStepFailure('UNSTABLE')
                    failure('UNSTABLE')
                    unstable('UNSTABLE')
                }
            }
        }
    }  
}

job(under_folder +'DownloadATGSearchIndexingFiles') {

    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave') {
            defaultValue('atg-${ENV_PREFIX}-aws-slv01')
        }
        stringParam('DB_SEED_ENV')
        stringParam('ATG_SEARCH_IDX_FILES_REL','ATG_SRCH_INDEX_REL_${DB_SEED_ENV}')
        stringParam('SEARCH_SERVER','atg-${ENV_PREFIX}-aws-sch01.aws.gha.kfplc.com')       
    }  
    
    steps {
        shell ('result=$(ssh ${SEARCH_SERVER} \'cd /app/ecomm/ATGSearchDeployShare; rm -f \'${ATG_SEARCH_IDX_FILES_REL}\'.jar ;wget \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/devops/search/indexing/\'${ATG_SEARCH_IDX_FILES_REL}\'.jar; jar -xvf \'${ATG_SEARCH_IDX_FILES_REL}\'.jar\') \n'+
	    'echo $result')
    }  
}

job(under_folder +'DownloadDeviceAtlas') {

    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX')
        labelParam('DeploymentSlave') {
            defaultValue('atg-${ENV_PREFIX}-aws-slv01')
        }
        stringParam('FileToDownload','http://lnxs0592.uk.b-and-q.com:8088/nexus/content/repositories/releases/kf/devops/publishing/data/deviceAtlasProperties.json')
    }  
    
    steps {
        shell ('result=$(ssh atg-${ENV_PREFIX}-aws-app01.aws.gha.kfplc.com \'cd /app/ecomm/ATGCommon/; rm -rf deviceatlas ; mkdir deviceatlas ; cd deviceatlas ; wget \'${FileToDownload}\'\')')
    }  
}

buildFlowJob(sub_folder +'/DeployToEnv') {

    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX')
        stringParam('ATG_RELEASE')
        stringParam('EAC_RELEASE_NO')
        stringParam('SEARCH_SERVICE_RELEASE_NO')
    }  
    buildFlow(
        "parallel (\n    { build(\'ATG/Update/Pipeline1/Update-Cloud-Environment-Flow\',ENV_PREFIX: params[\'ENV_PREFIX\'] , APP_VERSION: params[\'ATG_RELEASE\'] ) },\n    { build(\'ATG/Deploy/Endeca/DeployEAC\', ENV_PREFIX: params[\'ENV_PREFIX\'] , RELEASE_NO: params[\'EAC_RELEASE_NO\'], DeploymentSlave: \'atg-\'+params[\'ENV_PREFIX\']+\'-aws-slv01\') },\n    { build(\'ATG/Deploy/Endeca/DeploySearchServiceITL\',ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: \'atg-\'+params[\'ENV_PREFIX\']+\'-aws-slv01\', APP_VERSION: params[\'SEARCH_SERVICE_RELEASE_NO\'])  },\n    { build(\'ATG/Deploy/Endeca/DeploySearchServiceMdex\',ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: \'atg-\'+params[\'ENV_PREFIX\']+\'-aws-slv01\', APP_VERSION: params[\'SEARCH_SERVICE_RELEASE_NO\']) }\n    )"
       )  
    configure { project -> project / 'buildNeedsWorkspace'('true') }
}

buildFlowJob(under_folder +'/StartAllATGEnvs') {

    logRotator {
      daysToKeep -2
    }
    
    configure { project -> project / 'properties' / 'se.diabol.jenkins.pipeline.PipelineProperty'  << { stageName 'Stop or Start' }} 

    buildFlow(
        "parallel (\n    { build(\'StartATGEnv\' ,ENV_PREFIX: \'devops05\' , DeploymentSlave: \'atg-devops05-aws-slv01\',action: \'start\') },\n    { build(\'StartATGEnv\' ,ENV_PREFIX: \'devops07\' , DeploymentSlave: \'atg-devops07-aws-slv01\',action: \'start\') },\n    { build(\'StartATGEnv\' ,ENV_PREFIX: \'devops08\' , DeploymentSlave: \'atg-devops08-aws-slv01\',action: \'start\') },\n    { build(\'StartATGEnv\' ,ENV_PREFIX: \'devops14\' , DeploymentSlave: \'atg-devops14-aws-slv01\',action: \'start\') },\n    { build(\'StartATGEnv\' ,ENV_PREFIX: \'devops03\' , DeploymentSlave: \'atg-devops03-aws-slv01\',action: \'start\') },\n    { build(\'StartATGEnv\' ,ENV_PREFIX: \'devops09\' , DeploymentSlave: \'atg-devops09-aws-slv01\',action: \'start\') },\n    { build(\'StartATGEnv\' ,ENV_PREFIX: \'devops10\' , DeploymentSlave: \'atg-devops10-aws-slv01\',action: \'start\') }\n)"
       )  
    configure { project -> project / 'disabled'('true') }
    configure { project -> project / 'concurrentBuild'('true') }
    configure { project -> project / 'buildNeedsWorkspace'('false') }
     
}

job(under_folder +'StartATGEnv') {

    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','devopsXX')
        labelParam('DeploymentSlave')
       
    }  
    configure { project -> project / 'properties' / 'se.diabol.jenkins.pipeline.PipelineProperty'  << { stageName 'Start ATG' }}     
    concurrentBuild(true)
    
    steps {
	downstreamParameterized {
            trigger('RestartAllATGApps,/ATG/Test/Fitnesse/StartFitnesseOnAllServers') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
                parameters{
                    predefinedProp('action','start')                        
                    currentBuild() 
                }             
            }
        }
    }  
}

job(under_folder +'StartSearch') {
    description 'Restart a given ATG application'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('persona')
        choiceParam('action', ['start', 'stop', 'restart'], 'use restart to stop & start the app')
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
        stringParam('instanceId','01')
    }  
    
    concurrentBuild(true)
    
    steps {
        shell ('cd /deployment/ansible \n'+
	    'ansible-playbook -i is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/ATGctrl.yml  -e "inventory_group=${persona} request=${action} instanceId=${instanceId}"')
    }  
}

job(under_folder +'StartSonar') {
    description 'Restart a given ATG application'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        choiceParam('action', ['start', 'stop', 'restart'], 'use restart to stop & start the app')
        labelParam('DeploymentSlave')
        stringParam('SONAR_SERVER','atg-shared-aws-sonar011')
    }  
    
    concurrentBuild(true)
    
    triggers {
        cron('H 8 * * *')
    }
    
    steps {
        shell ('result=$(ssh -o StrictHostKeyChecking=no ${SONAR_SERVER} \'cd /app/ecomm/sonarqube-5.4/bin/linux-x86-64 ; \\ \n'+
            '    ./sonar.sh \'${action}\' \\ \n'+
	    '   \')')
    }  
}

job(under_folder +'StopATGEnv') {

    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX')
        labelParam('DeploymentSlave')
       
    }  
    configure { project -> project / 'properties' / 'se.diabol.jenkins.pipeline.PipelineProperty'  << { stageName 'Stop ATG' }}     
    
    steps {
	downstreamParameterized {
            trigger('RestartAllATGApps,/ATG/Test/Fitnesse/StartFitnesseOnAllServers') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
                parameters{
                    predefinedProp('action','stop')                        
                    currentBuild() 
                }             
            }
        }
    }  
}

job(under_folder +'Taillog') {
    description 'Restart a given ATG application'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        labelParam('DeploymentSlave')
        stringParam('server','atg-devops01-aws-app01.aws.gha.kfplc.com')
        stringParam('persona','storefront')
        stringParam('tailLog','-100f')
    }  
    
    steps {
        shell ('echo "tail ${tailLog} /app/ecomm/server/${persona}/log/server.log" | ssh -q -t -t ${server}')
    }  
}

job(under_folder +'test') {
    description 'Restart a given ATG application'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        labelParam('DeploymentSlave')
        stringParam('server','atg-devops01-aws-app01.aws.gha.kfplc.com')
        stringParam('SCRIPT')
    }  
    
    steps {
        shell ('result=$(ssh  ecommadm@${server} ${SCRIPT})')
    }  
}