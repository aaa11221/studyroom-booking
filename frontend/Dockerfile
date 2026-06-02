ARG NODE_IMAGE=node:20-alpine
ARG STATIC_IMAGE=busybox:1.36.1-musl

FROM ${NODE_IMAGE} AS builder
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY index.html style.css app.js config.js ./
COPY src ./src
COPY web ./web
RUN mkdir -p /dist && cp -r index.html style.css app.js config.js src web /dist/

FROM ${NODE_IMAGE} AS dev
WORKDIR /app

USER node
EXPOSE 5173

CMD ["node", "-e", "const http=require('http'),fs=require('fs'),path=require('path');const types={'.html':'text/html','.css':'text/css','.js':'application/javascript','.json':'application/json','.png':'image/png','.jpg':'image/jpeg','.svg':'image/svg+xml'};http.createServer((req,res)=>{let p=decodeURI(req.url.split('?')[0]);if(p==='/' )p='/index.html';const f=path.join('.',p);fs.readFile(f,(err,data)=>{if(err){res.statusCode=404;res.end('Not found');return;}res.setHeader('Content-Type',types[path.extname(f)]||'application/octet-stream');res.end(data);});}).listen(5173,'0.0.0.0');"]

FROM ${STATIC_IMAGE}
WORKDIR /www

COPY --from=builder /dist/ ./

USER 1000:1000
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost:8080/ || exit 1

CMD ["httpd", "-f", "-p", "8080", "-h", "/www"]
