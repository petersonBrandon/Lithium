# Lithium

**STILL IN EARLY DEVELOPMENT**

Lithium is a specialized programming language designed for automation testing. With an emphasis on simplicity, speed, and reliability, Lithium empowers testers to define and run tests without the complexity of traditional programming languages. Built to be accessible and efficient, Lithium aims to make test automation straightforward and flexible, allowing both beginners and seasoned engineers to focus on creating meaningful tests rather than wrestling with syntax or boilerplate code.

Lithium prioritizes readability and ease of use, streamlining the test creation process with commands that are intuitive and close to natural language. It’s a language where each command does exactly what it says, making it easy to create powerful, robust, and maintainable tests. Whether you’re testing web applications or mobile interfaces, Lithium is built to handle your needs with minimal setup and maximum efficiency.

### Key Features

- **Simplicity**: Commands are designed to be human-readable, enabling anyone to create and understand tests without a steep learning curve.
- **Speed**: Lightweight design and direct commands allow for rapid test creation and execution.
- **Reliability**: Designed to be stable and consistent, Lithium ensures that test automation is reliable and accurate, minimizing false negatives and positives.

---

### Planned Features

Lithium is still evolving, with several features planned to enhance its functionality and flexibility:

1. **Reusable Test Steps**: Define and reuse common test steps or sequences across multiple tests to reduce redundancy.
2. **Custom Functions and Objects**: Allow users to define utility functions and custom objects for handling complex interactions or data.
3. **Test Execution Concurrency**: Enable parallel test execution to reduce runtime and improve efficiency.
4. **Configuration Files**: Allow test settings and environment configurations to be managed in a centralized file (e.g., `lithium.config`), including options for thread count, base URLs, and environment-specific settings.
5. **Reporting and Logging**: Generate detailed test reports and logs to provide insights into test outcomes, failures, and execution times.
6. **Data-Driven Testing**: Introduce support for data tables or external data files to run the same test with multiple data sets.
7. **Test Suite Organization**: Group tests into suites and control execution order for more complex testing scenarios.
8. **Error Handling and Retry Mechanism**: Automatically retry failed steps or tests based on configurable retry logic.
9. **GUI for Test Execution**: Provide a graphical interface for running and monitoring tests, improving ease of use for non-technical users.
10. **Cross-Browser Support**: Add options to run tests across multiple browsers for cross-browser testing.

---

## Installation

1. **Clone the Repository**:
    ```bash
    git clone <repository-url>
    cd lithium
    ```

2. **Add Selenium and Other Dependencies**:
    - Use Maven to install dependencies. Ensure `selenium-java` is included in your `pom.xml`.

3. **Download WebDriver**:
    - Ensure the appropriate WebDriver (e.g., ChromeDriver) is installed and in your system PATH.
    - For ease of use, you can utilize the Aqua IDE by intellij.

## Usage

### Command Syntax
These commands will function after creating an executable and setting it as a PATH variable.

Run Lithium tests from the command line:

- **Run all tests in a file**:
  ```bash
  lit run <file-name>
  ```

- **Run a specific test within a file**:
  ```bash
  lit run <file-name> "<test-name>"
  ```

### Writing Tests in `.lit` Files

Lithium test files define tests with simple commands. Each test starts with `test "<test-name>" {` and ends with `}`. Here’s an example:

```plaintext
test "Example" {
    open "https://www.brandonpeterson.dev/"
    click link "Tools"
    wait tag "input" visible 10
    type tag "input" "Emoji"
}
```

#### Supported Commands

- **`open "<URL>"`**: Opens the specified URL.
- **`click <locator type> "<locator>"`**: Clicks an element specified by a locator.
- **`wait <locator type> "<locator>" [wait type] [timeout in seconds]`**: Waits for an element based on the specified locator and optional wait type and timeout.
    - **Wait Types** (optional): `presence`, `visible`, `clickable`.
    - **Timeout** (optional): Specifies the maximum wait time in seconds.
    - Examples:
        - `wait tag "input"` — waits indefinitely for the presence of an `input` element.
        - `wait css ".search-box" visible 10` — waits up to 10 seconds for the element with class `search-box` to be visible.
        - `wait id "submit-button" clickable 5` — waits up to 5 seconds for the element with `id="submit-button"` to be clickable.

- **`type <locator type> "<locator>" "<text>"`**: Types text into an element specified by a locator.

#### Locator Types
The following locator types are supported:
- `id`
- `css`
- `xpath`
- `name`
- `class`
- `link`
- `partialLink`
- `tag`

---

## Project Structure

```plaintext
├── src
│   └── main
│   │   └── java
│   │       └── com.lithium
│   │           ├── commands               # Individual command classes like OpenCommand, ClickCommand, etc.
│   │           ├── core                   # Core classes, e.g., TestCase, TestRunner
│   │           ├── exceptions             # Custom exception classes, e.g., TestSyntaxException
│   │           ├── locators               # Locator-related classes, e.g., Locator, LocatorParser
│   │           └── parser                 # Main parsing logic, e.g., TestParser, LithiumInterpreter
│   └── tests
│       └── testing.lit                        # Example Lithium test file
├── target                                 # Compiled files
└── README.md                              # Project documentation
```

## Troubleshooting

- **Syntax Errors**: Ensure `.lit` files follow correct syntax. For example, each command should be on a new line, and each test block should be closed with `}`.
- **Unknown Commands**: Currently supported commands are `open`, `click`, `wait`, and `type`.

## Contributing

1. Fork the repository.
2. Create a new branch (`feature/new-feature`).
3. Commit changes (`git commit -m 'Add new feature'`).
4. Push to the branch (`git push origin feature/new-feature`).
5. Open a Pull Request.

## License

This project is licensed under the MIT License.