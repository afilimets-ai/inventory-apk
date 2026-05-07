# Inventory APK

[![CI](https://github.com/afilimets-ai/inventory-apk/actions/workflows/ci.yml/badge.svg)](https://github.com/afilimets-ai/inventory-apk/actions/workflows/ci.yml)

An Android application for efficient inventory management and tracking.

## 📋 Overview

Inventory APK is a mobile inventory management solution designed to help businesses and individuals track, manage, and organize their inventory items efficiently. Built natively for Android, the application provides a streamlined interface for managing stock levels, tracking item movements, and generating reports on the go.

## 🎯 Purpose

Traditional inventory management often requires desktop software or complex web applications. Inventory APK brings the power of inventory tracking directly to your mobile device, enabling:

- **Real-time tracking**: Update inventory status immediately as items move
- **Offline-first operation**: Manage inventory without constant internet connectivity
- **Mobile convenience**: Use your smartphone to scan, update, and view inventory anywhere
- **Simplified workflow**: Intuitive interface designed for quick data entry and retrieval

## ✨ Key Features

### Core Functionality
- **Item Management**: Add, edit, and delete inventory items with detailed information
- **Hardware Barcode Scanning**: Quick item lookup and data entry through Newland/Honeywell scanner integrations with a generic scanner fallback
- **Stock Tracking**: Record receiving, audit, shipment, and transfer operations with local operation history
- **Category and Location Organization**: Group items by category and storage location
- **Search & Filter**: Quickly find items using barcode/name search and category/location filters
- **Offline-first Support**: Room-backed local database and outbox queue for resilient sync
- **Data Import/Export**: CSV, JSON, and Excel serializers for local folder, FTP/SFTP, WebDAV, and HTTP API providers

### Advanced Features (Future Roadmap)
- **Supplier Management**: Track vendor information and purchase orders
- **Reporting & Analytics**: Visual insights into inventory trends
- **Cloud Drive Integrations**: OneDrive, Google Drive, email, Telegram, and 1C providers are currently stubs
- **Multi-user Access**: Collaborative inventory management with role-based permissions

> **Note**: This project is in active development. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the current architecture.

## 🚀 Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **Android Studio**: Recent stable Android Studio release
- **JDK**: Java Development Kit 17
- **Android SDK**: compile SDK 34, minimum SDK 24
- **Git**: For version control

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/afilimets-ai/inventory-apk.git
   cd inventory-apk
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned repository folder
   - Click "OK"

3. **Sync Gradle dependencies**
   - Android Studio will automatically prompt to sync
   - Or manually sync via: File → Sync Project with Gradle Files

4. **Configure Android Emulator** (optional)
   - Open AVD Manager (Tools → AVD Manager)
   - Create a new Virtual Device with API Level 24+
   - Or connect a physical Android device with USB debugging enabled

5. **Build and Run**
   - Click the "Run" button (green play icon)
   - Select your target device (emulator or physical device)
   - The app will build and install automatically

### Quick Start Guide

Once the app is installed:

1. **First Launch**: The app will create a local database for your inventory
2. **Add Your First Item**:
   - Tap the "+" floating action button
   - Enter item details (name, SKU, quantity, etc.)
   - Optionally scan a barcode for quick entry
   - Save the item
3. **View Inventory**: Browse your items in the main list view
4. **Update Stock**: Tap any item to adjust quantity or edit details
5. **Search Items**: Use the search bar to filter by name, SKU, or category

> **Note**: Release signing uses GitHub Actions secrets. Do not commit keystores or `local.properties`. Because a release keystore was previously tracked, rotate release signing credentials before shipping production builds.

## 📚 Documentation

- **[ROADMAP.md](ROADMAP.md)** - Development roadmap, MVP scope, and milestones
- **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** - Technical architecture and design decisions
- **[docs/features/](docs/features/)** - Detailed feature specifications

## 🏗️ Project Status

**Current Phase**: Active Android implementation

The application contains Compose screens, Room persistence, scanner integrations, sync providers, WorkManager scheduling, and unit tests. Some provider integrations remain roadmap-only and return explicit "not implemented" failures.

## 🛠️ Technology Stack

**Target Platform**: Android 7.0 (API 24) and above

**Technologies**:
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (SQLite)
- **Dependency Injection**: Hilt/Dagger
- **Barcode Scanning**: Newland/Honeywell hardware SDK integrations plus scanner abstraction
- **UI Framework**: Jetpack Compose / Material Design 3
- **Background Work**: WorkManager
- **Sync/Network**: Retrofit, OkHttp, FTP/SFTP/WebDAV/HTTP providers

> See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for detailed technical decisions and rationale.

## 🤝 Contributing

We welcome contributions from the community! Whether you're fixing bugs, adding features, or improving documentation, your help is appreciated.

Use focused pull requests, keep generated/local files out of commits, and run the Gradle checks before submitting changes.

## 📝 License

This project's license is to be determined. Check back soon for licensing information.

## 📧 Contact & Support

- **Issues**: [GitHub Issues](https://github.com/afilimets-ai/inventory-apk/issues)
- **Discussions**: [GitHub Discussions](https://github.com/afilimets-ai/inventory-apk/discussions)

## 🙏 Acknowledgments

This project is built with modern Android development best practices and leverages the Android Jetpack suite of libraries.

---

**Status**: 🚧 In Development | **Version**: 0.1.0-alpha | **Last Updated**: March 2026
