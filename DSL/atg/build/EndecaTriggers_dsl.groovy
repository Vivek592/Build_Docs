def under_folder = '/ATG/Build'
def sub_folder = '/ATG/Build/Triggers'

folder(sub_folder)

job(sub_folder +'/EndecaBuild') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX', 'devops09')
        stringParam('EAC_MODE','update')
    }  
    
    concurrentBuild(true)
    
    steps {
        dsl('parallel ( \n'+
        '{ build(\'ATG/Build/BQ/Pipelines/develop_Endeca_Feature/Build-Flow\',ENV_PREFIX: params[\'ENV_PREFIX\'] , ATGSlave: \'atg-\'+params[\'ENV_PREFIX\']+\'-aws-jsa01\' }, \n'+
        '{ build(\'ATG/Build/Endeca/EAC-Pipelines/develop/Build-Flow\', ENV_PREFIX: params[\'ENV_PREFIX\'] , ATGSlave: \'atg-\'+params[\'ENV_PREFIX\']+\'-aws-jsa01\', EAC_MODE: params[\'EAC_MODE\']) }, \n'+	
        '{ build(\'ATG/Build/Endeca/AaaS-Pipelines/develop/Build-Flow\',ENV_PREFIX: params[\'ENV_PREFIX\'] , ATGSlave: \'atg-\'+params[\'ENV_PREFIX\']+\'-aws-jsa01\') \n)')
    }
}

job(sub_folder +'/EndecaBuildOnDEVOPS04') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
   concurrentBuild(true)
   
    triggers {
        cron('5 6 * * *')
    }
    
    steps {
        downstreamParameterized {     
            trigger('EndecaBuild'){
                parameters{
                    currentBuild()   
                    predefinedProp('ENV_PREFIX','devops04')
                }                                                           
            }
        }
    }
}

job(sub_folder +'/EndecaBuildOnDEVOPS11') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
   concurrentBuild(true)
   
    triggers {
        cron('H 9-16/2 * * 1-5')
    }
    
    steps {
        downstreamParameterized {     
            trigger('EndecaBuild'){
                parameters{
                    currentBuild()   
                    predefinedProp('ENV_PREFIX','devops11')
                    predefinedProp('EAC_MODE','init')
                }                                                           
            }
        }
    }
}

job(sub_folder +'/PromoteEndecaRulesDevops04') {
 
    logRotator {
      daysToKeep -1
      numToKeep 10
    }
    
    disabled(true)
    
    triggers {
        cron('H/30 * * * * ')
    }
    
    steps {
        downstreamParameterized {     
            trigger('ATG/Deploy/Endeca/PromoteEndecaRules'){
                parameters{
                    currentBuild()   
                    predefinedProp('ENV_PREFIX','devops04')
                    predefinedProp('DeploymentSlave','atg-devops04-aws-slv01')
                }                                                           
            }
        }
    }
}

job(sub_folder +'/PromoteEndecaRulesDevops10') {
 
    logRotator {
      daysToKeep -1
      numToKeep 10
    }
    
    disabled(true)
    
    triggers {
        cron('H/30 * * * * ')
    }
    
    steps {
        downstreamParameterized {     
            trigger('ATG/Deploy/Endeca/PromoteEndecaRules'){
                parameters{
                    currentBuild()   
                    predefinedProp('ENV_PREFIX','devops10')
                    predefinedProp('DeploymentSlave','atg-devops10-aws-slv01')
                }                                                           
            }
        }
    }
}