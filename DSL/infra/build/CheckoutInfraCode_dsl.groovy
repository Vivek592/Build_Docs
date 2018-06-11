def top_folder = '/Infra'
def under_folder = '/Infra/Build'
def wrapper_job_name = under_folder+'/CheckoutInfraCode'
def IS_App_Setup_job_name = 'Checkout-IS-AppSetup'
def IS_IAAS_job_name = 'Checkout-IS-IAAS'
def IS_Inventory_name = 'Checkout-IS_Inventory'

folder(top_folder)
folder(under_folder)

job(wrapper_job_name) {
    logRotator {
        daysToKeep 2
    }
    parameters {
        stringParam('branch','develop','Working branch')
        stringParam('ENV_PREFIX')
        labelParam('DeploymentSlave')
        }
    steps {
        downstreamParameterized {
            trigger(IS_App_Setup_job_name+','+IS_IAAS_job_name+','+IS_Inventory_name) {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
            }
        }
    }
}


def giturl = 'http://git.aws.gha.kfplc.com/KITS/'
def projects = ['IS-AppSetup','IS-IAAS','IS_Inventory']
def worspace = '/deployment/ansible/'
def project_ws = ['is-appsetup','is-iaas','is_inventory']

for( i in 0..2)
{
job(under_folder+'/Checkout-'+projects[i]) {
    
    logRotator {
        daysToKeep 2
    }
    
    parameters {
        stringParam('branch','develop','Working branch')
        labelParam('DeploymentSlave')
       
    } 
    
    customWorkspace(worspace+project_ws[i]);
        scm {
             git {
             remote {
                 url giturl+projects[i]
                 branch '${branch}'
                 credentials '943c3395-077b-4aab-9028-a173acc8155f'
                 name 'origin'
                 refspec '+refs/heads/${branch}:refs/remotes/origin/${branch} --depth=1'
                 }          
             }
        }
    }
}