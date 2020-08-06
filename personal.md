# 모델링

* 이벤트스토밍
![modeling](https://user-images.githubusercontent.com/66579960/89418966-c08a8f00-d76b-11ea-8343-feec7b4cf444.jpg)


# 환경구성

* EKS Cluster create
```
$ eksctl create cluster --name skccuer10-Cluster --version 1.15 --nodegroup-name standard-workers --node-type t3.medium --nodes 3 --nodes-min 1 --nodes-max 4
```

* EKS Cluster settings
```
$ aws eks --region ap-northeast-2 update-kubeconfig --name skccuer10-Cluster
$ kubectl config current-context
$ kubectl get all
```

* ECR 인증
```
$ aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com
```

* Metric Server 설치
```
$ kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.3.6/components.yaml
$ kubectl get deployment metrics-server -n kube-system
```

* Kafka install (kubernetes/helm)
```
$ curl https://raw.githubusercontent.com/kubernetes/helm/master/scripts/get | bash
$ kubectl --namespace kube-system create sa tiller      
$ kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller
$ helm init --service-account tiller
$ kubectl patch deploy --namespace kube-system tiller-deploy -p '{"spec":{"template":{"spec":{"serviceAccount":"tiller"}}}}'
$ helm repo add incubator http://storage.googleapis.com/kubernetes-charts-incubator
$ helm repo update
$ helm install --name my-kafka --namespace kafka incubator/kafka
$ kubectl get all -n kafka
```

* Istio 설치
```
$ curl -L https://git.io/getLatestIstio | ISTIO_VERSION=1.4.5 sh -
$ cd istio-1.4.5
$ export PATH=$PWD/bin:$PATH
$ for i in install/kubernetes/helm/istio-init/files/crd*yaml; do kubectl apply -f $i; done
$ kubectl apply -f install/kubernetes/istio-demo.yaml
$ kubectl get pod -n istio-system
```

* Kiali 설정
```
$ kubectl edit service/kiali -n istio-system

- type 변경 : ClusterIP -> LoadBalancer
- (접속주소) http://http://ac5885beaca174095bad6d5f5779a443-1156063200.ap-northeast-2.elb.amazonaws.com:20001/kiali
```

* Namespace 생성
```
$ kubectl create namespace mybnb
```

* Namespace istio enabled
```
$ kubectl label namespace mybnb istio-injection=enabled 

- (설정해제 : kubectl label namespace mybnb istio-injection=disabled --overwrite)
```

* siege deploy
```
cd mybnb/yaml
kubectl apply -f siege.yaml 
kubectl exec -it siege -n mybnb -- /bin/bash
apt-get update
apt-get install httpie
```

```
apiVersion: v1
kind: Pod
metadata:
  name: siege
  namespace: mybnb
spec:
  containers:
    - name: siege
      image: apexacme/siege-nginx
```
# Build & Deploy

* ECR image repository
```
$ aws ecr create-repository --repository-name user10-mybnb-gateway --region ap-northeast-2
$ aws ecr create-repository --repository-name user10-mybnb-room --region ap-northeast-2
$ aws ecr create-repository --repository-name user10-mybnb-booking --region ap-northeast-2
$ aws ecr create-repository --repository-name user10-mybnb-pay --region ap-northeast-2
$ aws ecr create-repository --repository-name user10-mybnb-mypage --region ap-northeast-2
$ aws ecr create-repository --repository-name user10-mybnb-alarm --region ap-northeast-2
$ aws ecr create-repository --repository-name user10-mybnb-html --region ap-northeast-2
$ aws ecr create-repository --repository-name user10-mybnb-commission --region ap-northeast-2
```

* image build & push
```
$ cd gateway
$ mvn package
$ docker build -t 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user10-mybnb-gateway:latest .
$ docker push 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user10-mybnb-gateway:latest

$ cd ../room
$ mvn package
$ docker build -t 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user10-mybnb-room:latest .
$ docker push 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user10-mybnb-room:latest

$ cd ../booking
$ mvn package
$ docker build -t 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user10-mybnb-booking:latest .
$ docker push 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user10-mybnb-booking:latest

$ cd ../pay
$ mvn package
$ docker build -t 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user10-mybnb-pay:latest .
$ docker push 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user10-mybnb-pay:latest

$ cd ../mypage
$ mvn package
$ docker build -t 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user10-mybnb-mypage:latest .
$ docker push 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user10-mybnb-mypage:latest

$ cd ../alarm
$ mvn package
$ docker build -t 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user10-mybnb-alarm:latest .
$ docker push 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user10-mybnb-alarm:latest

$ cd ../html
$ mvn package
$ docker build -t 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user10-mybnb-html:latest .
$ docker push 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user10-mybnb-html:latest

$ cd ../commission
$ mvn package
$ docker build -t 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user10-mybnb-commission:latest .
$ docker push 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/user10-mybnb-commission:latest
```

* Deploy
```
$ kubectl apply -f siege.yaml
$ kubectl apply -f configmap.yaml
$ kubectl apply -f gateway.yaml
$ kubectl apply -f html.yaml
$ kubectl apply -f room.yaml
$ kubectl apply -f booking.yaml
$ kubectl apply -f pay.yaml
$ kubectl apply -f mypage.yaml
$ kubectl apply -f alarm.yaml
$ kubectl apply -f commission.yaml
```

# 사전 검증

* 화면
- http://a53e98cf040714291aee2a9a41b17b39-1458229494.ap-northeast-2.elb.amazonaws.com:8080/html/index.html

* 숙소 예약 
```
```
![p1](https://user-images.githubusercontent.com/66579960/89418970-c1bbbc00-d76b-11ea-91f6-600d3901244b.jpg)

* 결과 확인
![p2](https://user-images.githubusercontent.com/66579960/89418971-c1bbbc00-d76b-11ea-86f5-16db6adccc02.jpg)
![p5](https://user-images.githubusercontent.com/66579960/89418975-c2ece900-d76b-11ea-863e-b41a1ea5c5aa.jpg)
```
```
# 검증1) 동기식 호출 과 Fallback 처리
- 예약 시 결제 서비스(동기)에 이어 수수료 서비스를 동기식으로 호출하고 있음.
- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 수수료 시스템이 장애가 나면 예약 등록을 못받는다는 것을 확인

* 수수료 서비스 중지
```
$ kubectl delete -f commission.yaml
```
![p21](https://user-images.githubusercontent.com/66579960/89478950-82718780-d7cc-11ea-8f96-8f4030c855ef.jpg)
```
```
* 예약 에러 확인
![p22](https://user-images.githubusercontent.com/66579960/89478951-830a1e00-d7cc-11ea-9452-47d355781fc3.jpg)
![p23](https://user-images.githubusercontent.com/66579960/89478955-83a2b480-d7cc-11ea-9ad3-a402edbe4fa0.jpg)
```
```

* 수수료 서비스 기동
```
$ kubectl apply -f comission.yaml
```

* 예약 성공 확인
![p29](https://user-images.githubusercontent.com/66579960/89479159-0f1c4580-d7cd-11ea-9395-3f0456e64a8f.jpg)
```
```
# 검증2) 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

- 숙소가 예약된 후 취소처리 시 수수료 취소는 비동기식

* 수수료 서비스 중지
```
kubectl delete -f comission.yaml
```

* 예약 정상 취소
![p24](https://user-images.githubusercontent.com/66579960/89478956-843b4b00-d7cc-11ea-899d-7067a7fb7bf8.jpg)
![p25](https://user-images.githubusercontent.com/66579960/89478957-843b4b00-d7cc-11ea-874a-d7c1bf04562e.jpg)
![p26](https://user-images.githubusercontent.com/66579960/89478959-84d3e180-d7cc-11ea-9a98-936b68057ed1.jpg)
```
```

* 수수료 서비스 기동
```
$ kubectl apply -f commission.yaml
```
* 수수료 취소 이력 조회
![p28](https://user-images.githubusercontent.com/66579960/89478961-856c7800-d7cc-11ea-8c4e-21e9fe3098e7.jpg)
```
```

# 검증3) 동기식 호출 / 서킷 브레이킹 / 장애격리
- 서킷 브레이킹 프레임워크의 선택: istio-injection + DestinationRule
- 숙소 등록시 동기로 호출되는 인증 서비스에 설정

* istio-injection 적용 (기 적용완료)
```
kubectl label namespace mybnb istio-injection=enabled
```

* 수수료 부하 발생 (siege 에서) - 동시사용자 100명, 60초 동안 실시
```
$ siege -v -c100 -t60S -r10 --content-type "application/json" 'http://commission:8080/commissions/1 PUT {"bookId":19, "payId":"7", "price":1000, "charge":"10", "status":"CommissionPayed"}'
```

* 서킷 브레이킹을 위한 DestinationRule 적용
```
$ kubectl apply -f dr-commission.yaml
```

* 서킷 브레이킹 확인 (siege 에서)
![p13](https://user-images.githubusercontent.com/66579960/89419003-c97b6080-d76b-11ea-8d94-edde8f11d08e.jpg)
```
```


# 검증4) 오토스케일 아웃
- 인증 서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 3개까지 늘려준다:

* HPA 적용
```
$ kubectl autoscale deploy commission -n mybnb --min=1 --max=3 --cpu-percent=15
$ kubectl get deploy auth -n mybnb -w 
```

* 수수료 부하 발생 (siege 에서) - 동시사용자 100명, 180초 동안 실시
```
$ siege -v -c100 -t180S -r10 --content-type "application/json" 'http://commission:8080/commissions/1 PUT {"bookId":19, "payId":"7", "price":1000, "charge":"10", "status":"CommissionPayed"}'
```
![p14](https://user-images.githubusercontent.com/66579960/89419004-ca13f700-d76b-11ea-857b-bf39c10c561d.jpg)
```
```

* 스케일 아웃 확인
![p16](https://user-images.githubusercontent.com/66579960/89419009-caac8d80-d76b-11ea-84fa-29c6f8bc8f67.jpg)
```
```

# 검증5) 무정지 재배포

* 수수료조회 부하 발생 (siege 에서) - 동시사용자 1명, 300초 동안 실시
```
siege -v -c100 -t180S -r10 --content-type "application/json" 'http://commission:8080/commissions/1 PUT {"bookId":19, "payId":"7", "price":1000, "charge":"10", "status":"CommissionPayed"}'
```

* 수수료 이미지 update (readness, liveness 미설정 상태)
```
$ kubectl apply -f comission-na.yaml

(POD 상태)
NAME                       READY   STATUS        RESTARTS   AGE
alarm-77cd9d57-96r4k       2/2     Running       0          13m
auth-5d4c8cd986-7v4v8      2/2     Running       0          20m
auth-5d4c8cd986-9vtht      2/2     Running       0          3m33s
auth-5d4c8cd986-lmdp2      2/2     Running       0          3m33s
booking-7ffd5f9d75-lwhq8   2/2     Running       0          99m
gateway-7885fb4994-whvw8   2/2     Running       0          100m
html-6fbc78fb49-t7dd8      2/2     Running       0          100m
mypage-595b95dbf5-x5xgt    2/2     Running       0          99m
pay-6ddbb7d4dd-c847f       2/2     Running       0          99m
review-69957988c6-4sdfd    2/2     Running       0          6s               <=============
review-7cfb746cbb-682ks    2/2     Terminating   0          99m
room-6448f765fc-jsbgr      2/2     Running       0          99m
siege                      2/2     Running       0          100m
```

* 에러 발생 확인
![p19](https://user-images.githubusercontent.com/66579960/89477450-40dedd80-d7c8-11ea-9e2c-9729aff1eb61.jpg)
```
```

* 수수료조회 부하 발생 (siege 에서) - 동시사용자 1명, 300초 동안 실시
```
siege -v -c100 -t180S -r10 --content-type "application/json" 'http://commission:8080/commissions/1 PUT {"bookId":19, "payId":"7", "price":1000, "charge":"10", "status":"CommissionPayed"}'
```

* 수수료 이미지 update (readness, liveness 설정 상태)
```
$ kubectl apply -f commission.yaml
```

* 정상 실행 확인 (siege 에서)

![p18](https://user-images.githubusercontent.com/66579960/89477448-3fadb080-d7c8-11ea-975c-cc20a25e3a21.jpg)
```
```
