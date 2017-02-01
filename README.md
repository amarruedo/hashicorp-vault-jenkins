# hashicorp-vault-jenkins

Jenkins pipeline step to retrieve secrets from Hashicorp's Vault server

## Install

You'll need [workflow-cps-global-lib](https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Shared+Groovy+Libraries+Plugin) plugin.

The step itself is added to Jenkins in *Manage Jenkins » Configure System » Global Pipeline Libraries*

![Alt text](images/vault_step.png?raw=true "Vault step configuration")

## Usage

The step must be run in a `curl` enabled `linux` node since it uses a `sh` step to `curl` Vault API.
We write a secret to Vault and then query it using the step inside a Jenkins pipeline

On the Vault server:

```
vault write secret/test key=my-key cert=my-cert
```
Jenkins DSL script:

```
#!groovy

@Library('vault-secrets') _

def username = ''
def password = ''
def secret = ''

timeout(time:5, unit:'MINUTES') {

  def userInput = input(
      id: 'userInput', message: 'User/Password/Secret', parameters: [
      [$class: 'TextParameterDefinition', defaultValue: '', description: 'Username input', name: 'username'],
      [$class: 'PasswordParameterDefinition', defaultValue: '', description: 'Password input', name: 'password'],
      [$class: 'TextParameterDefinition', defaultValue: '', description: 'Secret to retrieve', name: 'secret']
  ])

  username=userInput['username'].toString()
  password=userInput['password'].toString()
  secret=userInput['secret'].toString()
}

node("master"){

    def data = vaultSecret("master", "http://vault.default.svc.cluster.local:8200", username, password, secret)
    echo "CERT: " + data.cert.toString() + " KEY: " + data.key.toString()
}
```
