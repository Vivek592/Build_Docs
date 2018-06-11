def build_jobs_folder ='ATG/Build/BQ'

def parent_folder = build_jobs_folder+'/Pipelines'
def folder_name = "${FEATURE_NAME}"
def under_folder = '/'+parent_folder+'/'+folder_name+'/'
def initial_job = "Build-Flow"
def ansible_slave = 'atg-${ENV_PREFIX}-aws-slv01'
def atg_slave = 'atg-${ENV_PREFIX}-aws-jsa01'
def shared_slave = 'atg-shared-aws-slv01'
def feature_branch="${FEATURE_NAME}"

folder(parent_folder)
folder(parent_folder+'/'+folder_name)
{
  primaryView('BQ-CI-Pipeline')
}

buildFlowJob(under_folder+initial_job) {
    displayName("Build BQ")
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('branch',feature_branch,'Feature Branch to build')
        stringParam('ATGSlave',atg_slave,'Initial slave to launch')         
        
    }  
   label(atg_slave)
   
   
   
   scm {
             git {
             remote {
                 url 'https://github.com/KITSGitHubAdmin/KITS-App_ATG-Dev.git'
                 branch '${branch}'
                 credentials 'ca5be30b-657d-4c0d-8191-edc84a8ad31e'
                 }          
             }
           }
           
           
    buildFlow(
        "build('"+build_jobs_folder+"/BuildBQ',ENV_PREFIX: params['ENV_PREFIX'] , ATGSlave: 'atg-'+params['ENV_PREFIX']+'-aws-jsa01', branch:params['branch'], GIT_COMMIT: build.environment.get('GIT_COMMIT'))"
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
        stringParam('branch',feature_branch,'Feature Branch to build')
        
       
    }  
   buildFlow(
        "build('"+build_jobs_folder+"/Assembly/AssembleEars',ENV_PREFIX: params['ENV_PREFIX'] , ATGSlave: params['ATGSlave'], branch:params['branch'])"
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
        stringParam('branch',feature_branch,'Feature Branch to build')
        labelParam('ATGSlave')
       
    }  
     buildFlow(
         "build('"+build_jobs_folder+"/Release/CopyEarsToDeployServer',ENV_PREFIX: params['ENV_PREFIX'] , ATGSlave: params['ATGSlave'], branch:params['branch'])" 
          ) 
       
    configure { project -> project / 'buildNeedsWorkspace'('true') }
   
      publishers {
          downstreamParameterized {
                trigger('StopATG-Flow') {
                setCondition('UNSTABLE_OR_BETTER')
                parameters{
                    predefinedProp('ATGSlave',atg_slave)                        
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
         stringParam('branch',feature_branch,'Feature Branch to build')
        
    }  
    
   buildFlow(
        "build('"+build_jobs_folder+"/Database/dbIncAndLoadData', ATGSlave: params['ATGSlave'], ENV_PREFIX: params['ENV_PREFIX'], branch:params['branch'])"
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
            report('htmlreport') {
               reportName('HighSuiteReport')
                reportFiles('index.html')
                keepAll()
                allowMissing()
                alwaysLinkToLastBuild()
            }
            }
            
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
        stringParam('branch',feature_branch,'Feature Branch to build')
           
    }  
   buildFlow(
        "build('"+build_jobs_folder+"/Release/CreateCIRelease',APP_VERSION: 'ATG_CI_BQ_'+build.properties.get('number') , AnsibleSlave: params['AnsibleSlave'], branch:params['branch'])"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
    
  
    } 
    
    
deliveryPipelineView(under_folder+'BQ-CI-Pipeline') {
    showAggregatedPipeline true
    enableManualTriggers true
    pipelineInstances 5
     configure { view -> view / 'allowPipelineStart'('true') }
    configure { view -> view / 'allowRebuild'('true') }
    configure { view -> view / 'showTotalBuildTime'('true') }
    pipelines {
        component('BQ CI Pipeline - '+ feature_branch, initial_job)
    }
    }