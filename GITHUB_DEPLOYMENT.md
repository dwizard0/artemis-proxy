# GitHub Deployment Guide

## Step 1: Create GitHub Repository

1. Go to https://github.com/new
2. Repository name: `artemis-proxy`
3. Description: "Real-time data proxy for Artemis Spaceship Bridge Simulator"
4. Public or Private (your choice)
5. **Do NOT** initialize with README (we have our own)
6. Click "Create repository"

## Step 2: Prepare Local Repository

On your Linux server (192.168.1.200):

```bash
cd "/media/storage/Users/freeman/artemis-kiosk/Artemis Bridge Stations/artemis-proxy"

# Initialize git repository
git init

# Add GitHub files
# (Upload all the GitHub-related files to this directory first)

# Create directory structure for scripts
mkdir -p scripts
mv start-proxy-full.sh scripts/
mv start-proxy-full.bat scripts/
mv stop-proxy-full.sh scripts/
mv stop-proxy-full.bat scripts/
chmod +x scripts/*.sh

# Create docs directory
mkdir -p docs
mv FULL_SCRIPTS_GUIDE.md docs/

# Copy additional docs
cp README.md .
cp LICENSE .
cp CONTRIBUTING.md .
cp .gitignore .
```

## Step 3: Initial Commit

```bash
# Add all files
git add .

# Make initial commit
git commit -m "Initial commit: Artemis Proxy v1.0

- Comprehensive ship data extraction via IAN library
- UDP and JSON broadcasting
- Multiple ship/instance support
- Game object tracking
- Full-featured startup scripts for Linux and Windows
- Web configuration interface"

# Add your GitHub repository as remote
git remote add origin https://github.com/YOURUSERNAME/artemis-proxy.git

# Push to GitHub
git branch -M main
git push -u origin main
```

## Step 4: Set Up GitHub Repository

### Add Topics
Add these topics to help people find your project:
- `artemis`
- `spaceship-simulator`
- `bridge-simulator`
- `lcars`
- `star-trek`
- `java`
- `proxy-server`
- `udp`
- `real-time`

### Create Releases
1. Go to "Releases" → "Create a new release"
2. Tag: `v1.0.0`
3. Title: "Artemis Proxy v1.0 - Initial Release"
4. Description:
```markdown
## Features
- Comprehensive ship telemetry extraction
- UDP broadcasting to multiple targets
- JSON file output
- Game object tracking (enemies, stations)
- Multiple ship support
- Full-featured command-line scripts
- Web configuration interface

## Installation
Download the JAR file or clone the repository and build with Maven.
See README.md for complete instructions.
```
5. Upload `artemis-proxy-1.0-SNAPSHOT-jar-with-dependencies.jar`
6. Click "Publish release"

## Step 5: Enable GitHub Features

### Wiki
1. Go to Settings → Features
2. Enable "Wikis"
3. Create wiki pages:
   - Home - Project overview
   - Installation - Detailed setup
   - Configuration - All options
   - Troubleshooting - Common issues
   - Examples - Use cases

### Issues
1. Enable "Issues"
2. Create issue templates:
   - Bug Report
   - Feature Request
   - Question

### Discussions
1. Enable "Discussions"
2. Create categories:
   - General
   - Ideas
   - Q&A
   - Show and Tell

## Step 6: GitHub Actions (Optional CI/CD)

Create `.github/workflows/build.yml`:

```yaml
name: Build

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Build with Maven
      run: mvn clean package
    - name: Upload artifact
      uses: actions/upload-artifact@v3
      with:
        name: artemis-proxy-jar
        path: target/artemis-proxy-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Step 7: Add Badges to README

Update README.md with actual badges:

```markdown
![Build Status](https://github.com/YOURUSERNAME/artemis-proxy/workflows/Build/badge.svg)
![Release](https://img.shields.io/github/v/release/YOURUSERNAME/artemis-proxy)
![Downloads](https://img.shields.io/github/downloads/YOURUSERNAME/artemis-proxy/total)
```

## Step 8: Create Documentation Site (Optional)

Use GitHub Pages:

1. Settings → Pages
2. Source: Deploy from a branch
3. Branch: main, folder: /docs
4. Your docs will be at: https://yourusername.github.io/artemis-proxy

## File Checklist

Before pushing, ensure you have:

- [ ] README.md
- [ ] LICENSE
- [ ] CONTRIBUTING.md
- [ ] .gitignore
- [ ] scripts/start-proxy-full.sh
- [ ] scripts/start-proxy-full.bat
- [ ] scripts/stop-proxy-full.sh
- [ ] scripts/stop-proxy-full.bat
- [ ] docs/FULL_SCRIPTS_GUIDE.md
- [ ] src/ (all Java source files)
- [ ] pom.xml

## Ongoing Maintenance

### For each new version:

```bash
# Make changes
git add .
git commit -m "Description of changes"
git push

# Tag version
git tag -a v1.1.0 -m "Version 1.1.0"
git push origin v1.1.0

# Create GitHub release
# Go to Releases → Draft a new release → Select tag → Describe changes
```

## Example Repository Structure

```
artemis-proxy/
├── .github/
│   └── workflows/
│       └── build.yml
├── docs/
│   └── FULL_SCRIPTS_GUIDE.md
├── scripts/
│   ├── start-proxy-full.sh
│   ├── start-proxy-full.bat
│   ├── stop-proxy-full.sh
│   └── stop-proxy-full.bat
├── src/
│   └── main/
│       └── java/
│           └── com/ussveritas/artemis/proxy/
├── .gitignore
├── CONTRIBUTING.md
├── LICENSE
├── pom.xml
└── README.md
```

## Next Steps

1. Share on social media
2. Add to Artemis community forums
3. Create demonstration videos
4. Write blog posts about use cases
5. Engage with users in Discussions

---

🚀 **Your project is now on GitHub!**

Repository URL: `https://github.com/YOURUSERNAME/artemis-proxy`
