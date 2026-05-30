#!/usr/bin/env bash
set -euo pipefail

EDAPACK_VERSION=0.0.5
EDAPACK_ARCHIVE="edapack-linux_x86_64-${EDAPACK_VERSION}.tar.gz"
EDAPACK_URL="https://github.com/EDAPack/edapack/releases/download/${EDAPACK_VERSION}/${EDAPACK_ARCHIVE}"
EDAPACK_INSTALL_ROOT=/opt
EDAPACK_DIR="${EDAPACK_INSTALL_ROOT}/edapack-linux_x86_64-${EDAPACK_VERSION}"

sudo apt-get update
sudo apt-get install -y --no-install-recommends ca-certificates curl python3 python3-pip

curl -fsSL "${EDAPACK_URL}" -o "/tmp/${EDAPACK_ARCHIVE}"
sudo tar -xzf "/tmp/${EDAPACK_ARCHIVE}" -C "${EDAPACK_INSTALL_ROOT}"
rm -f "/tmp/${EDAPACK_ARCHIVE}"

python3 -m pip install --user --upgrade requests PyGithub

sudo tee "${EDAPACK_DIR}/bin/edapack" >/dev/null <<'EOF'
#!/usr/bin/env bash
if test -f $0; then
  script_dir=$(dirname $0)
  script_dir=$(cd $script_dir ; pwd)
else
  for elem in $(echo PATH | sed -e 's%:% %g'); do
    script=${elem}/$0
    if test -f $script; then
      script_dir=$elem
      break
    fi
  done
fi
edapack_dir=$(dirname $script_dir)
export PYTHONPATH=${edapack_dir}/lib
exec python3 -m edapack ${@:1}
EOF
sudo chmod +x "${EDAPACK_DIR}/bin/edapack"

source "${EDAPACK_DIR}/etc/edapack.sh"
edapack install verilator
module load verilator/latest

verilator_bin="$(command -v verilator || true)"
if [ -n "${verilator_bin}" ]; then
  sudo ln -sf "${verilator_bin}" /usr/local/bin/verilator
fi
