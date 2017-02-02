#!groovy​

import groovy.json.*

def parseJSON(String response, String secretName){

  def result = new JsonSlurperClassic().parseText(response)

  try {

      if (result.errors)
          error "Vault: " + result.errors[0].toString()
      else if (secretName == "client_token" && result.auth.client_token)
          return result.auth.client_token
      else if (secretName == "data" && result.data)
          return result.data
      else
          error "Can't retrieve secret"
  }
  catch(Exception err)
  {
      error err.toString()
  }
}

def call(String username, String password, String secretName,  String vaultAddress, String nodeName = 'master') {

  node(nodeName){

      response = sh(returnStdout: true, script:"set +x; curl -s " + vaultAddress + "/v1/auth/userpass/login/" + username +" -d '{ \"password\": \"" + password + "\" }'").trim()
      def result = parseJSON(response, "client_token")
      def vault_token = result.toString()

      secrets = sh(returnStdout: true, script:"set +x; curl -s -X GET -H \"X-Vault-Token:" + vault_token + "\" " + vaultAddress + "/v1/secret/" + secretName).trim()
      result = parseJSON(secrets, "data")

      return result
  }
}
