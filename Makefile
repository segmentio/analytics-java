bootstrap:
	.buildscript/bootstrap.sh

dependencies:
	mvn install

check:
	mvn spotless:check animal-sniffer:check test verify

build:
	mvn package -B

.PHONY: dependencies check build
