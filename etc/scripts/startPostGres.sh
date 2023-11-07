#!/usr/bin/env bash

docker run -itd -e POSTGRES_USER=dataflow -e POSTGRES_PASSWORD=dataflow123 -p 5432:5432 -v /Users/udo/postgresql/data:/var/lib/dataflow/data --name dataflow postgres > /dev/null
