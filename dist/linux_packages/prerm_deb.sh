#!/bin/bash

# summary of how this script can be called:
#        * <prerm_deb.sh> `remove'
#        * <old-prerm_deb.sh> `upgrade' <new version>
#        * <conflictor's-prerm_deb.sh> `remove' `in-favour' <package>
#          <new-version>
#        * <deconfigured's-prerm_deb.sh> `deconfigure' `in-favour'
#          <package-being-installed> <version> `removing'
#          <conflicting-package> <version>
#        * <new-prerm_deb.sh> `failed-upgrade' `old-version'
# for details, see https://www.debian.org/doc/debian-policy/ or
# the debian-policy package

case "$1" in remove|upgrade|deconfigure|failed-upgrade)
    xdg-desktop-menu uninstall __TARGET__/__PACKAGE__.desktop || true
    killall __TARGET__/bin/__EXECUTABLE_NAME__ --signal SIGTERM 2>/dev/null || true
    ;;

    *)
        echo "prerm called with unknown argument \`$1'" >&2
        exit 1
    ;;
esac

exit 0
