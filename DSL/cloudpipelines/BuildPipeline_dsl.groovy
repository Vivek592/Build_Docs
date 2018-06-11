def parent_folder = 'Cloud-Pipelines'
def folder_name = "${PIPELINE_FOLDER_NAME}"
def under_folder = '/'+parent_folder+'/'+folder_name+'/'
def initial_job = "Provision-Cloud-Environment-Flow"
def ansible_slave = 'atg-${ENV_PREFIX}-aws-slv01'
def atg_slave = 'atg-${ENV_PREFIX}-aws-jsa01'
def shared_slave = 'atg-shared-aws-slv01'
def atg_release = "${ATG_APP_REL_NO}"
def db_seed_env = "${DB_SEED_ENV_TAKEN}"
def db_seed_version = "${ATG_DB_REL_NO}"
def wmb_release = "${WMB_APP_REL_NO}"
def wmb_mock_release = "${WMB_MOCK_REL_NO}"

folder(parent_folder)
folder(parent_folder+'/'+folder_name)
{
  primaryView('Environment-Delivery-Pipeline')
}

buildFlowJob(under_folder+"Provision-Cloud-Environment-Flow") {
    displayName("Create Ansible Slave")
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('GenericSlave',shared_slave,'Initial slave to launch') 
        stringParam('AnsibleSlave',ansible_slave,'Initial slave to launch')         
        stringParam('Charge_Code','','Charge code')  
    }  
   label(shared_slave)
   
   buildFlow(
          "build('Infra/Build/CheckoutInfrCode', DeploymentSlave: params['GenericSlave']) \n\
          build('Infra/Deploy/CreateAnsibleSlave',ENV_PREFIX: params['ENV_PREFIX'] , GenericSlave: params['GenericSlave'] ,Charge_Code: params['Charge_Code']) \n\
          build('Infra/Build/CheckoutInfrCode',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: 'atg-'+params['ENV_PREFIX']+'-aws-slv01') \n\
          build('Infra/Build/CopyHostFileToAnsibleSlave', DeploymentSlave: params['GenericSlave'],ENV_PREFIX: params['ENV_PREFIX'])" 
          )
   configure { project -> project / 'buildNeedsWorkspace'('true') }            
    publishers {
          downstreamParameterized {
                trigger('CreateMQGateWay-Flow,CreateWMB-Flow,Create-RDS-Flow,Create-ATGSlave-Flow,Create-ATGEnv-Flow,Create-Fitnesse-Flow,Create-Endeca-Flow') {
                      setCondition('UNSTABLE_OR_BETTER')
                      parameters{
                          currentBuild()
                          predefinedProp('AnsibleSlave',ansible_slave)
                          predefinedProp('ATGSlave',atg_slave)
                      }                     

                }
            }
        }
                
    } 
    
    
buildFlowJob(under_folder+"Create-ATGSlave-Flow") {
     displayName("Create-ATG-Slave")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')
       
    }  
   buildFlow(
        "build('Infra/Deploy/CreateATGSlave',ENV_PREFIX: params['ENV_PREFIX'] , GenericSlave: params['AnsibleSlave'], Charge_Code: params['Charge_Code'] ) \n\
        build('ATG/Update/UpdateATGSlave',ENV_PREFIX: params['ENV_PREFIX'] , ATGSlave: params['ATGSlave'] ) "
       ) 
       
    configure { project -> project / 'buildNeedsWorkspace'('true') }
   
   
    publishers {
          downstreamParameterized {
                trigger('LoadRDSfromSeed-Flow') {
                setCondition('UNSTABLE_OR_BETTER')
                }
            }
        }
                
    }   
    
buildFlowJob(under_folder+"Create-ATGEnv-Flow") {
     displayName("Create-ATGEnv")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')
       
    }  
   buildFlow(
        "build('Infra/Deploy/CreateAllATGAppEnv',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'],Charge_Code: params['Charge_Code']) \n\
        build('Infra/Release/UploadATGInventoryToNexus' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'])"
       ) 
       
    configure { project -> project / 'buildNeedsWorkspace'('true') }
   
   
       publishers {
          downstreamParameterized {
                trigger('DeployATG-Flow') {
                setCondition('UNSTABLE_OR_BETTER')
                parameters{
                    currentBuild() 
                    predefinedProp('AnsibleSlave',ansible_slave)  
                    }                  
                }
            }
        }    
    } 
    
    
buildFlowJob(under_folder+"Create-RDS-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')
       
    }  
   buildFlow(
        "build('Infra/Database/CreateRDSBase',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'], Charge_Code: params['Charge_Code'])"
       )            
    configure { project -> project / 'buildNeedsWorkspace'('true') }

   
                
    } 
    
buildFlowJob(under_folder+"LoadRDSfromSeed-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')
        stringParam('DB_SEED_ENV',db_seed_env,'Environment  where seed was extracted from')
        stringParam('DB_SEED_VERSION',db_seed_version,'Seed version number')      
    }  
    quietPeriod(25)
    
   configure { project -> project / 'buildWrappers' / 'org.jenkinsci.plugin.Diskcheck' / 'failOnError'('false') }
   buildFlow(
        "build('Infra/Database/Checkout-DB-Seed-Scripts', DeploymentSlave: params['ATGSlave']) \n\
         build('Infra/Database/CreateDatabaseFromSeed',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'], DB_SEED_ENV: params['DB_SEED_ENV'], DB_SEED_VERSION: params['DB_SEED_VERSION']) \n\
         build('Infra/Release/UploadATGInventoryToNexus' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'])"
       )  
 configure { project -> project / 'buildNeedsWorkspace'('true') }
          
    publishers {
          downstreamParameterized {
                trigger('StartWMB-Flow,StartATG-Flow') {
                setCondition('UNSTABLE_OR_BETTER')
                    parameters{
                        currentBuild() 
                        predefinedProp('AnsibleSlave',ansible_slave)
                    }                    
                }
            }
        }
                
    } 




buildFlowJob(under_folder+"CreateWMB-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')
       
    }  
   buildFlow(
        "build('WMB/Deploy/CreateWMB',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'], Charge_Code: params['Charge_Code'])"
       )   
   configure { project -> project / 'buildNeedsWorkspace'('true') }
                
   
                
    } 


buildFlowJob(under_folder+"StartWMB-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')
        
       
    }  
   buildFlow(
        "build('WMB/Deploy/StopStartWMB',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'], action:'start')"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
             
    publishers {
          downstreamParameterized {
                trigger('DeployWMB-Flow') {
                    setCondition('UNSTABLE_OR_BETTER')
                }
            }
        } 
                
    }

buildFlowJob(under_folder+"DeployWMB-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('WMB_APP_VERSION',wmb_release,'ATG Application Release ')
        labelParam('AnsibleSlave')
       
    }  
   buildFlow(
        "build('WMB/Deploy/DeployWMBBars',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'], APP_VERSION: params['WMB_APP_VERSION'])"
       )   
   configure { project -> project / 'buildNeedsWorkspace'('true') }
   
        publishers {
          downstreamParameterized {
                trigger('DeployWMBMocks-Flow') {
                    setCondition('UNSTABLE_OR_BETTER')
                }
            }
        } 
                 
    } 
    
  
  buildFlowJob(under_folder+"DeployWMBMocks-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('APP_VERSION',wmb_mock_release,'ATG Application Release ')
        labelParam('AnsibleSlave')
       
    }  
   buildFlow(
        "build('WMB/Deploy/DeployAllWMBMocks',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'], APP_VERSION: params['APP_VERSION'])"
       )   
   configure { project -> project / 'buildNeedsWorkspace'('true') }
   
           publishers {
          downstreamParameterized {
                trigger('StartWMBMocks-Flow') {
                    setCondition('UNSTABLE_OR_BETTER')
                }
            }
        }
    } 

   buildFlowJob(under_folder+"StartWMBMocks-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')
       
    }  
   buildFlow(
        "build('WMB/Deploy/StopStartTomcat',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'])"
       )   
   configure { project -> project / 'buildNeedsWorkspace'('true') }
   
                 
    }  
    
    
buildFlowJob(under_folder+"CreateMQGateWay-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')
       
    }  
   buildFlow(
        "build('WMB/Deploy/CreateMQGateway',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'], Charge_Code: params['Charge_Code'])"
       )   
   configure { project -> project / 'buildNeedsWorkspace'('true') }
                
    publishers {
          downstreamParameterized {
                trigger('TestMQGateway-Flow') {
                    setCondition('UNSTABLE_OR_BETTER')
                }
            }
        }
                
    } 
    
    
    
buildFlowJob(under_folder+"TestMQGateway-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')
       
    }  
   buildFlow(
        "build('WMB/Test/TestMQGateWay',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'])"
       )            
    configure { project -> project / 'buildNeedsWorkspace'('true') }
    
    
    
     
    }     
    
      

buildFlowJob(under_folder+"DeployATG-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('APP_VERSION',atg_release,'ATG Application Release ')

        labelParam('AnsibleSlave')
       
    }  
   buildFlow(
        "build('Infra/Release/PrepareATGInventoryFromNexus',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave']) \n\
         build('ATG/Release/PrepareReleaseFromNexus',APP_VERSION: params['APP_VERSION'] ,ENV_PREFIX: params['ENV_PREFIX'],  DeploymentSlave: params['AnsibleSlave'])\n\
         build('ATG/Deploy/CreateATGLocalConfigs',ENV_PREFIX: params['ENV_PREFIX'],  DeploymentSlave: params['AnsibleSlave'])\n\
         build('ATG/Deploy/DeployAllATGApps',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave']) \n\
         build('ATG/Deploy/DeployAllLocalConfigs',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'])"
       )  
   configure { project -> project / 'buildNeedsWorkspace'('true') }
             
    
                
    } 
     
buildFlowJob(under_folder+"StartATG-Flow") {
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
                trigger('ConfigureATG-Flow') {
                      setCondition('UNSTABLE_OR_BETTER')
                      parameters{
                          predefinedProp('ATGSlave',atg_slave)
                          currentBuild()   
                      }                  
                }
            }
        }
                
    } 
 
 
 buildFlowJob(under_folder+"Create-Fitnesse-Flow") {
     displayName("Create-Fitnesse-Servers")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')
       
    }  
   buildFlow(
        "build('Infra/Deploy/CreateAllFitnesse',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'], Charge_Code: params['Charge_Code'] ) "
       ) 
       
    configure { project -> project / 'buildNeedsWorkspace'('true') }
   
   
    publishers {
          downstreamParameterized {
                trigger('DeployFitnesse-Flow') {
                setCondition('UNSTABLE_OR_BETTER')
                }
            }
        }
                
    } 
  
  
  buildFlowJob(under_folder+"DeployFitnesse-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')
        
       
    }  
   buildFlow(
        "build('Infra/Deploy/CreateFitnesseConfig',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave']) \n\
         build('ATG/Test/Fitnesse/PrepareFitnesseOnAllServers',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'])"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
             
    publishers {
          downstreamParameterized {
                trigger('StartFitnesse-Flow') {
                      setCondition('UNSTABLE_OR_BETTER')
                      parameters{
                          predefinedProp('ATGSlave',atg_slave)
                          currentBuild()   
                      }                  
                }
            }
        }
                
    } 



buildFlowJob(under_folder+"StartFitnesse-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')
        
       
    }  
   buildFlow(
        "build('ATG/Test/Fitnesse/StartFitnesseOnAllServers',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'])"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
             
 }   
    
    
buildFlowJob(under_folder+"ConfigureATG-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')
        labelParam('AnsibleSlave')
       
    }  
   buildFlow(
        "build('ATG/Test/Fitnesse/PrepareFitnesseForRun',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave']) \n\
        build('ATG/Deploy/ConfigureBccAgents',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave']) \n\
        build('ATG/Deploy/ConfigureSearchAndTriggerIndexing',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'])"
       )
       
    configure { project -> project / 'buildNeedsWorkspace'('true') }
                
    publishers {
          downstreamParameterized {
                trigger('TestATG-Flow') {
                	  setCondition('UNSTABLE_OR_BETTER')
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
        "build('ATG/Test/DevOpsATGHighSuiteTest',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'], FITNESSE_RESULT_DIR: build.properties.get('workspace'))"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
    
    
     publishers {
     
      
	     configure 
	    {   
	         project -> project / publishers << 'hudson.plugins.fitnesse.FitnesseResultsRecorder' { fitnessePathToXmlResultsIn 'fitnesse-results*.xml'  }
	    }
        publishers {
       			 archiveArtifacts('fitnesse_log_*.log')
    		}
     
    	 publishHtml {
            report('HighSuiteReport') {
                reportName('HighSuiteReport')
                reportFiles('index.html')
                keepAll()
                allowMissing()
                alwaysLinkToLastBuild()
            }
            }
            
          
        }
    } 




 buildFlowJob(under_folder+"Create-Endeca-Flow") {
     displayName("Create-Endeca-Servers")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')
       
    }  
   buildFlow(
        "build('Infra/Deploy/Endeca/CreateEndecaSilo',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'], Charge_Code: params['Charge_Code'] ) "
       ) 
       
    configure { project -> project / 'buildNeedsWorkspace'('true') }
   
   
    publishers {
          downstreamParameterized {
                trigger('Test-Endeca-Flow') {
                setCondition('UNSTABLE_OR_BETTER')
                }
            }
        }
                
    }  
    
  buildFlowJob(under_folder+"Test-Endeca-Flow") {
     displayName("Test-Endeca-Servers")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')
       
    }  
   buildFlow(
        "build('Infra/Deploy/Endeca/TestEndecaSilo',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'], Charge_Code: params['Charge_Code'] ) "
       ) 
       
    configure { project -> project / 'buildNeedsWorkspace'('true') }
  
                
    }   
    
deliveryPipelineView(under_folder+'Environment-Delivery-Pipeline') {
    showAggregatedPipeline true
    enableManualTriggers true
    pipelineInstances 5
    configure { view -> view / 'allowPipelineStart'('true') }
    configure { view -> view / 'allowRebuild'('true') }
    configure { view -> view / 'showTotalBuildTime'('true') }
    
    pipelines {
        component('Environment Provisioning Delivery Pipeline', initial_job)
    }
}