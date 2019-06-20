import {flatMap} from 'lodash';
import {unique} from '../../../helpers/collections';
import {Meter} from '../../../state/domain-models-paginated/meter/meterModels';
import {LegendItem, LegendItemSettings, SavedReportsState} from '../../../state/report/reportModels';
import {getLegendItems} from '../../../state/report/reportSelectors';
import {
  allQuantitiesMap,
  getMediumType,
  Medium,
  quantitiesToExclude,
  Quantity
} from '../../../state/ui/graph/measurement/measurementModels';
import {IdNamed} from '../../../types/Types';

export const legendViewSettings: LegendItemSettings = {
  isHidden: false,
  isRowExpanded: true,
};

export const toLegendItemAllQuantities = ({id, facility, medium}: Meter): LegendItem => {
  const type: Medium = getMediumType(medium);
  return ({
    id,
    label: facility as string,
    type,
    quantities: allQuantitiesMap[type],
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

const availableQuantities = (quantity: Quantity) => quantitiesToExclude.indexOf(quantity) === -1;

export const makeColumnQuantities = (state: SavedReportsState) => {
  const quantities: Quantity[][] = flatMap(getLegendItems(state), it => it.type)
    .map((medium: Medium) => allQuantitiesMap[medium]);
  return unique(flatMap(quantities).filter(availableQuantities));
};
