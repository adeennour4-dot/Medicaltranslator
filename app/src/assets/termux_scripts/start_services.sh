#!/bin/bash
# Start LanguageTool server
java -cp ~/languagetool/languagetool-server.jar org.languagetool.server.HTTPServer --port 8010 &

# Start llama.cpp server
~/llama.cpp/build/bin/server -m ~/models/mistral-7b-medical-Q8_0.gguf --port 8082 --ctx-size 2048 -t 4 &

echo "Services started on ports 8010 (LanguageTool) and 8082 (llama.cpp)"