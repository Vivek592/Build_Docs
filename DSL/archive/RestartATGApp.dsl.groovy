def under_folder = '/ATG/Deploy/'
def job_name = under_folder +'RestartATGApp'


job(job_name) {
    description 'Restart a given ATG application'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('persona','','Environment  pre-fix')
        labelParam('DeploymentSlave')
        choiceParam('action', ['start','stop','restart'],'use restart to stop & start the app')
       
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell (' echo "stop/start/restart script"')
    }  
}
