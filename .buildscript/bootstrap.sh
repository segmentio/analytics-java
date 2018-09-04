#!/bin/bash

if ! which brew >/dev/null; then
  echo "homebrew is not available. Install it from http://brew.sh"
  exit 1
else
  echo "homebrew already installed"
fi

if ! which java >/dev/null; then
  echo "installing java..."
  brew tap caskroom/versions
  brew cask install java8
else
  echo "java already installed"
fi

if ! which mvn >/dev/null; then
  echo "installing maven..."
  brew install maven
else
  echo "maven already installed"
fi

echo "all dependencies installed."
