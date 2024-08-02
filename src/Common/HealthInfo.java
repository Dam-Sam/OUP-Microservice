package Common;

public class HealthInfo {
    private long uptimeInMilliseconds;

    public HealthInfo(long uptimeInMilliseconds) {
        this.uptimeInMilliseconds = uptimeInMilliseconds;
    }

    public long getUptimeInMilliseconds() {
        return uptimeInMilliseconds;
    }

    public void setUptimeInMilliseconds(long uptimeInMilliseconds) {
        this.uptimeInMilliseconds = uptimeInMilliseconds;
    }
}
