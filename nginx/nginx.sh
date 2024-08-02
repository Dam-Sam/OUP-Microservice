CURRENT_DIR=$(pwd)
NGINX_VER=1.24.0
REINSTALL=false

if [[ "$2" == "-r" ]]; then
    REINSTALL=true
    echo NGINX will be forced to reinstall, even if it exists...
fi

start() {
    echo "nginx ($NGINX_VER) starting..."
    sleep 0.5
    if [[ "$REINSTALL" == "true" ]] || [ ! -f "$CURRENT_DIR/nginx-$NGINX_VER/objs/nginx" ]; then
        echo nginx not found or rebuild was selected...
        echo nginx will be downloaded and built from source packages starting in 5 seconds...
        sleep 5

        rm -fr "$CURRENT_DIR/nginx-$NGINX_VER/"
        rm -f "$CURRENT_DIR/nginx-$NGINX_VER.tar.gz"
        mkdir -p "$CURRENT_DIR/temp/"
        mkdir -p "$CURRENT_DIR/logs/"

        echo "Downloading nginx..."
        # use && to concatenate commands so a failure in any command will be detected
        wget "https://nginx.org/download/nginx-$NGINX_VER.tar.gz" && \
        tar -xvf "nginx-$NGINX_VER.tar.gz" && \
        cd "$CURRENT_DIR/nginx-$NGINX_VER" && \
        echo "Building nginx..." && \
        ./configure && make
        if [ $? -ne 0 ]; then
          echo "Build process failed."
          exit 1
        fi
        cd "$CURRENT_DIR"
        echo "nginx build complete."
    else
        echo "nginx found (Use '-r' argument to force rebuild.)"
    fi

    "$CURRENT_DIR/nginx-$NGINX_VER/objs/nginx" -p "$CURRENT_DIR" -c "$CURRENT_DIR/nginx.conf"
    if [ $? -ne 0 ]; then
      echo "nginx failed to start."
      exit 1
    fi
    echo nginx started successfully
}

stop() {
    "$CURRENT_DIR/nginx-$NGINX_VER/objs/nginx" -p "$CURRENT_DIR" -c "$CURRENT_DIR/nginx.conf" -s stop
        if [ $? -ne 0 ]; then
          echo "nginx stop command failed: nginx failed to stop or was not already running."
          exit 1
        fi
        echo nginx was stopped.
}


case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    *)
        echo "Usage: $0 start | stop [-r]"
        exit 1
        ;;
esac






