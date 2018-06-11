def top_folder = '/WMB'
def under_folder = '/WMB/Deploy'

folder(top_folder)
folder(under_folder)


job(under_folder +'/CreateMQGateway') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX')
 
    }  
    
    concurrentBuild(true)
     
    steps {
	   
        shell ('cd /deployment/ansible\n'+
    	    'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/createMQGateway.yml -e "inventory_group=gateway svr_create=yes chargeCode=${Charge_Code}"')
    }  
}

job(under_folder +'/CreateWMB') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX')
        stringParam('svr_create','yes')
        stringParam('Charge_Code')
    }  
    
    concurrentBuild(true)
     
    steps {
	   
        shell ('cd /deployment/ansible\n'+
    	    'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/createMB.yml -e "inventory_group=wmb svr_create=${svr_create} chargeCode=${Charge_Code}" -vvvv')
    }  
}

buildFlowJob(under_folder+"/DeployAllWMBMocks") {
    displayName("DeployAllWMBMocks")
    parameters {
        stringParam('APP_VERSION')  
    }  
   
    buildFlow(
          "parallel ( \n"+
          "           { build(\'DeployWMBMock\',WEB_SERVICE_NAME: \'MockCarrierBookingService\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], APP_VERSION: params[\'APP_VERSION\']) },\n"+
          "           { build(\'DeployWMBMock\',WEB_SERVICE_NAME: \'MockFulfilmentSourceService\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],APP_VERSION: params[\'APP_VERSION\']) },\n"+
          "           { build(\'DeployWMBMock\',WEB_SERVICE_NAME: \'MockProductAvailabilityService\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],APP_VERSION: params[\'APP_VERSION\']) },\n"+
          "           { build(\'DeployWMBMock\',WEB_SERVICE_NAME: \'MockProOnDemandService\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],APP_VERSION: params[\'APP_VERSION\']) }\n"+
          "         )"
    )
    configure { project -> project / 'buildNeedsWorkspace'('true') }  
   
} 

job(under_folder +'/DeployWMBBars') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('APP_VERSION','WMB_DEVOPS_REL_03')
        stringParam('DOWNLOAD_RELEASE_TO','/deployment/ecomm/wmbdeployment')
        labelParam('DeploymentSlave')
        stringParam('WMB_DEPLOY_DIR','/home/wmbadmin/WMBDeployables')
        stringParam('ENV_PREFIX')
        stringParam('TARGET_SERVER','atg-${ENV_PREFIX}-aws-wmb01.aws.gha.kfplc.com')
        stringParam('ssh_key_cmd','/deployment/ansible/is-appsetup/roles/installMessageBroker/files/mb_access')
        stringParam('wmb_user','wmbadmin')
    }  
    
    concurrentBuild(true)
     
    steps {
	   
        shell ('rm -rf ${DOWNLOAD_RELEASE_TO}\n'+
            'mkdir -p ${DOWNLOAD_RELEASE_TO}\n'+
            'cd ${DOWNLOAD_RELEASE_TO}\n'+
            'wget -q ${NEW_NEXUS_RELEASE_LOCATION}/kf/wmb/devops/${APP_VERSION}/${APP_VERSION}.zip\n\n\n'+
            'echo "cleaning dir  ${WMB_DEPLOY_DIR}"\n\n'+
            'chmod 600 ${ssh_key_cmd}\n'+
            'result=$(ssh  -i ${ssh_key_cmd} ${wmb_user}@${TARGET_SERVER} \'rm -rf \'${WMB_DEPLOY_DIR}\'\')\n'+
            'result=$(ssh -q -i ${ssh_key_cmd} ${wmb_user}@${TARGET_SERVER} \'mkdir -p \'${WMB_DEPLOY_DIR}\'\')\n\n'+
            'echo "copying artefacts to ${TARGET_SERVER}"\n'+
            'ls -l\n\n'+
            'scp  -i ${ssh_key_cmd} ./${APP_VERSION}.zip ${wmb_user}@${TARGET_SERVER}:${WMB_DEPLOY_DIR}/\n\n'+
            'echo "executing deploy command"\n\n'+
            'result=$(ssh -i ${ssh_key_cmd} ${wmb_user}@${TARGET_SERVER} \'cd \'${WMB_DEPLOY_DIR}\' ; unzip \'${APP_VERSION}\'.zip ; chmod +x *.sh; . /opt/ibm/mqsi/8.0.0.6/bin/mqsiprofile ; ./mqsideploy_commands.sh \'${ENV_PREFIX})\n\n'+
    	    'echo $result')
    }  
}

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

job(under_folder +'/StatusCheckMQ') {
    description 'Restart a given WMB application'
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        choiceParam('action', ['status'],'use restart to stop & start the app')
        stringParam('ENV_PREFIX')
        labelParam('DeploymentSlave')
        booleanParam('failIfDown')
    }
    
    concurrentBuild(true)
     
    steps {
	   
        shell ('cd /deployment/ansible\n'+
    	    'ansible-playbook -i is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/serviceIntegration.yml -e "action=${action} includeMQ=yes failIfDown=yes"')
    }  
} 

job(under_folder +'/StatusCheckSV') {
    description 'Restart a given WMB application'
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        choiceParam('action', ['status'],'use restart to stop & start the app')
        stringParam('ENV_PREFIX')
        labelParam('DeploymentSlave')
        booleanParam('failIfDown')
    }
    
    concurrentBuild(true)
     
    steps {
	   
        shell ('cd /deployment/ansible\n'+
    	    'ansible-playbook -i is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/serviceIntegration.yml -e "action=${action} includeSV=yes failIfDown=${failIfDown}"')
    }  
} 

job(under_folder +'/StatusCheckWMB') {
    description 'Restart a given WMB application'
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        choiceParam('action', ['status'],'use restart to stop & start the app')
        stringParam('ENV_PREFIX')
        labelParam('DeploymentSlave')
        booleanParam('failIfDown')
    }
    
    concurrentBuild(true)
     
    steps {
	   
        shell ('cd /deployment/ansible\n'+
    	    'ansible-playbook -i is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/serviceIntegration.yml -e "action=${action} includeMB=yes failIfDown=${failIfDown}"')
    }  
} 

job(under_folder +'/StopStartMQ') {
    description 'Change MQ status'
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        choiceParam('action', ['stop','start','restart'],'use restart to stop & start the app')
        stringParam('ENV_PREFIX')
        labelParam('DeploymentSlave')
    }
    
    concurrentBuild(true)
     
    steps {
	   
        shell ('cd /deployment/ansible\n'+
    	    'ansible-playbook -i is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/serviceIntegration.yml -e "action=${action} includeMQ=yes"')
    }  
} 

job(under_folder +'/StopStartTomcat') {
    description 'Stop/Start WMB mock services on Tomcat'
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        choiceParam('state', ['started','stopped'])
        stringParam('ENV_PREFIX')
        labelParam('DeploymentSlave')
    }
    
    concurrentBuild(true)
     
    steps {
	   
        shell ('cd /deployment/ansible\n'+
    	    'ansible-playbook -i is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/setTomcatState.yml -e "inventory_group=tomcat tomcatState=${state}"')
    }  
} 

job(under_folder +'/StopStartWMB') {
    description 'Restart a given WMB application'
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        choiceParam('action', ['stop','start','restart'],'use restart to stop & start the app')
        stringParam('ENV_PREFIX')
        labelParam('DeploymentSlave')
    }
    
    concurrentBuild(true)
     
    steps {
	   
        shell ('cd /deployment/ansible\n'+
    	    'ansible-playbook -i is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/serviceIntegration.yml -e "action=${action} includeMB=yes"')
    }  
} 

job(under_folder +'/TailTomcatLog') {
    description 'Restart a given WMB application'
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        labelParam('DeploymentSlave')
        stringParam('server','atg-devops01-aws-app01.aws.gha.kfplc.com')
        stringParam('tailLog','-100f')
    }
    
    steps {
	   
        shell ('echo "tail ${tailLog} /var/log/tomcat6/catalina.out" | ssh -q -t -t -i /deployment/ansible/is-appsetup/roles/installMessageBroker/files/mb_access wmbadmin@${server}')
    }  
} 