#!/bin/bash

# summary of how this script can be called:
#        * <postinst_deb.sh> `configure' <most-recently-configured-version>
#        * <old-postinst_deb.sh> `abort-upgrade' <new version>
#        * <conflictor's-postinst_deb.sh> `abort-remove' `in-favour' <package>
#          <new-version>
#        * <postinst_deb.sh> `abort-remove'
#        * <deconfigured's-postinst_deb.sh> `abort-deconfigure' `in-favour'
#          <failed-install-package> <version> `removing'
#          <conflicting-package> <version>
# for details, see https://www.debian.org/doc/debian-policy/ or
# the debian-policy package

case "$1" in configure)
    xdg-desktop-menu install --novendor __TARGET__/__PACKAGE__.desktop || true
    mandb > /dev/null 2>&1 || true
    ;;

    abort-upgrade|abort-remove|abort-deconfigure)
    xdg-desktop-menu uninstall __TARGET__/__PACKAGE__.desktop || true
    killall __TARGET__/bin/__EXECUTABLE_NAME__ --signal SIGTERM 2>/dev/null || true
    mandb > /dev/null 2>&1 || true
    ;;

    *)
        echo "postinst called with unknown argument \`$1'" >&2
        exit 1
    ;;
esac

exit 0
