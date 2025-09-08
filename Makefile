SRC_DIR = interpreter/src
BIN_DIR = bin
MAIN_CLASS = Main
TEST_FILE = test_files/test_1.f

SOURCES := $(shell find $(SRC_DIR) -name "*.java")

CLASSES := $(patsubst $(SRC_DIR)/%.java,$(BIN_DIR)/%.class,$(SOURCES))

all: run

$(BIN_DIR)/%.class: $(SRC_DIR)/%.java
	@mkdir -p $(dir $@)
	javac -d $(BIN_DIR) $(SOURCES)

compile: $(CLASSES)

run: compile
	java -cp $(BIN_DIR) $(MAIN_CLASS) $(TEST_FILE)

clean:
	rm -rf $(BIN_DIR)
