DIR="$1"
NAME="$2"
cd "$DIR" || exit
FILE=$(find . -path "./build/dist/artifacts/*.deb")
sudo -S apt remove "$NAME" -y
sudo -S apt install -y "$FILE"