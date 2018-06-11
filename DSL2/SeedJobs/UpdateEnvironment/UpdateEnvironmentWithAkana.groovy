def parent_folder = '/Infra/Pipelines'
def folder_name = "${PIPELINE_FOLDER_NAME}"
def under_folder = '/'+parent_folder+'/'+folder_name+'/'
def initial_job = "RecreateATGSlave-Flow"
def atg_slave = 'atg-${ENV_PREFIX}-aws-jsa01'
def infra_code_branch= "${INFRA_CODE_BRANCH}"


folder(parent_folder)
folder(parent_folder+'/'+folder_name)
{
  primaryView('Update-Environment-With-Akana-Pipeline')
}

buildFlowJob(under_folder+"RecreateATGSlave-Flow") {
	displayName("RecreateATGSlave")
	parameters{
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('Charge_Code','','Charge code')
        stringParam('infra_branch',infra_code_branch,'Infrastructure code branch to use')
		stringParam('IntegratedEnv','no','Is this an Integrated Env')
        stringParam('SiloCount','1','Required number of instances for SiloEnv')
		choiceParam('OPCO',['BQUK','CAFR'])
		labelParam('GenericSlave')
	}
	
	buildFlow(
		"build('Infra/Build/CheckoutInfrCode', DeploymentSlave: params['GenericSlave'], branch: params['infra_branch']) \n"+
		"build('Infra/Deploy/CreateATGSlave',ENV_PREFIX: params['ENV_PREFIX'] , GenericSlave: params['GenericSlave'], Charge_Code: params['Charge_Code'] ) \n"+
		"build('Infra/Build/CheckoutInfrCode',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: 'atg-'+params['ENV_PREFIX']+'-aws-jsa01', branch: params['infra_branch'])"
	)
   configure { project -> project / 'buildNeedsWorkspace'('true') }            
    publishers {
          downstreamParameterized {
                trigger('ELBCreation-Flow') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters {
                      currentBuild()
					  predefinedProp('ATGSlave',atg_slave)
                      }                  
                }
            }
        }
  }

buildFlowJob(under_folder+"ELBCreation-Flow") {
	displayName("ELB Creation")
	parameters{
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('Charge_Code','','Charge code')
		stringParam('IntegratedEnv','no','Is this an Integrated Env')
        stringParam('SiloCount','1','Required number of instances for SiloEnv')
		choiceParam('OpCo',['BQUK','CAFR'])
	}
	
	buildFlow(
		"build('Infra/Deploy/CreateELB' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: 'atg-'+params['ENV_PREFIX']+'-aws-jsa01' ,Charge_Code: params['Charge_Code'], IntegratedEnv: params['IntegratedEnv'], WAIT_FOR: 25) \n"+
		"build('Infra/Deploy/CreateELBForAFA' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: 'atg-'+params['ENV_PREFIX']+'-aws-jsa01' ,Charge_Code: params['Charge_Code'], IntegratedEnv: params['IntegratedEnv'], WAIT_FOR: 25)"
	)
   configure { project -> project / 'buildNeedsWorkspace'('true') }            
    publishers {
          downstreamParameterized {
                trigger('CreateApache-Flow') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters {
                      currentBuild()
					  predefinedProp('ATGSlave',atg_slave)
                      }                  
                }
            }
        }
  }
 
buildFlowJob(under_folder+"CreateApache-Flow") {
   displayName("Create Apache Flow")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('Charge_Code','','Charge code')
        stringParam('infra_branch',infra_code_branch,'Infrastructure code branch to use')
		stringParam('IntegratedEnv','no','Is this an Integrated Env')
        stringParam('SiloCount','1','Required number of instances for SiloEnv')
		labelParam('ATGSlave')
    }
	
	buildFlow(
		"build('Infra/Deploy/CreateApacheOpco' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: 'atg-'+params['ENV_PREFIX']+'-aws-jsa01' ,Charge_Code: params['Charge_Code'], IntegratedEnv: params['IntegratedEnv'], instance_count: params['SiloCount'], WAIT_FOR: 70, OPCO: params['OPCO'])"
	)
	configure { project -> project / 'buildNeedsWorkspace'('true') }            
    publishers {
          downstreamParameterized {
                trigger('OIDC-Changes-Flow') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters {
                      currentBuild()
					  predefinedProp('ATGSlave',atg_slave)					  
                      }                  
                }
            }
        }
  }

 buildFlowJob(under_folder+"OIDC-Changes-Flow") {
   displayName("Make OIDC Changes Flow")

    parameters {
		stringParam('ENV_PREFIX','','Environment  pre-fix')
		stringParam('OpCo')
		labelParam('ATGSlave')
	}
	
	buildFlow(
		"build('ATG/Deploy/DeployOIDCMappings' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: 'atg-'+params['ENV_PREFIX']+'-aws-jsa01' , OpCo: params['OpCo'], state: 'present') \n"+
		"build('ATG/Deploy/TriggerOIDCProcessInGAPIJenkins' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: 'atg-'+params['ENV_PREFIX']+'-aws-jsa01')"
	)
	configure { project -> project / 'buildNeedsWorkspace'('true') }            
  }
 
  deliveryPipelineView(under_folder+'Update-Environment-With-Akana-Pipeline') {
    showAggregatedPipeline()
    enableManualTriggers true
    pipelineInstances 5
    configure { view -> view / 'allowPipelineStart'('true') }
    configure { view -> view / 'allowRebuild'('true') }
    configure { view -> view / 'showTotalBuildTime'('true') }
    
    pipelines {
        component('Update Environment with Akana Delivery Pipeline', initial_job)
    }
    }