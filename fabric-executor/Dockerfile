FROM ubuntu:14.04
MAINTAINER guruprasad.sridharan@olacabs.com

RUN apt-get update \
  && apt-get install -y --no-install-recommends software-properties-common \
  && add-apt-repository ppa:webupd8team/java \
  && gpg --keyserver hkp://keys.gnupg.net --recv-keys 409B6B1796C275462A1703113804BB82D39DC0E3 \
  && apt-get update \
  && echo debconf shared/accepted-oracle-license-v1-1 select true |  debconf-set-selections \
  && echo debconf shared/accepted-oracle-license-v1-1 seen true |  debconf-set-selections \
  && apt-get install -y --no-install-recommends oracle-java8-installer \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

EXPOSE 8080

ENV basedir /executor

COPY fabric-executor-*.jar $basedir/fabric-executor.jar

WORKDIR $basedir

RUN mkdir /var/log/fabric-executor

RUN touch /var/log/fabric-executor/stdout.log

RUN touch /var/log/fabric-executor/stderr.log

RUN ln -sf /dev/stdout /var/log/fabric-executor/stdout.log

RUN ln -sf /dev/stderr /var/log/fabric-executor/stderr.log

CMD ["sh", "-c", "java -Xmx${HEAP_SIZE} -Xms${HEAP_SIZE} -Dfile.encoding=utf-8 ${JVM_OPTS} -jar fabric-executor.jar -d ${METRICS_DISABLED} --spec ${SPEC_LOCATION} -m ${OPENTSDB_URL}"]

