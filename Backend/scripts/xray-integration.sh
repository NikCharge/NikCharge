#!/bin/bash

# Xray API Configuration
XRAY_API_URL="https://xray.cloud.getxray.app/api/v2"

# Carregar variáveis do .env
if [ -f ../../.env ]; then
  source ../../.env
else
  echo "Arquivo .env não encontrado!"; exit 1
fi


# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "🚀 Starting Xray Integration Process..."

# Step 1: Generate test report
echo "📊 Generating test report..."
cd ..  # Mudar para o diretório do projeto
mvn verify

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Test report generated successfully${NC}"
else
    echo -e "${RED}❌ Failed to generate test report${NC}"
    exit 1
fi

# Step 2: Get authentication token
echo "🔑 Getting authentication token..."
TOKEN=$(curl -s -X POST "$XRAY_API_URL/authenticate" \
  -H "Content-Type: application/json" \
  -d "{\"client_id\": \"$XRAY_CLIENT_ID\", \"client_secret\": \"$XRAY_CLIENT_SECRET\"}" \
  | tr -d '"')

if [ -z "$TOKEN" ]; then
    echo -e "${RED}❌ Failed to get authentication token${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Authenticated with Xray${NC}"

# Step 3: Extract Test issue key from .feature (e.g., @SCRUM-11 from the Feature tag)
TEST_ISSUE_KEY=$(grep -h -o '@SCRUM-[0-9]\+' src/test/resources/features/*.feature | head -1 | sed 's/@//')

if [ -z "$TEST_ISSUE_KEY" ]; then
    echo -e "${RED}❌ Could not find user story tag (e.g., @SCRUM-11) in .feature file${NC}"
    exit 1
fi

echo "📎 Found related user story: $TEST_ISSUE_KEY"

# Step 4: Create new Test Execution issue linked to the User Story
echo "🔍 Searching for existing Test Execution..."

# Search for existing Test Execution linked to the User Story
SEARCH_PAYLOAD=$(cat <<EOF
{
  "jql": "project = $PROJECT_KEY AND issuetype = 'Test Execution' AND issue in linkedIssues($TEST_ISSUE_KEY) ORDER BY created DESC",
  "maxResults": 1
}
EOF
)

EXISTING_EXECUTION=$(curl -s -X POST https://nikcharge.atlassian.net/rest/api/3/search \
  -u "$JIRA_USER_EMAIL:$JIRA_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$SEARCH_PAYLOAD")

TEST_EXECUTION_KEY=$(echo "$EXISTING_EXECUTION" | jq -r '.issues[0].key')

if [ "$TEST_EXECUTION_KEY" == "null" ] || [ -z "$TEST_EXECUTION_KEY" ]; then
    echo "📄 No existing Test Execution found. Creating new one..."
    TEST_EXECUTION_PAYLOAD=$(cat <<EOF
{
  "fields": {
    "project": { "key": "$PROJECT_KEY" },
    "summary": "Automated Execution for $TEST_ISSUE_KEY",
    "issuetype": { "name": "Test Execution" },
    "description": {
      "type": "doc",
      "version": 1,
      "content": [
        {
          "type": "paragraph",
          "content": [
            {
              "type": "text",
              "text": "Created automatically by CI for $TEST_ISSUE_KEY"
            }
          ]
        }
      ]
    },
    "fixVersions": []
  }
}
EOF
)

    # Adicionar debug para ver o payload
    echo "Debug - Test Execution Payload:"
    echo "$TEST_EXECUTION_PAYLOAD"

    RESPONSE=$(curl -s -X POST https://nikcharge.atlassian.net/rest/api/3/issue \
      -u "$JIRA_USER_EMAIL:$JIRA_API_TOKEN" \
      -H "Content-Type: application/json" \
      -d "$TEST_EXECUTION_PAYLOAD")

    echo "Debug - API Response:"
    echo "$RESPONSE"

    TEST_EXECUTION_KEY=$(echo "$RESPONSE" | jq -r '.key')

    if [ "$TEST_EXECUTION_KEY" == "null" ] || [ -z "$TEST_EXECUTION_KEY" ]; then
        echo -e "${RED}❌ Failed to create Test Execution in Jira${NC}"
        echo "Response: $RESPONSE"
        exit 1
    fi

    echo -e "${GREEN}✅ Created Test Execution: $TEST_EXECUTION_KEY${NC}"

    # Step 5: Link Test Execution to User Story
    echo "🔗 Linking Test Execution to User Story..."
    LINK_PAYLOAD=$(cat <<EOF
{
  "type": {
    "name": "Test"
  },
  "inwardIssue": {
    "key": "$TEST_EXECUTION_KEY"
  },
  "outwardIssue": {
    "key": "$TEST_ISSUE_KEY"
  }
}
EOF
)

    LINK_RESPONSE=$(curl -s -X POST https://nikcharge.atlassian.net/rest/api/3/issueLink \
      -u "$JIRA_USER_EMAIL:$JIRA_API_TOKEN" \
      -H "Content-Type: application/json" \
      -d "$LINK_PAYLOAD")

    if [[ $LINK_RESPONSE == *"error"* ]]; then
        echo -e "${YELLOW}⚠️ Warning: Failed to link Test Execution to User Story${NC}"
        echo "Response: $LINK_RESPONSE"
    fi
else
    echo -e "${GREEN}✅ Found existing Test Execution: $TEST_EXECUTION_KEY${NC}"
fi

# Step 6: Import test results to that Test Execution
echo "📤 Importing test results to $TEST_EXECUTION_KEY..."

# Criar info.json com configurações específicas para Cucumber
cat > info.json << EOF
{
  "testExecutionKey": "$TEST_EXECUTION_KEY",
  "fields": {
    "summary": "Test Results for $TEST_ISSUE_KEY",
    "project": {
      "key": "$PROJECT_KEY"
    },
    "issuetype": {
      "name": "Test Execution"
    }
  },
  "testType": "Cucumber",
  "testInfo": {
    "type": "Cucumber",
    "format": "json"
  }
}
EOF

RESPONSE=$(curl -s -X POST "$XRAY_API_URL/import/execution/cucumber/multipart" \
  -H "Authorization: Bearer $TOKEN" \
  -F "results=@target/cucumber-reports/cucumber.json;type=application/json" \
  -F "info=@info.json;type=application/json")

rm info.json

if [[ $RESPONSE == *"error"* ]]; then
    echo -e "${RED}❌ Failed to import test results${NC}"
    echo "Response: $RESPONSE"
    exit 1
fi

echo -e "${GREEN}✅ Test results imported successfully to $TEST_EXECUTION_KEY${NC}"
