def under_folder = '/ATG/Update'

folder(under_folder)

buildFlowJob(under_folder+"/UpdateATGSlave") {
    displayName("UpdateATGSlave")
    parameters {
        labelParam('ATGSlave')
        stringParam('ENV_PREFIX')  
    }  
   
   buildFlow(
          " build(\'UpdateSiteBuilder\',ENV_PREFIX: params[\'ENV_PREFIX\'] , ATGSlave: params[\'ATGSlave\'] )\n"+
          " build(\'ATG/Build/Checkout_kfutil\',ENV_PREFIX: params[\'ENV_PREFIX\'] , ATGSlave: params[\'ATGSlave\'] )"
   )
    configure { project -> project / 'buildNeedsWorkspace'('true') }  
} 

job(under_folder+"/UpdateSiteBuilder") {
 
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    
    parameters {
        labelParam('ATGSlave')
        stringParam('APP_VERSION','3.1.11')  
    }  
   
    steps {
	shell ('REL_IN_NEW_NEXUS=${NEW_NEXUS_RELEASE_LOCATION}/spindrift/sitebuildermodule/${APP_VERSION}/sitebuildermodule-${APP_VERSION}.jar\n'+
	    'REL_IN_OLD_NEXUS=${OLD_NEXUS_RELEASE_LOCATION}/spindrift/sitebuildermodule/${APP_VERSION}/sitebuildermodule-${APP_VERSION}.jar\n\n'+
	    'if curl --output /dev/null --silent --head --fail "$REL_IN_NEW_NEXUS"; then\n'+
	    '  echo "URL exists:in  $url"\n'+
	    '   wget -q $REL_IN_NEW_NEXUS\n\n'+
	    'else\n'+
	    '  wget -q $REL_IN_OLD_NEXUS\n'+
	    'fi\n\n\n'+
	    'rm -rf ${ATG_HOME}/SiteBuilder \n'+
	    'mv sitebuildermodule-${APP_VERSION}.jar ${ATG_HOME}\n'+
	    'cd ${ATG_HOME}\n'+
	    'jar -xvf sitebuildermodule-${APP_VERSION}.jar\n'+
	    'cat SiteBuilder/META-INF/MANIFEST.MF')
    }
} 