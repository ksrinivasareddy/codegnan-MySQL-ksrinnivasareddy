# 🎬 Media Compression Service

A powerful Spring Boot application for adaptive media compression based on device type and network conditions.

## ✨ Features

- **Adaptive Compression**: Automatically adjusts quality based on device and network
- **Multi-Format Support**: Images (JPEG, PNG), Videos (MP4, MKV), Audio (MP3, AAC)
- **Device Detection**: Automatic detection from User-Agent headers
- **Network Optimization**: Compression profiles for 2G, 3G, 4G, 5G, WiFi
- **TCP Server**: Direct file upload via TCP socket (port 9090)
- **REST API**: Full RESTful API for compression operations
- **History Tracking**: Database storage of all compression operations
- **Chunked Upload**: Support for large file uploads

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- FFmpeg and FFprobe installed and in PATH

### Install FFmpeg

**Amazon Linux 2023:**
```bash
sudo dnf install -y ffmpeg
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install -y ffmpeg
```

**macOS:**
```bash
brew install ffmpeg
```

**Windows:**
Download from https://ffmpeg.org/download.html

### Build and Run

```bash
# Navigate to project directory
cd media-compression

# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📡 API Endpoints

### Compression

**POST** `/compress`
- Upload and compress media files
- Parameters:
  - `file`: MultipartFile (required)
  - `deviceType`: String (optional) - e.g., "Android Phone", "iPhone", "Desktop"
  - `networkType`: String (optional) - e.g., "4G", "5G", "WiFi"
  - `speedMbps`: Double (optional) - Network speed in Mbps

**Example:**
```bash
curl -X POST http://localhost:8080/compress \
  -F "file=@/path/to/video.mp4" \
  -F "deviceType=Android Phone" \
  -F "networkType=4G" \
  -F "speedMbps=15.5"
```

### Download Compressed File

**GET** `/compress/download?file={filename}`

**Example:**
```bash
curl -O http://localhost:8080/compress/download?file=compressed_12345_video.mp4
```

### History

**GET** `/history` - Get all compression history

**DELETE** `/history/{id}` - Delete history entry

### Speed Test

**GET** `/compress/speedtest?sizeKB=5000` - Download test data to measure speed

## 🔌 TCP Server

The TCP server runs on port **9090** and accepts direct file uploads.

### Using the TCP Client

```bash
# Edit the file path in TcpCompressionClient.java
# Then run:
cd src/main/java/com/media/compression/client
javac TcpCompressionClient.java
java TcpCompressionClient
```

## 📊 Compression Profiles

| Network | Speed Range | Target Height | Bitrate | Quality |
|---------|-------------|---------------|---------|---------|
| 2G      | < 1 Mbps    | 360p          | 400k    | Very Low |
| 3G      | 1-3 Mbps    | 480p          | 600k    | Low |
| 4G      | 3-30 Mbps   | 720p          | 1200k   | Medium |
| 5G      | 30-100 Mbps | 1080p         | 3000k   | High |
| WiFi    | > 100 Mbps  | 1080p         | 4000k   | Very High |

## 🗂️ Project Structure

```
media-compression/
├── src/main/java/com/media/compression/
│   ├── MediaCompressionApplication.java
│   ├── controller/
│   │   ├── CompressionController.java
│   │   ├── FileServeController.java
│   │   ├── HistoryController.java
│   │   └── SpeedTestController.java
│   ├── service/
│   │   ├── CompressionService.java
│   │   ├── FFmpegExecutor.java
│   │   └── UploadService.java
│   ├── entity/
│   │   ├── CompressionHistory.java
│   │   └── User.java
│   ├── repository/
│   │   ├── CompressionHistoryRepository.java
│   │   └── UserRepository.java
│   ├── model/
│   │   ├── CompressionMetadata.java
│   │   └── CompressionQualityProfile.java
│   ├── util/
│   │   ├── DeviceNetworkDetector.java
│   │   ├── DeviceQualityUtil.java
│   │   └── FileUtil.java
│   ├── tcp/
│   │   ├── TcpCompressionServer.java
│   │   └── TcpCompressionHandler.java
│   └── config/
│       ├── CorsConfig.java
│       └── WebMvcConfig.java
├── src/main/resources/
│   └── application.properties
├── uploads/
│   ├── input/
│   ├── output/
│   └── temp/
├── pom.xml
└── README.md
```

## 🔧 Configuration

Edit `src/main/resources/application.properties`:

```properties
# Server
server.port=8080
server.address=localhost

# File Upload
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB

# Database
spring.datasource.url=jdbc:h2:file:./data/compression_db

# FFmpeg
app.ffmpeg.path=ffmpeg
app.ffprobe.path=ffprobe

# TCP Server
app.tcp.port=9090
app.tcp.enabled=true
```

## 🧪 Testing

```bash
# Run tests
mvn test

# Test compression endpoint
curl -X POST http://localhost:8080/compress \
  -F "file=@test-image.jpg" \
  -F "deviceType=iPhone" \
  -F "networkType=5G"
```

## 📝 Database

The application uses H2 embedded database. Access the console at:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/compression_db`
- Username: `sa`
- Password: (empty)

## 🐛 Troubleshooting

### FFmpeg not found
```bash
# Check if FFmpeg is installed
ffmpeg -version
ffprobe -version

# If not, install it (see Prerequisites section)
```

### Port already in use
```bash
# Change port in application.properties
server.port=8081
```

### Large file upload fails
```bash
# Increase limits in application.properties
spring.servlet.multipart.max-file-size=1GB
spring.servlet.multipart.max-request-size=1GB
```

## 📄 License

MIT License

## 👥 Contributors

- Your Name

## 🙏 Acknowledgments

- FFmpeg for media processing
- Spring Boot for the framework
- Lombok for reducing boilerplate code
