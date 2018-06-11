def under_folder = '/ATG/Deploy/'
def job_name = under_folder +'DeployATGApp'


job(job_name) {
    description 'Deploys a given ATG application'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('persona','','Environment  pre-fix')
        labelParam('DeploymentSlave')
       
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell ('echo "deploy atg script"')
    }  
}
