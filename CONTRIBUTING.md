# Contributing to Artemis Proxy

Thank you for your interest in contributing to Artemis Proxy! This document provides guidelines for contributions.

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/yourusername/artemis-proxy.git`
3. Create a feature branch: `git checkout -b feature/amazing-feature`
4. Make your changes
5. Test thoroughly
6. Commit: `git commit -m 'Add amazing feature'`
7. Push: `git push origin feature/amazing-feature`
8. Open a Pull Request

## Development Setup

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Artemis Spaceship Bridge Simulator (for testing)

### Build
```bash
mvn clean package
```

### Run Tests
```bash
mvn test
```

### Run Locally
```bash
java -jar target/artemis-proxy-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Code Style

- Follow standard Java conventions
- Use meaningful variable and method names
- Add comments for complex logic
- Keep methods focused and concise

## Testing

- Add unit tests for new features
- Test with actual Artemis server when possible
- Verify UDP broadcasting works correctly
- Check JSON output format

## Pull Request Guidelines

1. **Clear Description** - Explain what and why
2. **One Feature Per PR** - Keep changes focused
3. **Update Documentation** - If you add features, update README/docs
4. **Test Your Changes** - Ensure everything works
5. **Follow Code Style** - Match existing patterns

## Reporting Issues

When reporting bugs, include:
- Artemis version
- Java version
- Operating system
- Proxy configuration
- Steps to reproduce
- Expected vs actual behavior
- Relevant log output

## Feature Requests

For new features:
- Explain the use case
- Describe the proposed solution
- Consider backward compatibility
- Discuss alternatives

## Questions?

Open a [Discussion](https://github.com/yourusername/artemis-proxy/discussions) for:
- Usage questions
- Architecture discussions
- Integration ideas
- General help

## Code of Conduct

Be respectful, constructive, and welcoming to all contributors.

## License

By contributing, you agree your contributions will be licensed under the MIT License.

---

Thank you for helping make Artemis Proxy better! 🚀
