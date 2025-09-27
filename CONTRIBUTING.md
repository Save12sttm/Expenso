# 🤝 Contributing to Expenso

Thank you for your interest in contributing to Expenso! This document provides guidelines and information for contributors.

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog | 2023.1.1+
- Android SDK 26+
- Kotlin 1.9.23+
- Git

### Development Setup

1. **Fork and Clone**
   ```bash
   git clone https://github.com/yourusername/expenso.git
   cd expenso
   ```

2. **Create Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Setup Development Environment**
   - Open project in Android Studio
   - Sync Gradle files
   - Run tests to ensure everything works

## 📋 How to Contribute

### 🐛 Bug Reports
- Use the GitHub issue tracker
- Include Android version, device model
- Provide steps to reproduce
- Include screenshots if applicable

### ✨ Feature Requests
- Check existing issues first
- Describe the feature clearly
- Explain the use case
- Consider implementation complexity

### 🔧 Code Contributions

#### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable names
- Add comments for complex logic
- Follow existing project structure

#### Commit Guidelines
```
type(scope): description

feat(dashboard): add monthly spending chart
fix(database): resolve transaction deletion bug
docs(readme): update installation instructions
```

#### Pull Request Process
1. Update documentation if needed
2. Add tests for new features
3. Ensure all tests pass
4. Update CHANGELOG.md
5. Request review from maintainers

## 🏗️ Architecture Guidelines

### MVVM Pattern
- ViewModels handle UI logic
- Repository manages data sources
- Use Hilt for dependency injection

### Database Changes
- Create migration scripts for Room
- Test migrations thoroughly
- Update DAO interfaces accordingly

### UI Components
- Use Jetpack Compose
- Follow Material 3 guidelines
- Ensure responsive design
- Add accessibility support

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### UI Tests
```bash
./gradlew connectedAndroidTest
```

### Code Coverage
- Aim for >80% coverage
- Focus on business logic
- Test edge cases

## 📝 Documentation

- Update README.md for new features
- Add KDoc comments for public APIs
- Include code examples
- Update architecture diagrams

## 🎯 Areas for Contribution

### High Priority
- [ ] Biometric authentication
- [ ] Cloud sync functionality
- [ ] Advanced analytics
- [ ] Widget support

### Medium Priority
- [ ] Multi-currency support
- [ ] Budget planning
- [ ] Recurring transactions
- [ ] Data visualization improvements

### Low Priority
- [ ] Wear OS support
- [ ] Voice commands
- [ ] Machine learning insights
- [ ] Social features

## 🚫 What Not to Contribute

- Breaking changes without discussion
- Features that compromise security
- Code that doesn't follow architecture
- Untested functionality

## 📞 Getting Help

- **GitHub Discussions:** For general questions
- **Issues:** For bugs and feature requests
- **Email:** your.email@example.com

## 🏆 Recognition

Contributors will be:
- Listed in CONTRIBUTORS.md
- Mentioned in release notes
- Given credit in app about section

## 📄 License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

**Happy Contributing! 🎉**