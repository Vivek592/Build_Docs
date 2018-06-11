def under_folder = '/ATG/Test'
def sub_folder = '/ATG/Test/Fitnesse'

folder(under_folder)
folder(sub_folder)

job(under_folder +'/CaptureFitnesseLogs') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('LabelParameterValue')
        labelParam('DeploymentSlave')
    }  
     
    steps {
	shell ('echo ${LabelParameterValue} \n'+
	    'echo LabelParameterValue \n'+
	    ' \n'+
	    '$(ssh -o StrictHostKeyChecking=no ${LabelParameterValue} \' tail -500  /app/ecomm/fitnesse/ConditionalBuildTest/reports/console/console.log >fitnesse_log_\'${LabelParameterValue}\'.log; \\ curl --upload-file fitnesse_log_\'${LabelParameterValue}\'.log \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/fitnesse/logs/fitnesse_log_\'${LabelParameterValue}\'.log --user darwin:darwin\')')
    }
}

job(under_folder +'/CloseHTMLReport') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        labelParam('DeploymentSlave')
        stringParam('FITNESSE_RESULT_DIR')
        stringParam('ENV_PREFIX')
    }  
     
    steps {
	shell ('cd ${FITNESSE_RESULT_DIR} \n'+
	    'echo "</table>" >> index.html \n'+
	    ' \n'+
	    'echo "<iframe  id=\'iftest\' height=\'100%\' width=\'100%\' marginheight=\'1\' marginwidth=\'1\'>" >> index.html \n'+
	    '  echo "<p>Your browser does not support iframes.</p>" >> index.html \n'+
	    'echo "</iframe>" >> index.html \n'+
	    ' \n'+
	    '</body> \n'+
	    '</html>')
    }
}

buildFlowJob(under_folder+"/DevOpsATG_AFA_HighSuiteTest") {
    displayName("DevOpsATG_AFA_HighSuiteTest")
    parameters {
        stringParam('ENV_PREFIX')  
        labelParam('DeploymentSlave')
        stringParam('FITNESSE_RESULT_DIR')
    }  
   
   buildFlow(
          "build(\'OpenHTMLReport\', DeploymentSlave: params[\'DeploymentSlave\'],ENV_PREFIX: params[\'ENV_PREFIX\'], FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\']) \n\n"+
          "parallel ( \n\n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit01.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWebForAgent.TestForCloud.TestModule1?suite&excludeSuiteFilter=dev&site=AGENT&suiteFilter=agntHDops8&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-agent-block1.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit02.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWebForAgent.TestForCloud.TestModule2?suite&excludeSuiteFilter=dev&site=AGENT&suiteFilter=agntHDops8&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-agent-block2.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit03.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWebForAgent.TestForCloud.TestModule3?suite&excludeSuiteFilter=dev&site=AGENT&suiteFilter=agntHDops8&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-agent-block3.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit04.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWebForAgent.TestForCloud.TestModule4?suite&excludeSuiteFilter=dev&site=AGENT&suiteFilter=agntHDops8&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-agent-block4.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit05.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWebForAgent.TestForCloud.TestModule5?suite&excludeSuiteFilter=dev&site=AGENT&suiteFilter=agntHDops8&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-agent-block5.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit06.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWebForAgent.TestForCloud.TestModule6?suite&excludeSuiteFilter=dev&site=AGENT&suiteFilter=agntHDops8&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-agent-block6.html\') } \n\n"+
          ")\n\n"+
          "//build(\'CloseHTMLReport\', DeploymentSlave: params[\'DeploymentSlave\'],ENV_PREFIX: params[\'ENV_PREFIX\'], FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\']) \n\n"+
          "  // For testing      { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit01.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWeb.TestForCloud.TestModule1.CheckOut.CheckoutWithMasterCard?suite&excludeSuiteFilter=dev&site=DIY&suiteFilter=hsit&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-diy-block1.html\') }"
          )
    configure { project -> project / 'buildNeedsWorkspace'('true') }  
   
    publishers {
     
        configure {   
	        project -> project / publishers << 'hudson.plugins.fitnesse.FitnesseResultsRecorder' { fitnessePathToXmlResultsIn 'fitnesse-results*.xml' }
        }
     
    	publishHtml {
            report(' ') {
                reportName('HighSuiteReport')
                reportFiles('index.html')
                keepAll()
                allowMissing()
                alwaysLinkToLastBuild()
            }
        }
    }           
} 

buildFlowJob(under_folder+"/DevOpsATGEndecaSuiteTest") {
    displayName("DevOpsATGEndecaSuiteTest")
    parameters {
        stringParam('ENV_PREFIX')  
        labelParam('DeploymentSlave')
        stringParam('FITNESSE_RESULT_DIR')
    }  
   
   buildFlow(
          "build(\'OpenHTMLReport\', DeploymentSlave: params[\'DeploymentSlave\'],ENV_PREFIX: params[\'ENV_PREFIX\'], FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\']) \n\n"+
          "parallel ( \n\n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit01.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'EndecaTests.IntegrationTestSuite?suite&excludeSuiteFilter=dev&site=DIY&suiteFilter=EndecaDevops&profile=CLOUD&device=/app/ecomm/fitnesse/ConditionalBuildTest/src/main/resources/devices/device-pc-win8_1-chrome51_0.properties\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-diy-block1.html\') } \n\n"+
          ")\n\n"+
          "//build(\'CloseHTMLReport\', DeploymentSlave: params[\'DeploymentSlave\'],ENV_PREFIX: params[\'ENV_PREFIX\'], FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\']) \n\n"+
          "  // For testing      { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit01.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWeb.TestForCloud.TestModule1.CheckOut.CheckoutWithMasterCard?suite&excludeSuiteFilter=dev&site=DIY&suiteFilter=hsit&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-diy-block1.html\') }"
          )
    configure { project -> project / 'buildNeedsWorkspace'('true') }  
   
    publishers {
     
        configure {   
	        project -> project / publishers << 'hudson.plugins.fitnesse.FitnesseResultsRecorder' { fitnessePathToXmlResultsIn 'fitnesse-results*.xml' }
        }
        publishers {
       	    archiveArtifacts('fitnesse_log_*.log')
    	}
    	     
    	publishHtml {
            report(' ') {
                reportName('HTML Report')
                reportFiles('index.html')
                keepAll()
                allowMissing()
                alwaysLinkToLastBuild()
            }
        }
    }           
} 

buildFlowJob(under_folder+"/DevOpsATGHighSuiteTest") {
    displayName("DevOpsATGHighSuiteTest")
    parameters {
        stringParam('ENV_PREFIX')  
        labelParam('DeploymentSlave')
        stringParam('FITNESSE_RESULT_DIR')
    }  
   
   buildFlow(
          "build(\'OpenHTMLReport\', DeploymentSlave: params[\'DeploymentSlave\'],ENV_PREFIX: params[\'ENV_PREFIX\'], FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\']) \n\n"+
          "parallel ( \n\n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit01.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWeb.TestForCloud.TestModule1?suite&excludeSuiteFilter=dev&site=DIY&suiteFilter=hsit&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-diy-block1.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit02.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWeb.TestForCloud.TestModule2?suite&excludeSuiteFilter=dev&site=DIY&suiteFilter=hsit&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-diy-block2.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit03.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWeb.TestForCloud.TestModule3?suite&excludeSuiteFilter=dev&site=DIY&suiteFilter=hsit&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-diy-block3.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit04.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWeb.TestForCloud.TestModule4?suite&excludeSuiteFilter=dev&site=DIY&suiteFilter=hsit&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-diy-block4.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit05.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWeb.TestForCloud.TestModule5?suite&excludeSuiteFilter=dev&site=DIY&suiteFilter=hsit&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-diy-block5.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit06.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWeb.TestForCloud.TestModule6?suite&excludeSuiteFilter=dev&site=DIY&suiteFilter=hsit&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-diy-block6.html\') } \n\n"+
          ")\n\n"+
          "//build(\'CloseHTMLReport\', DeploymentSlave: params[\'DeploymentSlave\'],ENV_PREFIX: params[\'ENV_PREFIX\'], FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\']) \n\n"+
          "  // For testing      { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit01.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWeb.TestForCloud.TestModule1.CheckOut.CheckoutWithMasterCard?suite&excludeSuiteFilter=dev&site=DIY&suiteFilter=hsit&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-diy-block1.html\') }"
          )
    configure { project -> project / 'buildNeedsWorkspace'('true') }  
   
    publishers {
     
        configure {   
	        project -> project / publishers << 'hudson.plugins.fitnesse.FitnesseResultsRecorder' { fitnessePathToXmlResultsIn 'fitnesse-results*.xml' }
        }
        publishers {
       	    archiveArtifacts('fitnesse_log_*.log')
    	}
    	     
    	publishHtml {
            report(' ') {
                reportName('HTML Report')
                reportFiles('index.html')
                keepAll()
                allowMissing()
                alwaysLinkToLastBuild()
            }
        }
    }           
} 

buildFlowJob(under_folder+"/DevOpsATGMediumSuiteTest") {
    displayName("DevOpsATGMediumSuiteTest")
    parameters {
        stringParam('DeploymentSlave')
        stringParam('ENV_PREFIX')  
    }  
   
   buildFlow(
          "build(\'OpenHTMLReport\', DeploymentSlave: params[\'DeploymentSlave\'],ENV_PREFIX: params[\'ENV_PREFIX\'], FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\']) \n\n"+
          "parallel ( \n\n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit01.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWeb.TestForCloud.TestModule1?suite&excludeSuiteFilter=dev&site=DIY&suiteFilter=msit&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-diy-block1.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit02.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWeb.TestForCloud.TestModule2?suite&excludeSuiteFilter=dev&site=DIY&suiteFilter=msit&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-diy-block2.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit03.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWeb.TestForCloud.TestModule3?suite&excludeSuiteFilter=dev&site=DIY&suiteFilter=msit&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-diy-block3.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit04.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWeb.TestForCloud.TestModule4?suite&excludeSuiteFilter=dev&site=DIY&suiteFilter=msit&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-diy-block4.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit05.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWeb.TestForCloud.TestModule5?suite&excludeSuiteFilter=dev&site=DIY&suiteFilter=msit&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-diy-block5.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit06.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWeb.TestForCloud.TestModule6?suite&excludeSuiteFilter=dev&site=DIY&suiteFilter=msit&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-diy-block6.html\') } \n"+
          ")"
          )

    configure { project -> project / 'buildNeedsWorkspace'('true') }  
   
    publishers {
     
        publishers {
       	    archiveArtifacts('fitnesse_log_*.log')
    	}
    	     
    	publishHtml {
            report(' ') {
                reportName('Medium Suite Report')
                reportFiles('index.html')
                keepAll()
                allowMissing()
                alwaysLinkToLastBuild()
            }
        }
    }           
} 

buildFlowJob(under_folder+"/DevOpsATGMediumSuiteTestAFA") {
    displayName("DevOpsATGMediumSuiteTestAFA")
    parameters {
        stringParam('DeploymentSlave')
        stringParam('ENV_PREFIX')  
    }  
   
   buildFlow(
          "build('OpenHTMLReport', DeploymentSlave: params['DeploymentSlave'],ENV_PREFIX: params['ENV_PREFIX'], FITNESSE_RESULT_DIR: params['FITNESSE_RESULT_DIR']) \n\n"+
          "parallel ( \n\n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit01.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWebForAgent.TestForCloud.TestModule1?suite&excludeSuiteFilter=dev&site=AGENT&suiteFilter=agntMDops&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-agent-block1.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit02.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWebForAgent.TestForCloud.TestModule2?suite&excludeSuiteFilter=dev&site=AGENT&suiteFilter=agntMDops&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-agent-block2.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit03.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWebForAgent.TestForCloud.TestModule3?suite&excludeSuiteFilter=dev&site=AGENT&suiteFilter=agntMDops&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-agent-block3.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit04.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWebForAgent.TestForCloud.TestModule4?suite&excludeSuiteFilter=dev&site=AGENT&suiteFilter=agntMDops&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-agent-block4.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit05.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWebForAgent.TestForCloud.TestModule5?suite&excludeSuiteFilter=dev&site=AGENT&suiteFilter=agntMDops&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-agent-block5.html\') }, \n"+
          "           { build(\'Fitnesse_template\',FITNESSE_RESULT_DIR: params[\'FITNESSE_RESULT_DIR\'] ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'],FITNESSE_HOST: \'atg-${ENV_PREFIX}-aws-fit06.aws.gha.kfplc.com\', FITNESSE_PORT: \'8086\',FITNESSE_TARGET_PAGE: \'DarwinAcceptanceTests.IntegrationWebForAgent.TestForCloud.TestModule6?suite&excludeSuiteFilter=dev&site=AGENT&suiteFilter=agntMDops&profile=CLOUD\', FITNESSE_RESULT_PATH: \'${FITNESSE_RESULT_DIR}/fitnesse-results-agent-block6.html\') } \n"+
          ")"
          )

    configure { project -> project / 'buildNeedsWorkspace'('true') }  
   
    publishers {
     
        publishers {
       	    archiveArtifacts('fitnesse_log_*.log')
    	}
    	     
    	publishHtml {
            report(' ') {
                reportName('Medium Suite Report')
                reportFiles('index.html')
                keepAll()
                allowMissing()
                alwaysLinkToLastBuild()
            }
        }
    }           
} 

job(under_folder +'/Fitnesse_template') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('FITNESSE_HOST')
        stringParam('FITNESSE_PORT','8086')
        stringParam('FITNESSE_TARGET_PAGE')
        stringParam('FITNESSE_RESULT_PATH')
        labelParam('DeploymentSlave')
    }  
     
    configure { project -> 
        project / builders / 'hudson.plugins.fitnesse.FitnesseBuilder' { 
            options { 
                entry { 
                    string("fitnesseStart") 
                    string("false") 
                } 
                entry { 
                    string("fitnesseHost") 
                    string("${FITNESSE_HOST}") 
                } 
                entry { 
                    string("fitnesseHttpTimeout") 
                    string("7200000") 
                } 
                entry { 
                    string("fitnesseTestTimeout") 
                    string("7200000") 
                } 
                entry { 
                    string("fitnesseTargetIsSuite") 
                    string("true") 
                } 
                entry { 
                    string("fitnessePortRemote") 
                    string("8086") 
                } 
                entry { 
                    string('fitnesseTargetPage') 
                    string('${FITNESSE_TARGET_PAGE}') 
                } 
                entry { 
                    string("fitnessePathToXmlResultsOut") 
                    string("fitnesse-results.xml") 
                } 
            } 
        } 
    } 
    
    steps {
	   
	downstreamParameterized {
            trigger('CaptureFitnesseLogs') {
                block {
                    buildStepFailure('never')
                    failure('never')
                    unstable('never')
                }
                parameters{
                    predefinedProp('LabelParameterValue','${FITNESSE_HOST}')                        
                    currentBuild() 
                }
            }
        }
        
        shell ('java -jar /app/ecomm/fitnesse/tools/XmlToHTmlTransformer.jar fitnesse-results.xml  ${FITNESSE_RESULT_PATH} /app/ecomm/fitnesse/tools/fitnesse-html.xsl \n'+
            'export to_dir=`dirname ${FITNESSE_RESULT_PATH}` \n'+
            'echo $to_dir \n'+
            'export file_name=`basename ${FITNESSE_RESULT_PATH}` \n'+
    	    ' \n\n'+
    	    'cp fitnesse-results.xml $to_dir/${file_name}.xml \n'+
    	    ' \n'+
    	    'cd $to_dir \n'+
    	    'rm -f fitnesse_log_${FITNESSE_HOST}.log \n'+
    	    'wget ${NEW_NEXUS_RELEASE_LOCATION}/kf/fitnesse/logs/fitnesse_log_${FITNESSE_HOST}.log \n'+
    	    ' \n'+
    	    'echo "<tr><td><a href=\'"${file_name}"\' >"${file_name}"</a><tr><td>" >> index.html ')
    }  
}

job(under_folder +'/OpenHTMLReport') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        labelParam('DeploymentSlave')
        stringParam('FITNESSE_RESULT_DIR')
        stringParam('ENV_PREFIX')
 
    }  
    
    concurrentBuild(true)
     
    steps {
	   
        shell ('cd ${FITNESSE_RESULT_DIR} \n'+
            'rm -f  *.html \n'+
            'rm -f *.log \n'+
            'rm -f *.xml \n'+
    	    ' \n'+
    	    'echo "<html><body><table>" >> index.html \n'+
    	    'echo "<tr> Automation report for Environment : ${ENV_PREFIX} </tr>" >> index.html ')
    }  
}

job(under_folder +'/TailFitnesseLog') {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        labelParam('FitnesseSlave')
    }  
    
    concurrentBuild(true)
     
    steps {
	   
        shell ('tail -100f /app/ecomm/fitnesse/BuildFitnesseCodeCR4/reports/console/console.log ')
    }  
}

job(sub_folder +'/BuildFitnesseCodeDevOps') {
    description 'Automatically triggers the build when changes are committed into {Branch : master} of KITS-Automation-Test'
 
    logRotator {
      daysToKeep -1
      numToKeep 5
    }
    scm {
        git {
            remote {
                url 'git@git.aws.gha.kfplc.com:KITS/ATG-AutomationTests.git'
              	branch '*/master'
               	credentials 'ca5be30b-657d-4c0d-8191-edc84a8ad31e'
            }          
        }
    }    
    steps {
	   
        shell ('export suite_file_name=devops_fitnesse_reg_suite.tar.gz \n'+
            'rm -f /app/ecomm/tmp/${suite_file_name} \n'+
            'cd .. \n'+
            'tar -czf /app/ecomm/tmp/${suite_file_name} BuildFitnesseCodeCR4 --exclude \'.git/*\' \n'+
    	    'curl --upload-file /app/ecomm/tmp/${suite_file_name} ${NEW_NEXUS_RELEASE_LOCATION}/kf/fitnesse/${suite_file_name} --user darwin:darwin ')
    }  
}

job(sub_folder +'/PrepareEndecaFitnesseOnServer') {
 
    logRotator {
      daysToKeep -1
      numToKeep 5
    }
    
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX')
        stringParam('FITNESSE_SERVER','atg-${ENV_PREFIX}-aws-fit${instanceId}')
    }  
    
    concurrentBuild(true)
 
    steps {
	   
        shell ('result=$(ssh -o StrictHostKeyChecking=no ${FITNESSE_SERVER} \'rm -rf /app/ecomm/fitnesse; \\\n'+
            ' mkdir /app/ecomm/fitnesse; \\\n'+
            ' cd /app/ecomm/fitnesse; \\\n'+
            'rm -rf ConditionalBuildTest; \\\n'+
            'rm -f cr4_fitnesse_reg_suite.tar.gz; \\\n'+
            'wget \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/fitnesse/cr4_fitnesse_reg_suite.tar.gz ;\\\n'+
            'tar -xf cr4_fitnesse_reg_suite.tar.gz; \\\n'+
            'cd ConditionalBuildTest/src/main/resources/configurations; \\\n'+
            'rm -f teconf-\'${ENV_PREFIX}\'.properties; \\\n'+
            'wget \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/devops/fitnesse/teconf-\'${ENV_PREFIX}\'.properties; \\\n'+
            'cd /app/ecomm/fitnesse/ConditionalBuildTest/firefoxRC/ ;\\\n'+
            'rm -f  prefs.js; \\\n'+
            'rm -f cert8.db; \\\n'+
            'wget \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/devops/fitnesse/firefoxRC/prefs.js; \\\n'+
            'wget \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/devops/fitnesse/firefoxRC/cert8.db; \\\n'+
            'mkdir /app/ecomm/fitnesse/tools; \\\n'+
            'cd /app/ecomm/fitnesse/tools; \\\n'+
            'wget \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/fitnesse/tools/transformer/fitnesse-html.xsl; \\\n'+
            'wget \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/fitnesse/tools/transformer/XmlToHTmlTransformer.jar; \\\n'+
            'wget \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/fitnesse/tools/startFitnesse.sh ; \\\n'+
            'chmod 777 XmlToHTmlTransformer.jar fitnesse-html.xsl startFitnesse.sh\')\n'+
            '\n'+
    	    ' echo $result')
    }  
}

buildFlowJob(sub_folder+"/PrepareFitnesseOnAllServers") {
    displayName("PrepareFitnesseOnAllServers")
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX')  
    }  
   
    buildFlow(
          "parallel ( \n"+
          "           { build(\'PrepareFitnesseOnServer\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'01\') }, \n"+
          "           { build(\'PrepareFitnesseOnServer\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'02\') }, \n"+
          "           { build(\'PrepareFitnesseOnServer\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'03\') }, \n"+
          "           { build(\'PrepareFitnesseOnServer\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'04\') }, \n"+
          "           { build(\'PrepareFitnesseOnServer\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'05\') }, \n"+
          "           { build(\'PrepareFitnesseOnServer\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'06\') } \n"+
          "           //{ build(\'PrepareFitnesseOnServer\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'07\') }, \n"+
          "          // { build(\'PrepareFitnesseOnServer\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'08\') } \n"+
          "         )"
    )
    configure { project -> project / 'buildNeedsWorkspace'('true') }  
   
} 

job(sub_folder +'/PrepareFitnesseOnServer') {
 
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX')
        stringParam('FITNESSE_SERVER','atg-${ENV_PREFIX}-aws-fit${instanceId}.aws.gha.kfplc.com')
    }  
 
    steps {
	   
        shell ('result=$(ssh -o StrictHostKeyChecking=no ${FITNESSE_SERVER} \'rm -rf /app/ecomm/fitnesse; \\\n'+
            ' mkdir /app/ecomm/fitnesse; \\\n'+
            ' cd /app/ecomm/fitnesse; \\\n'+
            'rm -rf ConditionalBuildTest; \\\n'+
            'rm -f cr4_fitnesse_reg_suite.tar.gz; \\\n'+
            'wget \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/fitnesse/cr4_fitnesse_reg_suite.tar.gz ;\\\n'+
            'tar -xf cr4_fitnesse_reg_suite.tar.gz; \\\n'+
            'cd ConditionalBuildTest/src/main/resources/configurations; \\\n'+
            'rm -f teconf-\'${ENV_PREFIX}\'.properties; \\\n'+
            'wget \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/devops/fitnesse/teconf-\'${ENV_PREFIX}\'.properties; \\\n'+
            'cd /app/ecomm/fitnesse/ConditionalBuildTest/firefoxRC/ ;\\\n'+
            'rm -f  prefs.js; \\\n'+
            'rm -f cert8.db; \\\n'+
            'wget \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/devops/fitnesse/firefoxRC/prefs.js; \\\n'+
            'wget \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/devops/fitnesse/firefoxRC/cert8.db; \\\n'+
            'mkdir /app/ecomm/fitnesse/tools; \\\n'+
            'cd /app/ecomm/fitnesse/tools; \\\n'+
            'wget \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/fitnesse/tools/transformer/fitnesse-html.xsl; \\\n'+
            'wget \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/fitnesse/tools/transformer/XmlToHTmlTransformer.jar; \\\n'+
            'wget \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/fitnesse/tools/startFitnesse.sh ; \\\n'+
            'chmod 777 XmlToHTmlTransformer.jar fitnesse-html.xsl startFitnesse.sh\')\n'+
            '\n'+
    	    ' echo $result')
    }  
}

job(sub_folder +'/RestartFitnesse') {
 
    parameters {
        labelParam('FitnesseSlave')
        stringParam('ENV_PREFIX')
    }  
    
    concurrentBuild(true)
 
    steps {
	   
        shell ('FITNESSE_URL=http://${FitnesseSlave}.aws.gha.kfplc.com:8087/files/html/index.html\n'+
            'if curl --output /dev/null --silent --head --fail "$FITNESSE_URL"; then\n'+
            '  echo "Fitnesse running  $FITNESSE_URL shutting down"\n'+
            '  wget http://${FitnesseSlave}:8086/?responder=shutdown\n\n'+
            ' sleep 30\n'+
    	    'fi')
    }  
}

job(sub_folder +'/StartFitnesse') {
 
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX')
    }  
    
    concurrentBuild(true)
    
        configure { project -> 
        project / buildWrappers / 'org.jenkinsci.plugins.testinprogress.TestInProgressBuildWrapper' { }
    }
 
    steps {
	   
        shell ('BUILD_ID=222222\n'+
            'export fitnesse_port=8086\n'+
            'export PATH=/app/ecomm/tools/jruby/bin:/$PATH\n'+
            'export JAVA_HOME=/usr/java/jdk1.6.0_45/jre\n'+
            'export DISPLAY=localhost:99.0\n\n'+
            'cd /app/ecomm/fitnesse/ConditionalBuildTest\n'+
            'Xvfb :99 -screen 0 1024x768x24 2>&1 >/dev/null &\n'+
            'buildr fitnesse:start port=8086 test=no config=/app/ecomm/fitnesse/ConditionalBuildTest/src/main/resources/configurations/teconf-${ENV_PREFIX}.properties browser=*firefox site=DIY excludeFilter=dev,smoke,slow,integration,preload xmlFormat=yes &\n'+
            'sleep 30\n'+
    	    'exit 0')
    }  
}

buildFlowJob(sub_folder+"/StartFitnesseOnAllServers") {
    displayName("StartFitnesseOnAllServers")
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX')  
    }  
   
    buildFlow(
          "parallel ( \n"+
          "           { build(\'StartFitnesseUsingSSH\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'01\') }, \n"+
          "           { build(\'StartFitnesseUsingSSH\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'02\') }, \n"+
          "           { build(\'StartFitnesseUsingSSH\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'03\') }, \n"+
          "           { build(\'StartFitnesseUsingSSH\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'04\') }, \n"+
          "           { build(\'StartFitnesseUsingSSH\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'05\') }, \n"+
          "           { build(\'StartFitnesseUsingSSH\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'06\') } \n"+
          "         )"
    )
    configure { project -> project / 'buildNeedsWorkspace'('true') }  
   
} 

job(sub_folder +'/StartFitnesseUsingSSH') {
 
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX')
        stringParam('FITNESSE_SERVER','atg-${ENV_PREFIX}-aws-fit${instanceId}.aws.gha.kfplc.com')
    }  
    
    concurrentBuild(true)
    
    configure { project -> 
        project / buildWrappers / 'org.jenkinsci.plugins.testinprogress.TestInProgressBuildWrapper' { }
    }
    
    steps {
	   
        shell ('BUILD_ID=222222\n\n'+
            'nohup ssh -o StrictHostKeyChecking=no ecommadm@${FITNESSE_SERVER} \'cd /app/ecomm/fitnesse/tools ; ./startFitnesse.sh \'${ENV_PREFIX} & \n\n\n'+
            'sleep 30\n'+
    	    'exit 0')
    }  
}

job(sub_folder +'/StopFitnesse') {
 
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX')
        stringParam('FITNESSE_SERVER','atg-${ENV_PREFIX}-aws-fit${instanceId}.aws.gha.kfplc.com')
    }  
    
    concurrentBuild(true)
    
        configure { project -> 
        project / buildWrappers / 'org.jenkinsci.plugins.testinprogress.TestInProgressBuildWrapper' { }
    }
 
    steps {
	   
        shell ('#!/bin/sh \n'+
            'wget http://${FITNESSE_SERVER}:8086/?responder=shutdown\n\n'+
            'if [ "$?" = "0" ]; then\n'+
            'sleep 30\n'+
            'else\n'+
            'echo "error code trapped $?"\n'+
            'exit 0\n'+
    	    'fi')
    }  
}

buildFlowJob(sub_folder+"/StopFitnesseOnAllServers") {
    displayName("StopFitnesseOnAllServers")
    parameters {
        labelParam('DeploymentSlave')
        stringParam('ENV_PREFIX')  
    }  
   
    buildFlow(
          "parallel ( \n"+
          "           { build(\'StopFitnesse\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'01\') }, \n"+
          "           { build(\'StopFitnesse\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'02\') }, \n"+
          "           { build(\'StopFitnesse\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'03\') }, \n"+
          "           { build(\'StopFitnesse\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'04\') }, \n"+
          "           { build(\'StopFitnesse\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'05\') }, \n"+
          "           { build(\'StopFitnesse\' ,ENV_PREFIX: params[\'ENV_PREFIX\'] , DeploymentSlave: params[\'DeploymentSlave\'], instanceId: \'06\') } \n"+
          "         )"
    )
    configure { project -> project / 'buildNeedsWorkspace'('true') }  
   
} 