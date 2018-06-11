def top_folder = '/ATG'
def sub_folder = '/ATG/Build'
def sub_sub_folder = '/ATG/Build/Casto'

folder(top_folder)
folder(sub_folder)
folder(sub_sub_folder)

def parent_folder = 'ATG/Build/Casto/Pipelines'
def folder_name = "${FEATURE_NAME}"
def under_folder = '/'+parent_folder+'/'+folder_name+'/'
def initial_job = "Build-Flow"
def ansible_slave = 'atg-${ENV_PREFIX}-aws-slv01'
def atg_slave = 'atg-${ENV_PREFIX}-aws-jsa01'
def shared_slave = 'atg-shared-aws-slv01'
def branch="${FEATURE_NAME}"

folder(parent_folder)
folder(parent_folder+'/'+folder_name)
{
  primaryView('Casto-CI-Pipeline')
}

buildFlowJob(under_folder+initial_job) {
    displayName("Build Casto")
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('branch',branch,'Feature Branch to build')
        stringParam('ATGSlave',atg_slave,'Initial slave to launch')         
        
    }  
   label(shared_slave)
   
   
    buildFlow(
        "build('ATG/Build/Casto/BuildCasto',ENV_PREFIX: params['ENV_PREFIX'] , ATGSlave: 'atg-'+params['ENV_PREFIX']+'-aws-jsa01', branch:params['branch'])"
       )  
   
   publishers {
       downstreamParameterized {
           trigger('AssembleFlow') {
               setCondition('UNSTABLE_OR_BETTER')
               parameters{
                   predefinedProp('ATGSlave',atg_slave)                        
                   currentBuild() 
               }
           }
       }                
   }

   configure { project -> project / 'buildNeedsWorkspace'('true') }       
} 
    


 buildFlowJob(under_folder+"AssembleFlow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')
        
       
    }  
   buildFlow(
        "build('ATG/Build/Casto/Assembly/AssembleEars',ENV_PREFIX: params['ENV_PREFIX'] , ATGSlave: params['ATGSlave'])"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
             
    publishers {
        downstreamParameterized {
            trigger('Prepare-ForDeploy-Flow') {
                      setCondition('UNSTABLE_OR_BETTER')
                parameters{
                   predefinedProp('ATGSlave',atg_slave)                        
                   currentBuild() 
                }
            }
        }
    }
}
    
buildFlowJob(under_folder+"Prepare-ForDeploy-Flow") {
     displayName("Prepare")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('APP_VERSION','','Release Number')

        labelParam('ATGSlave')
       
    }  
     buildFlow(
         "build('ATG/Build/Casto/Release/CopyEarsToDeployServer',ENV_PREFIX: params['ENV_PREFIX'] , ATGSlave: params['ATGSlave'])" 
          ) 
       
    configure { project -> project / 'buildNeedsWorkspace'('true') }
   
    publishers {
        downstreamParameterized {
            trigger('StopATG-Flow') {
                setCondition('UNSTABLE_OR_BETTER')
                parameters{
                   predefinedProp('AnsibleSlave',ansible_slave)                        
                   currentBuild() 
                }                   
            }
        }
    }          
}   
    

    
 buildFlowJob(under_folder+"StopATG-Flow") {
      displayName("Stop Env")
 
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')
        
       
    }  
   buildFlow(
        "build('ATG/Deploy/StopATGEnv',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'])"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
             
    publishers {
        downstreamParameterized {
            trigger('UpdateDatabase-Flow') {
                setCondition('UNSTABLE_OR_BETTER')
                parameters{
                    predefinedProp('ATGSlave',atg_slave)                        
                    currentBuild() 
                }
            }
        }
    }
}

    
buildFlowJob(under_folder+"UpdateDatabase-Flow") {
     displayName("DBUpdate")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')      
    }  
    
   buildFlow(
        "build('ATG/Build/Casto/Database/dbIncAndLoadData', ATGSlave: params['ATGSlave'], ENV_PREFIX: params['ENV_PREFIX'])"
       )  
 configure { project -> project / 'buildNeedsWorkspace'('true') }
          
    publishers {
        downstreamParameterized {
            trigger('DeployATG-Flow') {
                setCondition('UNSTABLE_OR_BETTER')
                parameters{
                    predefinedProp('AnsibleSlave',ansible_slave)                        
                    currentBuild() 
                }                
            }
        }
    }
}


      

buildFlowJob(under_folder+"DeployATG-Flow") {
     displayName("Deploy")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('APP_VERSION','','ATG Application Release ')

        labelParam('AnsibleSlave')
       
    }  
   buildFlow(
        "build('ATG/Deploy/CreateATGLocalConfigs',ENV_PREFIX: params['ENV_PREFIX'],  DeploymentSlave: params['AnsibleSlave'])\n\
         build('ATG/Deploy/DeployAllATGApps',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave']) \n\
         build('ATG/Deploy/DeployAllLocalConfigs',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'])"
       )  
   configure { project -> project / 'buildNeedsWorkspace'('true') }
             
    publishers {
          downstreamParameterized {
                trigger('StartATG-Flow') {
                      setCondition('UNSTABLE_OR_BETTER')
                }
            }
        }
                
    } 
     
buildFlowJob(under_folder+"StartATG-Flow") {
     displayName("Start Env")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')
        
       
    }  
   buildFlow(
        "build('ATG/Deploy/StartATGEnv',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'])"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
             
    publishers {
        downstreamParameterized {
            trigger('TestATG-Flow') {
                setCondition('UNSTABLE_OR_BETTER')
                parameters{
                    predefinedProp('ATGSlave',atg_slave)                        
                    currentBuild() 
                }                                 
            }
        }
    }
} 
    
    
   
    

buildFlowJob(under_folder+"TestATG-Flow") {
     displayName("Test")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')
       
    }  
   buildFlow(
        "build('ATG/Test/HighSuiteTestATGEnv',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'])"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
     
     publishers {
          downstreamParameterized {
                trigger('CreateRelease-Flow') {
                      setCondition('UNSTABLE_OR_BETTER')
                }
            }
        }
    } 
     
buildFlowJob(under_folder+"CreateRelease-Flow") {
     displayName("CreateRelease")

    parameters {
        labelParam('AnsibleSlave')
       
    }  
   buildFlow(
        "build('ATG/Build/Casto/Release/CreateCIRelease',APP_VERSION: 'ATG_CI_CASTO_'+build.properties.get('number')  , AnsibleSlave: params['AnsibleSlave'])"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
    
  
    } 
deliveryPipelineView(under_folder+'Casto-CI-Pipeline') {
    showAggregatedPipeline true
    enableManualTriggers true
    pipelineInstances 5
     configure { view -> view / 'allowPipelineStart'('true') }
    configure { view -> view / 'allowRebuild'('true') }
    configure { view -> view / 'showTotalBuildTime'('true') }
    pipelines {
        component('Casto CI Pipeline - '+ branch, initial_job)
    }
    }