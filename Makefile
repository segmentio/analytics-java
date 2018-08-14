bootstrap:
	brew cask install java8
	brew install maven

dependencies:
	mvn install

check:
	mvn verify

build:
	mvn package -B

.PHONY: dependencies check build
