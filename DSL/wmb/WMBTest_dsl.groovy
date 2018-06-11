def top_folder = '/WMB'
def under_folder = '/WMB/Test'

folder(top_folder)
folder(under_folder)

job(under_folder +'/TestMQGateWay') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX')
    }  
    
    
    steps {
	downstreamParameterized {
            trigger('WMB/Deploy/StatusCheckMQ') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
            }
        }
    }  
}

job(under_folder +'/TestWMB') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX')
    }  
    
    
    steps {
	downstreamParameterized {
            trigger('WMB/Deploy/StatusCheckWMB') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
            }
        }
    }  
}

