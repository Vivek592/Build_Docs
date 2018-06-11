def top_folder = '/Infra'
def under_folder = '/Infra/Database'

folder(top_folder)
folder(under_folder)

def workspace = '/deployment/ansible/'
def project_ws = ['is-appsetup','is-iaas','is_inventory']

def job_name= under_folder +'/CreateSnapshotAndDeleteRDS'

job(job_name) {
    description 'Create Database snapshot and delete RDS'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }

parameters {
		stringParam('ENV_PREFIX')
		stringParam('snapshot_name')
		labelParam('DeploymentSlave')
		}
steps {
          downstreamParameterized {
                        trigger("CreateRDSSnapshot")
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
      shell('rm -rf /deployment/ecomm/rds \n'+
			'mkdir /deployment/ecomm/rds \n'+
			'echo "snapshot_name="${snapshot_name} > /deployment/ecomm/rds/snapshot.properties')

          downstreamParameterized {
                        trigger("DeleteRDS")
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
