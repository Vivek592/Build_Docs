def opco = "${OPCO}"
def build_jobs_folder ='Infra'

def parent_folder = build_jobs_folder+'/Pipelines'
def folder_name = "${PIPELINE_NAME}"
def under_folder = '/'+parent_folder+'/'+folder_name+'/'
def initial_job = "Pull-Code"
def ansible_slave = 'atg-${ENV_PREFIX}-aws-jsa01'
def atg_slave = 'atg-${ENV_PREFIX}-aws-jsa01'
def feature_branch="${FEATURE_BRANCH}"
def build_workspace="/deployment/ecomm/codebase/ATG"

folder(parent_folder)
folder(parent_folder+'/'+folder_name)
{
  primaryView(opco+'-Regression-CI-Pipeline')
}


job(under_folder +initial_job) {
    description 'Pull Code from Source Control'
    displayName("Pull Code ")
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
  parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('branch',feature_branch,'Feature Branch to build')
        choiceParam('DB_SERVER', ['czudcasym0md', 'cgedocbjqkfs'], 'For Devops ENVs value is cgedocbjqkfs For Dev ENVS value is czudcasym0md')

         labelParam('ATGSlave')
               
        
    } 
    
    customWorkspace(build_workspace) 
   
   scm {
             git {
             remote {
                 url 'https://github.com/KITSGitHubAdmin/KITS-App_ATG-Dev.git'
                 branch '${branch}'
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
		                trigger('Pull-Infra-Code') {
		                      condition('UNSTABLE_OR_BETTER')
		                      parameters{
		                      currentBuild() 
		                      predefinedProp('GIT_COMMIT','${GIT_COMMIT}')
		                      }                    
		                }
		            }
            }
}

buildFlowJob(under_folder+"Pull-Infra-Code") {
     displayName("Pull-Infra-Code")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
    }  
    
     buildFlow(
        "build('Infra/Build/CheckoutInfrCode',ENV_PREFIX: params['ENV_PREFIX'], branch: 'pre_develop', DeploymentSlave: params['ATGSlave'])"
       ) 
 
 configure { project -> project / 'buildNeedsWorkspace'('true') }


publishers {
          downstreamParameterized {
                trigger('Build-Flow') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters{
                      predefinedProp('ATGSlave',atg_slave)
                      currentBuild() 
                      }                    
                }
            }
        }
                
    }

buildFlowJob(under_folder+"Build-Flow") {
    displayName("Build " +opco)
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('branch',feature_branch,'Feature Branch to build')
        choiceParam('DB_SERVER', ['czudcasym0md', 'cgedocbjqkfs'], 'For Devops ENVs value is cgedocbjqkfs For Dev ENVS value is czudcasym0md')

         labelParam('ATGSlave')
               
        
    }  
    
      
    buildFlow(
        "build('ATG/Build/Opco/Build',ENV_PREFIX: params['ENV_PREFIX'] , ATGSlave: 'atg-'+params['ENV_PREFIX']+'-aws-jsa01', branch:params['branch'], OPCO:'"+opco+"' )"
       )  
   
   publishers {
   
    downstreamParameterized {
                trigger('AssembleFlow,SonarFlow') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters{
                      predefinedProp('ATGSlave',atg_slave)
                      currentBuild() 
                      }                    
                }
            }
            
      joinTrigger {
            publishers {
                downstreamParameterized {
	                trigger('Prepare-ForDeploy-Flow') {
	                      condition('UNSTABLE_OR_BETTER')
	                      parameters{
	                      predefinedProp('ATGSlave',atg_slave)
	                      currentBuild()      
	                      }               
	                      }
	                  }
                }
            } 
            
    }
   configure { project -> project / 'buildNeedsWorkspace'('true') }       
           
    } 
   
 buildFlowJob(under_folder+"SonarFlow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')
        stringParam('branch',feature_branch,'Feature Branch to build')
        
       
    }  

   buildFlow(
        "build('ATG/Build/SonarAnalysis',ENV_PREFIX: params['ENV_PREFIX'] , ATGSlave: 'atg-'+params['ENV_PREFIX']+'-aws-jsa01', branch:params['branch'], GIT_COMMIT: build.environment.get('GIT_COMMIT'), version_no: build.environment.get('BUILD_NUMBER'), CUSTOM_WS:'"+build_workspace+"')"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
   
                
    }  

 buildFlowJob(under_folder+"AssembleFlow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')
        stringParam('branch',feature_branch,'Feature Branch to build')
        
       
    }  
    customWorkspace(build_workspace) 
   buildFlow(
        "build('ATG/Build/Opco/Assembly/AssembleEars',ENV_PREFIX: params['ENV_PREFIX'] , ATGSlave: params['ATGSlave'], branch:params['branch'], OPCO:'"+opco+"')"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
             
    
                
    } 
    
buildFlowJob(under_folder+"Prepare-ForDeploy-Flow") {
     displayName("Prepare")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('APP_VERSION','','Release Number')
        stringParam('branch',feature_branch,'Feature Branch to build')
        labelParam('ATGSlave')
       
    }
    customWorkspace(build_workspace)   
     buildFlow(
         "build('ATG/Build/Opco/Release/CopyEarsToDeployDir',ENV_PREFIX: params['ENV_PREFIX'] , ATGSlave: params['ATGSlave'], branch:params['branch'],OPCO:'"+opco+"')" 
          ) 
       
    configure { project -> project / 'buildNeedsWorkspace'('true') }
   
      publishers {
          downstreamParameterized {
                trigger('StopATG-Flow') {
                condition('UNSTABLE_OR_BETTER')
                parameters{
                currentBuild() 
                predefinedProp('AnsibleSlave',ansible_slave)         
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
        "build('ATG/Deploy/StopATGEnv',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'], Stop_Fitnesse: 'no')"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
             
    publishers {
          downstreamParameterized {
                trigger('UpdateDatabase-Flow') {
                      condition('UNSTABLE_OR_BETTER')
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
         choiceParam('DB_SERVER', ['czudcasym0md','cgedocbjqkfs'], 'For Devops ENVs value is cgedocbjqkfs For Dev ENVS value is czudcasym0md')


        
    }  
    
   buildFlow(
        "build('ATG/Build/Opco/Database/dbIncAndLoadData', ATGSlave: params['ATGSlave'], ENV_PREFIX: params['ENV_PREFIX'], branch:params['branch'],DEV_PROFILE:'DEV', DB_SERVER:params['DB_SERVER'], OPCO:'"+opco+"')"
       )  
 configure { project -> project / 'buildNeedsWorkspace'('true') }
          
    publishers {
          downstreamParameterized {
                trigger('DeployATG-Flow') {
                condition('UNSTABLE_OR_BETTER')
                parameters{
                currentBuild() 
                predefinedProp('AnsibleSlave',ansible_slave)  
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
                      condition('UNSTABLE_OR_BETTER')
                      parameters{
                      currentBuild()
                      }                     
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
        "build('ATG/Deploy/StartATGEnv',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'],Start_Fitnesse: 'no')"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
             
    publishers {
          downstreamParameterized {
                trigger('EndPointTest-Flow') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters{
                      predefinedProp('ATGSlave',atg_slave)
                      currentBuild() 
                      }                    
                }
            }
        }
                
    } 
    
    
   
buildFlowJob(under_folder+"EndPointTest-Flow") {
     displayName("EndPointTest")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')      
      
    } 

   buildFlow(
"sleep(300000)\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://'+params['ENV_PREFIX']+'-afa.aws.gha.kfplc.com/agent-front')\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://'+params['ENV_PREFIX']+'-storefront.aws.gha.kfplc.com/healthcheck/build_version.properties')\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://atg-'+params['ENV_PREFIX']+'-aws-aux01.aws.gha.kfplc.com:8050/dyn/admin/atg/dynamo/admin/en/')\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://atg-'+params['ENV_PREFIX']+'-aws-pub01.aws.gha.kfplc.com:8070/atg/bcc')\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://'+params['ENV_PREFIX']+'-storefront.aws.gha.kfplc.com/')\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://atg-'+params['ENV_PREFIX']+'-aws-app01.aws.gha.kfplc.com:8080/')\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://atg-'+params['ENV_PREFIX']+'-aws-stg01.aws.gha.kfplc.com:8060/')\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://atg-'+params['ENV_PREFIX']+'-aws-app01.aws.gha.kfplc.com:8080/dyn/admin')\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://atg-'+params['ENV_PREFIX']+'-aws-itl01.aws.gha.kfplc.com:8006/endeca_jspref/build_version.properties')\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://atg-'+params['ENV_PREFIX']+'-aws-aux03.aws.gha.kfplc.com:8110/dyn/admin/nucleus/atg/commerce/endeca/index/ProductCatalogSimpleIndexingAdmin/')\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://atg-'+params['ENV_PREFIX']+'-aws-stg01.aws.gha.kfplc.com:8060/dyn/admin/nucleus/atg/commerce/endeca/index/ProductCatalogSimpleIndexingAdmin/')\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://atg-'+params['ENV_PREFIX']+'-aws-itl01.aws.gha.kfplc.com:8006/endeca_jspref/controller.jsp?N=0&eneHost=localhost&enePort=16002')\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://atg-'+params['ENV_PREFIX']+'-aws-itl01.aws.gha.kfplc.com:8006/endeca_jspref/controller.jsp?N=0&eneHost=atg-'+params['ENV_PREFIX']+'-aws-mdex01.aws.gha.kfplc.com&enePort=16000')\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://atg-'+params['ENV_PREFIX']+'-aws-itl01.aws.gha.kfplc.com:8080/search-service/json/pages/browse')\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://'+params['ENV_PREFIX']+'-mdex.aws.gha.kfplc.com:8080/search-service/json/pages/browse')\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://atg-'+params['ENV_PREFIX']+'-aws-mdex01.aws.gha.kfplc.com:8080/search-service/json/pages/browse')\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://'+params['ENV_PREFIX']+'-mdex.aws.gha.kfplc.com:8080/search-service/build_version.properties')\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://'+params['ENV_PREFIX']+'-tradepoint.aws.gha.kfplc.com/')\n\
 build('Infra/Test/EndPointTest', ENV_PREFIX: params['ENV_PREFIX'],URL_TO_CHECK: 'http://atg-'+params['ENV_PREFIX']+'-aws-itl01.aws.gha.kfplc.com:8006/')"
 )

 configure { project -> project / 'buildNeedsWorkspace'('true') }
 
 publishers {
          downstreamParameterized {
                trigger('RegressionTest-Flow') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters{
                      predefinedProp('ATGSlave',atg_slave)
                      currentBuild() 
                      }                    
                }
            }
        }
                
    } 
    
    
   
buildFlowJob(under_folder+"RegressionTest-Flow") {
     displayName("RegressionTest")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
    }  
    
     buildFlow(
        "build('Infra/Control/ProvideSSHAccessToUser',ENV_PREFIX: params['ENV_PREFIX'], user_name: 'Reg_test', inventory_group: 'mdex', user_group: 'endeca', delete: 'yes', expires: '1', DeploymentSlave: params['AnsibleSlave'])\n\
         build('Infra/Control/ProvideSSHAccessToUser',ENV_PREFIX: params['ENV_PREFIX'], user_name: 'Reg_test', inventory_group: 'storefront', user_group: 'jboss', delete: 'yes', expires: '1', DeploymentSlave: params['AnsibleSlave'])\n\
         build('Infra/Control/ReTagAwsEnvironment',ENV_PREFIX: params['ENV_PREFIX'], DeploymentSlave: params['AnsibleSlave'], Charge_Code: '22040')"
       ) 
 
 configure { project -> project / 'buildNeedsWorkspace'('true') }


publishers {
          downstreamParameterized {
                trigger('ProductTest-Flow') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters{
                      predefinedProp('ATGSlave',atg_slave)
                      currentBuild() 
                      }                    
                }
            }
        }
                
    } 
    
    
   
buildFlowJob(under_folder+"ProductTest-Flow") {
     displayName("ProductTest")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
    }  
    
     buildFlow(
        "build('ATG/Test/ExtractTestMaterialForAllTestSlaves',branch: 'master_cr4')\n\
         build('AutomatedTests/DynamicArea/HighSuite/CheckoutWithDsvAndSfcProduct',ENV_PREFIX: params['ENV_PREFIX'], TestSlaveLabel: 'ACTIVATE_TESTER', RunWhere: 'DEFAULTGRID',Browser: 'chrome', GridURL: 'http://atg-shared-aws-selenium01.aws.gha.kfplc.com:4445/wd/hub',site: 'DIY',testName:'DarwinAcceptanceTests.IntegrationWeb.TestForCloud.CheckoutWithDsvAndSfcProduct')\n\
         build('AutomatedTests/DynamicArea/HighSuite/Esp13ProductSearchResult',ENV_PREFIX: params['ENV_PREFIX'], TestSlaveLabel: 'ACTIVATE_TESTER', RunWhere: 'DEFAULTGRID',Browser: 'chrome', GridURL: 'http://atg-shared-aws-selenium01.aws.gha.kfplc.com:4445/wd/hub',site: 'DIY',testName:'EndecaTests.IntegrationTestSuite.Esp13ProductSearchResult')\n\
         build('AutomatedTests/DynamicArea/HighSuite/SearchProduct',ENV_PREFIX: params['ENV_PREFIX'], TestSlaveLabel: 'ACTIVATE_TESTER', RunWhere: 'DEFAULTGRID',Browser: 'chrome', GridURL: 'http://atg-shared-aws-selenium01.aws.gha.kfplc.com:4445/wd/hub',site: 'TP',testName:'DarwinAcceptanceTests.WebTpSuiteForSqa3.Sqa3Scripts.SearchProduct')"
       ) 
 
 configure { project -> project / 'buildNeedsWorkspace'('true') } 
              
    } 


deliveryPipelineView(under_folder+opco+'-CI-Pipeline') {
    showAggregatedPipeline false
    enableManualTriggers true
    pipelineInstances 5
     configure { view -> view / 'allowPipelineStart'('true') }
    configure { view -> view / 'allowRebuild'('true') }
    configure { view -> view / 'showTotalBuildTime'('true') }
    pipelines {
        component(opco+' Regression CI Pipeline for branch - '+ feature_branch, initial_job)
    }
    }
