FROM nginx:alpine

# Copiar configuración de Nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Copiar index.html y archivos estáticos
COPY index.html /usr/share/nginx/html/index.html

COPY static /usr/share/nginx/html/static

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]