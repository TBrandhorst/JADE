FROM alpine/latest

COPY entrypoint.sh /entrypoint.sh

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y git && \
	apt-get install -y git-svn && \
	apt-get install subversion && \
	apk update && \
	apk add openjdk11

ENTRYPOINT ["sh","/entrypoint.sh"]
