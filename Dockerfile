# ── Stage 1: Pinpoint Agent 다운로드 ──────────────────────────────
FROM eclipse-temurin:17-jre AS agent-downloader
ARG PINPOINT_VERSION=2.5.4
RUN apt-get update -qq && apt-get install -y -qq wget ca-certificates && \
    wget -q "https://github.com/pinpoint-apm/pinpoint/releases/download/v${PINPOINT_VERSION}/pinpoint-agent-${PINPOINT_VERSION}.tar.gz" \
         -O /tmp/pinpoint-agent.tar.gz && \
    mkdir -p /pinpoint-agent && \
    tar -xzf /tmp/pinpoint-agent.tar.gz --strip-components=1 -C /pinpoint-agent

# ── Stage 2: 애플리케이션 이미지 ──────────────────────────────────
FROM eclipse-temurin:17-jre

COPY --from=agent-downloader /pinpoint-agent /pinpoint-agent

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# Pinpoint 설정 (docker-compose 또는 배포 환경에서 오버라이드 가능)
ENV PINPOINT_COLLECTOR_IP=pinpoint-collector
ENV PINPOINT_APP_NAME=MatchFit
ENV PINPOINT_AGENT_ID=matchfit-1

ENTRYPOINT java \
  -javaagent:/pinpoint-agent/pinpoint-bootstrap-2.5.4.jar \
  -Dpinpoint.agentId=${PINPOINT_AGENT_ID} \
  -Dpinpoint.applicationName=${PINPOINT_APP_NAME} \
  -Dprofiler.collector.ip=${PINPOINT_COLLECTOR_IP} \
  -Xms256m -Xmx512m -Xss512k \
  -XX:+UseContainerSupport \
  -XX:+UseG1GC -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/tmp/heapdump.hprof \
  -XX:+ExitOnOutOfMemoryError \
  -jar /app.jar
