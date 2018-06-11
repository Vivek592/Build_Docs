def top_folder = '/Infra'
def under_folder = '/Infra/Test'

folder(top_folder)
folder(under_folder)

job(under_folder +'/VerifyATGSlave') {
    
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        labelParam('DeploymentSlave')
        stringParam('JAVA_HOME','/usr/java/jdk1.6.0_45/jre')
        stringParam('RUBY_HOME','/app/ecomm/tools/jruby')
        stringParam('GIT_VERSION','1.8.5.5')
        stringParam('ORACLE_HOME','/usr/lib/oracle/12.1/client64/')
    } 
    
    steps {
        shell('#CHECK JAVA is installed with correct version \n'+
        'cd ${JAVA_HOME} \n'+
	'export PATH=${JAVA_HOME}/bin:${PATH} \n'+
	'java -version \n'+
	' \n'+
	'#Check Ruby is installed for Buildr \n'+
	'cd ${RUBY_HOME} \n'+
	'export PATH=${RUBY_HOME}/bin:${PATH} \n'+
	'buildr --version \n'+
	' \n'+
	'#Check Git is installed \n'+
	'git --version \n'+
	' \n'+
	'#Check Oracle is installed \n'+
	'cd ${ORACLE_HOME}/bin \n'+
	'ls -l sqlplus \n')  
    }
}



