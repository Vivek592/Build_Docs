def top_folder = '/Infra'
def under_folder = '/Infra/Database'

folder(top_folder)
folder(under_folder)

def workspace = '/deployment/ansible/'
def project_ws = ['is-appsetup','is-iaas','is_inventory']

def job_name= under_folder +'/RestoreRDSAndDeleteSnapshot'

job(job_name) {
    description 'Restore RDS And Delete Snapshot'
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
                        trigger("RestoreRDSSnapshot")
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
			downstreamParameterized {
                        trigger("DeleteRDSSnapshot")
                        {
                        parameters{
                        currentBuild()
                        }
                        }
                        }

}
}
