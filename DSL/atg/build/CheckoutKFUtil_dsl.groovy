def under_folder = '/ATG/Build/'
def job_name = under_folder +'Checkout_kfutil'


job(job_name) {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        labelParam('ATGSlave')
        stringParam('branch','master')
    }  
    scm {
        git {
            remote {
                 url 'https://github.com/KITSGitHubAdmin/KITS-KFutil_Build-Dev.git'
                 branch '${branch}'
                 credentials 'a598502-4513-475a-9bbc-33bd216f73b6'
            }          
        }
    }
    
    concurrentBuild(true)
    
    steps {
 	shell ('export JAVA_HOME=/usr/java/jdk1.6.0_45 \n' +
	    'export PATH=/app/ecomm/tools/jruby/bin:$JAVA_HOME/bin:$PATH \n' +
    	    '#gem uninstall -Iax sfxutil \n' +
	    './reinstallGem.sh')
    }  
}



