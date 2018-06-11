def parent_folder = 'ATG/Update'
def folder_name = 'Pipeline1'
def under_folder = '/'+parent_folder+'/'+folder_name+'/'
def initial_job = "Update-Cloud-Environment-Flow"
def ansible_slave = 'atg-${ENV_PREFIX}-aws-slv01'
def atg_slave = 'atg-${ENV_PREFIX}-aws-jsa01'
def shared_slave = 'atg-shared-aws-slv01'


folder(parent_folder)
folder(parent_folder+'/'+folder_name)
{
  primaryView('Environment-Update-Pipeline')
}

job(under_folder+initial_job) {
    displayName("Update-Cloud-Environment")
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('APP_VERSION','','Release number of the app')
    }  
   label(shared_slave)
   
   steps {
       downstreamParameterized {     
           trigger('StopATG-Flow'){
               block {
                   buildStepFailure('FAILURE')
                   failure('FAILURE')
                   unstable('UNSTABLE')
               }
               parameters{
                   currentBuild()   
                   predefinedProp('AnsibleSlave',ansible_slave)
                   predefinedProp('ATGSlave',atg_slave) 
               }                                                           
           }
        }
    }
   configure { project -> project / 'buildNeedsWorkspace'('true') }       
        
           
    } 
    


 buildFlowJob(under_folder+"StopATG-Flow") {
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
                trigger('Prepare-Release-Flow') {
                      setCondition('UNSTABLE_OR_BETTER')
                      parameters{
                          predefinedProp('ATGSlave',atg_slave)
                          currentBuild()     
                      }                
                }
            }
        }
                
    } 
    
buildFlowJob(under_folder+"EnableEndeca-Flow") {
    parameters {
        labelParam('AnsibleSlave')
        stringParam('ENV_PREFIX')
    }  
   buildFlow(
        "build('ATG/Deploy/Endeca/EnableEndecaOnstart',AnsibleSlave: params['AnsibleSlave'] , ENV_PREFIX: params['ENV_PREFIX'])"
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
    
buildFlowJob(under_folder+"Prepare-Release-Flow") {
     displayName("Prepare Release for deployment")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('APP_VERSION','','Release Number')

        labelParam('AnsibleSlave')
       
    }  
     buildFlow(
          "parallel (  { build('ATG/Release/PrepareReleaseFromNexus',APP_VERSION: params['APP_VERSION'] ,ENV_PREFIX: params['ENV_PREFIX'],  DeploymentSlave: params['AnsibleSlave']) },\n\
                       {  build('Infra/Build/CheckoutInfrCode',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'] )\n\
                          build('Infra/Release/PrepareATGInventoryFromNexus',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'])\n\
                        },\n\
                        { build('ATG/Release/PrepareReleaseFromNexus',APP_VERSION: params['APP_VERSION'] ,ENV_PREFIX: params['ENV_PREFIX'],  DeploymentSlave: params['ATGSlave']) \n\
                          build('ATG/Release/PrepareReleaseForDBUpdate',APP_VERSION: params['APP_VERSION'] ,ENV_PREFIX: params['ENV_PREFIX'],  DeploymentSlave: params['ATGSlave']) \n\
                        }\n\
                        )" 
          ) 
       
    configure { project -> project / 'buildNeedsWorkspace'('true') }
   
      publishers {
          downstreamParameterized {
                trigger('UpdateDatabase-Flow') {
                setCondition('UNSTABLE_OR_BETTER')
                parameters{
                    currentBuild() 
                    predefinedProp('ATGSlave',atg_slave) 
                    }                   
                }
            }
        }          
    }   
    

    

    
buildFlowJob(under_folder+"UpdateDatabase-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')      
    }  
    
   buildFlow(
        "build('ATG/Database/dbIncAndLoadDataFor', DeploymentSlave: params['ATGSlave'], ENV_PREFIX: params['ENV_PREFIX'])"
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



      

buildFlowJob(under_folder+"DeployATG-Flow") {
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
        "build('ATG/Test/DevOpsATGHighSuiteTest',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'], FITNESSE_RESULT_DIR: build.properties.get('workspace'))"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
    publishers {
        archiveArtifacts { 
            pattern('fitnesse_log_*.log') 
	}
	fitnesseResultsRecorder {
	    fitnessePathToXmlResultsIn('fitnesse-results*.xml')
	}
	publishHtml { 
            report(' '){
                reportName('HighSuiteReport')
                reportFiles('index.html') 
                keepAll() 
                allowMissing() 
                alwaysLinkToLastBuild() 
            }
        } 
    }
} 
     
    
    
buildPipelineView(under_folder+'Update-Environment-Flow') {
    startsWithParameters()
    filterBuildQueue()
    filterExecutors()
    title('Pipeline for Environment Updating')
    displayedBuilds(5)
    selectedJob(initial_job)
    alwaysAllowManualTrigger()
    showPipelineParameters()
    showPipelineDefinitionHeader()
    refreshFrequency(60)
}

deliveryPipelineView(under_folder+'Environment-Update-Pipeline') {
    showAggregatedPipeline true
    enableManualTriggers true
    pipelineInstances 5
    pipelines {
        component('Environment Provisioning Delivery Pipeline', initial_job)
    }
    }