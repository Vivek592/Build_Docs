def top_folder = '/Infra'
def under_folder = '/Infra/Control'
def under_folder2 = '/Infra/Control/ssh'
def under_folder3 = '/Infra/Control/VPN'

folder(top_folder)
folder(under_folder)
folder(under_folder2)
folder(under_folder3)

job(under_folder3 +'/CreateOpenVPN') {
    
    parameters {
        stringParam('userName','sgamarny')
        stringParam('fullName','Sergii Gamarnyk')
        stringParam('userEmail','Sergii_Gamarnyk@epam.com')        
    } 

    label('atg-devops11-aws-slv01')
 
    concurrentBuild(true)

    steps {
        shell('cd /deployment/ecomm/gapi/infra/ansible \n'+
	      'ansible-playbook -i ./ec2.py playbooks/createUsers.yml -e "userName=${userName}  fullName=\'${fullName}\' userEmail=${userEmail}"')  
    }
}

