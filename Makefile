bootstrap:
	brew cask install java8
	brew install maven

dependencies:
	mvn install

check:
	mvn spotless:check animal-sniffer:check test verify

build:
	mvn package -B

.PHONY: dependencies check build
