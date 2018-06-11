def parent_folder = '/Infra/Pipelines'
def opco="${OPCO}"
def folder_name = opco+"-${PIPELINE_FOLDER_NAME}"
def under_folder = '/'+parent_folder+'/'+folder_name+'/'
def initial_job = "Provision-Cloud-Environment-Flow"
def ansible_slave = 'atg-${ENV_PREFIX}-aws-jsa01'
def atg_slave = 'atg-${ENV_PREFIX}-aws-jsa01'
def shared_slave = "${SHARED_SLAVE}"
def db_seed_env = ""
def db_seed_version = ""
def rds_instance_type="${RDS_INSTANCE_TYPE}"
def rds_db_size_GB="${RDS_DB_SIZE}"
def dryRun="${DRY_RUN}"
def eac_release = opco+'-develop-1.0.0'
def ss_release = opco+'-develop-1.0.0'
def infra_code_branch= "${INFRA_CODE_BRANCH}"
def wmb_mock_release = "1"


folder(parent_folder)
folder(parent_folder+'/'+folder_name)
{
  primaryView('Environment-Delivery-Pipeline')
}

buildFlowJob(under_folder+"Provision-Cloud-Environment-Flow") {
    displayName("Create ATG Slave")
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('Charge_Code','','Charge code')
        stringParam('infra_branch',infra_code_branch,'Infrastructure code branch to use')
        stringParam('atg_branch','develop','ATG Code branch to use')   
        stringParam('DB_SEED_ENV',db_seed_env,'Environment  where seed was extracted from')
        stringParam('DB_SEED_VERSION',db_seed_version,'Seed version number')  
        stringParam('IntegratedEnv','no','Is this an Integrated Env')
        stringParam('SiloCount','1','Required number of instances for SiloEnv')
        choiceParam('EndecaAppName',['bqendeca','caendecafrfr','bdendecafrfr'],'Endeca Application to be deployed (e.g. "caendecafrfr" for Casto, "bqendeca" for DIY)')
        labelParam('GenericSlave')
    }  
   
   buildFlow(
          dryRun+" build('Infra/Build/CheckoutInfrCode', DeploymentSlave: params['GenericSlave'], branch: params['infra_branch']) \n"+
          dryRun+" build('Infra/Deploy/CreateATGSlave',ENV_PREFIX: params['ENV_PREFIX'] , GenericSlave: params['GenericSlave'], Charge_Code: params['Charge_Code'] ) \n"+
          dryRun+" build('Infra/Build/CheckoutInfrCode',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: 'atg-'+params['ENV_PREFIX']+'-aws-jsa01', branch: params['infra_branch']) \n"+
          dryRun+" build('Infra/Deploy/CreateFitnesseConfig',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: 'atg-'+params['ENV_PREFIX']+'-aws-jsa01') \n"+
          dryRun+" build('Infra/Deploy/PrepareS3File' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: 'atg-'+params['ENV_PREFIX']+'-aws-jsa01')" 
          )
   configure { project -> project / 'buildNeedsWorkspace'('true') }            
    publishers {
          downstreamParameterized {
                trigger('CreateMQGateWay-Flow,Create-RDS-Flow,Create-ATGEnv-Flow,Create-Endeca-Flow,Create-ATGSearch-Flow') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters {
                      currentBuild()
                      predefinedProp('ATGSlave',atg_slave)   
                      }                  

                }
            }
        }
  }
    
    
 
    
buildFlowJob(under_folder+"Create-ATGEnv-Flow") {
     displayName("Create-ATGEnv")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')
       
    }  
   buildFlow(
        dryRun+"build('Infra/Deploy/CreateAllATGAppEnvOpco',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'],Charge_Code: params['Charge_Code'],IntegratedEnv: params['IntegratedEnv'],SiloCount: params['SiloCount'],OPCO:'"+opco+"')"
       ) 
       
    configure { project -> project / 'buildNeedsWorkspace'('true') }
   
   
       
    } 
    
 
 
 buildFlowJob(under_folder+"Create-ATGSearch-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')
       
    }  
   buildFlow(
        dryRun+"build('Infra/Deploy/CreateAllATGSearch',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'], Charge_Code: params['Charge_Code'])"
       )            
    configure { project -> project / 'buildNeedsWorkspace'('true') }
                
    }
    
    
       
buildFlowJob(under_folder+"Create-RDS-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('rds_instance_type',rds_instance_type,'RDS Instance Type')
        stringParam('rds_db_size_GB',rds_db_size_GB,'RDS instance size')
        stringParam('DB_SEED_ENV',db_seed_env,'Environment  where seed was extracted from')
        stringParam('DB_SEED_VERSION',db_seed_version,'Seed version number')  
        labelParam('ATGSlave')
       
    }  
   buildFlow(
        dryRun+"build('Infra/Database/CreateRDSBase',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'], Charge_Code: params['Charge_Code'], rds_instance_type: params['rds_instance_type'], rds_db_size_GB: params['rds_db_size_GB'])"
       )            
    configure { project -> project / 'buildNeedsWorkspace'('true') }
  
  publishers {
          downstreamParameterized {
                trigger('LoadRDSfromSeed-Flow') {
                condition('UNSTABLE_OR_BETTER')
                parameters{
                currentBuild() 
                }                
                }
            }
        }    
                
    } 
  
  
  
buildFlowJob(under_folder+"LoadRDSfromSeed-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')
        stringParam('DB_SEED_ENV',db_seed_env,'Environment  where seed was extracted from')
        stringParam('DB_SEED_VERSION',db_seed_version,'Seed version number')    
        stringParam('GenericSlave',shared_slave,'Generic Slave')
    }  
    
   buildFlow(
        dryRun+"build('Infra/Database/Checkout-DB-Seed-Scripts', DeploymentSlave: params['ATGSlave']) \n"+
         dryRun+"build('Infra/Database/CreateDatabaseFromSeed',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'], DB_SEED_ENV: params['DB_SEED_ENV'], DB_SEED_VERSION: params['DB_SEED_VERSION'])\n"+
         dryRun+"build('Infra/Control/Schedulers/CreateActivateQuiesceSchedule',ENV_PREFIX: params['ENV_PREFIX'], SHARED_SLAVE: params['GenericSlave'] )"
       )  
 configure { project -> project / 'buildNeedsWorkspace'('true') }

    publishers {
          downstreamParameterized {
                trigger('PullATGCode') {
                condition('UNSTABLE_OR_BETTER')
                parameters{
                currentBuild() 
                }                  
                }
            }
        }
    } 

buildFlowJob(under_folder+"CreateMQGateWay-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')
       
    }  
   buildFlow(
        dryRun+"build('WMB/Deploy/CreateMQGateway',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'], Charge_Code: params['Charge_Code'])"
       )   
   configure { project -> project / 'buildNeedsWorkspace'('true') }
                
    publishers {
          downstreamParameterized {
                trigger('TestMQGateway-Flow') {
                    condition('UNSTABLE_OR_BETTER')
                    parameters{
                    currentBuild()      
                    }               
                }
            }
        }
                
    } 
    
    
    
buildFlowJob(under_folder+"TestMQGateway-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')
       
    }  
   buildFlow(
        dryRun+"build('WMB/Test/TestMQGateWay',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'])"
       )            
    configure { project -> project / 'buildNeedsWorkspace'('true') }
    
    
    
     
    }     
    

job(under_folder +"PullATGCode") {
    description 'Pull Code from Source Control'
    displayName("Pull ATG Code ")
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
  parameters {
         stringParam('ENV_PREFIX','','Environment  pre-fix')
         stringParam('atg_branch','develop','Environment  pre-fix')
         labelParam('ATGSlave')            
    }   
    
    customWorkspace("/deployment/ecomm/codebase/ATG") 
   
   scm {
             git {
             remote {
                 url 'https://github.com/KITSGitHubAdmin/KITS-App_ATG-Dev.git'
                 branch '${atg_branch}'
                 credentials 'ca5be30b-657d-4c0d-8191-edc84a8ad31e'
                 }
                         
             }
             
           }
           
      steps {           
       shell('git clean -fd \n' +
             'echo BUILD_VERSION=${GIT_COMMIT} > build_version.properties')
             }
      
       publishers {
		   
		    downstreamParameterized {
		                trigger('BuildATG-Flow') {
		                      condition('UNSTABLE_OR_BETTER')
		                      parameters{
		                      currentBuild() 
		                      predefinedProp('GIT_COMMIT','${GIT_COMMIT}')
		                      }                    
		                }
		            }
            }
}

buildFlowJob(under_folder+"BuildATG-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('atg_branch','develop','Environment  pre-fix')
        labelParam('ATGSlave')
       
    }  
    
      configure { project -> project / 'buildNeedsWorkspace'('true') }
      
   buildFlow(
        dryRun+"build('ATG/Build/Opco/Build', branch:params['atg_branch'] ,ENV_PREFIX: params['ENV_PREFIX'],  ATGSlave: params['ATGSlave'], OPCO:'"+opco+"')\n"
       )  
             
    
     publishers {
          downstreamParameterized {
                trigger('AssembleATG-Flow') {
                condition('UNSTABLE_OR_BETTER')
                parameters{
                currentBuild() 
                }                
                }
            }
        }  
        
          
                
    } 


buildFlowJob(under_folder+"AssembleATG-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('atg_branch','develop','Environment  pre-fix')
        
        labelParam('ATGSlave')
       
    }  
   buildFlow(
        dryRun+" build('ATG/Build/Opco/Assembly/AssembleEars',ENV_PREFIX: params['ENV_PREFIX'],  ATGSlave: params['ATGSlave'], branch:params['atg_branch'], OPCO:'"+opco+"')\n"+
        dryRun+" build('ATG/Build/Opco/Release/CopyEarsToDeployDir',ENV_PREFIX: params['ENV_PREFIX'] , ATGSlave: params['ATGSlave'], branch:params['atg_branch'], OPCO:'"+opco+"')"
       )  
   configure { project -> project / 'buildNeedsWorkspace'('true') }
             
    
     publishers {
          downstreamParameterized {
                trigger('UpdateDatabase-Flow') {
                condition('UNSTABLE_OR_BETTER')
                parameters{
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
        stringParam('atg_branch','develop','Feature Branch to build')
      	stringParam('DB_SERVER','czudcasym0md','For Devops ENVs value is cgedocbjqkfs, For Dev ENVS value is czudcasym0md')
    }  
    
   buildFlow(
        dryRun+"build('ATG/Build/Opco/Database/dbIncAndLoadData', ATGSlave: params['ATGSlave'], ENV_PREFIX: params['ENV_PREFIX'], branch:params['atg_branch'], OPCO:'"+opco+"',DB_SERVER: params['DB_SERVER'], DEV_PROFILE: 'DEV')"
       )  
 configure { project -> project / 'buildNeedsWorkspace'('true') }
          
    publishers {
          downstreamParameterized {
                trigger('DeployATG-Flow,Deploy-Endeca-Flow,DeployWMBMocks-Flow') {
                condition('UNSTABLE_OR_BETTER')
                parameters{
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

        labelParam('ATGSlave')
       
    }  
   buildFlow( dryRun+"build('ATG/Deploy/CreateATGLocalConfigs',ENV_PREFIX: params['ENV_PREFIX'],  DeploymentSlave: params['ATGSlave']) \n "+
        dryRun+"build('ATG/Deploy/DeployAllATGApps',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave']) \n"+
        //commenting out as we are not deleting/creating swagger anymore - we will only update
        //dryRun+" build('ATG/Deploy/deployAkanaApi',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'])\n"+
        dryRun+"build('ATG/Deploy/DeployAllLocalConfigs',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'])"
       )  
   configure { project -> project / 'buildNeedsWorkspace'('true') }
             
    publishers {
          downstreamParameterized {
                trigger('StartATG-Flow') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters{
                      currentBuild()          
                      }           
                }
            }
        }
                
    } 
      
buildFlowJob(under_folder+"StartATG-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')
        
       
    }  
   buildFlow(
        dryRun+"build('ATG/Deploy/StartATGEnv',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'], Start_Fitnesse: 'false')"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
             
    publishers {
          downstreamParameterized {
                trigger(['ConfigureATG-Flow','Endeca-Indexing-Flow']) {
                      condition('UNSTABLE_OR_BETTER')
                      parameters{
                      currentBuild()         
                      }            
                }
            }
        }
                
    } 


buildFlowJob(under_folder+"Endeca-Indexing-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')
       
    }  
   buildFlow(
        dryRun+"build('ATG/Deploy/Endeca/TriggerBaselineIndex',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'])"
       )
       
    configure { project -> project / 'buildNeedsWorkspace'('true') }
                
                
    } 
 
 
    
    
buildFlowJob(under_folder+"ConfigureATG-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')
       
    }  
   buildFlow(
        dryRun+"build('ATG/Test/Fitnesse/PrepareFitnesseForRun',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave']) \n"+
        dryRun+"build('ATG/Deploy/ConfigureBccAgents',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave']) \n"
       )
       
    configure { project -> project / 'buildNeedsWorkspace'('true') }
                
    publishers {
          downstreamParameterized {
                trigger('TestATG-Flow') {
                	  condition('UNSTABLE_OR_BETTER')
                	  parameters{
                      currentBuild()        
                      }             
                }
            }
        }
                
    } 
 
 
   


 buildFlowJob(under_folder+"Create-Endeca-Flow") {
     displayName("Create-Endeca-Servers")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')
       
    }  
   buildFlow(
        dryRun+"build('Infra/Deploy/Endeca/CreateEndecaSilo',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'], Charge_Code: params['Charge_Code'], EndecaAppName: params['EndecaAppName'] ) "
       ) 
       
    configure { project -> project / 'buildNeedsWorkspace'('true') }
   
                
    }  
  
  
  buildFlowJob(under_folder+"Deploy-Endeca-Flow") {
     displayName("Deploy-Endeca-Servers")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')
       
    }

   /* if ( opco == "CASTO" ){
   eac_release = 'Casto-develop-1.0.0'
   ss_release = 'Casto-develop-1.0.0'
   }
   
   if ( opco == "BRICO" ){
   eac_release = 'Brico-develop-1.0.0'
   ss_release = 'Brico-develop-1.0.0'
   } */
   
   if ( opco == "BQUK" ){
   eac_release = 'BQ-develop-1.0.0'
   ss_release = 'BQ-develop-1.0.0'
   }
   
   if ( opco == "CAFR" ){
   eac_release = 'Casto-develop-1.0.0'
   ss_release = 'Casto-develop-1.0.0'
   }
   
   if ( opco == "BDFR" ){
   eac_release = 'Brico-develop-1.0.0'
   ss_release = 'Brico-develop-1.0.0'
   }
     
   buildFlow(
        dryRun+"build('ATG/Deploy/Endeca/DeployEndecaApps',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'], EAC_RELEASE: '"+eac_release+"', SS_RELEASE: '"+ss_release+"' , deploy_mode: 'init', APP_NAME: params['EndecaAppName'])"
       ) 
       
    configure { project -> project / 'buildNeedsWorkspace'('true') }

                
    } 

  buildFlowJob(under_folder+"DeployWMBMocks-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('APP_VERSION',wmb_mock_release,'ATG Application Release ')
        labelParam('ATGSlave')
    }  

   buildFlow(
        dryRun+"build('WMB/Deploy/DeployAllWMBMocks',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'], APP_VERSION: params['APP_VERSION'])"
       )   
   configure { project -> project / 'buildNeedsWorkspace'('true') }
   
           publishers {
          downstreamParameterized {
                trigger('StartWMBMocks-Flow') {
                    condition('UNSTABLE_OR_BETTER')
                    parameters{
                    currentBuild()        
                    }             
                }
            }
        }
   } 

   buildFlowJob(under_folder+"StartWMBMocks-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')
       
    }  
   buildFlow(
        dryRun+"build('WMB/Deploy/StopStartTomcat',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'], action:'restart')"
       )   
   configure { project -> project / 'buildNeedsWorkspace'('true') }
    } 

deliveryPipelineView(under_folder+'Environment-Delivery-Pipeline') {
    showAggregatedPipeline()
    enableManualTriggers true
    pipelineInstances 5
    configure { view -> view / 'allowPipelineStart'('true') }
    configure { view -> view / 'allowRebuild'('true') }
    configure { view -> view / 'showTotalBuildTime'('true') }
    
    pipelines {
        component('Environment Provisioning Delivery Pipeline', initial_job)
    }
    }
