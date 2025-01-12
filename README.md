Ce poganjamo lokalno in je baza v docker:
#connection-url: jdbc:postgresql://localhost:5432/portfolios
#connection-url: jdbc:postgresql://localhost:5432/transactions
#connection-url: jdbc:postgresql://localhost:5432/users

1:Ce poganjamo mikrostoritev kot image:
kumuluzee:
  datasources:
    - jndi-name: jdbc/[ServiceName]DB  # PortfolioDB, TransactionsDB, or UsersDB
      connection-url: jdbc:postgresql://finance-db:5432/finance
      username: postgres
      password: postgres


2: potem ponovno zgradimo mikrostoritev:
# In each service directory
mvn clean package
docker build -t finance/[service-name]:1.0 .



# 1. Create network (if not already created)
docker network create finance-network

# 2. Remove existing containers (if any)
docker rm -f portfolio-service transactions-service users-service finance-db

# 3. Start PostgreSQL
docker run -d \
  --name finance-db \
  --network finance-network \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=finance \
  -p 5432:5432 \
  postgres:latest

# Wait for PostgreSQL to initialize
sleep 10

# 4. Start Portfolio Service
docker run -d \
  --name portfolio-service \
  --network finance-network \
  -p 8080:8080 \
  finance/portfolio-service:1.0

# 5. Start Transactions Service
docker run -d \
  --name transactions-service \
  --network finance-network \
  -p 8082:8082 \
  finance/transactions-service:1.0

# 6. Start Users Service
docker run -d \
  --name users-service \
  --network finance-network \
  -p 8081:8081 \
  finance/users-service:1.0

# 7. Verify all containers are running
docker ps

# 8. Check logs of each service
docker logs portfolio-service
docker logs transactions-service
docker logs users-service





## 1. Build Services

For each service (portfolio, transactions, users)

bash
cd portfolio-service # or transactions-service or users-service
mvn clean package
docker build -t finance/portfolio-service:1.0 .

## 2. Deploy to Kubernetes

### Create Namespace
kind create cluster --name finance
kubectl create namespace finance
kubectl config set-context --current --namespace=finance

### Deploy Services
kubectl apply -f portfolio-service/k8s/deployment.yaml
kubectl apply -f transactions-service/k8s/deployment.yaml
kubectl apply -f users-service/k8s/deployment.yaml


### kubectl apply -f k8s/deployment.yaml


kubectl get pods
kubectl get services



## 3. Service URLs
- Portfolio Service: http://localhost:8080
- Transactions Service: http://localhost:8082
- Users Service: http://localhost:8081

## 4. Health Check URLs
- Portfolio: http://localhost:8080/health/live
- Transactions: http://localhost:8082/health/live
- Users: http://localhost:8081/health/live

## 5. Troubleshooting
Check service logs:


bash
kubectl logs -f deployment/portfolio-service
kubectl logs -f deployment/transactions-service
kubectl logs -f deployment/users-service



## 6. Cleanup
Cleanup
bash
kubectl delete -f k8s/deployment.yaml