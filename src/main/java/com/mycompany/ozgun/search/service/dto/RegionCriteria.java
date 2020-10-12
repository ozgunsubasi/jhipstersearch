package com.mycompany.ozgun.search.service.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the {@link com.mycompany.ozgun.search.domain.Region} entity. This class is used
 * in {@link com.mycompany.ozgun.search.web.rest.RegionResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /regions?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class RegionCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter regionName;

    public RegionCriteria() {
    }

    public RegionCriteria(RegionCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.regionName = other.regionName == null ? null : other.regionName.copy();
    }

    @Override
    public RegionCriteria copy() {
        return new RegionCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getRegionName() {
        return regionName;
    }

    public void setRegionName(StringFilter regionName) {
        this.regionName = regionName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RegionCriteria that = (RegionCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(regionName, that.regionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        regionName
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RegionCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (regionName != null ? "regionName=" + regionName + ", " : "") +
            "}";
    }

}
