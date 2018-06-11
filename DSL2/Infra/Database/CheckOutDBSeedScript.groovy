def top_folder = '/Infra'
def under_folder = '/Infra/Database'

folder(top_folder)
folder(under_folder)

def workspace = '/deployment/ansible/'
def project_ws = ['is-appsetup','is-iaas','is_inventory']

def job_name= under_folder +'/Checkout-DB-Seed-Scripts'

job(job_name) {
    description 'Wrapper job to checkout DB repositories'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }

parameters {
        labelParam('DeploymentSlave')
           }
customWorkspace('/deployment/ecomm/database');
scm {
             git {
             remote {
                 url 'http://git.aws.gha.kfplc.com/KITS/database.git'
                 branch '*/master'
                 credentials 'a598502-4513-475a-9bbc-33bd216f73b6'
                 }
                         
             }
     }
}
