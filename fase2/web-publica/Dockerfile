# Etapa de compilación
FROM node:20-alpine AS build
WORKDIR /app

# Copiar archivos de configuración
COPY package.json package-lock.json ./

# Instalar dependencias
RUN npm ci

# Copiar todo el código fuente
COPY . .

# Construir la aplicación Next.js con salida standalone
ARG NEXT_PUBLIC_RECAPTCHA_SITE_KEY
ENV NEXT_PUBLIC_RECAPTCHA_SITE_KEY=${NEXT_PUBLIC_RECAPTCHA_SITE_KEY}
RUN npm run build

# Etapa de producción (standalone)
FROM node:20-alpine AS production
WORKDIR /app

# Copiar solo lo necesario desde la etapa de build
COPY --from=build /app/.next ./.next
COPY --from=build /app/package.json ./package.json
COPY --from=build /app/next.config.js ./next.config.js
COPY --from=build /app/public ./public

# Instalar solo producción
RUN npm ci --omit=dev

# Exponer el puerto
EXPOSE 3000

# Variable de entorno para el servicio consultas
ENV NEXT_PUBLIC_RECAPTCHA_SITE_KEY=${NEXT_PUBLIC_RECAPTCHA_SITE_KEY}
ENV PORT=3000

# Comando de entrada
CMD ["npm", "start"]