#!/bin/bash
# xMCS specific
    XMCS_HOME=/home/xmcs/XMCS/xmcs_server
    WORKDIR=/home/xmcs/XMCS
    xmcs() {
        cd ${XMCS_HOME}
    }
    corelog() {
        tail -F ${XMCS_HOME}/logs/CORE/CORE_1-trace.log
    }
    viewcorelog() {
        view ${XMCS_HOME}/logs/CORE/CORE_1-trace.log
    }
    corealertlog() {
        tail -F ${XMCS_HOME}/logs/CORE/CORE_1-alert.log
    }
    viewcorealertlog() {
        view ${XMCS_HOME}/logs/CORE/CORE_1-alert.log
    }
    amhslog() {
        tail -F ${XMCS_HOME}/logs/AMHSIF/AMHS-IF_1-trace.log
    }
    viewamhslog() {
        view ${XMCS_HOME}/logs/AMHSIF/AMHS-IF_1-trace.log
    }
    amhsalertlog() {
        tail -F ${XMCS_HOME}/logs/AMHSIF/AMHS-IF_1-alert.log
    }
    hostlog() {
        tail -F ${XMCS_HOME}/logs/HOSTIF/HOST-IF_1-trace.log
    }
    viewhostlog() {
        view ${XMCS_HOME}/logs/HOSTIF/HOST-IF_1-trace.log
    }
    hostalertlog() {
        tail -F ${XMCS_HOME}/logs/HOSTIF/HOST-IF_1-alert.log
    }
    cachelog() {
        tail -F ${XMCS_HOME}/logs/CACHE/CACHE_1-trace.log
    }
    viewcachelog() {
        view ${XMCS_HOME}/logs/CACHE/CACHE_1-trace.log
    }
    cachealertlog() {
        tail -F ${XMCS_HOME}/logs/CACHE/CACHE_1-alert.log
    }
    clientlog() {
        tail -F ${XMCS_HOME}/logs/CLIENT/CLIENT-IF_1-trace.log
    }
    viewclientlog() {
        view ${XMCS_HOME}/logs/CLIENT/CLIENT-IF_1-trace.log
    }
    clientalertlog() {
        tail -F ${XMCS_HOME}/logs/CLIENT/CLIENT-IF_1-alert.log
    }
    layoutlog() {
        tail -F ${XMCS_HOME}/logs/LAYOUTMAN/LAYOUT-MAN_1-trace.log
    }
    viewlayoutlog() {
        view ${XMCS_HOME}/logs/LAYOUTMAN/LAYOUT-MAN_1-trace.log
    }
    layoutalertlog() {
        tail -F ${XMCS_HOME}/logs/LAYOUTMAN/LAYOUT-MAN_1-alert.log
    }
    hazellog() {
        tail -F ${XMCS_HOME}/logs/CACHE/hazelcast-CACHE_1.log
    }
    checkif() {
        ${XMCS_HOME}/scripts/check-if.sh
    }

    XMCS_MONITOR=/home/xmcs/XMCS/xmcs-monitor
    monitorlog() {
        tail -F ${XMCS_MONITOR}/logs/xmcs-monitor.log
    }
    monitorstart() {
        ${XMCS_MONITOR}/scripts/xmcs-monitor.sh start
    }
    monitorstatus() {
        ${XMCS_MONITOR}/scripts/xmcs-monitor.sh status
    }
    monitorstop() {
        ${XMCS_MONITOR}/scripts/xmcs-monitor.sh stop
    }

    XMCS_EMU=/home/xmcs/XMCS/amhs_emulator
    emulog() {
        tail -F ${XMCS_EMU}/logs/emulator.log
    }
    emustart() {
        ${XMCS_EMU}/scripts/amhs_emulator.sh start
    }
    emustatus() {
        ${XMCS_EMU}/scripts/amhs_emulator.sh status
    }
    emustop() {
        ${XMCS_EMU}/scripts/amhs_emulator.sh stop
    }
