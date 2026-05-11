FROM amazoncorretto:25-alpine

ARG APP_VERSION=dev
ENV APP_VERSION=${APP_VERSION}

WORKDIR /app
COPY app/build/install/app ./

EXPOSE 8080

CMD ["bin/app"]
