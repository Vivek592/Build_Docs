def top_folder = '/WMB'
def under_folder = '/WMB/Deploy'

folder(top_folder)
folder(under_folder)

job(under_folder +'/DeployWMBMock') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('APP_VERSION')
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX')
        stringParam('WEB_SERVICE_NAME')
    }  
    
    concurrentBuild(true)
     
    steps {
	   
        shell ('cd /deployment/ansible\n\n'+
    	    'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/deployWarToTomcat.yml -e "inventory_group=tomcat nexus_file_path=${WMB_NEXUS_RELEASE_LOCATION}/com/kfplc/easier/wmb/mock/${WEB_SERVICE_NAME}/${APP_VERSION} filename=${WEB_SERVICE_NAME}-${APP_VERSION}.war" -vvvv')
    }  
}