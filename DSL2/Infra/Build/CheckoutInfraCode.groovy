def under_folder = '/Infra/Build/'
def wrapper_job_name = under_folder+'CheckoutInfraCode'
def IS_App_Setup_job_name = under_folder+'Checkout-IS-AppSetup'
def IS_IAAS_job_name = under_folder+'Checkout-IS-IAAS'
def IS_Inventory_name = under_folder+'Checkout-IS-Inventory'

folder('Infra')
folder('Infra/Build')

job(wrapper_job_name) {
    description 'Wrapper job to checkout Infrastructure repositories'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        stringParam('branch','develop','Working branch')
        labelParam('DeploymentSlave')
       
    } 
 
 steps {
        
        downstreamParameterized {
                trigger(IS_App_Setup_job_name+','+IS_IAAS_job_name+','+IS_Inventory_name)
                 {
                   block {
                    buildStepFailure('FAILURE')
                     failure('FAILURE')
                     unstable('UNSTABLE')
                   }
                     parameters{
                      currentBuild()
                    }
                }

            }
        }
    
   
}


def giturl = 'http://git.aws.gha.kfplc.com/KITS/'
def projects = ['IS-AppSetup','IS-IAAS','IS-Inventory']
def worspace = '/deployment/ansible/'
def project_ws = ['is-appsetup','is-iaas','is-inventory']

for( i in 0..2)
{
job(under_folder+'Checkout-'+projects[i]) {
    
  logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        stringParam('branch','develop','Working branch')
        labelParam('DeploymentSlave')
       
    } 
    
    customWorkspace(worspace+project_ws[i]);
        scm {
             git {
             remote {
                 url giturl+projects[i]+'.git'
                 branch '${branch}'
                 credentials 'efce5953-3a01-4bcd-b4ad-da13c22f712a'
                 refspec '+refs/heads/${branch}:refs/remotes/origin/${branch} --depth=1'
                 }          
             }
    }
    }
}