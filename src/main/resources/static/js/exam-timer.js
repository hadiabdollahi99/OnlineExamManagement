class ExamTimer {
    constructor(totalSeconds, onTimeUpdate, onTimeEnd) {
        this.totalSeconds = totalSeconds;
        this.remainingSeconds = totalSeconds;
        this.onTimeUpdate = onTimeUpdate;
        this.onTimeEnd = onTimeEnd;
        this.timerInterval = null;
        this.isRunning = false;
    }

    start() {
        if (this.isRunning) return;

        this.isRunning = true;
        this.timerInterval = setInterval(() => {
            this.remainingSeconds--;

            if (this.onTimeUpdate) {
                this.onTimeUpdate(this.remainingSeconds);
            }

            if (this.remainingSeconds <= 0) {
                this.stop();
                if (this.onTimeEnd) {
                    this.onTimeEnd();
                }
            }
        }, 1000);
    }

    stop() {
        if (this.timerInterval) {
            clearInterval(this.timerInterval);
            this.timerInterval = null;
            this.isRunning = false;
        }
    }

    getRemainingTime() {
        return {
            hours: Math.floor(this.remainingSeconds / 3600),
            minutes: Math.floor((this.remainingSeconds % 3600) / 60),
            seconds: this.remainingSeconds % 60
        };
    }

    formatTime() {
        const time = this.getRemainingTime();
        return `${time.hours.toString().padStart(2, '0')}:${time.minutes.toString().padStart(2, '0')}:${time.seconds.toString().padStart(2, '0')}`;
    }

    addTime(seconds) {
        this.remainingSeconds += seconds;
    }
}