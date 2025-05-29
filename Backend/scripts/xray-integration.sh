#!/bin/bash

# Verificar se o arquivo .env existe e carregar as variáveis
#if [ -f ../../.env ]; then
#    source ../../.env
#else
#    echo "Arquivo .env não encontrado!"
#    exit 1
#fi

# Variáveis de ambiente já estão configuradas via GitHub Actions
echo "Usando variáveis de ambiente da pipeline."
cd "$(dirname "$0")"/..

# Verificar se todas as variáveis necessárias estão definidas
if [ -z "$XRAY_CLIENT_ID" ] || [ -z "$XRAY_CLIENT_SECRET" ] || [ -z "$JIRA_USER_EMAIL" ] || [ -z "$JIRA_API_TOKEN" ] || [ -z "$PROJECT_KEY" ]; then
    echo "Erro: Todas as variáveis de ambiente necessárias devem estar definidas no arquivo .env"
    echo "Variáveis necessárias:"
    echo "- XRAY_CLIENT_ID"
    echo "- XRAY_CLIENT_SECRET"
    echo "- JIRA_USER_EMAIL"
    echo "- JIRA_API_TOKEN"
    echo "- PROJECT_KEY"
    exit 1
fi

# Definir CREATE_TEST_EXECUTION como false se não estiver definido
if [ -z "$CREATE_TEST_EXECUTION" ]; then
    CREATE_TEST_EXECUTION="false"
fi

echo "🚀 Iniciando pipeline de testes e integração com Xray..."

# Gerar relatório de testes
echo "📊 Gerando relatório de testes..."
mvn verify

# Verificar se o relatório foi gerado
if [ ! -f "target/cucumber-reports/cucumber.json" ]; then
    echo "❌ Erro: Relatório de testes não encontrado!"
    exit 1
fi

# Autenticar com Xray
echo "🔑 Autenticando com Xray..."
XRAY_TOKEN=$(curl -s -H "Content-Type: application/json" -X POST --data "{\"client_id\":\"$XRAY_CLIENT_ID\",\"client_secret\":\"$XRAY_CLIENT_SECRET\"}" https://xray.cloud.getxray.app/api/v2/authenticate | tr -d '"')

if [ -z "$XRAY_TOKEN" ]; then
    echo "❌ Erro: Falha na autenticação com Xray"
    exit 1
fi

TEST_EXECUTION_KEY="SCRUM-99"
echo "ℹ️ Usando Test Execution existente: $TEST_EXECUTION_KEY"


# Importar resultados dos testes
echo "📤 Importando resultados para a Test Execution existente: $TEST_EXECUTION_KEY"
IMPORT_RESPONSE=$(curl -s \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $XRAY_TOKEN" \
  -X POST \
  --data @target/cucumber-reports/cucumber.json \
  "https://xray.cloud.getxray.app/api/v2/import/execution/cucumber?testExecutionKey=$TEST_EXECUTION_KEY")

if [[ $IMPORT_RESPONSE == *"error"* ]]; then
    echo "❌ Erro ao importar resultados: $IMPORT_RESPONSE"
    exit 1
fi

echo "✅ Pipeline concluído com sucesso!"
if [ -n "$TEST_EXECUTION_KEY" ]; then
    echo "📊 Test Execution: $TEST_EXECUTION_KEY"
fi

