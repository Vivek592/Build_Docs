def top_folder = '/ATG'
def sub_folder = '/ATG/Build'
def under_folder = '/ATG/Build/Endeca'

folder(top_folder)
folder(sub_folder)
folder(under_folder)

job(under_folder +'/BuildAAS') {
    
    logRotator {
        daysToKeep -1
        numToKeep 30
    }
    
    parameters {
        stringParam('branch','develop','Working branch')
        labelParam('ATGSlave')
       
    } 
        scm {
             git {
             	 remote {
                 	url 'https://github.com/KITSGitHubAdmin/KITS-Endeca.git'
                 	branch '${branch}'
                 	credentials 'ca5be30b-657d-4c0d-8191-edc84a8ad31e'
                 }          
             }
    	}
    
    steps {
	shell('export JAVA_HOME=${JDK_VER_TO_USE} \n'+
		'echo $JAVA_HOME \n'+
		'export PATH=${RUBY_HOME}:/$PATH  \n'+
		'export APP_VERSION=1.0.0 \n'+
		'cd search-service \n'+
		'echo BUILD_VERSION=${GIT_COMMIT} > src/j2ee/search-service.war/build_version.properties \n'+
		'buildr clean build package emma:xml pmd:rule:xml pmd:cpd:xml \n'+
		'curl --upload-file target/search-service-${APP_VERSION}.war ${OLD_NEXUS_RELEASE_LOCATION}/com/endeca/search-service/${APP_VERSION}/search-service-${APP_VERSION}.war --user darwin:darwin \n'+
		'curl --upload-file target/env-install-${APP_VERSION}.zip ${OLD_NEXUS_RELEASE_LOCATION}/com/endeca/search-service/${APP_VERSION}/env-install-${APP_VERSION}.zip --user darwin:darwin' )
    }
    publishers {
        pmd('**/pmd.xml') {
            healthLimits(3,0)
            thresholdLimit('high')
            thresholds(
                    failedTotal: [ high: 2]
            )
        }
        dry('**/cpd.xml', 80, 20) {
            healthLimits(3,0)
            thresholdLimit('high')
            canRunOnFailed(false)
            computeNew(false)
            thresholds(
                    failedTotal: [ high: 18],
            )
        }
        
        archiveJunit('**/junit/*.xml')
        
        emma('**/reports/emma/coverage.xml') {
            minClass(0)
            maxClass(90)
            minMethod(0)
            maxMethod(80)
            minBlock(0)
            maxBlock(80)
            minLine(0)
            maxLine(80)
        }
        jacocoCodeCoverage { 
            execPattern('**/reports/emma/coverage.xml')
            classPattern('**/target/classes')
            sourcePattern('**/src/main/java')
            inclusionPattern('')
            exclusionPattern('**/*Test*.class')
            maximumInstructionCoverage('0') 
            minimumInstructionCoverage('0') 
            maximumBranchCoverage('0') 
            minimumBranchCoverage('0') 
            maximumComplexityCoverage('0') 
            minimumComplexityCoverage('0') 
            maximumLineCoverage('82') 
            minimumLineCoverage('69') 
            maximumMethodCoverage('80') 
            minimumMethodCoverage('85') 
            maximumClassCoverage('90') 
            minimumClassCoverage('95') 
        } 
    }
}

job(under_folder +'/BuildTest') {
    
    logRotator {
        daysToKeep -1
        numToKeep 30
    }
    
    parameters {
        stringParam('branch','develop','Working branch')
        labelParam('ATGSlave')
       
    } 
        scm {
             git {
             	 remote {
                 	url 'https://github.com/KITSGitHubAdmin/KITS-Endeca.git'
                 	branch '${branch}'
                 	credentials 'ca5be30b-657d-4c0d-8191-edc84a8ad31e'
                 } 
             }
    	}
    
    steps {
	shell('export JAVA_HOME=${JDK_VER_TO_USE} \n'+
		'echo $JAVA_HOME \n'+
		'export PATH=${RUBY_HOME}:/$PATH  \n'+
		'export APP_VERSION=1.0.0 \n'+
		'cd search-service \n'+
		'echo BUILD_VERSION=${GIT_COMMIT} > src/j2ee/search-service.war/build_version.properties \n'+
		'buildr clean build package emma:xml pmd:rule:xml pmd:cpd:xml' )
    }
    publishers {
        pmd('**/pmd.xml') {
            healthLimits(3,0)
            thresholdLimit('high')
            thresholds(
                    failedTotal: [ high: 2]
            )
        }
        dry('**/cpd.xml', 80, 20) {
            healthLimits(3,0)
            thresholdLimit('high')
            canRunOnFailed(false)
            computeNew(false)
            thresholds(
                    failedTotal: [ high: 18],
            )
        }
        
        archiveJunit('**/junit/*.xml')
        
        emma('**/reports/emma/coverage.xml') {
            minClass(15)
            maxClass(90)
            minMethod(15)
            maxMethod(80)
            minBlock(15)
            maxBlock(80)
            minLine(15)
            maxLine(80)
        }
    }
}