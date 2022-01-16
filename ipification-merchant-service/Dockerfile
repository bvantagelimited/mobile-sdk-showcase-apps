FROM node:12-alpine
RUN apk update && apk add bash
WORKDIR /usr/app
COPY package.json ./
RUN npm install

COPY . .

ENV PORT 80

EXPOSE 80
CMD [ "npm", "run", "start" ]