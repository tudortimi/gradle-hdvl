#!/usr/bin/env sh

echo "Dummy dvt-cli.sh"

touch dvt_cli.sh.log

echo 'args:' >> dvt_cli.sh.log
echo $@ >> dvt_cli.sh.log
