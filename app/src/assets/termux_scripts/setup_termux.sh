#!/bin/bash
# Update packages
pkg update -y
pkg upgrade -y

# Install required packages
pkg install -y openjdk-17 git clang make cmake wget unzip

# Install LanguageTool
wget https://languagetool.org/download/LanguageTool-stable.zip
unzip LanguageTool-stable.zip
mv LanguageTool-* ~/languagetool

# Build llama.cpp
git clone https://github.com/ggerganov/llama.cpp
cd llama.cpp
mkdir build
cd build
cmake .. -DLLAMA_CLBLAST=ON
make -j4

# Create models directory
mkdir -p ~/models

echo "Termux setup complete! Please download a medical GGUF model to ~/models/"