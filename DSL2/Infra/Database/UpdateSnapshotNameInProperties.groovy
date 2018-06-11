def top_folder = '/Infra'
def under_folder = '/Infra/Database'

folder(top_folder)
folder(under_folder)

def workspace = '/deployment/ansible/'
def project_ws = ['is-appsetup','is-iaas','is_inventory']

def job_name= under_folder +'/UpdateSnapshotNameInProperties'

job(job_name) {
    description 'Update snapshot name in slave properties'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }

parameters {
		labelParam('DeploymentSlave')
		stringParam('snapshot_name')
        }
steps {
          shell('rm -rf /deployment/ecomm/rds \n'+
				'mkdir /deployment/ecomm/rds \n'+
				'echo "snapshot_name="${snapshot_name} > /deployment/ecomm/rds/snapshot.properties')
				
}
}
