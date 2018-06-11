def top_folder = '/Infra'
def under_folder = '/Infra/Deploy'
def under_folder2 = '/Infra/Deploy/Endeca'

folder(top_folder)
folder(under_folder)
folder(under_folder2)

job(under_folder +'/CreateAnsibleSlave') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        stringParam('ENV_PREFIX','','environment identifier')
        labelParam('GenericSlave')
    } 
    
    steps {
        shell('cd /deployment/ansible \n'+
	      'sleep ${WAIT_FOR} \n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/deployAnsible.yml -e "inventory_group=slave svr_create=yes chargeCode=${Charge_Code}" \n')  
    }
}

job(under_folder +'/CreateApache') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        stringParam('group_name','','server group name')
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX','','environment identifier')
        stringParam('WAIT_FOR','0','')
    } 
    
    concurrentBuild(true)
    
    steps {
        shell('cd /deployment/ansible \n'+
	      'sleep ${WAIT_FOR} \n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/deployApache.yml -e "inventory_group=web svr_create=yes chargeCode=${Charge_Code}" -vvvv \n')  
    }
}

job(under_folder +'/CreateATGAppEnv') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        stringParam('group_name','','server group name')
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX','','environment identifier')
        stringParam('WAIT_FOR','5','')
    } 
    
    concurrentBuild(true)
    
    steps {
        shell('cd /deployment/ansible \n'+
	      'sleep ${WAIT_FOR} \n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/deployApache.yml -e "inventory_group=web svr_create=yes chargeCode=${Charge_Code}" -vvvv \n')  
    }
}

job(under_folder +'/CreateATGSlave') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        stringParam('ENV_PREFIX','','environment identifier')
        labelParam('GenericSlave')
        stringParam('svr_create','yes','create server')
    } 
    
    concurrentBuild(true)
    
    steps {
        shell('cd /deployment/ansible \n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/deploySlaveAtg.yml -e "inventory_group=slaveATG svr_create=${svr_create} chargeCode=${Charge_Code}" \n')  
    }
}

job(under_folder +'/CreateAuxIndex') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        stringParam('ENV_PREFIX','','environment identifier')
        labelParam('DeploymentSlave')
        stringParam('Charge_Code','','cost code for AWS charges')
    } 
    
    concurrentBuild(true)
    
    steps {
        dsl('build(\'CreateATGAppEnv\',group_name: \'auxindex\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], Charge_Code: params[\'Charge_Code\'], WAIT_FOR: 3)')  
    }
}

job(under_folder +'/CreateELB') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX','','environment identifier')
        stringParam('WAIT_FOR','0','')
    } 
    
    concurrentBuild(true)
    
    steps {
        shell('cd /deployment/ansible \n'+
	      'sleep ${WAIT_FOR} \n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-iaas/playbooks/createElb.yml -e "inventory_group=elb svr_create=yes chargeCode=${Charge_Code}" -vvvv \n')  
    }
}

job(under_folder +'/CreateFitnesseConfig') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX','','environment identifier')
        stringParam('WAIT_FOR','0','')
    } 
    
    concurrentBuild(true)
    
    steps {
        shell('cd /deployment/ansible \n'+
	      'sleep ${WAIT_FOR} \n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/modifyFitnessePropsFile.yml \n'+
	      '\n'+
	      '\n'+
	      'curl --upload-file /deployment/source/devops-source/fitnesse/teconf-${ENV_PREFIX}.properties ${NEW_NEXUS_RELEASE_LOCATION}/kf/devops/fitnesse/teconf-${ENV_PREFIX}.properties --user darwin:darwin')  
    }
}

job(under_folder +'/CreateFitnesseServer') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        stringParam('ENV_PREFIX','','environment identifier')
        labelParam('DeploymentSlave')
        stringParam('svr_create','yes','create server')
        stringParam('instanceId','01','create server')
        stringParam('Charge_Code','','cost code for AWS charges')
        stringParam('WAIT_FOR','5','')
    } 
    
    concurrentBuild(true)
    
    steps {
        shell('cd /deployment/ansible \n'+
	      'sleep ${WAIT_FOR} \n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/deployFitnesse.yml -e "inventory_group=fitnesse svr_create=${svr_create} chargeCode=${Charge_Code} instanceId=${instanceId}" \n')  
    }
}

job(under_folder +'/CreateNFS') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX','','environment identifier')
        stringParam('WAIT_FOR','0','')
    } 
    
    concurrentBuild(true)
    
    steps {
        shell('cd /deployment/ansible \n'+
	      'sleep ${WAIT_FOR} \n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-iaas/playbooks/nfsService.yml -e "inventory_group=nfs svr_create=yes chargeCode=${Charge_Code}"')  
    }
}

job(under_folder +'/CreateSearch') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX','','environment identifier')
        stringParam('WAIT_FOR','0','')
        stringParam('instanceId','01','create server')
        stringParam('Charge_Code','','cost code for AWS charges')
    } 
    
    concurrentBuild(true)
    
    steps {
        shell('cd /deployment/ansible \n'+
	      'sleep ${WAIT_FOR} \n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/deploySearch.yml -e "inventory_group=search svr_create=yes chargeCode=${Charge_Code} instanceId=${instanceId}" -vvvv')  
    }
}

job(under_folder +'/CreateTomcat') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        stringParam('group_name','','server group name')
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX','','environment identifier')
        stringParam('WAIT_FOR','0','')
        stringParam('Charge_Code','','cost code for AWS charges')
    } 
    
    concurrentBuild(true)
    
    steps {
        shell('cd /deployment/ansible \n'+
	      'sleep ${WAIT_FOR} \n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/deployTomcat.yml -e "inventory_group=tomcat svr_create=yes chargeCode=${Charge_Code}" -vvvv \n')  
    }
}

job(under_folder2 +'/CreateEndecaSilo') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
    	labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX','','environment identifier')
        stringParam('Charge_Code','','cost code for AWS charges')
    } 
    
    steps {
        dsl('parallel ( \n'+
        '{ build(\'Infra/Deploy/Endeca/CreateITLServer\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], WAIT_FOR: 15) }, \n'+	
        '{ build(\'Infra/Deploy/Endeca/CreateMDEXServer\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], WAIT_FOR: 20) }, \n)')
    }
}

job(under_folder2 +'/CreateITLServer') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX','','environment identifier')
        stringParam('WAIT_FOR','0','')
        stringParam('Charge_Code','','cost code for AWS charges')
        stringParam('svr_create','yes','create server')
    } 
    
    concurrentBuild(true)
    
    steps {
        shell('cd /deployment/ansible \n'+
	      'sleep ${WAIT_FOR} \n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/deployITL.yml -e "inventory_group=itl svr_create=${svr_create} chargeCode=${Charge_Code}" -vvvv \n')  
    }
}

job(under_folder2 +'/CreateMDEXServer') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX','','environment identifier')
        stringParam('WAIT_FOR','0','')
        stringParam('Charge_Code','','cost code for AWS charges')
    } 
    
    concurrentBuild(true)
    
    steps {
        shell('cd /deployment/ansible \n'+
	      'sleep ${WAIT_FOR} \n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/deployMdex.yml -e "inventory_group=mdex svr_create=yes chargeCode=${Charge_Code}" -vvvv \n')  
    }
}

job(under_folder +'/PrepareS3File') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX','','environment identifier')
    } 
    
    concurrentBuild(true)
    
    steps {
        shell('cd /deployment/ansible \n'+
	      'ansible-playbook /deployment/ansible/is-appsetup/playbooks/pullS3Files.yml \n')  
    }
}

job(under_folder +'/CreateAllATGAppEnv') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        stringParam('ENV_PREFIX','','environment identifier')
    	labelParam('DeploymentSlave')
        stringParam('Charge_Code','','cost code for AWS charges')
    } 
    
    steps {
        dsl('build(\'Infra/Deploy/PrepareS3File\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\']) \n'+
        	'parallel (  \n'+
        	'{ build(\'CreateATGAppEnv\',group_name: \'storefront\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], Charge_Code: params[\'Charge_Code\'], WAIT_FOR: 3) }, \n'+
        	'{ build(\'CreateATGAppEnv\',group_name: \'fulfillment\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], WAIT_FOR: 15) }, \n'+
        	'{ build(\'CreateATGAppEnv\',group_name: \'agent\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], WAIT_FOR: 30) }, \n'+
		'{ build(\'CreateATGAppEnv\',group_name: \'staging\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], WAIT_FOR: 45) }, \n'+
		'{ build(\'CreateATGAppEnv\',group_name: \'publishing\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], WAIT_FOR: 65) }, \n'+
		'{ build(\'CreateATGAppEnv\',group_name: \'lockstorefront\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], WAIT_FOR: 80) }, \n'+
		'{ build(\'CreateATGAppEnv\',group_name: \'lockagent\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], WAIT_FOR: 95) }, \n'+
		'{ build(\'CreateATGAppEnv\',group_name: \'auxagent\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], WAIT_FOR: 105) }, \n'+
		'{ build(\'CreateATGAppEnv\',group_name: \'auxstorefront\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], WAIT_FOR: 120) }, \n'+
		'{ build(\'Infra/Deploy/CreateELB\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'] ,Charge_Code: params[\'Charge_Code\'], WAIT_FOR: 25) }, \n'+
		'{ build(\'Infra/Deploy/CreateNFS\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], WAIT_FOR: 5) }, \n'+
		'{ build(\'Infra/Deploy/CreateApache\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], WAIT_FOR: 70) }, \n'+
		'{ build(\'Infra/Deploy/CreateSearch\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], instanceId: \'01\', WAIT_FOR: 85) }, \n'+
		'{ build(\'Infra/Deploy/CreateSearch\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], instanceId: \'02\', WAIT_FOR: 125) }, \n'+
		'{ build(\'Infra/Deploy/CreateSearch\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], instanceId: \'03\', WAIT_FOR: 145) }, \n'+
		'{ build(\'Infra/Deploy/CreateTomcat\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], WAIT_FOR: 190) } \n)')
        	
    }
}

job(under_folder +'/CreateAllFitnesse') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        stringParam('ENV_PREFIX','','environment identifier')
    	labelParam('DeploymentSlave')
        stringParam('Charge_Code','','cost code for AWS charges')
    } 
    
    steps {
       dsl('parallel (  \n'+
          	'{ build(\'Infra/Deploy/CreateFitnesseServer\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], instanceId: \'01\', WAIT_FOR: 155) }, \n'+
          	'{ build(\'Infra/Deploy/CreateFitnesseServer\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], instanceId: \'02\', WAIT_FOR: 175) }, \n'+
          	'{ build(\'Infra/Deploy/CreateFitnesseServer\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], instanceId: \'03\', WAIT_FOR: 185) }, \n'+
          	'{ build(\'Infra/Deploy/CreateFitnesseServer\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], instanceId: \'04\', WAIT_FOR: 190) }, \n'+
          	'{ build(\'Infra/Deploy/CreateFitnesseServer\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], instanceId: \'05\', WAIT_FOR: 195) }, \n'+
          	'{ build(\'Infra/Deploy/CreateFitnesseServer\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], instanceId: \'06\', WAIT_FOR: 200) } \n)')
    }
}

job(under_folder +'/CreateAllATGSearch') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        stringParam('ENV_PREFIX','','environment identifier')
    	labelParam('DeploymentSlave')
        stringParam('Charge_Code','','cost code for AWS charges')
    } 
    
    steps {
       dsl('parallel (  \n'+
          	'{ build(\'Infra/Deploy/CreateSearch\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], instanceId: \'01\', WAIT_FOR: 85) }, \n'+
          	'{ build(\'Infra/Deploy/CreateSearch\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], instanceId: \'02\', WAIT_FOR: 125) }, \n'+
          	'{ build(\'Infra/Deploy/CreateSearch\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],Charge_Code: params[\'Charge_Code\'], instanceId: \'03\', WAIT_FOR: 145) }\n)')
    }
}

job(under_folder2 +'/CreateELBForMDEX') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX')
        stringParam('WAIT_FOR')
        stringParam('Charge_Code')
    } 
    
    concurrentBuild(true)
    
    steps {
        shell('cd /deployment/ansible\n'+
              'sleep ${WAIT_FOR}\n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-appsetup/playbooks/createMdexElb.yml -e "inventory_group=mdex chargeCode=${Charge_Code} elb_sg_id=sg-631f6204" -vvvv')  
    }
}