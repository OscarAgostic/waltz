package org.finos.waltz.model.application;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableRatingsView.class)
public interface MeasurableRatingsView {

    Set<MeasurableCategory> measurableCategories();
    Set<Measurable> measurables();
    Set<MeasurableRating> measurableRatings();
    Set<RatingSchemeItem> ratingSchemeItems();
}
