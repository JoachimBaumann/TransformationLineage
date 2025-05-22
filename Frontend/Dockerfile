# Use an official Nginx image
FROM nginx:alpine

# Remove default config (optional, only needed if customizing nginx.conf)
# RUN rm /etc/nginx/conf.d/default.conf

# Copy build folder into Nginx html directory
COPY ./dist /usr/share/nginx/html

# Optionally copy a custom nginx.conf if needed for routing
# COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
