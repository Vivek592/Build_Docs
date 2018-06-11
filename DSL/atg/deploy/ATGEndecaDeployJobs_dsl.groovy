def under_folder = '/ATG/Deploy'
def sub_folder = '/ATG/Deploy/Endeca'

folder(under_folder)
folder(sub_folder)

job(sub_folder +'/DeployEAC') {
    
  logRotator {
      daysToKeep 2
    }
    
    parameters {
        stringParam('ENV_PREFIX','','AWS environment')
        labelParam('DeploymentSlave')
        stringParam('APP_VERSION','1.0.0','Release no')
        choiceParam('deploy_mode', ['update', 'init'], 'init : will run first-time-deployment-or-rebuild-app.sh')
        choiceParam('domain', ['aws', 'ndc', 'fdc'])
        stringParam('clusterMembers','1')
        stringParam('mdexThreads','2')
        stringParam('mdexCacheSize','1024')
    } 
    
    steps {
        shell( 
	    'cd /deployment/source/devops-source\n' + 
	    'rm -rf endeca\n' +
	    'mkdir endeca\n' +
	    'cd endeca\n' +
	    'wget ${OLD_NEXUS_RELEASE_LOCATION}/com/endeca/eac-project/${APP_VERSION}/eac-project-${APP_VERSION}.tar\n' +
	    'mv eac-project-${APP_VERSION}.tar endeca_eac.tar\n' +
	    'tar -zxvf endeca_eac.tar eac-project/kfendeca/config/env/bqendeca/cloud/environment.properties\n\n' +
	    'cd /deployment/ansible\n\n' +
            'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts  is-appsetup/playbooks/deployEndeca.yml -e "inventory_group=itl deploy_mode=${deploy_mode} endecaEnv=${ENV_PREFIX} domain=${domain} clusterMembers=${clusterMembers} mdexThreads=${mdexThreads} mdexCacheSize=${mdexCacheSize}" -vvvv'
            ) 
    }
}

job(sub_folder +'/DeployEACTemp') {
    
  logRotator {
      daysToKeep 2
    }
    
    parameters {
        stringParam('ENV_PREFIX','','AWS environment')
        labelParam('DeploymentSlave')
        stringParam('APP_VERSION','1.0.0','Release no')
        choiceParam('deploy_mode', ['update', 'init'], 'init : will run first-time-deployment-or-rebuild-app.sh')
        choiceParam('domain', ['aws', 'ndc', 'fdc'])
        stringParam('clusterMembers','1')
    } 
    
    steps {
        shell( 
	    'cd /deployment/source/devops-source\n' + 
	    'rm -rf endeca\n' +
	    'mkdir endeca\n' +
	    'cd endeca\n' +
	    'wget ${OLD_NEXUS_RELEASE_LOCATION}/com/endeca/eac-project/${APP_VERSION}/eac-project-${APP_VERSION}.tar\n' +
	    'mv eac-project-${APP_VERSION}.tar endeca_eac.tar\n' +
	    'tar -zxvf endeca_eac.tar eac-project/kfendeca/config/env/bqendeca/cloud/environment.properties\n\n' +
	    'cd /deployment/ansible\n\n' +
            'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts  is-appsetup/playbooks/test_endeca_cluster.yml -e "inventory_group=itl deploy_mode=${deploy_mode} endecaEnv=${ENV_PREFIX} domain=${domain}" -vvvv'
            ) 
    }
}

buildFlowJob(sub_folder+"/DeployEndecaApps") {
    displayName("DeployEndecaApps")
    
    parameters {
        stringParam('ENV_PREFIX')
        stringParam('APP_VERSION','1.0.0')
        choiceParam('deploy_mode', ['update', 'init'])
	stringParam('clusterMembers','1')
        choiceParam('domain', ['aws', 'ndc', 'fdc'])
        labelParam('DeploymentSlave')
    } 
   
   buildFlow(
          "parallel( { build(\'ATG/Deploy/Endeca/DeploySearchServiceITL\',ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'AnsibleSlave\'], APP_VERSION: params[\'APP_VERSION\'])  },\n"+
          "          { build(\'ATG/Deploy/Endeca/DeploySearchServiceMdex\',ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'AnsibleSlave\'], APP_VERSION: params[\'APP_VERSION\']) },\n"+
          "         { build(\'ATG/Deploy/Endeca/DeployEAC\',ENV_PREFIX: params[\'ENV_PREFIX\'],  DeploymentSlave: params[\'AnsibleSlave\'] , deploy_mode: params[\'EAC_MODE\'] )}\n"+
          "        )"
   )
    configure { project -> project / 'buildNeedsWorkspace'('true') }  
} 

job(sub_folder +'/DeploySearchService') {
    
    logRotator {
        daysToKeep 2
    }
    
    parameters {
        stringParam('ENV_PREFIX','','AWS environment')
        labelParam('DeploymentSlave')
        stringParam('APP_VERSION','1.0.0')
    }
    
    steps {
        shell( 
	    'cd /deployment/ansible\n\n' + 
            'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/deployWarToTomcatEndeca.yml -e "inventory_group=${inventory_group} nexus_file_path=${OLD_NEXUS_RELEASE_LOCATION}/com/endeca/search-service/${APP_VERSION}  warFile=search-service-${APP_VERSION}.war configFile=env-install-${APP_VERSION}.zip post_deployment_task=true" -vvvv'
        ) 
    }
}

job(sub_folder +'/DeploySearchServiceConfigs') {
    
    logRotator {
        daysToKeep 2
    }
    
    parameters {
        stringParam('ENV_PREFIX','','AWS environment')
        labelParam('DeploymentSlave')
        stringParam('APP_VERSION','1.0.0')
    }
    
    steps {
        shell( 
	    'cd /deployment/ansible\n\n' + 
            'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/deployWarToTomcatEndeca.yml -e "inventory_group=${inventory_group} post_deployment_task=true nexus_file_path=${OLD_NEXUS_RELEASE_LOCATION}/com/endeca/search-service/${APP_VERSION}/  filename=env-install-${APP_VERSION}.zip service_name=search-service-${APP_VERSION}" -vvvv'
        ) 
    }
}

buildFlowJob(sub_folder+"/DeploySearchServiceITL") {
    displayName("DeploySearchServiceITL")
    
    parameters {
        stringParam('ENV_PREFIX')
        labelParam('DeploymentSlave')
        stringParam('APP_VERSION','','APP_VERSION')
    } 
   
   buildFlow(
          "build(\'RestartSearchService\',inventory_group: \'itl\',ENV_PREFIX: params[\'ENV_PREFIX\'],  DeploymentSlave: params[\'DeploymentSlave\'] , state: \'stopped\')\n"+
          "build(\'DeploySearchService\',inventory_group: \'itl\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], APP_VERSION: params[\'APP_VERSION\'])\n"+
          "build(\'RestartSearchService\',inventory_group: \'itl\',ENV_PREFIX: params[\'ENV_PREFIX\'],  DeploymentSlave: params[\'DeploymentSlave\'] , state: \'started\')"
   )
} 

buildFlowJob(sub_folder+"/DeploySearchServiceMdex") {
    displayName("DeploySearchServiceMdex")
    
    parameters {
        stringParam('ENV_PREFIX')
        labelParam('DeploymentSlave')
        stringParam('APP_VERSION','','APP_VERSION')
    } 
   
   buildFlow(
          "build(\'RestartSearchService\',inventory_group: \'mdex\',ENV_PREFIX: params[\'ENV_PREFIX\'],  DeploymentSlave: params[\'DeploymentSlave\'] , state: \'stopped\')\n"+
          "build(\'DeploySearchService\',inventory_group: \'mdex\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], APP_VERSION: params[\'APP_VERSION\'])\n"+
          "build(\'RestartSearchService\',inventory_group: \'mdex\',ENV_PREFIX: params[\'ENV_PREFIX\'],  DeploymentSlave: params[\'DeploymentSlave\'] , state: \'started\')"
   )
} 

job(sub_folder +'/EnableEndeca') {

    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX')
        stringParam('servers')
        stringParam('port')
    }  
    
    steps {
	downstreamParameterized {
            trigger('Common/PostUsingCurl') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
                parameters{
                    predefinedProp('url','/dyn/admin/nucleus/kf/endeca/search/EndecaSearchConfiguration/')
                    predefinedProp('post_props','invokeMethod=enableEndecaFeature&submit=Invoke+Method')                          
                    currentBuild() 
                }             
            }
        }
    }  
}

job(sub_folder +'/EnableEndecaOnstart') {
    
    logRotator {
        daysToKeep 2
    }
    
    parameters {
        labelParam('AnsibleSlave')
        stringParam('ENV_PREFIX')
    }
    
    steps {
        shell( 
	    'if [ "\'${ENV_PREFIX}\'" = "\'devops04\'" ] || [ "\'${ENV_PREFIX}\'" = "\'devops09\'" ] || [ "\'${ENV_PREFIX}\'" = "\'devops10\'" ]\n\n' + 
	    'then\n\n' + 
	    'echo "Endeca feature toggle to be enabled by default. "\n\n' + 
	    'result=$(ssh \'atg-\'${ENV_PREFIX}\'-aws-app01.aws.gha.kfplc.com\' \'cd /app/ecomm/server/storefront/ATG_DATA/localconfig/ ; mkdir -p kf/common/featuretoggles/ ; cd /app/ecomm/server/storefront/ATG_DATA/localconfig/kf/common/featuretoggles/ ; wget \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/devops/localconfigsOverride/FeatureToggles.properties.gz ; gunzip -f FeatureToggles.properties.gz \')\n\n' + 
	    'result=$(ssh \'atg-\'${ENV_PREFIX}\'-aws-stg01.aws.gha.kfplc.com\' \'cd /app/ecomm/server/staging/ATG_DATA/localconfig/ ; mkdir -p kf/common/featuretoggles/ ; cd /app/ecomm/server/staging/ATG_DATA/localconfig/kf/common/featuretoggles/ ; wget \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/devops/localconfigsOverride/FeatureToggles.properties.gz ; gunzip -f FeatureToggles.properties.gz \')\n\n\n\n' + 
	    'echo "Enabling the Endeca baseline index to run on schedule every 30 minutes (on the hour and at half past) "\n\n' + 
	    'result=$(ssh \'atg-\'${ENV_PREFIX}\'-aws-aux03.aws.gha.kfplc.com\' \'cd /app/ecomm/server/auxindex/ATG_DATA/localconfig/ ; mkdir -p atg/commerce/endeca/index/ ; cd /app/ecomm/server/auxindex/ATG_DATA/localconfig/atg/commerce/endeca/index/ ; wget \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/devops/localconfigsOverride/ProductCatalogSimpleIndexingAdmin.properties.gz ; gunzip -f ProductCatalogSimpleIndexingAdmin.properties.gz \')\n\n' + 
            'fi'
        ) 
    }
}

job(sub_folder +'/ExecuteEndecaScripts') {
    
    logRotator {
        daysToKeep 2
    }
    
    parameters {
        stringParam('ENV_PREFIX')
        labelParam('DeploymentSlave')
        stringParam('scriptToExecute') 
    }
    
    steps {
        shell( 
	    'cd /deployment/ansible\n\n' + 
            'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts  is-appsetup/playbooks/executeEndecaScripts.yml -e "inventory_group=itl  endecaEnv=${ENV_PREFIX} scriptToExecute=${scriptToExecute}" -vvvv'
        ) 
    }
}

job(sub_folder +'/PromoteEndecaRules') {
    
    logRotator {
        daysToKeep 2
    }
    
    parameters {
        stringParam('ENV_PREFIX')
        labelParam('DeploymentSlave')
        stringParam('scriptToExecute','promote_content.sh') 
    }
    
    steps {
        shell( 
	    'cd /deployment/ansible\n\n' + 
            'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts  is-appsetup/playbooks/executeEndecaScripts.yml -e "inventory_group=itl  endecaEnv=${ENV_PREFIX} scriptToExecute=${scriptToExecute}" -vvvv'
        ) 
    }
}

job(sub_folder +'/RestartSearchService') {
    description 'Stop/Start ITL services on Tomcat'
    
    logRotator {
        daysToKeep 2
    }
    
    parameters {
        choiceParam('state', ['started', 'stopped'])
        stringParam('ENV_PREFIX')
        labelParam('DeploymentSlave')
        stringParam('inventory_group')
    }
    
    steps {
        shell( 
	    'cd /deployment/ansible\n' + 
            'ansible-playbook -i is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/setTomcatState.yml -e "inventory_group=${inventory_group} tomcatState=${state}"'
        ) 
    }
}

job(sub_folder +'/TriggerBaselineIndex') {

    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX')
        stringParam('servers')
        stringParam('port')
    }  
    
    steps {
	downstreamParameterized {
            trigger('Common/PostUsingCurl') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
                parameters{
                    predefinedProp('url','/dyn/admin/nucleus/atg/commerce/endeca/index/ProductCatalogSimpleIndexingAdmin/')
                    predefinedProp('servers','atg-${ENV_PREFIX}-aws-aux03.aws.gha.kfplc.com') 
                    predefinedProp('post_props','invokeMethod=indexBaseline&submit=Invoke+Method') 
                    predefinedProp('port','8110')                          
                    currentBuild() 
                }             
            }
        }
    }  
}