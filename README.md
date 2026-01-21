# The Little Library

A personal library management web application for tracking and organizing your book collection. Search for books via OpenLibrary API and maintain your own curated reading list with custom notes and ratings.

**Live Application:** [https://fionadark.com/littlelibrary/](https://fionadark.com/littlelibrary/)

## Features

- **User Authentication**: Secure login/signup with Firebase Authentication
- **Book Search**: Search and discover books using the OpenLibrary API
- **Personal Library**: Add books to your collection with custom metadata
- **Organization**: Filter by read status, genre, and sort by various criteria
- **Notes & Ratings**: Add personal notes and star ratings to your books

## Tech Stack

### Backend
- **Java 21** with Spring Boot 3.5.6
- **Firebase Admin SDK**: User authentication and Firestore database
- **Gradle**: Build automation
- **Railway**: Cloud hosting for backend API

### Frontend
- **Vanilla JavaScript**: No framework dependencies
- **Firebase JS SDK**: Client-side authentication
- **GitHub Pages**: Static site hosting

### APIs
- **OpenLibrary API**: Book search and metadata

## Architecture

- **Backend**: RESTful API hosted on Railway (`the-little-library-production.up.railway.app`)
- **Frontend**: Static HTML/CSS/JS hosted on GitHub Pages (`fionadark.com/littlelibrary`)
- **Database**: Firebase Firestore for user data and book collections
- **Authentication**: Firebase Authentication with email/password

## Local Development

### Prerequisites
- Java 21+
- Gradle 8.9+
- Firebase service account key

### Setup

1. Clone the repository
2. Add `firebase-service-account.json` to project root
3. Build: `./gradlew bootJar`
4. Run: `java -jar build/libs/app.jar`
5. Access at `http://localhost:8080`

## Deployment

- **Backend**: Automatically deployed to Railway on push to `main`
- **Frontend**: Manually copied to [fionadark.com](https://github.com/fionadark/fionadark.com) repository

---

*Part of Fiona's personal projects at [fionadark.com](https://fionadark.com)*
