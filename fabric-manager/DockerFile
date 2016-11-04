#Copyright 2016 ANI Technologies Pvt. Ltd.
#
#Licensed under the Apache License, Version 2.0 (the "License");
#you may not use this file except in compliance with the License.
#You may obtain a copy of the License at
#
#http://www.apache.org/licenses/LICENSE-2.0
#
#Unless required by applicable law or agreed to in writing, software
#distributed under the License is distributed on an "AS IS" BASIS,
#WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#See the License for the specific language governing permissions and
#limitations under the License.

FROM ubuntu:14.04
MAINTAINER maintainer@fabric.com

RUN apt-get update --fix-missing \
  && apt-get install -y --no-install-recommends software-properties-common \
  && add-apt-repository ppa:webupd8team/java \
  && gpg --keyserver hkp://keys.gnupg.net --recv-keys 409B6B1796C275462A1703113804BB82D39DC0E3 \
  && apt-get update \
  && echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections \
  && echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections \
  && apt-get install -y --no-install-recommends oracle-java8-installer \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

  EXPOSE 17000

  VOLUME /var/log/fabric-manager

  COPY target/fabric-manager*.jar fabric-manager.jar
  COPY target/classes/docker-compose-config.yml docker-compose-config.yml

  CMD sh -c "sleep 20 ; java -jar -Xms${JAVA_PROCESS_MIN_HEAP} -Xmx${JAVA_PROCESS_MAX_HEAP} -XX:+${GC_ALGO} -Dfile.encoding=utf-8 fabric-manager.jar server ${CONFIG_ENV}-config.yml"
