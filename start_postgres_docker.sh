
IMAGE_NAME="postgres"
CONTAINER_NAME="postgres"

# Pull the latest official nginx docker image
docker pull postgres:latest

# Check if the custom nginx container is already running, stop, and remove it if it is
if [ $(docker ps -aq -f name=postgres) ]; then
    docker stop postgres
    docker rm postgres
fi

#docker run --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=1234 -d postgres
docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=1234 --name postgres