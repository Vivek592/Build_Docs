def top_folder = '/Infra'
def under_folder = '/Infra/Release'

folder(top_folder)
folder(under_folder)

job(under_folder +'/PrepareATGInventoryFromNexus') {

    parameters {
        stringParam('PATH_TO_DYNAMIC_INVENTORY','/deployment/ansible/is-inventory/group_hosts')
        labelParam('DeploymentSlave')
        stringParam('APP_VERSION','ATG_INV_CI_REL')
        stringParam('ENV_PREFIX','','environment identifier')
        
    } 
    
    steps {
        shell('cd ${PATH_TO_DYNAMIC_INVENTORY} \n'+
	      'rm -f ${ENV_PREFIX}-hosts \n'+
	      '\n'+
	      'wget ${NEW_NEXUS_RELEASE_LOCATION}/kf/devops/inventory/${APP_VERSION}/${ENV_PREFIX}-hosts  \n')  
    }
}

job(under_folder +'/TagSearchIndexingFiles') {

    parameters {
        stringParam('ENV_PREFIX','','environment identifier')
        labelParam('DeploymentSlave')
        { 
            defaultValue('atg-${ENV_PREFIX}-aws-slv01') 
        } 
        stringParam('ATG_SEARCH_IDX_FILES_REL','ATG_SRCH_INDEX_REL_${ENV_PREFIX}')
    } 
    
    steps {
        shell('result=$(ssh atg-${ENV_PREFIX}-aws-sch01.aws.gha.kfplc.com \'cd /app/ecomm/ATGSearchDeployShare; jar -cf /tmp/\'${ATG_SEARCH_IDX_FILES_REL}\'.jar *.gz  ; curl --upload-file /tmp/\'${ATG_SEARCH_IDX_FILES_REL}\'.jar \'${NEW_NEXUS_RELEASE_LOCATION}\'/kf/devops/search/indexing/\'${ATG_SEARCH_IDX_FILES_REL}\'.jar --user darwin:darwin\') \n'+
	      'echo $result  \n')  
    }
}

job(under_folder +'/UploadATGInventoryToNexus') {

    parameters {
        stringParam('PATH_TO_DYNAMIC_INVENTORY','/deployment/ansible/is-inventory/group_hosts')
        labelParam('DeploymentSlave')
        stringParam('APP_VERSION','ATG_INV_CI_REL')
        stringParam('ENV_PREFIX','','environment identifier')
        
    } 
    
    steps {
        shell('curl --upload-file ${PATH_TO_DYNAMIC_INVENTORY}/${ENV_PREFIX}-hosts ${NEW_NEXUS_RELEASE_LOCATION}/kf/devops/inventory/${APP_VERSION}/${ENV_PREFIX}-hosts --user darwin:darwin \n')  
    }
}
