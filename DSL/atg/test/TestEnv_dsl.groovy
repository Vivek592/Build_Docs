def under_folder = '/ATG/Test/'
def job_name = under_folder +'TestATGEnv'

job(job_name ) {
    description 'test a given ATG application'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {

        labelParam('DeploymentSlave')
       
    }  
        deliveryPipelineConfiguration("test","test ATG ENV");

    
    steps {
    
      shell (' echo "testscript"')
    }  
}
