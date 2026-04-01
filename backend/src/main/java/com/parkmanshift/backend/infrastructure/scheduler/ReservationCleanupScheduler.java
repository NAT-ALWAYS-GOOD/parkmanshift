package com.parkmanshift.backend.infrastructure.scheduler;

import com.parkmanshift.backend.application.port.in.CancelUnconfirmedReservationsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservationCleanupScheduler.class);

    private final CancelUnconfirmedReservationsUseCase cancelUnconfirmedReservationsUseCase;

    public ReservationCleanupScheduler(CancelUnconfirmedReservationsUseCase cancelUnconfirmedReservationsUseCase) {
        this.cancelUnconfirmedReservationsUseCase = cancelUnconfirmedReservationsUseCase;
    }

    /**
     * Runs every day at 11:00 Europe/Paris (UTC+1 in winter, UTC+2 in summer).
     * Cancels all reservations for today that are still in RESERVED status (not checked in).
     */
    @Scheduled(cron = "0 0 11 * * *", zone = "Europe/Paris")
    public void cancelUnconfirmedReservations() {
        log.info("[Scheduler] Running daily unconfirmed reservation cleanup...");
        int cancelled = cancelUnconfirmedReservationsUseCase.cancelUnconfirmedReservationsForToday();
        log.info("[Scheduler] Cancelled {} unconfirmed reservation(s) for today.", cancelled);
    }
}
