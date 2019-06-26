import {flatMap} from 'lodash';
import {unique} from '../../../helpers/collections';
import {Meter} from '../../../state/domain-models-paginated/meter/meterModels';
import {LegendItem, LegendItemSettings, SavedReportsState} from '../../../state/report/reportModels';
import {getLegendItems} from '../../../state/report/reportSelectors';
import {searchableQuantitiesFrom} from '../../../state/ui/graph/measurement/measurementActions';
import {
  allQuantitiesMap,
  availableQuantities,
  getMediumType,
  Medium,
  Quantity
} from '../../../state/ui/graph/measurement/measurementModels';
import {IdNamed} from '../../../types/Types';

export const legendViewSettings: LegendItemSettings = {
  isHidden: false,
  isRowExpanded: true,
};

export const toLegendItemAllSearchableQuantities = ({id, facility, medium}: Meter): LegendItem => {
  const type: Medium = getMediumType(medium);
  return ({
    id,
    label: facility as string,
    type,
    quantities: searchableQuantitiesFrom(type),
    ...legendViewSettings,
  });
};

export const toLegendItem = ({id, facility, medium}: Meter): LegendItem => ({
  id,
  label: facility as string,
  type: getMediumType(medium),
  quantities: [],
  ...legendViewSettings
});

export const toAggregateLegendItem = ({id, name}: IdNamed): LegendItem => ({
  id,
  label: name,
  type: 'aggregate',
  quantities: [],
  ...legendViewSettings
});

export const makeSelectableQuantities = (state: SavedReportsState): Quantity[] => {
  const quantities: Quantity[][] = flatMap(getLegendItems(state), it => it.type)
    .map((medium: Medium) => allQuantitiesMap[medium]);
  return unique(flatMap(quantities).filter(availableQuantities));
};
