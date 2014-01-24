compile:
	g++ -Iopen-zwave-read-only/cpp/src -Iopen-zwave-read-only/cpp/src/value_classes/ -Iopen-zwave-read-only/cpp/src/platform/ -Iopen-zwave-read-only/cpp/src/platform/unix -Iopen-zwave-read-only/cpp/src/command_classes/ -Lopen-zwave-read-only/cpp/lib/linux -lopenzwave -lpthread  -ludev fibaro.cpp

run:
	sudo LD_LIBRARY_PATH=open-zwave-read-only/cpp/lib/linux/ ./a.out
	
