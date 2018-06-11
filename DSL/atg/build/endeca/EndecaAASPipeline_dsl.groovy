def top_folder = '/ATG'
def sub_folder = '/ATG/Build'
def sub_folder1 = '/ATG/Deploy'
def under_folder1 = '/ATG/Build/Endeca'
def under_folder2 = '/ATG/Deploy/Endeca'

folder(top_folder)
folder(sub_folder)
folder(under_folder1)
folder(sub_folder1)
folder(under_folder2)

def parent_folder = 'ATG/Build/Endeca/AaaS-Pipelines'
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
  primaryView('Endeca AAS-CI-Pipeline')
}

buildFlowJob(under_folder+initial_job) {
    displayName("Build")
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('branch',branch,'Feature Branch to build')
        stringParam('ATGSlave',atg_slave,'Initial slave to launch')         
        
    }  
   label(shared_slave)
   
   
    buildFlow(
        "build('ATG/Build/Endeca/BuildAAS',ENV_PREFIX: params['ENV_PREFIX'] , ATGSlave: 'atg-'+params['ENV_PREFIX']+'-aws-jsa01', branch:params['branch'])"
       )  
   
    publishers {
        downstreamParameterized {
            trigger('DeployEndeca-Flow') {
                setCondition('UNSTABLE_OR_BETTER')
                parameters{
                    predefinedProp('AnsibleSlave','atg-${ENV_PREFIX}-aws-slv01')                        
                    currentBuild() 
                }                      
            }
        }    
    }
    configure { project -> project / 'buildNeedsWorkspace'('true') }       
} 

buildFlowJob(under_folder+"DeployEndeca-Flow") {
    displayName("Deploy")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('APP_VERSION','1.0.0','Application Release ')
        labelParam('AnsibleSlave')
    }  
    buildFlow(
	"parallel( { build('ATG/Deploy/Endeca/DeploySearchServiceITL',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'], APP_VERSION: params['APP_VERSION'])  }, \n\
	{ build('ATG/Deploy/Endeca/DeploySearchServiceMdex',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'], APP_VERSION: params['APP_VERSION']) } \n\
        )"
    ) 
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
             
    publishers {
        downstreamParameterized {
            trigger('Test-Flow') {
                      setCondition('UNSTABLE_OR_BETTER')
            }
        }
    }
}

buildFlowJob(under_folder+"Test-Flow") {
     displayName("Test")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('APP_VERSION','1.0.0','Application Release ')

        labelParam('AnsibleSlave')
    }       
    configure { project -> project / 'buildNeedsWorkspace'('true') }
}  
    
deliveryPipelineView(under_folder+'Endeca AAS-CI-Pipeline') {
    showAggregatedPipeline true
    enableManualTriggers true
    pipelineInstances 5
     configure { view -> view / 'allowPipelineStart'('true') }
    configure { view -> view / 'allowRebuild'('true') }
    configure { view -> view / 'showTotalBuildTime'('true') }
    pipelines {
        component('Endeca AaaS CI Pipeline - '+ branch, initial_job)
    }
}