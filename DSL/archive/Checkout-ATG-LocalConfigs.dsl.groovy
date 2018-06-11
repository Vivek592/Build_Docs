def under_folder = '/ATG/Build/'

job(under_folder +'Checkout-ATG-LocalConfigs') {
    
  logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        stringParam('branch','develop','Working branch')
        labelParam('DeploymentSlave')
       
    } 
    
    customWorkspace('/deployment/ansible/atglocalconfigs');
        scm {
             git {
             remote {
                 url 'http://git.aws.gha.kfplc.com/KITS/ATGLocalConfigs.git'
                 branch '${branch}'
                 credentials 'a598502-4513-475a-9bbc-33bd216f73b6'
                 }          
             }
    }
    }
