def under_folder = 'Common'

folder(under_folder)

job(under_folder +'/FixForGzIssue') {
    
    logRotator {
        daysToKeep 2
    }
    
    parameters {
        labelParam('ATGSlave')
    }
    
    steps {
        shell( 
	    'find /app/ecomm/ATG/ -name \\*gz |awk \'{print "gunzip " $0}\' |sh' 
        ) 
    }
} 
 
job(under_folder +'/PostUsingCurl') {
    
    logRotator {
        daysToKeep 2
    }
    
    parameters {
        stringParam('url')
        stringParam('servers')
        choiceParam('post_props', ['invokeMethod=indexBaseline&submit=Invoke+Method'])
        stringParam('port','8110')
    }
    
    steps {
        shell( 
	    'for server in ${servers}\n' + 
	    'do\n' +
	    ' curl --data "${post_props}" http://${server}:${port}/${url}\n' +
            'done'
        ) 
    }
    publishers { 
        consoleParsing { 
            globalRules('ParsingRules')
            failBuildOnError() 
        } 
    } 
} 

job(under_folder +'/UploadArtifactToNexus') {
    
    logRotator {
        daysToKeep 2
    }
    
    parameters {
        labelParam('BuildSlave')
        stringParam('artifact_name')
        stringParam('artifact_version')
        stringParam('artifact_location')
        stringParam('nexus_location')
    }
    
    steps {
        shell( 
	    'curl --upload-file ${artifact_location}/${artifact_name} ${OLD_NEXUS_RELEASE_LOCATION}/${nexus_location}/${artifact_version}/${artifact_name} --user darwin:darwin' 
        ) 
    }
} 