#!/bin/bash
CURRENT_DIR=$(pwd)
JAVA_BIN=jdk-21.0.2/bin/java
JAVAC_BIN=jdk-21.0.2/bin/javac
LIBS=lib/
USE_CUSTOM_JAVA=1
error=0
java_url='https://download.java.net/java/GA/jdk21.0.2/f2283984656d49d69e91c558476027ac/13/GPL/openjdk-21.0.2_linux-x64_bin.tar.gz'
postgresql_url='https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.2/postgresql-42.7.2.jar'
hazelcast_url='https://repo1.maven.org/maven2/com/hazelcast/hazelcast/5.3.6/hazelcast-5.3.6.jar'
hikari_url='https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.1.0/HikariCP-5.1.0.jar'
jackson_core_url='https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.13.3/jackson-core-2.13.3.jar'
jackson_anno_url='https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.13.3/jackson-annotations-2.13.3.jar'
jackson_databind_url='https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.13.3/jackson-databind-2.13.3.jar'
slf4j_url='https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.12/slf4j-api-2.0.12.jar'
if [ $USE_CUSTOM_JAVA == 0 ]; then
    JAVA_BIN=java
    JAVAC_BIN=javac
fi

download_lib_if_missing() {
    local url=$1
    local file="lib/$(basename $url)"
    [[ ! -f "$file" ]] && wget -P lib/ "$url"
}

verify_java() {
    if [ $USE_CUSTOM_JAVA == 1 ]; then
        if [ ! -f "$CURRENT_DIR/jdk-21.0.2/bin/java" ]; then
            echo "Java 21 not found. Downloading..."
            wget "$java_url" && \
            mv "openjdk-21.0.2_linux-x64_bin.tar.gz" "java21.tar.gz" && \
            tar -xvf "java21.tar.gz"
            if [ $? -ne 0 ]; then
                echo "Java 21 download failure."
                exit 1
            fi
            rm "java21.tar.gz"
            echo "Java 21 downloaded and ready."
        fi
    fi
}

verify_libs() {
    mkdir -p "$CURRENT_DIR/libs/"
    echo "Verifying required Java libraries are available and downloading if missing."
    download_lib_if_missing "$postgresql_url"
    download_lib_if_missing "$hazelcast_url"
    download_lib_if_missing "$hikari_url"
    download_lib_if_missing "$jackson_core_url"
    download_lib_if_missing "$jackson_anno_url"
    download_lib_if_missing "$jackson_databind_url"
    download_lib_if_missing "$slf4j_url"
    echo "All dependencies are ready."
}


compile() {
    target=${1:-all}
    verify_java
    verify_libs
    echo -e '\033[32mCompiling project...\033[0m'
    echo -e '\033[32mDownloading dependencies if missing...\033[0m'

    if [[ "$target" == "all" || "$target" == "u" ]]; then
        echo -e '\033[32mCompiling UserService...\033[0m'
        mkdir -p ./compiled/UserService/lib/
        $JAVAC_BIN -d compiled/UserService -cp ".:lib/*" src/Common/*.java src/UserService/*.java
        if [ $? -ne 0 ]; then
            echo -e "\033[31mError compiling UserService.\033[0m"
            error=1
        fi
        echo -e '\033[32mCopying UserService dependencies...\033[0m'
        cp --verbose ./lib/* ./compiled/UserService/lib/
    fi

    if [[ "$target" == "all" || "$target" == "p" ]]; then
        echo -e '\033[32mCompiling ProductService...\033[0m'
        mkdir -p ./compiled/ProductService/lib/
        $JAVAC_BIN -d compiled/ProductService -cp ".:lib/*" src/Common/*.java src/ProductService/*.java
        if [ $? -ne 0 ]; then
            echo -e "\033[31mError compiling ProductService.\033[0m"
            error=1
        fi
        echo -e '\033[32mCopying ProductService dependencies...\033[0m'
        cp --verbose ./lib/* ./compiled/ProductService/lib/
    fi

    if [[ "$target" == "all" || "$target" == "o" ]]; then
        echo -e '\033[32mCompiling OrderService...\033[0m'
        mkdir -p ./compiled/OrderService/lib/
        $JAVAC_BIN -d compiled/OrderService -cp ".:lib/*" src/Common/*.java src/OrderService/*.java
        if [ $? -ne 0 ]; then
            echo -e "\033[31mError compiling OrderService.\033[0m"
            error=1
        fi
        echo -e '\033[32mCopying OrderService dependencies...\033[0m'
        cp --verbose ./lib/* ./compiled/OrderService/lib/
    fi

    if [[ "$target" == "all" || "$target" == "l" ]]; then
        echo -e '\033[32mCompiling LoadGen...\033[0m'
        mkdir -p ./compiled/LoadGen/lib/
        $JAVAC_BIN -d compiled/LoadGen -cp ".:lib/*" src/Common/*.java src/LoadGen/*.java
        if [ $? -ne 0 ]; then
            echo -e "\033[31mError compiling LoadGen.\033[0m"
            error=1
        fi
        echo -e '\033[32mCopying LoadGen dependencies...\033[0m'
        cp --verbose ./lib/* ./compiled/LoadGen/lib/
        echo -e '\033[32mCopying LoadGen load files...\033[0m'
        cp --verbose ./src/LoadGen/*.load ./compiled/LoadGen/
    fi

    if [ $error -ne 0 ]; then
        echo -e "\033[31mBUILD FAILED\n- At least one service failed to compile.\033[0m"
    else
        echo -e '\033[32mBUILD SUCCESS\n- All services compiled succesfully\033[0m'
    fi

}

start_user_service() {
    verify_java
    verify_libs
    config=${1:-config.json}
    clear
    echo Starting UserService using config: $config...
    $JAVA_BIN --enable-preview -cp compiled/UserService:compiled/UserService/lib/* UserService.UserService $config
}

start_product_service() {
    verify_java
    verify_libs
    config=${1:-config.json}
    clear
    echo Starting ProductService using config: $config...
    $JAVA_BIN --enable-preview -cp compiled/ProductService:compiled/ProductService/lib/* ProductService.ProductService $config
}

start_order_service() {
    verify_java
    verify_libs
    config=${1:-config.json}
    clear
    echo Starting OrderService using config: $config...
    #$JAVA_BIN --enable-preview -cp compiled/OrderService:compiled/OrderService/lib/* OrderService.OrderService $config
    $JAVA_BIN --enable-preview --add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED -cp "compiled/OrderService:compiled/OrderService/lib/*" OrderService.OrderService $config

}

start_iscs() {
    echo 'ISCS is not used. NGINX should be launched instead'
}

start_all() {
    verify_java
    verify_libs
    config=${1:-config.json}

    echo Starting all services on separate screens using config: $config.
    echo Use runme.sh with the correct switch to switch screens/services:
    echo 'UserService: runme.sh -su'
    echo 'ProductService: runme.sh -sp'
    echo 'OrderService: runme.sh -so'
    echo Press CTRL-a then d to detach from a screen.
    echo You can list all screens with 'screen -ls' if multiple instances of a service
    echo 'To stop all screens runme.sh -as'

    screen -dmS userservice $JAVA_BIN --enable-preview -cp compiled/UserService:compiled/UserService/lib/* UserService.UserService $config
    screen -dmS productservice $JAVA_BIN --enable-preview -cp compiled/ProductService:compiled/ProductService/lib/* ProductService.ProductService $config
    # wait 1 seconds for user service and product service to start
    sleep 2
    screen -dmS orderservice $JAVA_BIN --enable-preview -cp compiled/OrderService:compiled/OrderService/lib/* OrderService.OrderService $config
}

switch_to() {
  screen -r "$1"
}

kill_all() {
  pkill --echo -f 'UserService.UserService'
  pkill --echo -f 'ProductService.ProductService'
  pkill --echo -f 'OrderService.OrderService'
}

run_workload_parser() {
      verify_java
      verify_libs

      $JAVA_BIN --enable-preview -cp compiled/LoadGen:compiled/LoadGen/lib/* LoadGen.LoadGen "$1" "$2"
}

health_check() {
if curl -s -o /dev/null -w "%{http_code}" "http://$1:$2/health" | grep -q "200"; then
  echo -e "Service on host $1 with port $2 is \033[32mUP\033[0m"
  echo $(curl -s "http://$1:$2/health")
else
  echo -e "Service on $1 with port $2 is \033[31mDOWN\033[0m"
fi
}

clean() {
    rm -rf compiled/*
}

case "$1" in
    -cpv)
        compile_preview "$2"
        ;;
    -c)
        compile "$2"
        ;;
    -clean)
        clean
        ;;
    -conf)
        copy_config
        ;;
    -u)
        start_user_service $2
        ;;
    -p)
        start_product_service $2
        ;;
    -o)
        start_order_service $2
        ;;
    -i)
        start_iscs
        ;;
    -a)
        start_all $2
        ;;
    -as)
        stop_all
        ;;
    -su)
        switch_to userservice
        ;;
    -sp)
        switch_to productservice
        ;;
    -so)
        switch_to orderservice
        ;;
    -w)
        run_workload_parser "$2" "$3"
        ;;
    -killall)
        kill_all
        ;;
    -h)
        health_check $2 $3
        ;;
    *)
        echo "Usage: $0 -c | -u | -p | -o | -i | -w workloadfile"
        exit 1
        ;;
esac