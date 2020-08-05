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
- http://a37129e7032f641e69219a10e58e79d5-1497029974.ap-northeast-2.elb.amazonaws.com:8080/html/index.html

* 숙소 예약 
![p1](https://user-images.githubusercontent.com/66579960/89418970-c1bbbc00-d76b-11ea-91f6-600d3901244b.jpg)

```

```


* 예약 확인 (siege 에서)
```
http http://booking:8080/bookings/1
```

# 검증1) 동기식 호출 과 Fallback 처리
- 숙소 등록시 인증 서비스를 동기식으로 호출하고 있음.
- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 인증시스템이 장애가 나면 숙소 등록을 못받는다는 것을 확인

* 인증 서비스 중지
```
$ kubectl delete -f auth.yaml

(POD 상태)
NAME                           READY   STATUS    RESTARTS   AGE
pod/alarm-77cd9d57-fj6dm       2/2     Running   0          78m
pod/booking-7ffd5f9d75-lwhq8   2/2     Running   0          78m
pod/gateway-7885fb4994-whvw8   2/2     Running   0          79m
pod/html-6fbc78fb49-t7dd8      2/2     Running   0          78m
pod/mypage-595b95dbf5-x5xgt    2/2     Running   0          78m
pod/pay-6ddbb7d4dd-c847f       2/2     Running   0          78m
pod/review-7cfb746cbb-682ks    2/2     Running   0          78m
pod/room-6448f765fc-jsbgr      2/2     Running   0          78m
pod/siege                      2/2     Running   0          79m
```

* 숙소 등록 에러 확인 (siege 에서)
```
http POST http://room:8080/rooms name=호텔1 price=1000 address=서울 host=Superman
http POST http://room:8080/rooms name=펜션1 price=1000 address=양평 host=Superman

(처리에러)
HTTP/1.1 500 Internal Server Error
content-type: application/json;charset=UTF-8
date: Wed, 05 Aug 2020 12:33:03 GMT
server: envoy
transfer-encoding: chunked
x-envoy-upstream-service-time: 10042

{
    "error": "Internal Server Error",
    "message": "Could not commit JPA transaction; nested exception is javax.persistence.RollbackException: Error while committing the transaction",
    "path": "/rooms",
    "status": 500,
    "timestamp": "2020-08-05T12:33:03.761+0000"
}
```

* 인증 서비스 기동
```
$ kubectl apply -f auth.yaml

(POD 상태)
NAME                       READY   STATUS    RESTARTS   AGE
alarm-77cd9d57-fj6dm       2/2     Running   0          80m
auth-5d4c8cd986-7v4v8      2/2     Running   0          83s       <===
booking-7ffd5f9d75-lwhq8   2/2     Running   0          80m
gateway-7885fb4994-whvw8   2/2     Running   0          81m
html-6fbc78fb49-t7dd8      2/2     Running   0          81m
mypage-595b95dbf5-x5xgt    2/2     Running   0          80m
pay-6ddbb7d4dd-c847f       2/2     Running   0          80m
review-7cfb746cbb-682ks    2/2     Running   0          80m
room-6448f765fc-jsbgr      2/2     Running   0          80m
siege                      2/2     Running   0          81m
```

* 숙소 등록 성공 확인 (siege 에서)
```
http POST http://room:8080/rooms name=호텔2 price=1000 address=서울 host=Superman
http POST http://room:8080/rooms name=펜션2 price=1000 address=양평 host=Superman

(처리)
HTTP/1.1 201 Created
content-type: application/json;charset=UTF-8
date: Wed, 05 Aug 2020 12:36:58 GMT
location: http://room:8080/rooms/10
server: envoy
transfer-encoding: chunked
x-envoy-upstream-service-time: 57

{
    "_links": {
        "room": {
            "href": "http://room:8080/rooms/10"
        },
        "self": {
            "href": "http://room:8080/rooms/10"
        }
    },
    "address": "서울",
    "host": "Superman",
    "name": "호텔2",
    "price": 1000
}
```

# 검증2) 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

- 숙소가 등록된 후에 알림 처리는 동기식이 아니라 비동기식으로 처리하여 알림 시스템의 처리를 위하여 등록 블로킹 되지 않아도록 처리한다.

* 알림 서비스 중지
```
kubectl delete -f alarm.yaml

(POD 상태)
NAME                       READY   STATUS        RESTARTS   AGE
alarm-77cd9d57-fj6dm       2/2     Terminating   0          83m       <=====
auth-5d4c8cd986-7v4v8      2/2     Running       0          4m13s
booking-7ffd5f9d75-lwhq8   2/2     Running       0          83m
gateway-7885fb4994-whvw8   2/2     Running       0          83m
html-6fbc78fb49-t7dd8      2/2     Running       0          83m
mypage-595b95dbf5-x5xgt    2/2     Running       0          83m
pay-6ddbb7d4dd-c847f       2/2     Running       0          83m
review-7cfb746cbb-682ks    2/2     Running       0          83m
room-6448f765fc-jsbgr      2/2     Running       0          83m
siege                      2/2     Running       0          84m
```

* 알림 이력 조회 불가 확인 (siege 에서)
```
http http://alarm:8080/alarms

(처리에러)
http: error: ConnectionError: HTTPConnectionPool(host='alarm', port=8080): Max retries exceeded with url: /alarms (Caused by NewConnectionError('<urllib3.connection.HTTPConnection object at 0x7f7230913eb8>: Failed to establish a new connection: [Errno -2] Name or service not known')) while doing GET request to URL: http://alarm:8080/alarms
```

* 숙소 등록 성공 확인 (siege 에서)
```
http POST http://room:8080/rooms name=호텔3 price=1000 address=서울 host=Superman
http POST http://room:8080/rooms name=펜션3 price=1000 address=양평 host=Superman

(처리성공)
HTTP/1.1 201 Created
content-type: application/json;charset=UTF-8
date: Wed, 05 Aug 2020 12:39:25 GMT
location: http://room:8080/rooms/11
server: envoy
transfer-encoding: chunked
x-envoy-upstream-service-time: 54

{
    "_links": {
        "room": {
            "href": "http://room:8080/rooms/11"
        },
        "self": {
            "href": "http://room:8080/rooms/11"
        }
    },
    "address": "서울",
    "host": "Superman",
    "name": "호텔3",
    "price": 1000
}
```

* 알림 서비스 기동
```
kubectl apply -f alarm.yaml

(POD 상태)
NAME                       READY   STATUS    RESTARTS   AGE
alarm-77cd9d57-96r4k       1/2     Running   0          5s          <==================
auth-5d4c8cd986-7v4v8      2/2     Running   0          6m43s
booking-7ffd5f9d75-lwhq8   2/2     Running   0          86m
gateway-7885fb4994-whvw8   2/2     Running   0          86m
html-6fbc78fb49-t7dd8      2/2     Running   0          86m
mypage-595b95dbf5-x5xgt    2/2     Running   0          85m
pay-6ddbb7d4dd-c847f       2/2     Running   0          86m
review-7cfb746cbb-682ks    2/2     Running   0          85m
room-6448f765fc-jsbgr      2/2     Running   0          86m
siege                      2/2     Running   0          86m
```

* 알림 이력 조회 성송 확인 (siege 에서)
```
http http://alarm:8080/alarms 

(처리성공)
HTTP/1.1 200 OK
content-type: application/hal+json;charset=UTF-8
date: Wed, 05 Aug 2020 12:41:56 GMT
server: envoy
transfer-encoding: chunked
x-envoy-upstream-service-time: 390

{
    "_embedded": {
        "alarms": [
            {
                "_links": {
                    "alarm": {
                        "href": "http://alarm:8080/alarms/1"
                    },
                    "self": {
                        "href": "http://alarm:8080/alarms/1"
                    }
                },
                "message": "AuthApproved",
                "receiver": "(host)Superman"
            }
        ]
    },
    "_links": {
        "profile": {
            "href": "http://alarm:8080/profile/alarms"
        },
        "self": {
            "href": "http://alarm:8080/alarms{?page,size,sort}",
            "templated": true
        }
    },
    "page": {
        "number": 0,
        "size": 20,
        "totalElements": 1,
        "totalPages": 1
    }
}
```

# 검증3) 동기식 호출 / 서킷 브레이킹 / 장애격리
- 서킷 브레이킹 프레임워크의 선택: istio-injection + DestinationRule
- 숙소 등록시 동기로 호출되는 인증 서비스에 설정

* istio-injection 적용 (기 적용완료)
```
kubectl label namespace mybnb istio-injection=enabled
```

* 숙소등록 부하 발생 (siege 에서) - 동시사용자 10명, 60초 동안 실시
```
$ siege -v -c10 -t60S -r10 --content-type "application/json" 'http://room:8080/rooms POST {"name":"호텔4", "price":1000, "address":"서울", "host":"Superman"}'
```

* 서킷 브레이킹을 위한 DestinationRule 적용
```
$ cd ~/jihwancha/mybnb2/yaml
$ kubectl apply -f dr-auth.yaml
```

* 서킷 브레이킹 확인 (siege 에서)
```
HTTP/1.1 500     0.08 secs:     247 bytes ==> POST http://room:8080/rooms
HTTP/1.1 500     0.11 secs:     247 bytes ==> POST http://room:8080/rooms
HTTP/1.1 500     0.08 secs:     247 bytes ==> POST http://room:8080/rooms
HTTP/1.1 500     0.06 secs:     247 bytes ==> POST http://room:8080/rooms
...

Transactions:                    634 hits
Availability:                  38.03 %
Elapsed time:                  21.48 secs
Data transferred:               0.39 MB
Response time:                  0.34 secs
Transaction rate:              29.52 trans/sec
Throughput:                     0.02 MB/sec
Concurrency:                    9.96
Successful transactions:         634
Failed transactions:            1033
Longest transaction:            0.54
Shortest transaction:           0.01
```

* 서킷 브레이킹 확인 (kiali 화면)
![cb](https://user-images.githubusercontent.com/61722732/89416771-b915b680-d768-11ea-9560-b242e617c245.PNG)


* 서킷 브레이킹을 위한 DestinationRule 제거
```
$ cd ~/jihwancha/mybnb2/yaml
$ kubectl delete -f dr-auth.yaml
```

* 숙소등록 부하 발생 (siege 에서) - 동시사용자 10명, 60초 동안 실시
```
$ siege -v -c10 -t60S -r10 --content-type "application/json" 'http://room:8080/rooms POST {"name":"호텔5", "price":1000, "address":"서울", "host":"Superman"}'
```

* 정상 동작 확인 (siege 에서)
```
...

Transactions:                   5306 hits
Availability:                 100.00 %                <=================
Elapsed time:                  59.05 secs
Data transferred:               1.22 MB
Response time:                  0.11 secs
Transaction rate:              89.86 trans/sec
Throughput:                     0.02 MB/sec
Concurrency:                    9.93
Successful transactions:        5306
Failed transactions:               0
Longest transaction:            0.48
Shortest transaction:           0.01
```


# 검증4) 오토스케일 아웃
- 인증 서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 3개까지 늘려준다:

* HPA 적용
```
$ kubectl autoscale deploy auth -n mybnb --min=1 --max=3 --cpu-percent=15
$ kubectl get deploy auth -n mybnb -w 
```

* 숙소등록 부하 발생 (siege 에서) - 동시사용자 10명, 180초 동안 실시
```
$ siege -v -c10 -t180S -r10 --content-type "application/json" 'http://room:8080/rooms POST {"name":"호텔6", "price":1000, "address":"서울", "host":"Superman"}'
```

* 정상 동작 확인 (siege 에서)
```
...
Transactions:                  20932 hits
Availability:                 100.00 %             <==========================
Elapsed time:                 179.13 secs
Data transferred:               4.87 MB
Response time:                  0.08 secs
Transaction rate:             116.85 trans/sec
Throughput:                     0.03 MB/sec
Concurrency:                    9.77
Successful transactions:       20932
Failed transactions:               0
Longest transaction:            3.69
Shortest transaction:           0.00
```

* 스케일 아웃 확인
```
NAME   READY   UP-TO-DATE   AVAILABLE   AGE
auth   1/1     1            1           15m
auth   1/3     1            1           16m
auth   1/3     1            1           16m
auth   1/3     1            1           16m
auth   1/3     3            1           16m
auth   2/3     3            2           18m
auth   3/3     3            3           18m
```


# 검증5) 무정지 재배포

* 리뷰조회 부하 발생 (siege 에서) - 동시사용자 1명, 300초 동안 실시
```
siege -v -c1 -t300S -r10 --content-type "application/json" 'http://review:8080/reviews'
```

* 리뷰 이미지 update (readness, liveness 미설정 상태)
```
$ kubectl apply -f review_na.yaml

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

* 에러 발생 확인 (siege 에서)
```
Transactions:                   4700 hits
Availability:                  82.11 %              <=======================
Elapsed time:                  31.22 secs
Data transferred:               3.39 MB
Response time:                  0.01 secs
Transaction rate:             150.54 trans/sec
Throughput:                     0.11 MB/sec
Concurrency:                    0.96
Successful transactions:        4700
Failed transactions:            1024
Longest transaction:            0.08
Shortest transaction:           0.00
```

* 리뷰조회 부하 발생 (siege 에서) - 동시사용자 1명, 300초 동안 실시
```
siege -v -c1 -t300S -r10 --content-type "application/json" 'http://review:8080/reviews'
```

* 리뷰 이미지 update (readness, liveness 설정 상태)
```
$ kubectl apply -f review.yaml

(POD 상태)
NAME                       READY   STATUS    RESTARTS   AGE
alarm-77cd9d57-96r4k       2/2     Running   0          15m
auth-5d4c8cd986-7v4v8      2/2     Running   0          22m
auth-5d4c8cd986-9vtht      2/2     Running   0          5m15s
auth-5d4c8cd986-lmdp2      2/2     Running   0          5m15s
booking-7ffd5f9d75-lwhq8   2/2     Running   0          101m
gateway-7885fb4994-whvw8   2/2     Running   0          101m
html-6fbc78fb49-t7dd8      2/2     Running   0          101m
mypage-595b95dbf5-x5xgt    2/2     Running   0          101m
pay-6ddbb7d4dd-c847f       2/2     Running   0          101m
review-69957988c6-4sdfd    2/2     Running   0          108s
review-7cfb746cbb-ttv8s    1/2     Running   0          11s       <=========
room-6448f765fc-jsbgr      2/2     Running   0          101m
siege                      2/2     Running   0          102m
```

* 정상 실행 확인 (siege 에서)
```
...
Transactions:                  23586 hits
Availability:                 100.00 %              <======================
Elapsed time:                  98.46 secs
Data transferred:               7.85 MB
Response time:                  0.00 secs
Transaction rate:             239.55 trans/sec
Throughput:                     0.08 MB/sec
Concurrency:                    0.96
Successful transactions:       23586
Failed transactions:               0
Longest transaction:            0.72
Shortest transaction:           0.00
```


