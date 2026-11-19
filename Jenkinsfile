// CI/CD pipeline: test -> analyse -> build image -> scan -> push -> (Terraform) -> deploy to Amazon EKS.
//
// Requires the shared library https://github.com/ElloughWassim/jenkins-pipeline-library
// registered in Jenkins as "jenkins-pipeline-library". See README.md > "CI/CD pipeline" for
// the plugins, tools and credentials this pipeline expects.
@Library('jenkins-pipeline-library@v1.0.0') _

def image

pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 90, unit: 'MINUTES')
    }

    parameters {
        choice(name: 'ACTION', choices: ['build-and-deploy', 'build-only', 'destroy-infrastructure'],
               description: 'build-only stops after pushing the image; destroy-infrastructure removes the EKS cluster')
        choice(name: 'REGISTRY', choices: ['ecr', 'dockerhub'], description: 'Container registry for the image')
        booleanParam(name: 'PROVISION_INFRA', defaultValue: false,
                     description: 'Run Terraform plan/apply for the EKS cluster before deploying')
    }

    environment {
        // ---- Replace the placeholders (or define them as global Jenkins environment variables) ----
        AWS_REGION          = 'eu-west-1'
        AWS_ACCOUNT_ID      = '<AWS_ACCOUNT_ID>'
        DOCKERHUB_NAMESPACE = '<DOCKERHUB_NAMESPACE>'
        EKS_CLUSTER_NAME    = 'springboot-demo-eks'
        IMAGE_NAME          = 'springboot-terraform-eks'
        K8S_NAMESPACE       = 'springboot-demo'
        K8S_DEPLOYMENT      = 'springboot-demo'
    }

    stages {
        stage('Build and verify') {
            when { expression { params.ACTION != 'destroy-infrastructure' } }
            stages {
                stage('Unit tests') {
                    steps { mavenUnitTest() }
                }
                stage('Integration tests') {
                    steps { mavenIntegrationTest() }
                }
                stage('SonarQube analysis') {
                    steps { sonarQubeAnalysis(installationName: 'sonarqube') }
                }
                stage('Quality gate') {
                    steps { sonarQubeQualityGate(timeoutMinutes: 10) }
                }
                stage('OWASP Dependency-Check') {
                    steps {
                        owaspDependencyCheck(odcInstallation: 'dependency-check',
                                             nvdApiKeyCredentialsId: 'nvd-api-key', failOnCvss: 7)
                    }
                }
                stage('Trivy filesystem scan') {
                    steps { trivyScan(type: 'fs', target: '.') }
                }
                stage('Trivy IaC scan') {
                    steps { trivyScan(type: 'config', target: '.') }
                }
            }
        }

        stage('Container image') {
            when { expression { params.ACTION != 'destroy-infrastructure' } }
            stages {
                stage('Docker build') {
                    steps {
                        script {
                            Map imageConfig = params.REGISTRY == 'ecr'
                                ? [registry: 'ecr', awsAccountId: env.AWS_ACCOUNT_ID, awsRegion: env.AWS_REGION,
                                   name: env.IMAGE_NAME, tags: imageTags(extraTags: [])]       // ECR tags are immutable
                                : [registry: 'dockerhub', namespace: env.DOCKERHUB_NAMESPACE,
                                   name: env.IMAGE_NAME, tags: imageTags(extraTags: ['latest'])]
                            image = dockerBuild(image: imageConfig)
                        }
                    }
                }
                stage('Trivy image scan') {
                    steps { trivyScan(type: 'image', target: image.primaryReference) }
                }
                stage('Push image') {
                    steps {
                        script {
                            if (params.REGISTRY == 'ecr') {
                                dockerPush(image: image, awsCredentialsId: 'aws-credentials')
                            } else {
                                dockerPush(image: image, credentialsId: 'dockerhub-credentials')
                            }
                        }
                    }
                }
            }
        }

        stage('Terraform plan') {
            when {
                expression { params.ACTION == 'destroy-infrastructure' || (params.ACTION == 'build-and-deploy' && params.PROVISION_INFRA) }
            }
            steps {
                script {
                    String destroyFlag = params.ACTION == 'destroy-infrastructure' ? '-destroy' : ''
                    withTerraformCredentials {
                        sh 'terraform init -input=false -backend-config="$TF_BACKEND_CONFIG"'
                        sh "terraform plan -input=false ${destroyFlag} -var-file=\"\$TF_VARS_FILE\" -var \"aws_region=\$AWS_REGION\" -out=tfplan"
                        sh 'terraform show -no-color tfplan > tfplan.txt'
                    }
                    archiveArtifacts artifacts: 'terraform/tfplan.txt', allowEmptyArchive: false
                }
            }
        }

        stage('Terraform apply') {
            when {
                expression { params.ACTION == 'destroy-infrastructure' || (params.ACTION == 'build-and-deploy' && params.PROVISION_INFRA) }
            }
            steps {
                timeout(time: 30, unit: 'MINUTES') {
                    input message: "Apply the Terraform plan (${params.ACTION})? Review terraform/tfplan.txt first.",
                          ok: 'Apply'
                }
                withTerraformCredentials {
                    sh 'terraform apply -input=false tfplan'
                }
            }
        }

        stage('Deploy to EKS') {
            when { expression { params.ACTION == 'build-and-deploy' } }
            steps {
                withCredentials([aws(credentialsId: 'aws-credentials',
                                     accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                                     secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                    withEnv(["KUBECONFIG=${env.WORKSPACE}/.kube/config"]) {
                        sh 'aws eks update-kubeconfig --region "$AWS_REGION" --name "$EKS_CLUSTER_NAME" --kubeconfig "$KUBECONFIG"'
                        kubernetesDeploy(manifests: 'k8s', namespace: env.K8S_NAMESPACE,
                                         deployment: env.K8S_DEPLOYMENT, image: image.primaryReference)
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                if (image) {
                    dockerCleanup(image: image)
                }
            }
            sh 'rm -rf .kube terraform/tfplan || true'
        }
    }
}

/**
 * Runs `body` inside terraform/ with AWS credentials and the git-ignored Terraform
 * configuration files injected from Jenkins "Secret file" credentials.
 */
void withTerraformCredentials(Closure body) {
    withCredentials([
        aws(credentialsId: 'aws-credentials', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'),
        file(credentialsId: 'terraform-backend-hcl', variable: 'TF_BACKEND_CONFIG'),
        file(credentialsId: 'terraform-tfvars', variable: 'TF_VARS_FILE'),
    ]) {
        dir('terraform') {
            body()
        }
    }
}
