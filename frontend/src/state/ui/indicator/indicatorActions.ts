import {toggle} from '../../../helpers/collections';
import {GetState} from '../../../reducers/rootReducer';
import {payloadActionOf} from '../../../types/Types';
import {
  allQuantities,
  defaultQuantityForMedium,
  Medium,
  Quantity,
  quantityAttributes
} from '../graph/measurement/measurementModels';
import {IndicatorState} from './indicatorReducer';

export const SET_REPORT_INDICATOR_WIDGETS = 'SET_REPORT_INDICATOR_WIDGETS';
export const SET_SELECTED_QUANTITIES = 'SET_SELECTED_QUANTITIES';

// TODO[!must!] these will be used later in the legend.
export const setSelectedQuantities = payloadActionOf<Quantity[]>(SET_SELECTED_QUANTITIES);
export const selectQuantities = (quantities: Quantity[]) => setSelectedQuantities(quantities);
export const setReportIndicatorWidgets = payloadActionOf<Medium[]>(SET_REPORT_INDICATOR_WIDGETS);

// TODO[!must!] these will be used later in the legend.
export const toggleReportIndicatorWidget =
  (medium: Medium) =>
    (dispatch, getState: GetState) => {
      const {selectedQuantities, selectedIndicators: {report}}: IndicatorState = getState().ui.indicator;
      const newTypes: Medium[] = toggle(medium, report);
      const wasActivated: boolean = newTypes.length > report.length;
      const quantities: Quantity[] = selectedQuantities;

      dispatch(setReportIndicatorWidgets(newTypes));

      const quantityForMedium: Quantity = defaultQuantityForMedium(medium);
      if (wasActivated &&
          !quantities.includes(quantityForMedium) &&
          canToggleMedia(quantities, quantityForMedium)
      ) {
        // TODO *never* dispatch twice, be more clever with reducers & payload
        dispatch(setSelectedQuantities([...quantities, quantityForMedium]));
      } else if (!wasActivated) {
        const allCurrentlyPossibleQuantities: Set<Quantity> = new Set(newTypes
          .map((medium: Medium) => allQuantities[medium])
          .reduce((all, current) => all.concat(current), []));
        const newQuantities: Quantity[] = quantities
          .filter((quantity: Quantity) => allCurrentlyPossibleQuantities.has(quantity));
        if (newQuantities.length !== quantities.length) {
          // TODO *never* dispatch twice, be more clever with reducers & payload
          dispatch(setSelectedQuantities(newQuantities));
        }
      }
    };

export const canToggleMedia =
  (previouslySelected: Quantity[], addToSelection: Quantity): boolean => {
    const units = new Set(
      previouslySelected
        .map((quantity) => quantityAttributes[quantity].unit || null)
        .filter((unit) => unit !== null),
    );
    units.add(quantityAttributes[addToSelection].unit);
    return units.size <= 2;
  };
