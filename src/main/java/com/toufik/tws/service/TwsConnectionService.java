package com.toufik.tws.service;

import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.ib.client.EReader;
import com.toufik.tws.config.TwsProperties;
import com.toufik.tws.dto.ConnectionStatusResponse;
import com.toufik.tws.dto.TwsConnectionConfig;
import com.toufik.tws.dto.TwsErrorInfo;
import com.toufik.tws.wrapper.TwsWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwsConnectionService {

    private final TwsProperties twsProperties;

    private EClientSocket clientSocket;
    private EReader reader;
    private TwsWrapper wrapper;

    public TwsWrapper getWrapper() {
        return wrapper;
    }

    public EClientSocket getClientSocket() {
        return clientSocket;
    }

    public boolean isConnected() {
        return wrapper != null && wrapper.isConnected();
    }

    public ConnectionStatusResponse getStatus() {
        return buildStatusResponse();
    }

    public ConnectionStatusResponse connect() {
        try {
            if (wrapper != null && wrapper.isConnected()) {
                return buildStatusResponse();
            }

            CountDownLatch connectionLatch = new CountDownLatch(1);
            wrapper = new TwsWrapper();
            wrapper.setConnectionLatch(connectionLatch);
            wrapper.clearError();

            EJavaSignal signal = new EJavaSignal();
            clientSocket = new EClientSocket(wrapper, signal);

            clientSocket.eConnect(
                twsProperties.getHost(),
                twsProperties.getPort(),
                twsProperties.getClientId()
            );

            if (!clientSocket.isConnected()) {
                log.error("Failed to establish initial connection");
                return buildStatusResponse();
            }

            reader = new EReader(clientSocket, signal);
            reader.start();

            new Thread(() -> {
                while (clientSocket.isConnected()) {
                    signal.waitForSignal();
                    try {
                        reader.processMsgs();
                    } catch (Exception e) {
                        log.error("Error processing messages", e);
                    }
                }
            }, "tws-message-processor").start();

            boolean connected = connectionLatch.await(twsProperties.getTimeout(), TimeUnit.MILLISECONDS);

            if (!connected) {
                log.error("Connection timeout");
                disconnect();
            }

        } catch (Exception e) {
            log.error("Connection error", e);
        }

        return buildStatusResponse();
    }

    public void disconnect() {
        if (clientSocket != null && clientSocket.isConnected()) {
            clientSocket.eDisconnect();
        }
    }

    private ConnectionStatusResponse buildStatusResponse() {
        boolean isConnected = wrapper != null && wrapper.isConnected();
        TwsWrapper.ErrorInfo errorInfo = wrapper != null ? wrapper.getLastError() : null;

        TwsErrorInfo error = null;
        if (errorInfo != null) {
            error = TwsErrorInfo.builder()
                .code(errorInfo.getErrorCode())
                .message(errorInfo.getErrorMsg())
                .timestamp(Instant.ofEpochMilli(errorInfo.getErrorTime()))
                .build();
        }

        String status;
        if (isConnected) {
            status = "CONNECTED";
        } else if (error != null) {
            status = "ERROR";
        } else {
            status = "DISCONNECTED";
        }

        return ConnectionStatusResponse.builder()
            .isConnected(isConnected)
            .status(status)
            .error(error)
            .config(TwsConnectionConfig.builder()
                .host(twsProperties.getHost())
                .port(twsProperties.getPort())
                .clientId(twsProperties.getClientId())
                .build())
            .build();
    }
}
