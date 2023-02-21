# Github Repositories API

This is a simple RESTful API that retrieves non-fork repositories of a given GitHub user along with their branches. The API is built using Spring Boot and uses WebClient to make requests to the GitHub API.

## API Endpoints

### Get Non-Fork Repositories

Get non-fork repositories for the specified user.

- `GET /api/repositories/{username}`
- Request Parameters:
    - `username` - the username of the user to get repositories for
- Response:
    - Returns a Flux of Repository objects
- Headers:
    - `Accept` - the accept header in the request (must not be `application/xml`)
- Status Codes:
    - 200 OK - on success
    - 400 Bad Request - if a username is not provided
    - 404 Not Found - if the specified user is not found
    - 406 Not Acceptable - if the specified media type is not supported
    - 500 Internal Server Error - if an unexpected error occurs

### Get Non-Fork Repositories Without User

Returns a 400 Bad Request error with an error response body, indicating that a username is required to access this endpoint.

- `GET /api/repositories` or `GET /api/repositories/`
- Response:
    - Returns a Flux of error response body
- Headers:
    - `Accept` - the accept header in the request (must not be `application/xml`)
- Status Codes:
    - 400 Bad Request - always returned

## Exceptions

The API handles the following exceptions:

- `IllegalArgumentException` - thrown when the provided username is null or empty
- `InvalidUsernameException` - thrown when a username is not provided in the request
- `MediaTypeNotSupportedException` - thrown when the specified media type is not supported
- `GithubUserNotFoundException` - thrown when the specified user is not found
- `Exception` - thrown for any other unexpected errors

## Installation and Usage

1. Clone the repository
2. Build the application using `mvn clean package`
3. Run the application using `java -jar target/github-repositories-api-0.0.1-SNAPSHOT.jar`
4. Access the API endpoints using a REST client such as Postman or curl

## License

This project is licensed under the [MIT License](https://opensource.org/licenses/MIT).
# GithubExercise
