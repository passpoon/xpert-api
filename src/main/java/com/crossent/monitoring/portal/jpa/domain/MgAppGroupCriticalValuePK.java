package com.crossent.monitoring.portal.jpa.domain;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

public class MgAppGroupCriticalValuePK implements Serializable {
    private Integer mgAppGroupId;
    private Integer metricId;

    @Column(name = "mg_app_group_id", nullable = false)
    @Id
    public Integer getMgAppGroupId() {
        return mgAppGroupId;
    }

    public void setMgAppGroupId(Integer mgAppGroupId) {
        this.mgAppGroupId = mgAppGroupId;
    }

    @Column(name = "metric_id", nullable = false)
    @Id
    public Integer getMetricId() {
        return metricId;
    }

    public void setMetricId(Integer metricId) {
        this.metricId = metricId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MgAppGroupCriticalValuePK{");
        sb.append("mgAppGroupId=").append(mgAppGroupId);
        sb.append(", metricId=").append(metricId);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MgAppGroupCriticalValuePK that = (MgAppGroupCriticalValuePK) o;

        if (mgAppGroupId != null ? !mgAppGroupId.equals(that.mgAppGroupId) : that.mgAppGroupId != null) return false;
        return metricId != null ? metricId.equals(that.metricId) : that.metricId == null;
    }

    @Override
    public int hashCode() {
        int result = mgAppGroupId != null ? mgAppGroupId.hashCode() : 0;
        result = 31 * result + (metricId != null ? metricId.hashCode() : 0);
        return result;
    }
}
