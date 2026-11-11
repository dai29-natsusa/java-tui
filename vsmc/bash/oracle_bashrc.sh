#!/bin/bash
# xMCS specific

PATH=/usr/bin:/usr/sbin:/usr/ucb:/usr/dt/bin:/sbin:$PATH:$HOME/bin:/usr/bin/X11

export PATH

NLS_LANG=American_America.AL32UTF8

ORACLE_BASE=/opt/oracle
ORACLE_HOME=/opt/oracle/product/19/xmcs
ORACLE_SID=xmcs
ORACLE_UNQNAME=xmcs
ORACLE_TERM=xterm
TEMP=/tmp
TEPDIR=/tmp
PATH=/usr/lib64/qt-3.3/bin:/usr/kerberos/sbin:/usr/kerberos/bin:/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/sbin:$ORACLE_HOME/bin:/usr/bin/X11
export PATH ORACLE_BASE ORACLE_HOME ORACLE_SID ORACLE_UNQNAME ORACLE_TERM TEMP TEPDIR NLS_LANG
umask 022

# Set prompt
PS1="% `hostname`@`whoami`> "

# Set shortcut command
alias ll='ls -al'
