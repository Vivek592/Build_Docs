def top_folder = '/ATG'
def sub_folder = '/ATG/Build'


folder(top_folder)
folder(sub_folder)

job(sub_folder +'/SonarAnalysis') {
    
  logRotator {
      daysToKeep -1
      numToKeep 5
    }
    
    parameters {
        labelParam('ATGSlave')
    } 
   customWorkspace('/deployment/ecomm/jenkins-slave/workspace/ATG/Build/BQ/BuildBQ')
   
    configure { project ->
        project / publishers << 'hudson.plugins.sonar.MsBuildSQRunnerBegin' {
            jdk('(Inherit From Job)')
            projectKey('KITS:ATG')
            projectName('KITS ATG Code')
	    projectVersion('1.0')
	    additionalArguments('# path to source directories required \n' +
'sonar.sources=KF/Common/Agent/src/main,KF/Common/API/src/main,KF/Common/Base/src/main,KF/Common/Messaging/src/main,KF/Common/Validation/src/main,KF/Web/src/main,KF/Agent/src/main,KF/Auxiliary-Web/src/main,KF/Fulfillment/src/main,KF/Fulfillment-Agent/src/main,KF/Publishing/src/main \n\n' +

'# path to test source directories (optional) \n' +
'sonar.tests=KF/Common/Agent/src/test,KF/Common/API/src/test,KF/Common/Base/src/test,KF/Common/Messaging/src/test,KF/Common/Validation/src/test,KF/Web/src/test,KF/Agent/src/test,KF/Auxiliary-Web/src/test,KF/Fulfillment/src/test,KF/Fulfillment-Agent/src/test,KF/Publishing/src/test \n\n' +

'# path to project binaries (optional), for example directory of Java bytecode \n' +
'sonar.binaries=KF/Common/Agent/target,KF/Common/API/target,KF/Common/Base/target,KF/Common/Messaging/target,KF/Common/Validation/target,KF/Web/target,KF/Agent/target,KF/Auxiliary-Web/target,KF/Fulfillment/target,KF/Fulfillment-Agent/target,KF/Publishing/target \n\n' +

'sonar.junit.reportsPath=**/junit/*.xml \n' +
'sonar.core.codeCoveragePlugin=emma \n' +
'-Dsonar.emma.reportPath=**/reports/emma/coverage.xml')
        }
    }
}