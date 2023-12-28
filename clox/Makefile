SRC=src
TARGET=clox
BUILD=build

CC=clang
DEPFLAGS=-MD -MP
CFLAGS=-Wall -Wextra -g -I$(SRC) $(DEPFLAGS)

FILES=$(wildcard $(SRC)/*.c)
OBJECTS=$(patsubst $(SRC)/%.c,$(BUILD)/%.o,$(FILES))
DEPFILES=$(patsubst $(SRC)/%.c,$(BUILD)/%.d,$(FILES))

.PHONY: all
all: $(TARGET)

$(TARGET): $(OBJECTS)
	$(CC) $^ -o $@

$(BUILD)/%.o: $(SRC)/%.c | $(BUILD)
	$(CC) $(CFLAGS) -c $< -o $@

$(BUILD):
	mkdir -p $(BUILD)

.PHONY: clean
clean:
	rm -r $(TARGET) $(BUILD)

-include $(DEPFILES)
