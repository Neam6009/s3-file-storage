## S3 File Storage Service
This Spring Boot application provides a backend REST API for a user-based file storage service. It allows users to upload, search for, and download files from a dedicated folder within a single AWS S3 bucket. Each user's files are logically separated into their own "folder" (prefix) within the bucket.

## Requirements
To build and run this application, you'll need the following:

Java Development Kit (JDK) 17 or higher

Apache Maven

An AWS Account with a configured S3 bucket

## Setup
Follow these steps to get the application running on your local machine.

Clone the Repository
```
git clone https://github.com/Neam6009/s3-file-storage.git
```
```
cd file-storage
```
## Configure AWS S3
The application connects to AWS S3 using credentials and a bucket name. For this to work, you must first create a single S3 bucket in your AWS account. Once created, you can configure the application in one of two ways:

a) Environment Variables:
Set the following environment variables on your system:

AWS_ACCESS_KEY

AWS_SECRET_KEY

AWS_REGION

AWS_BUCKET_NAME

b) application.properties File:
Alternatively, you can configure the src/main/resources/application.properties file with your AWS details.

application.properties

Properties

### Application Name
spring.application.name=file-storage

## AWS S3 Configuration
cloud.aws.credentials.access-key=${AWS_ACCESS_KEY}
cloud.aws.credentials.secret-key=${AWS_SECRET_KEY}
cloud.aws.region.static=${AWS_REGION}
cloud.aws.bucket.name=${AWS_BUCKET_NAME}
Note: For security, it is highly recommended to use environment variables instead of hard-coding credentials in your properties file.

# Run the Application
## Build the Project
Navigate to the project's root directory and use Maven to build the application. This will also download all necessary dependencies.
```
./mvnw clean install
```
## Run the Application
Once the build is successful, you can start the application with the following command:
```
./mvnw spring-boot:run
```
The application will start on http://localhost:8080.

## Tests
To run the complete suite of unit and integration tests, execute the following Maven command from the project root:
```
./mvnw test
```
This command will compile the test classes and run all tests.

# API Endpoints
The application exposes the following REST endpoints. You can use tools like Postman or curl to interact with them. The base URL for all endpoints is http://localhost:8080/api/s3.

1. Upload a File
   Uploads a file into a user's dedicated folder in the S3 bucket.

Endpoint: POST /api/s3/{username}/upload

Method: POST

Path Variable:

username: The name of the user whose folder the file will be uploaded to.

Request Parameters:

file: The MultipartFile to be uploaded.

Example using curl:

```
curl --location --request POST 'http://localhost:8080/api/s3/test-user/upload' \
--header 'Content-Type: multipart/form-data' \
--form 'file=@"/path/to/your/logistics_report.pdf"'
```
Success Response (200 OK):

S3 File Uploaded successfully

2. Search for Files
   Searches for files within a user's folder that contain the specified search term in their filename.

Endpoint: GET /api/s3/{username}/search

Method: GET

username: The user's folder to search within.

Query Parameter:

searchKey (optional): The term to search for in the filenames. If omitted, all files for the user are returned.

Example Request:
GET http://localhost:8080/api/s3/test-user/search?searchKey=logistics

Success Response (200 OK):

```json
[
"logistics_report.pdf",
"old_logistics_data.csv"
]
```

3. Download a File
   Downloads a specific file from a user's folder.

Endpoint: GET /api/s3/{username}/download/{filename}

Method: GET

Path Variables:

username: The user whose file you want to download.

filename: The name of the file to be downloaded.

Example Request:
GET http://localhost:8080/api/s3/test-user/download/logistics_report.pdf

Success Response (200 OK):
The response will be the raw binary content of the file with the Content-Disposition header set to prompt a download.