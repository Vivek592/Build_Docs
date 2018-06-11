def under_folder = '/ATG/Build/'
def job_name = under_folder +'Update_ATG-App'


job(job_name) {
    description 'Restart a given ATG application'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        labelParam('DeploymentSlave')
    }  
    scm {
        git {
            remote {
                 url 'http://git.aws.gha.kfplc.com/KITS/ATG-App.git'
                 branch '*/test'
                 credentials 'a598502-4513-475a-9bbc-33bd216f73b6'
            }          
        }
    }
    
    concurrentBuild(true)
    
    steps {
 	shell ('export JAVA_HOME=/usr/java/jdk1.6.0_45 \n' +
	    'export PATH=/app/ecomm/tools/jruby/bin:$JAVA_HOME/bin:$PATH \n' +
    	    '#git remote rename origin upstream \n' +
	    'git remote add upstream http://jenkins_build:test1234@git.aws.gha.kfplc.com/KITS/ATG-App.git \n' +
	    '#git fetch origin Core_Maintenance_Refactor \n' +
	    '#git reset --hard origin/Core_Maintenance_Refactor \n' +
	    '#git remote set-url upstream http://atg:${password}@git.aws.gha.kfplc.com/KITS/ATG-App.git \n' +
	    'git checkout -b test \n' +
	    'git push upstream test')
    }  
}



