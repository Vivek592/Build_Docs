
def under_folder = '/Orchestration/'

folder("Orchestration")

buildFlowJob(under_folder+"Provision-DeploymentSlave-Flow") {
    displayName("Create-Deployment-Slave")
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('DeploymentSlaveToBeCreated','')
       
    }  
   
   buildFlow(
        "build('Infra/Deploy/CreateDeploymentSlave',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'])"
       ) 
   configure { project -> project / 'buildNeedsWorkspace'('true') }            
    publishers {
          downstreamParameterized {
                trigger('Provision-ATGEnv-Flow') {
                      setCondition('UNSTABLE_OR_BETTER')
                      currentBuild()                     
                }
            }
        }
                
    } 
    
    
    
    
buildFlowJob(under_folder+"Provision-ATGEnv-Flow") {
     displayName("Create-ATGEnv")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
       
    }  
   buildFlow(
        "build('Infra/Deploy/CreateAllATGAppEnv',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'])"
       ) 
       
    configure { project -> project / 'buildNeedsWorkspace'('true') }
               
    publishers {
          downstreamParameterized {
                trigger('Provision-RDS-Flow'){
                 setCondition('UNSTABLE_OR_BETTER')
                 currentBuild()                     
                }
            }
        }
                
    } 
    
    
buildFlowJob(under_folder+"Provision-RDS-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
       
    }  
   buildFlow(
        "build('Infra/Database/CreateRDSBase',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'])"
       )            
    configure { project -> project / 'buildNeedsWorkspace'('true') }

    publishers {
          downstreamParameterized {
                trigger('LoadRDSfromSeed-Flow') {
                setCondition('UNSTABLE_OR_BETTER')
                currentBuild()                     
                }
            }
        }
                
    } 
    
buildFlowJob(under_folder+"LoadRDSfromSeed-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
       
    }  
   buildFlow(
        "build('Infra/Database/CreateDatabaseFromSeed',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'])"
       )  
 configure { project -> project / 'buildNeedsWorkspace'('true') }
          
    publishers {
          downstreamParameterized {
                trigger('DeployATG-Flow,CreateMQGateWay-Flow') {
                setCondition('UNSTABLE_OR_BETTER')
                currentBuild()                     
                }
            }
        }
                
    } 


buildFlowJob(under_folder+"CreateMQGateWay-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
       
    }  
   buildFlow(
        "build('Infra/Deploy/CreateMQGateWay',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'])"
       )   
   configure { project -> project / 'buildNeedsWorkspace'('true') }
                
    publishers {
          downstreamParameterized {
                trigger('ConfigureMQGateWay-Flow') {
                    setCondition('UNSTABLE_OR_BETTER')
                    currentBuild()                     
                }
            }
        }
                
    } 
    
    
buildFlowJob(under_folder+"ConfigureMQGateWay-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
       
    }  
   buildFlow(
        "build('Infra/Deploy/ConfigureMQGateWay',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'])"
       )      
    configure { project -> project / 'buildNeedsWorkspace'('true') }
             
    publishers {
          downstreamParameterized {
                trigger('TestMQGateway-Flow') {
               		setCondition('UNSTABLE_OR_BETTER')
                    currentBuild()                     
                }
            }
        }
                
    }  
    
    
    
buildFlowJob(under_folder+"TestMQGateway-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
       
    }  
   buildFlow(
        "build('Infra/Test/TestMQGateWay',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'])"
       )            
    configure { project -> project / 'buildNeedsWorkspace'('true') }
                
    }     
    
      

buildFlowJob(under_folder+"DeployATG-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
       
    }  
   buildFlow(
        "build('ATG/Deploy/DeployAllATGApps',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'])"
       )  
   configure { project -> project / 'buildNeedsWorkspace'('true') }
             
    publishers {
          downstreamParameterized {
                trigger('StartATG-Flow') {
                      setCondition('UNSTABLE_OR_BETTER')
                      currentBuild()                     
                }
            }
        }
                
    } 
     
buildFlowJob(under_folder+"StartATG-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
       
    }  
   buildFlow(
        "build('ATG/Deploy/StartATG',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'])"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
             
    publishers {
          downstreamParameterized {
                trigger('ConfigureATG-Flow') {
                      setCondition('UNSTABLE_OR_BETTER')
                      currentBuild()                     
                }
            }
        }
                
    } 
    
    
    
buildFlowJob(under_folder+"ConfigureATG-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
       
    }  
   buildFlow(
        "build('ATG/Deploy/StartATG',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'])"
       )
       
    configure { project -> project / 'buildNeedsWorkspace'('true') }
                
    publishers {
          downstreamParameterized {
                trigger('ConfigureATG-Flow') {
                	 setCondition('UNSTABLE_OR_BETTER')
                      currentBuild()                     
                }
            }
        }
                
    } 
    
    
    
    
buildPipelineView(under_folder+'Provision-Envionment-Flow') {
    startsWithParameters()
    filterBuildQueue()
    filterExecutors()
    title('Pipeline for Environment Provisioning')
    displayedBuilds(5)
    selectedJob('Provision-DeploymentSlave-Flow')
    alwaysAllowManualTrigger()
    showPipelineParameters()
    showPipelineDefinitionHeader()
    refreshFrequency(60)
}
