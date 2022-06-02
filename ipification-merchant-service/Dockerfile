FROM node:16-alpine
RUN apk update && apk add bash
WORKDIR /usr/app
COPY package.json ./
RUN npm install

COPY . .

ENV PORT 8080

EXPOSE 8080
CMD [ "npm", "run", "start" ]