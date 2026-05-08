---
description: >
  AWS 부하테스트 한 사이클 실행. 인자로 받은 k6 시나리오 파일을 SUT(m5.2xlarge) + loadgen(c5.4xlarge) 위에서 돌리고,
  k6 가 정상 시작된 것을 확인하면 대시보드 URL 4개를 출력하고 즉시 종료한다 (테스트 완료를 기다리지 않는다).
  Docker 이미지는 콘텐츠 해시 태그 + Hub 존재 체크로 변경 없을 때 빌드/푸시를 통째로 스킵한다.
arguments: [scenario]
---

`loadtest/k6/scenarios/<scenario>` 를 SUT 와 loadgen 인스턴스에서 한 번 돌리고 **부하가 정상 시작된 것만 확인하고 종료**한다. 테스트 완료를 기다리지 않는다 — 결과는 사용자가 살아있는 인스턴스에 SSH 해서 보거나 k6 web dashboard 로 본다.

인스턴스는 destroy 하지 않는다. 정리는 `/performance-test-clear`.

## 입력

- `$scenario` = `loadtest/k6/scenarios/` 안의 파일명 (예: `ticker_websocket.js`, `place_order.js`)

인자가 없거나 해당 파일이 없으면 사용자에게 사용 가능한 시나리오 목록을 보여주고 종료한다.

## 사전 점검

다음 중 하나라도 실패하면 사용자에게 알리고 중단한다.

| 항목 | 검증 |
|---|---|
| 시나리오 존재 | `test -f loadtest/k6/scenarios/$scenario` |
| Docker 데몬 | `docker info` 성공 |
| Docker Hub 인증 | `docker info` 출력에 `Username: kimsehee98` 포함 (없으면 `docker login` 안내) |
| AWS 인증 | `aws sts get-caller-identity` 성공 (region `ap-northeast-2`) |
| 테라폼 | `terraform -chdir=terraform init` 성공 |
| SSH 키 | `~/.ssh/trypto-key-pair.pem` 존재, 권한 0400 |
| 베이스 AMI | `aws ec2 describe-images --owners self --filters "Name=tag:Project,Values=trypto-loadtest" --query 'Images[0].ImageId' --output text` 가 `None` 아님. 없으면 `cd packer && packer init . && packer build loadtest-base.pkr.hcl` 안내 후 중단 |

## 변수 / 경로 (스킬이 사용하는 고정값)

```
SSH_KEY        = ~/.ssh/trypto-key-pair.pem
SSH_USER       = ec2-user
RESULTS_DIR    = loadtest/results/             (없으면 생성)
ENV_FILE       = .env                          (이미지 태그를 여기에 기록)
SUT_SG         = sg-0b382d035f1af8797
LOADGEN_SG     = sg-0acffea9e1179d5a0
DASHBOARD_PORTS_SUT      = 3000 9091 15672      (Grafana / Prometheus / RabbitMQ)
DASHBOARD_PORTS_LOADGEN  = 5665                  (k6 web dashboard)
```

## 흐름

### 1. 시나리오 검증

```bash
test -f loadtest/k6/scenarios/$scenario
```

없으면 `loadtest/k6/scenarios/` ls 결과를 보여주고 종료.

### 2. Docker 이미지 — 콘텐츠 해시 태그 + Hub 존재 체크

빌드 입력 파일들의 SHA256 해시를 만들고 `lt-<H12>` 태그로 쓴다. Hub 에 그 태그 이미지가 이미 있으면 빌드/푸시를 통째로 스킵하고 `.env` 만 갱신한다.

```bash
# 빌드 입력 = 각 모듈 src + Dockerfile + build.gradle + 루트 gradle 자산
hash_inputs() {
  local module=$1
  find "$module/src" "$module/Dockerfile" "$module/build.gradle" \
       "settings.gradle" "gradlew" "gradle/" \
       -type f 2>/dev/null | sort | xargs sha256sum | sha256sum | cut -c1-12
}

API_H=$(hash_inputs api)
COL_H=$(hash_inputs collector)
ENG_H=$(hash_inputs engine)

API_TAG="lt-$API_H"
COL_TAG="lt-$COL_H"
ENG_TAG="lt-$ENG_H"

# 항상 .env 는 새 태그로 덮어쓴다 (Hub 존재 여부와 무관하게 SUT 가 pull 할 태그를 명시)
{
  echo "COLLECTOR_IMAGE=kimsehee98/trypto-collector:$COL_TAG"
  echo "API_IMAGE=kimsehee98/trypto-api:$API_TAG"
  echo "ENGINE_IMAGE=kimsehee98/trypto-engine:$ENG_TAG"
} > .env

# Hub 에 이미 있는지 확인. 모두 있으면 빌드/푸시 통째 스킵.
need_build_api=1; need_build_col=1; need_build_eng=1
docker manifest inspect "kimsehee98/trypto-api:$API_TAG"      >/dev/null 2>&1 && need_build_api=0
docker manifest inspect "kimsehee98/trypto-collector:$COL_TAG" >/dev/null 2>&1 && need_build_col=0
docker manifest inspect "kimsehee98/trypto-engine:$ENG_TAG"    >/dev/null 2>&1 && need_build_eng=0

if [ $need_build_api -eq 0 ] && [ $need_build_col -eq 0 ] && [ $need_build_eng -eq 0 ]; then
  echo "[skip] 모든 이미지가 Hub 에 이미 존재. 빌드/푸시 생략."
else
  export DOCKER_BUILDKIT=1
  export COMPOSE_DOCKER_CLI_BUILD=1
  # docker compose build 는 변경 모듈만 알아서 빌드 (Dockerfile 의 cache mount + layered jar 로 변경분만 layer 변경)
  docker compose build collector backend engine

  # 병렬 푸시: 이미 Hub 에 있는 건 docker push 가 layer 단위로 알아서 dedup 함
  pids=()
  [ $need_build_api -eq 1 ] && (docker push "kimsehee98/trypto-api:$API_TAG"      ) & pids+=($!)
  [ $need_build_col -eq 1 ] && (docker push "kimsehee98/trypto-collector:$COL_TAG") & pids+=($!)
  [ $need_build_eng -eq 1 ] && (docker push "kimsehee98/trypto-engine:$ENG_TAG"   ) & pids+=($!)
  for p in "${pids[@]}"; do wait "$p" || { echo "[ERROR] push 실패"; exit 1; }; done
fi
```

해시 입력에 변경이 없으면 — 같은 코드로 R0/R1/R2 회차를 돌릴 때 — 이 단계는 5초 안에 끝난다.
한 줄만 고쳐도 layered jar 덕분에 application layer (~5MB) 만 다시 push 되어 1~2분.

### 3. 인스턴스 준비 — terraform apply

state 가 비어있으면 새로 띄우고, 이미 running 이면 그대로 쓴다. spot 인스턴스라 stop/start 는 없다 — 회차 사이엔 항상 destroy 후 다시 apply.

```bash
terraform -chdir=terraform init -upgrade
terraform -chdir=terraform apply -auto-approve

SUT_IP=$(terraform -chdir=terraform output -raw sut_public_ip)
SUT_PRIVATE_IP=$(terraform -chdir=terraform output -raw sut_private_ip)
LOADGEN_IP=$(terraform -chdir=terraform output -raw loadgen_public_ip)
```

⚠️ **공인 IP vs 사설 IP**: SUT/loadgen 은 같은 VPC 의 서로 다른 서브넷에 있다. loadgen 이 SUT 의 공인 IP 로 트래픽을 보내면 인터넷 게이트웨이로 나갔다 들어오면서 source IP 가 NAT 되어 SUT SG 의 sg-ref(`sg-0acffea9...` 허용) 규칙이 매칭되지 않고 막힌다. 따라서 **k6 가 호출할 SUT 주소는 반드시 사설 IP** 를 쓴다. 공인 IP 는 사용자가 브라우저로 대시보드 띄울 때만 쓴다.

### 4. 현재 공인 IP 를 SG 에 등록 (SSH + 대시보드 포트 동시 처리)

⚠️ **중요**: SG 의 22번과 대시보드 포트(3000/9091/15672/5665) 가 사용자 IP CIDR 만 허용하도록 잠겨있다. ISP DHCP 가 IP 를 갱신했거나 다른 네트워크에서 호출하면 SSH/대시보드 둘 다 막힌다.

```bash
MY_IP=$(curl -s https://checkip.amazonaws.com)
CIDR="$MY_IP/32"

ensure_ingress() {
  local sg=$1 port=$2
  local existing
  existing=$(aws ec2 describe-security-groups --region ap-northeast-2 --group-ids "$sg" \
    --query "SecurityGroups[0].IpPermissions[?FromPort==\`$port\` && ToPort==\`$port\` && contains(IpRanges[].CidrIp, '$CIDR')]" --output text)
  if [ -z "$existing" ]; then
    aws ec2 authorize-security-group-ingress --region ap-northeast-2 \
      --group-id "$sg" --protocol tcp --port "$port" --cidr "$CIDR" >/dev/null
  fi
}

# SUT: SSH + 대시보드 포트
for p in 22 3000 9091 15672; do ensure_ingress sg-0b382d035f1af8797 $p; done
# loadgen: SSH + k6 대시보드
for p in 22 5665; do ensure_ingress sg-0acffea9e1179d5a0 $p; done

# loadgen SG → SUT SG 의 9091 (Prometheus remote-write) ingress 보장
# k6 가 자기 메트릭(e2e latency / 끊김 카운터 등) 을 SUT 의 prometheus 로 푸시함.
ensure_sg_ingress() {
  local sg=$1 port=$2 src_sg=$3
  local existing
  existing=$(aws ec2 describe-security-groups --region ap-northeast-2 --group-ids "$sg" \
    --query "SecurityGroups[0].IpPermissions[?FromPort==\`$port\` && ToPort==\`$port\` && contains(UserIdGroupPairs[].GroupId, '$src_sg')]" --output text)
  if [ -z "$existing" ]; then
    aws ec2 authorize-security-group-ingress --region ap-northeast-2 \
      --group-id "$sg" --protocol tcp --port "$port" --source-group "$src_sg" >/dev/null
  fi
}
ensure_sg_ingress sg-0b382d035f1af8797 9091 sg-0acffea9e1179d5a0
```

추가한 규칙은 `/performance-test-clear` 가 정리하지 않는다. SG 가 너저분해지면 사용자가 콘솔에서 정리.

### 5. SSH + bootstrap 완료 대기

user_data 종료 마커(`/var/log/trypto-bootstrap-done`) 가 생길 때까지 polling.

```bash
SSH_OPTS="-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o ConnectTimeout=5 -i ~/.ssh/trypto-key-pair.pem"

for host in $SUT_IP $LOADGEN_IP; do
  deadline=$(( $(date +%s) + 600 ))
  until ssh $SSH_OPTS ec2-user@$host "test -f /var/log/trypto-bootstrap-done" 2>/dev/null; do
    [ "$(date +%s)" -gt "$deadline" ] && { echo "[ERROR] $host bootstrap timeout"; exit 1; }
    sleep 5
  done
done
```

### 6. SUT 로 compose 자산 동기화

```bash
rsync -e "ssh $SSH_OPTS" -avz --delete \
  docker-compose.yml docker-compose.loadtest.yml docker/ .env \
  ec2-user@$SUT_IP:~/trypto/
```

### 7. SUT compose up + healthy 대기

`reset.sh` 가 시나리오 인자를 받아서 자동으로 적절한 compose 파일들을 같이 적용한다 (예: `ticker_websocket.js` 면 `-f docker-compose.loadtest.yml` 자동 추가하여 collector 가 처음부터 loadtest 프로파일로 뜸). 그 다음 healthy 까지 기다린다.

```bash
rsync -e "ssh $SSH_OPTS" -avz loadtest/reset.sh ec2-user@$SUT_IP:~/trypto/loadtest/
ssh $SSH_OPTS ec2-user@$SUT_IP "cd ~/trypto && bash loadtest/reset.sh $scenario"
```

### 8. 시나리오에 따른 SUT 측 사전 작업

reset.sh 가 compose 오버라이드는 알아서 처리하므로, 이 단계는 시나리오에 따라 추가로 필요한 데이터/요청 만 처리한다.

| 시나리오 | 추가 작업 |
|---|---|
| `ticker_websocket.js` | collector 에 ramp 프로파일 POST. |
| `place_order.js`, `match_pending*.js` | `loadtest/loadtest.sql` 을 mysql 에 적재 (지갑 1000개, 코인 10개 등 시드). |
| 그 외 | 시나리오 상단 주석을 보고 사용자에게 확인받는다. |

`ticker_websocket` 의 ramp profile POST 예 (10분 ramp → 10분 sustain → 5분 ramp down):

```bash
ssh $SSH_OPTS ec2-user@$SUT_IP 'curl -s -X POST http://localhost:8081/loadtest/ticker/ramp \
  -H "Content-Type: application/json" \
  -d "{\"phases\":[
    {\"durationSeconds\":600,\"toRates\":{\"UPBIT\":217,\"BITHUMB\":12,\"BINANCE\":12}},
    {\"durationSeconds\":600,\"toRates\":{\"UPBIT\":217,\"BITHUMB\":12,\"BINANCE\":12}},
    {\"durationSeconds\":300,\"toRates\":{\"UPBIT\":0,\"BITHUMB\":0,\"BINANCE\":0}}
  ]}"'
```

### 9. loadgen 으로 k6 자산 동기화

```bash
rsync -e "ssh $SSH_OPTS" -avz --delete \
  loadtest/k6/ ec2-user@$LOADGEN_IP:~/k6/
```

### 10. 시나리오별 환경변수

k6 가 SUT 에 보내는 호스트는 모두 **사설 IP**(`$SUT_PRIVATE_IP`) 를 쓴다 — 이유는 step 3 의 경고 참고.

| 시나리오 | env |
|---|---|
| `ticker_websocket.js` | `API_HOST=$SUT_PRIVATE_IP:8080`, `COLLECTOR_HOST=$SUT_PRIVATE_IP:8081` |
| `place_order.js`, `match_pending*.js` | `API_TARGET=http://$SUT_PRIVATE_IP:8080` |

### 11. k6 detached 실행 (fire-and-forget)

k6 컨테이너를 `-d` 로 띄우고 즉시 빠져나온다. **파일을 어디에도 남기지 않는다** — live dashboard(5665) 만 띄우고, stdout 은 docker 로그로만 본다 (`docker logs -f`).

```bash
TS=$(date +%Y%m%d-%H%M%S)
CONTAINER_NAME="k6-$scenario-$TS"

case "$scenario" in
  ticker_websocket.js)
    SCENARIO_ENV="-e API_HOST=$SUT_PRIVATE_IP:8080 -e COLLECTOR_HOST=$SUT_PRIVATE_IP:8081"
    ;;
  place_order.js|match_pending*.js)
    SCENARIO_ENV="-e API_TARGET=http://$SUT_PRIVATE_IP:8080"
    ;;
  *)
    SCENARIO_ENV=""
    ;;
esac

# k6 커스텀 메트릭(message_e2e_latency / stomp_disconnects 등) 을 SUT 의 prometheus 로 push.
# k6 web dashboard 에는 패널이 없는 메트릭들이라 grafana 에서 봐야 한다.
# native histogram 으로 보내면 trend 메트릭의 percentile 이 prometheus 에서 직접 계산됨.
PROMETHEUS_PUSH_ENV="-e K6_PROMETHEUS_RW_SERVER_URL=http://$SUT_PRIVATE_IP:9091/api/v1/write -e K6_PROMETHEUS_RW_TREND_AS_NATIVE_HISTOGRAM=true"

ssh $SSH_OPTS ec2-user@$LOADGEN_IP "
  docker run -d --name $CONTAINER_NAME \
    -p 5665:5665 \
    -v ~/k6:/scripts:ro \
    -e K6_WEB_DASHBOARD=true \
    -e K6_WEB_DASHBOARD_HOST=0.0.0.0 \
    -e K6_WEB_DASHBOARD_PORT=5665 \
    $SCENARIO_ENV \
    $PROMETHEUS_PUSH_ENV \
    grafana/k6:latest run -o experimental-prometheus-rw /scripts/scenarios/$scenario
"
```

### 12. 부하 시작 검증 + 대시보드 URL 헬스체크

k6 컨테이너가 실제 떠있고 5665 가 바인딩됐는지, SUT 의 Grafana/Prometheus/RabbitMQ 가 응답하는지 확인. 못 닿는 URL 은 ❌ 표시.

```bash
# k6 컨테이너 상태
sleep 5
RUNNING=$(ssh $SSH_OPTS ec2-user@$LOADGEN_IP "docker ps -q -f name=$CONTAINER_NAME")
[ -z "$RUNNING" ] && { echo "[ERROR] k6 컨테이너가 시작 직후 죽었다"; \
  ssh $SSH_OPTS ec2-user@$LOADGEN_IP "docker logs $CONTAINER_NAME"; exit 1; }

check_url() {
  local url=$1 label=$2
  if curl -fsS -m 5 -o /dev/null "$url" 2>/dev/null; then
    echo "  ✓ $label  $url"
  else
    echo "  ✗ $label  $url  (아직 응답 없음 — 1~2분 후 다시 시도)"
  fi
}

echo ""
echo "🟢 부하테스트 시작 (시나리오: $scenario, container: $CONTAINER_NAME)"
echo ""
echo "▶ 실시간 대시보드"
check_url "http://$LOADGEN_IP:5665"           "k6 Web Dashboard       "
check_url "http://$SUT_IP:3000/api/health"    "Grafana                "
check_url "http://$SUT_IP:9091/-/ready"       "Prometheus             "
check_url "http://$SUT_IP:15672"              "RabbitMQ Management    "
echo ""
echo "  Grafana 로그인: admin / admin"
echo "  RabbitMQ 로그인: guest / guest"
echo ""
echo "▶ SSH / 로그"
echo "  SUT       ssh -i ~/.ssh/trypto-key-pair.pem ec2-user@$SUT_IP"
echo "  loadgen   ssh -i ~/.ssh/trypto-key-pair.pem ec2-user@$LOADGEN_IP"
echo "  k6 stdout ssh ... ec2-user@$LOADGEN_IP 'docker logs -f $CONTAINER_NAME'"
echo ""
echo "끝나면 /performance-test-clear 로 인스턴스 정리."
```

여기서 스킬은 **종료**한다. 테스트 완료를 기다리지 않는다.

## 실패 처리

| 단계 | 실패 시 |
|---|---|
| 사전 점검 | 즉시 중단, 사용자에게 어디서 막혔는지 한 줄로 보고 |
| 빌드/푸시 | 빌드 로그 마지막 30줄 보여주고 중단 |
| terraform apply | terraform 출력 그대로 |
| SUT bootstrap timeout | `ssh ec2-user@$SUT_IP "sudo cat /var/log/cloud-init-output.log \| tail -100"` 로 user_data 로그 확인 |
| reset.sh healthy 대기 실패 | SUT 에 ssh 해서 `docker compose ps` 와 unhealthy 컨테이너 로그 회수 |
| k6 컨테이너 5초 안에 죽음 | `docker logs $CONTAINER_NAME` 출력 후 중단 |
| 대시보드 헬스체크 일부 실패 | 중단하지 않고 ❌ 표시만. 사용자가 1~2분 후 직접 재시도 |

destructive 한 자동 복구는 시도하지 않는다.

## 비용 인지

- m5.2xlarge spot: ~$0.13/h
- c5.4xlarge spot: ~$0.27/h
- 합계: ~$0.40/h (use_spot=true 기준). 다 끝났으면 `/performance-test-clear`.

## 산출물

스킬은 **어디에도 결과 파일을 남기지 않는다**. 메트릭은 살아있는 동안의 4개 대시보드(k6 web 5665 / Grafana / Prometheus / RabbitMQ) 로만 본다. 인스턴스가 destroy 되면 다 사라짐 — 그게 의도다.
