package com.unifun.smpp.service.imp;

import org.apache.log4j.Logger;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.Session;
import org.jsmpp.session.SessionStateListener;

public class StateSessionListenerImp implements SessionStateListener {
    static final Logger logger = Logger.getLogger(StateSessionListenerImp.class);


    @Override
    public void onStateChange(SessionState newState, SessionState oldState, Session source) {
        if(!newState.isBound()) {
            logger.trace(String.format("SmppSession changed status from {%s} to {%s}. {%s}", newState, oldState, source.getSessionId()));
        }
    }
}
